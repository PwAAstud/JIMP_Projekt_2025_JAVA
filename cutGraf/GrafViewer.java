package cutGraf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GrafViewer extends JFrame {
    private List<Graf> grafy = new ArrayList<>();
    private Graf fullGraf;
    private JPanel grafPanel;
    private JFileChooser fileChooser = new JFileChooser();
    private JTextField marginField = new JTextField("0.1", 5);
    private JTextField splitCountField = new JTextField("2", 3);
    private JTextField outputFileField = new JTextField("output.txt", 15);

    private static final Color[] COLORS = {
        Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.CYAN, Color.PINK
    };

    public GrafViewer() {
        setTitle("Przeglądarka Grafów");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel kontrolny
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

        // Panel grafu
        grafPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (grafy.isEmpty()) return;

                int scale = 30;
                int radius = 8;

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

              // Rysuj wierzchołki na końcu (nad krawędziami)
for (int i = 0; i < grafy.size(); i++) {
    Graf gPart = grafy.get(i);
    Color color = COLORS[i % COLORS.length];
    g.setColor(color);

    List<Node> nodes = gPart.getGrafNodes();
    for (int j = 0; j < nodes.size(); j++) {
        Node node = nodes.get(j);

        int x = node.getX() * scale + 50;
        int y = node.getY() * scale + 50;

        // Rysuj węzeł
        g.fillOval(x - radius / 2, y - radius / 2, radius, radius);

        // Rysuj numer na środku węzła (węzeł + numer w kolorze kontrastowym)
        g.setColor(Color.BLACK); // kolor numeru, możesz zmienić na kontrastowy
        String text = String.valueOf(j); // numer węzła

        // Wycentrowanie tekstu na środku koła
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getAscent();

        g.drawString(text, x - textWidth / 2, y + textHeight / 2);

        // Przywróć kolor węzła na dalsze rysowanie jeśli potrzebne
        g.setColor(color);
    }
}

            }
        };
        add(new JScrollPane(grafPanel), BorderLayout.CENTER);

        // Akcje
        loadButton.addActionListener(e -> loadGraph());
        runButton.addActionListener(e -> splitAndSaveGraph());
    }

    private void loadGraph() {
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            fullGraf = new Graf();
            if (selectedFile.getName().endsWith(".csrrg")) {
                fullGraf.loadFromCsrrg(selectedFile.getAbsolutePath());
            } else {
                JOptionPane.showMessageDialog(this, "Obsługiwane tylko pliki .csrrg");
                return;
            }
            grafy.clear();
            grafy.add(fullGraf);
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

        // Zapisz do pliku
        try {
            Graf.saveToFileTxt(grafy, outputFileField.getText());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Błąd zapisu: " + ex.getMessage());
            return;
        }

        grafPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GrafViewer().setVisible(true);
        });
    }
}
