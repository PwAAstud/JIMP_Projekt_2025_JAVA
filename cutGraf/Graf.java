package cutGraf;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Graf {
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
            // TODO Auto-generated catch block
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
}
