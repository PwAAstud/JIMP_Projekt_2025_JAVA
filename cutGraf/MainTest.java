package cutGraf;

import java.util.ArrayList;

import cutGraf.Graf.BrakujeWszystkichId;

public class MainTest {
    public static void main(String[] args) {
        // System.out.println("aaa");
        // Graf aaa;
        Graf testGraf = new Graf();
        testGraf.loadFromCsrrg("graf.csrrg");
        Graf testGrafB = testGraf.cutGraf(new StonerCut(), 1);
        System.out.println(testGraf);
        System.out.println(testGrafB);
        
        ArrayList<Graf> saveList = new ArrayList<Graf>();
        saveList.add(testGraf);
        saveList.add(testGrafB);
        
        try {
            Graf.saveToFileTxt(saveList, "out.txt");
        } catch (BrakujeWszystkichId e) {
            e.printStackTrace();
        }
    }   
}