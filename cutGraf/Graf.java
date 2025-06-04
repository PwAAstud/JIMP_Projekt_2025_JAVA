package cutGraf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Graf{
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
        // System.err.println(fileName);
        Scanner fileInput = null;
        try {
            fileInput = new Scanner(new File(fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        ArrayList<Integer> line = readIntLine(fileInput);
        // System.out.println(line);
        maxInRow = line.get(0);
        ArrayList<Integer> xForNode = readIntLine(fileInput);
        ArrayList<Integer> yRangeNode = readIntLine(fileInput);
        // System.out.println(xForNode);
        // System.out.println(yRangeNode);
        for(int y=0; y < yRangeNode.size()-1; y+=1){
            int start = yRangeNode.get(y);
            int end = yRangeNode.get(y+1);
            for(int id = start; id<end; id+=1){
                grafNodes.add(new Node(id, xForNode.get(id), y));
            }
        }

        ArrayList<Integer> conectionList = readIntLine(fileInput);
        ArrayList<Integer> conectionRange = readIntLine(fileInput);
        // System.out.println(conectionList);
        // System.out.println(conectionRange);

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

    private ArrayList<Integer> readIntLineFromBytes(InputStream input){
        ArrayList<Integer> retVal = new ArrayList<Integer>();
        while (true) {
            int curentInt = 0;
            byte[] bufer;

            try {
                bufer = input.readNBytes(4);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            for(int i = 0; i< 4; i++){
                curentInt |= bufer[i]<<(i*8);
            }
            if(curentInt == -1){
                break;
            }
            retVal.add(curentInt);
        }
        return retVal;
    }

    public void loadFromBinary(String fileName){
        InputStream fileInput = null;
        try {
            fileInput = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        ArrayList<Integer> line = readIntLineFromBytes(fileInput);
        maxInRow = line.get(0);
        ArrayList<Integer> xForNode = readIntLineFromBytes(fileInput);
        ArrayList<Integer> yRangeNode = readIntLineFromBytes(fileInput);
        for(int y=0; y < yRangeNode.size()-1; y+=1){
            int start = yRangeNode.get(y);
            int end = yRangeNode.get(y+1);
            for(int id = start; id<end; id+=1){
                grafNodes.add(new Node(id, xForNode.get(id), y));
            }
        }

        ArrayList<Integer> conectionList = readIntLineFromBytes(fileInput);
        ArrayList<Integer> conectionRange = readIntLineFromBytes(fileInput);

        for(int i=0; i < conectionRange.size()-1; i+=1){
            int start = conectionRange.get(i);
            int end = conectionRange.get(i+1);
            Node curentNode = grafNodes.get(conectionList.get(start));
            for(int j=start+1; j < end; j+=1){
                curentNode.conectTo(grafNodes.get(conectionList.get(j)));
            }
        }
    }

    private static class DataToSave {
        int maxInRowOfGrafs;
        ArrayList<Integer> xRow;
        ArrayList<Integer> yRow;
        ArrayList<Integer> conToSave;
        ArrayList<ArrayList<Integer>> conRange;

        private void readGraf(Graf graf){
            ArrayList<Integer> newList = new ArrayList<Integer>();
            newList.add(conToSave.size());

            for(Node node : graf.grafNodes){
                conToSave.add(node.getId());
                Boolean isBigeId = false;
                for(Node c : node.getConection()){
                    if(node.compareTo(c) > 0){
                        continue;
                    }
                    isBigeId = true;
                    conToSave.add(c.getId());
                }
                if(isBigeId == false){
                    conToSave.removeLast();
                }else{
                    newList.add(conToSave.size());
                }
            }
            conRange.add(newList);
        }

        DataToSave(List<Graf> grafList){
            ArrayList<Node> nodesToSave = new ArrayList<Node>();
            maxInRowOfGrafs = 0;
            for (Graf graf : grafList) {
                if( maxInRowOfGrafs < graf.maxInRow ){
                    maxInRowOfGrafs = graf.maxInRow;
                }
                nodesToSave.addAll(graf.grafNodes);
            }
            nodesToSave.sort(null);

            // sprawdzanie poprawnosci danych wejsciowych
            if(nodesToSave.getFirst().getId() != 0){
                throw new IllegalArgumentException("brakuje id 0");
            }
            for (int i = 1; i < nodesToSave.size(); i++) {
                if( (nodesToSave.get(i).getId() - nodesToSave.get(i-1).getId()) != 1){
                    throw new IllegalArgumentException("brakuje id " + (nodesToSave.get(i-1).getId()+1) );
                }
            }

            xRow = new ArrayList<Integer>();
            yRow = new ArrayList<Integer>();

            int curentY = 0;
            yRow.add(0);
            for (Node node : nodesToSave) {
                xRow.add(node.getX());
                if(curentY != node.getY()){
                    yRow.add(node.getId());
                    curentY+=1;
                }
            }
            yRow.add(nodesToSave.getLast().getId() + 1);

            conToSave = new ArrayList<Integer>();
            conRange = new ArrayList<ArrayList<Integer>>();
            for(Graf graf : grafList){
                readGraf(graf);
            }
        }
    }

    public static void saveToFileTxt(List<Graf> grafList, String outFileName){
        DataToSave data = new DataToSave(grafList);

        try {
            FileWriter writer = new FileWriter(outFileName);
            writer.write(data.maxInRowOfGrafs + "\n");

            writer.write(
                data.xRow.stream()
                .map(o -> String.valueOf(o))
                .collect(Collectors.joining(";")) + "\n"
            );

            writer.write(
                data.yRow.stream()
                .map(o -> String.valueOf(o))
                .collect(Collectors.joining(";")) + "\n"
            );

            writer.write(
                data.conToSave.stream()
                .map(o -> String.valueOf(o))
                .collect(Collectors.joining(";"))
            );

            for (ArrayList<Integer> arrayList : data.conRange) {
                writer.write(
                    "\n" +
                    arrayList.stream()
                    .map(o -> String.valueOf(o))
                    .collect(Collectors.joining(";"))
                );
            }

            writer.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] intToByts(int value){
        byte[] retVal = new byte[Integer.BYTES];
        for (int i = 0; i < retVal.length; i++) {
            retVal[i] = (byte) (value & 0xff);
            value>>=8;
        }
        return retVal;
    }

    public static void saveToFileBinary(List<Graf> grafList, String outFileName) throws IOException{
        DataToSave data = new DataToSave(grafList);

        try {
            FileOutputStream output = new FileOutputStream(outFileName);

            output.write(intToByts(data.maxInRowOfGrafs));
            output.write(intToByts(-2));
            
            for (int i : data.xRow) {
                output.write(intToByts(i));
            }
            output.write(intToByts(-2));

            for (int i : data.yRow) {
                output.write(intToByts(i));
            }
            output.write(intToByts(-2));

            for (int i : data.conToSave) {
                output.write(intToByts(i));
            }
            // output.write(intToByts(-2));

            for (ArrayList<Integer> arrayList : data.conRange) {
                output.write(intToByts(-2));
                for (int i : arrayList) {
                    output.write(intToByts(i));
                }
            }

            output.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public interface GrafCutFinder {
        ArrayList<Node> nodesForSecentGraf(ArrayList<Node> graf, int minSize);
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
}