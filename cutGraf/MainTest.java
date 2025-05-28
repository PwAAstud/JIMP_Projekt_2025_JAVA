package cutGraf;

import java.io.IOException;
import java.util.ArrayList;

public class MainTest {
    public static void main(String[] args) {
        // System.out.println("aaa");
        // Graf aaa;
        Graf testGraf = new Graf();
        testGraf.loadFromBinary("graf.out");
        // testGraf.loadFromCsrrg("test.csrrg");
        System.out.println(testGraf);
        System.out.println();

        Graf testGrafB = testGraf.cutGraf(new cutUnconected(), 1);
        // Graf testGrafB = testGraf.cutGraf(new StonerCut(), 0.9);
        System.out.println(testGraf);
        System.out.println(testGrafB);
        
        ArrayList<Graf> saveList = new ArrayList<Graf>();
        saveList.add(testGraf);
        saveList.add(testGrafB);
        
        // Graf.saveToFileTxt(saveList, "out.txt");

        // try {
        //     Graf.saveToFileBinary(saveList, "out.txt");
        // } catch (IOException e) {
        //     e.printStackTrace();
        // }
    }   
}