package cutGraf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class StonerCut implements GrafCutFinder{
    private Random rand = new Random();

    private class WeightNode{
        ArrayList<Node> compinationOf = new ArrayList<Node>();
        HashMap<WeightNode, Integer> conection = new HashMap<WeightNode, Integer>();

        // nie usuwa wn. musziasz sam to zrobic
        void addMerge(WeightNode wn){
            this.compinationOf.addAll(wn.compinationOf);

            this.conection.remove(wn);
            wn.conection.remove(this);
            for (Map.Entry<WeightNode, Integer> entry : wn.conection.entrySet()) {
                WeightNode entryKey = entry.getKey();
                Integer newWeigh = entry.getValue(); 
                if(this.conection.containsKey(entryKey)){
                    newWeigh += this.conection.get(entryKey);
                }
                entryKey.conection.remove(wn);
                entryKey.conection.put(this, newWeigh);
                this.conection.put(entryKey, newWeigh);
            }
        }

        @Override
        public String toString() {
            String conString = "";
            for (Map.Entry<WeightNode, Integer> entry : conection.entrySet()) {
                conString += String.valueOf( entry.getKey().compinationOf.get(0).getId() ) + ":" + entry.getValue() + " ";
                // conString += entry.getValue() + " ";
            }
            if(conString != ""){
                conString = conString.substring(0, conString.length()-1);
            }
            conString = "(" + conString + ")";
            String combinationStirng = "";
            for (Node node : compinationOf) {
                combinationStirng += node.getId() + " ";
            }
            combinationStirng = combinationStirng.substring(0, combinationStirng.length()-1);
            combinationStirng = "(" + combinationStirng + ")";
            return combinationStirng + ":" + conString;
            // return compinationOf.toString();
        }

        @Override
        public int hashCode() {
            // poniewsz wiemy ze nodey w compinationOf sa unikatowe
            return compinationOf.get(0).getId();
        }
    }

    // start

    // zwraca index to weighGraf i weights
    private int indexOfMaxWeing(ArrayList<WeightNode> weighGraf, int[] weights, List<Integer> indexList){
        int maxWeight = indexList.get(0);
        for (int i = 1; i<indexList.size();i++) {
            int indexFromList = indexList.get(i);
            if(weights[maxWeight] < weights[indexFromList]){
                maxWeight = indexFromList;
            }
            if(weights[maxWeight] != weights[indexFromList]){
                continue;
            }
            if(weighGraf.get(maxWeight).compinationOf.size() > weighGraf.get(indexFromList).compinationOf.size()){
                maxWeight = indexFromList;
            }
            // if(rand.nextBoolean()){
            //     maxWeight = i;
            // }
        }
        // System.out.println(maxWeight);
        return maxWeight;
    }

    // nie usuwa element indexChosen z list
    private void addToBlock(ArrayList<WeightNode> weighGraf, HashMap<WeightNode, Integer> translatr, int[] weightToMain, ArrayList<Integer> priIndexToCheck, ArrayList<Integer> lowIndexToCheck, int indexChosen){
        weightToMain[indexChosen] = -1; // -1 == odwiedzony
        for (Map.Entry<WeightNode, Integer> c : weighGraf.get(indexChosen).conection.entrySet()) {
            int indexInGraf = translatr.get(c.getKey());
            if(weightToMain[indexInGraf] == -1){
                continue;
            }
            boolean inLowList = false;
            if(weightToMain[indexInGraf] == 1){
                inLowList = true;
            }
            weightToMain[indexInGraf] += c.getValue();
            if(weightToMain[indexInGraf] >= 2){
                if(inLowList){
                    lowIndexToCheck.remove(Integer.valueOf(indexInGraf));
                }
                priIndexToCheck.add(indexInGraf);
            }else{
                lowIndexToCheck.add(indexInGraf);
            }
        }
    }

    private int fardesNode(ArrayList<WeightNode> weighGraf){
        HashMap<WeightNode, Integer> nodeToIndex = new HashMap<WeightNode, Integer>();
        int[] weightToMain = new int[weighGraf.size()];
        for(int i=0; i< weighGraf.size(); i++){
            weightToMain[i] = 0;
            nodeToIndex.put(weighGraf.get(i), i);
        }
        ArrayList<Integer> priIndexToCheck = new ArrayList<Integer>(); // piorytet
        ArrayList<Integer> lowIndexToCheck = new ArrayList<Integer>();
        // indexToCheck.add(rand.nextInt(weighGraf.size()));
        lowIndexToCheck.add(0);

        long sumA = 0;
        long sumB = 0;
        for(int i=1; i < weighGraf.size(); i++){
            // System.out.println(priIndexToCheck);
            // System.out.println(lowIndexToCheck);
            // for (int j = 0; j < weightToMain.length; j++) {
            //     System.out.print(weightToMain[j]+" ");                
            // }
            // System.out.println();
            long start = System.nanoTime();   
            int indexChosen;
            if(priIndexToCheck.size() > 0){
                indexChosen = indexOfMaxWeing(weighGraf, weightToMain, priIndexToCheck);
                priIndexToCheck.remove(Integer.valueOf(indexChosen));
            }else{
                indexChosen = indexOfMaxWeing(weighGraf, weightToMain, lowIndexToCheck);
                lowIndexToCheck.remove(Integer.valueOf(indexChosen));
            }
            sumA += (System.nanoTime() - start);
            start = System.nanoTime();
            addToBlock(weighGraf, nodeToIndex, weightToMain, priIndexToCheck, lowIndexToCheck, indexChosen);
            sumB += (System.nanoTime() - start);
        }
        sumA /= 1000000;
        sumB /= 1000000;
        // System.out.println(sumA + " " + sumB);
        // System.out.println(indexToCheck);
        if(priIndexToCheck.size() > 0){
            return priIndexToCheck.get(0);
        }else{
            return lowIndexToCheck.get(0);
        }
    }

    // end

    // zwraca czy dalej jest spelniony maxSize
    private boolean removeNode(ArrayList<WeightNode> weighGraf, int removeIndex, int maxSize){
        WeightNode nodeToRemove = weighGraf.get(removeIndex);

        WeightNode nodeToMergeWith = null;
        int conectionStrenght = 0;
        ArrayList<WeightNode> aceptableNodes = new ArrayList<WeightNode>();
        for (Map.Entry<WeightNode, Integer> entry : nodeToRemove.conection.entrySet()) {
            WeightNode entryKey = entry.getKey();
            int entryVal = entry.getValue();

            if(entryKey.compinationOf.size() + nodeToRemove.compinationOf.size() > maxSize){
                continue;
            }
            if(entryVal > conectionStrenght){
                aceptableNodes.clear();
                aceptableNodes.add(entryKey);
                conectionStrenght = entryVal;
                // nodeToMergeWith = entryKey;
            }
            else if(entryVal == conectionStrenght){
                aceptableNodes.add(entryKey);
            }
            // System.out.println(aceptableNodes);
            // else if(entryVal == conectionStrenght && rand.nextBoolean()){
            //     conectionStrenght = entryVal;
            //     nodeToMergeWith = entryKey;
            // }
        }
        System.out.println(aceptableNodes.size());
        if(aceptableNodes.size() == 0){
            return false;
        }
        nodeToMergeWith = aceptableNodes.get(rand.nextInt(aceptableNodes.size()));

        nodeToMergeWith.addMerge(nodeToRemove);

        weighGraf.remove(removeIndex);
        return true;
    }

    @Override
    public ArrayList<Node> nodesForSecentGraf(ArrayList<Node> graf, int minSize) {
        int maxSize = graf.size() - minSize;
        
        graf = new ArrayList<Node>(graf);
        Collections.sort(graf);
        ArrayList<WeightNode> weighGraf = new ArrayList<WeightNode>();
        for (Node n : graf) {
            WeightNode toAdd = new WeightNode();
            toAdd.compinationOf.add(n);
            weighGraf.add(toAdd);
        }
        for (int i = 0; i < graf.size(); i++) {
            Node n = graf.get(i);
            for (Node c : n.getConection()) {
                if(c.compareTo(n) < 0){
                    continue;
                }
                int conectWith = Collections.binarySearch(graf, c);
                weighGraf.get(i).conection.put(weighGraf.get(conectWith), 1);
                weighGraf.get(conectWith).conection.put(weighGraf.get(i), 1);
            }
        }

        ArrayList<Node> retVal = new ArrayList<Node>();
        int cutCost = Integer.MAX_VALUE;

        while (weighGraf.size() > 1) {
            // System.out.print(weighGraf.size()+" ");
            // long start = System.nanoTime();
            int fardesIndex = fardesNode(weighGraf);
            // System.out.print((System.nanoTime() - start)/1000000 + " ");
            // long start = System.nanoTime();


            WeightNode nodeToChek = weighGraf.get(fardesIndex);
            int nodeToChekSize = nodeToChek.compinationOf.size();
            if(nodeToChekSize >= minSize && nodeToChekSize <= maxSize){
                int cutCostOfnewNode = 0;
                for (Map.Entry<WeightNode, Integer> c : nodeToChek.conection.entrySet()){
                    cutCostOfnewNode += c.getValue();
                }
                if(cutCostOfnewNode < cutCost){
                    retVal = new ArrayList<Node>(nodeToChek.compinationOf);
                    cutCost = cutCostOfnewNode;
                }
            }
            if(!removeNode(weighGraf, fardesIndex, maxSize)){
                break;
            }

            // System.out.print((System.nanoTime() - start)/1000000);

        }
        System.out.println(weighGraf);

        // weighGraf.get(3).addMerge(weighGraf.get(4));
        // System.out.println(retVal);
        return retVal;
        // throw new UnsupportedOperationException("Unimplemented method 'nodesFromSecentGraf'");
    }



}