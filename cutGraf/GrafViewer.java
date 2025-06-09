package cutGraf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GrafViewer extends JFrame {
    private List<Graf> grafy = new ArrayList<>();
    private Graf fullGraf;
    private JPanel grafPanel;
    private JFileChooser fileChooser;
    private JTextField marginField = new JTextField("0.1", 5);
    private JTextField splitCountField = new JTextField("2", 3);
    private JTextField outputFileField = new JTextField("output.txt", 15);
    private Thread cutingThread;
    JButton loadButton;

    private static final Color[] COLORS = {
        Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.CYAN, Color.PINK
    };

    private Point mousePressPoint;    // punkt, gdzie rozpoczęto przeciąganie
    private JScrollPane scrollPane;   // scroll zawierający panel grafu

    public GrafViewer() {
        setTitle("Przeglądarka Grafów");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // JFileChooser startuje w katalogu programu
        fileChooser = new JFileChooser(new File(".").getAbsoluteFile());

        // Panel kontrolny z przyciskami i polami tekstowymi
        JPanel controlPanel = new JPanel();
        loadButton = new JButton("Wczytaj graf");
        JButton runButton = new JButton("Przetnij i Zapisz");

        controlPanel.add(loadButton);
        controlPanel.add(new JLabel("Margines:"));
        controlPanel.add(marginField);
        controlPanel.add(new JLabel("Podziały:"));
        controlPanel.add(splitCountField);
        controlPanel.add(new JLabel("Plik wyjściowy:"));
        controlPanel.add(outputFileField);
        controlPanel.add(runButton);
        add(controlPanel, BorderLayout.NORTH);

        // Panel, na którym rysujemy graf
        grafPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (grafy.isEmpty()) return;

                int scale = 30;
                int radius = 16;

                // Najpierw rysujemy krawędzie grafu
                for (int i = 0; i < grafy.size(); i++) {
                    Graf gPart = grafy.get(i);
                    Color color = COLORS[i % COLORS.length];
                    g.setColor(color);
                    for (Node node : gPart.getGrafNodes()) {
                        for (Node con : node.getConection()) {
                            g.drawLine(
                                node.getX() * scale + 50,
                                node.getY() * scale + 50,
                                con.getX() * scale + 50,
                                con.getY() * scale + 50
                            );
                        }
                    }
                }

                // Rysujemy węzły na wierzchu (nad krawędziami)
                for (int i = 0; i < grafy.size(); i++) {
                    Graf gPart = grafy.get(i);
                    Color color = COLORS[i % COLORS.length];
                    g.setColor(color);

                    List<Node> nodes = gPart.getGrafNodes();
                    for (Node node : nodes) {
                        int x = node.getX() * scale + 50;
                        int y = node.getY() * scale + 50;

                        // Rysuj węzeł (koło)
                        g.fillOval(x - radius / 2, y - radius / 2, radius, radius);

                        // Rysuj ID węzła na środku
                        g.setColor(Color.BLACK);
                        String text = String.valueOf(node.getId());
                        FontMetrics fm = g.getFontMetrics();
                        int textWidth = fm.stringWidth(text);
                        int textHeight = fm.getAscent();

                        g.drawString(text, x - textWidth / 2, y + textHeight / 2);

                        // Przywróć kolor do następnego węzła
                        g.setColor(color);
                    }
                }
            }

            // Opcjonalnie ustaw minimalny rozmiar panelu, jeśli graf jest duży,
            // żeby JScrollPane miał co przewijać
            @Override
            public Dimension getPreferredSize() {
                // Możesz ustawić na podstawie maksymalnych współrzędnych w grafie
                int maxX = 0, maxY = 0;
                int scale = 30;
                for (Graf gPart : grafy) {
                    for (Node n : gPart.getGrafNodes()) {
                        maxX = Math.max(maxX, n.getX());
                        maxY = Math.max(maxY, n.getY());
                    }
                }
                return new Dimension(maxX * scale + 100, maxY * scale + 100);
            }
        };

        scrollPane = new JScrollPane(grafPanel);
        add(scrollPane, BorderLayout.CENTER);

        // Listener do przeciągania panelu grafu (przesuwanie widoku)
        grafPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mousePressPoint = e.getPoint();
            }
        });

        grafPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (mousePressPoint == null) return;

                Point dragPoint = e.getPoint();
                JViewport viewport = scrollPane.getViewport();
                Point viewPos = viewport.getViewPosition();

                // Różnica ruchu myszy
                int dx = mousePressPoint.x - dragPoint.x;
                int dy = mousePressPoint.y - dragPoint.y;

                // Oblicz nową pozycję widoku (zabezpieczamy zakres)
                int newX = Math.max(0, Math.min(viewPos.x + dx, grafPanel.getWidth() - viewport.getWidth()));
                int newY = Math.max(0, Math.min(viewPos.y + dy, grafPanel.getHeight() - viewport.getHeight()));

                viewport.setViewPosition(new Point(newX, newY));
            }
        });

        // Akcje przycisków
        loadButton.addActionListener(e -> loadGraph());
        // runButton.addActionListener(e -> splitAndSaveGraph());
        runButton.addActionListener(e -> splitAndSaveGraphThed());
    }

    private void loadGraph() {
        // Akceptuj dowolny plik
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            fullGraf = new Graf();
            
            String fileName = selectedFile.getName();
            int i = fileName.lastIndexOf('.');
            fileName = fileName.substring(i+1);
            try {
                if( fileName.equals("csrrg") ){
                    fullGraf.loadFromCsrrg(selectedFile.getAbsolutePath());
                }else if( fileName.equals("out") ){
                    fullGraf.loadFromBinary(selectedFile.getAbsolutePath());
                }else{
                    JOptionPane.showMessageDialog(this, "nieznane rozszerzenie");
                    return;
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Błąd podczas wczytywania pliku: " + ex.getMessage());
                return;
            }

            grafy.clear();

            do {
                Graf tempG = fullGraf.cutGraf(new CutUnconected(), i);
                grafy.add(fullGraf);
                fullGraf = tempG;
            } while (fullGraf != null);

            grafPanel.revalidate(); // odśwież rozmiar panelu
            grafPanel.repaint();
        }
    }

    private void splitAndSaveGraph() {
        if (grafy.size() == 0) {
            JOptionPane.showMessageDialog(this, "Najpierw załaduj graf!");
            return;
        }
        loadButton.setEnabled(false);

        double margin;
        int splits;
        try {
            margin = Double.parseDouble(marginField.getText());
            splits = Integer.parseInt(splitCountField.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Niepoprawne dane wejściowe.");
            return;
        }

        // grafy.clear();
        int grafi = 0;
        Graf working = grafy.get(grafi);
        for (int i = 0; i < splits - 1; i++) {
            Graf cut = working.cutGraf(new StonerCut(), margin);
            if (cut != null) {
                grafy.add(cut);
            }else if(++grafi < grafy.size()){
                working = grafy.get(grafi);
                i--;
            }else{
                break;
            }
        }
        // grafy.add(working); // ostatnia część
        
        loadButton.setEnabled(true);
        try {
            Graf.saveToFileTxt(grafy, outputFileField.getText());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Błąd zapisu: " + ex.getMessage());
            return;
        }

        grafPanel.revalidate();
        grafPanel.repaint();
    }

    private void splitAndSaveGraphThed(){
        cutingThread = new Thread(){
            {
                setDaemon(true);
            }
            @Override
            public void run() {
                splitAndSaveGraph();
                // // super.run();
                grafPanel.revalidate();
                grafPanel.repaint();
                Thread.currentThread().interrupt();
            }
        };
        cutingThread.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GrafViewer().setVisible(true);
        });
    }
}
