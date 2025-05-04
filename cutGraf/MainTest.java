package cutGraf;

public class MainTest {
    public static void main(String[] args) {
        // System.out.println("aaa");
        // Graf aaa;
        Graf testGraf = new Graf();
        testGraf.loadFromCsrrg("graf1.csrrg");
        Graf testGrafB = testGraf.cutGraf(new StonerCut(), 0.1);
        System.out.println(testGraf);
        System.out.println(testGrafB);
    }   
}