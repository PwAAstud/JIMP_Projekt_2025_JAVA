package cutGraf;

import java.util.ArrayList;

import cutGraf.Graf.BrakujeWszystkichId;

public class MainTest {
    public static void main(String[] args) {
        // System.out.println("aaa");
        // Graf aaa;
        try {
            Graf.saveToFile(new ArrayList<Graf>());
        } catch (BrakujeWszystkichId e) {
            e.printStackTrace();
        }
        Graf testGraf = new Graf();
        testGraf.loadFromCsrrg("graf1.csrrg");
        for (Node node : testGraf) {
            System.out.println(node);
        }
        // Graf testGrafB = testGraf.cutGraf(new StonerCut(), 0.1);
        // System.out.println(testGraf);
        // System.out.println(testGrafB);
    }   
}