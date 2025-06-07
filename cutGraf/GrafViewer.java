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
        JButton loadButton = new JButton("Wczytaj graf");
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
        runButton.addActionListener(e -> splitAndSaveGraph());
    }

    private void loadGraph() {
        // Akceptuj dowolny plik
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            fullGraf = new Graf();

            try {
                fullGraf.loadFromCsrrg(selectedFile.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Błąd podczas wczytywania pliku: " + ex.getMessage());
                return;
            }

            grafy.clear();
            grafy.add(fullGraf);
            grafPanel.revalidate(); // odśwież rozmiar panelu
            grafPanel.repaint();
        }
    }

    private void splitAndSaveGraph() {
        if (fullGraf == null) {
            JOptionPane.showMessageDialog(this, "Najpierw załaduj graf!");
            return;
        }

        double margin;
        int splits;
        try {
            margin = Double.parseDouble(marginField.getText());
            splits = Integer.parseInt(splitCountField.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Niepoprawne dane wejściowe.");
            return;
        }

        grafy.clear();
        Graf working = fullGraf;
        for (int i = 0; i < splits - 1; i++) {
            Graf cut = working.cutGraf(new CombinationCut(), margin);
            if (cut != null) {
                grafy.add(cut);
            }
        }
        grafy.add(working); // ostatnia część

        try {
            Graf.saveToFileTxt(grafy, outputFileField.getText());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Błąd zapisu: " + ex.getMessage());
            return;
        }

        grafPanel.revalidate();
        grafPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GrafViewer().setVisible(true);
        });
    }
}
