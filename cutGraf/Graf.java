package cutGraf;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class Graf implements Iterable<Node>{
    private int maxInRow;
    private ArrayList<Node> grafNodes = new ArrayList<Node>();

    public int getMaxInRow() {
        return maxInRow;
    }

    public ArrayList<Node> getGrafNodes() {
        return new ArrayList<Node>(grafNodes);
    }

    private ArrayList<Integer> readIntLine(Scanner sorce){
        String line = sorce.nextLine();
        ArrayList<Integer> retVal = new ArrayList<Integer>();
        for (String s : line.split(";")) {
            retVal.add(Integer.parseInt(s));
        }
        return retVal;
    }

    public void loadFromCsrrg(String fileName){
        System.err.println(fileName);
        Scanner fileInput = null;
        try {
            fileInput = new Scanner(new File(fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        ArrayList<Integer> line = readIntLine(fileInput);
        maxInRow = line.get(0);
        ArrayList<Integer> xForNode = readIntLine(fileInput);
        ArrayList<Integer> yRangeNode = readIntLine(fileInput);
        for(int y=0; y < yRangeNode.size()-1; y+=1){
            int start = yRangeNode.get(y);
            int end = yRangeNode.get(y+1);
            for(int id = start; id<end; id+=1){
                grafNodes.add(new Node(id, xForNode.get(id), y));
            }
        }

        ArrayList<Integer> conectionList = readIntLine(fileInput);
        ArrayList<Integer> conectionRange = readIntLine(fileInput);

        for(int i=0; i < conectionRange.size()-1; i+=1){
            int start = conectionRange.get(i);
            int end = conectionRange.get(i+1);
            Node curentNode = grafNodes.get(conectionList.get(start));
            for(int j=start+1; j < end; j+=1){
                curentNode.conectTo(grafNodes.get(conectionList.get(j)));
            }
        }


        // for (Node n : grafNodes) {
        //     System.err.println(n.getId() + " " + n.getConection());
        // }
    }

    public static class BrakujeWszystkichId extends Exception {
        public BrakujeWszystkichId(int lostId) {
            super("brakuje id " + lostId);
        }
    }

    public static void saveToFile(List<Graf> grafList) throws BrakujeWszystkichId{
        // throw new BrakujeWszystkichId(1);
        ArrayList<Node> nodesToSave = new ArrayList<Node>();
    }

    public Graf cutGraf(GrafCutFinder metod, double margin){
        int minSize = (int)Math.ceil((2-margin)*grafNodes.size()/4);
        if(minSize <= 0){
            minSize=1;
        }
        if(grafNodes.size() - 2*minSize < 0){
            return null;
        }

        Graf retGraf = new Graf();
        retGraf.maxInRow = maxInRow;
        retGraf.grafNodes = metod.nodesForSecentGraf(grafNodes, minSize);
        Collections.sort(retGraf.grafNodes);
        // System.out.println(retGraf.grafNodes);
        if(retGraf.grafNodes.size() == 0){
            return null;
        }

        ArrayList<Node> myNewNodes = new ArrayList<Node>();
        int i = 0;
        for (Node nodeInB : retGraf.grafNodes) {
            // System.out.println(nodeInB);
            // System.out.println(i);
            while (grafNodes.get(i) != nodeInB) {
                // System.out.println(grafNodes.get(i));
                myNewNodes.add( grafNodes.get(i) );
                i+=1;
            }
            i+=1;
            // System.out.println(i);
            if(i == grafNodes.size()){
                break;
            }
        }
        for(; i<grafNodes.size(); i+=1){
            myNewNodes.add( grafNodes.get(i) );
        }
        // System.out.println(myNewNodes);
        grafNodes = myNewNodes;

        for (Node NodeA : this.grafNodes) {
            for (Node NodeB : retGraf.grafNodes) {
                NodeB.removeConectTo(NodeA);
            }
        }

        return retGraf;
    }

    @Override
    public String toString() {
        return grafNodes.toString();
    }

    @Override
    public Iterator<Node> iterator() {
        return grafNodes.iterator();
    }
}