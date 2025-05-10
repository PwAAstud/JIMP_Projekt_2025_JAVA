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

    private class WeightsBuckets {
        HashMap<WeightNode, Integer> WeightToNodes = new HashMap<WeightNode, Integer>();
        int bigestValue = 0; // numer jakdalszego wiadra z wartoscia
        ArrayList<ArrayList<WeightNode>> buckets = new ArrayList<ArrayList<WeightNode>>(); // index wiadra + 1 odpowiada jakiej wadze wiadro odpoiwadza 0 i -1 pomijamy 

        WeightsBuckets(List<WeightNode> weighGraf){
            for (WeightNode weightNode : weighGraf) {
                WeightToNodes.put(weightNode, 0);
            }
        }

        void removeFromPul(WeightNode node){
            int curentBucek = WeightToNodes.get(node);
            if(curentBucek > 0){
                buckets.get(curentBucek-1).remove(node);
                if(buckets.get(curentBucek-1).size() == 0){
                    while (bigestValue > 0 && buckets.get(bigestValue-1).size() == 0) {
                        bigestValue-=1;
                    }
                }
            }
            WeightToNodes.put(node, -1);
        }

        // prosze nie podawac licz ujemnych
        void incresWeightOfNode(WeightNode node, int incresBy){
            int curentBucek = WeightToNodes.get(node);
            if(curentBucek  == -1){
                return;
            }
            if(curentBucek > 0){
                buckets.get(curentBucek-1).remove(node);
            }
            curentBucek += incresBy;
            WeightToNodes.put(node, curentBucek);
            if(curentBucek > bigestValue){
                bigestValue = curentBucek;
            }
            while (buckets.size() < curentBucek) {
                buckets.add(new ArrayList<WeightNode>());
            }
            buckets.get(curentBucek-1).add(node);
        }

        ArrayList<WeightNode> getHiestList(){
            return buckets.get(bigestValue-1);
        }

        @Override
        public String toString() {
            String retString = "";
            // retString += bigestValue + "\n";
            for (int i = 0; i < buckets.size(); i++) {
                retString += i+1 + ":";
                for (WeightNode node : buckets.get(i)) {
                    retString += node.compinationOf.get(0).getId() + "," + node.compinationOf.size() + " ";
                }
                retString += "; ";
            }
            return retString;
        }
    }

    // zwraca index to weighGraf i weights
    private WeightNode getNextBestNode(WeightsBuckets buckets){
        ArrayList<WeightNode> list = buckets.getHiestList();
        if(list.size() == 1){
            return list.getLast();
        }
        WeightNode last = list.getLast();
        WeightNode preLast = list.get(list.size()-2);
        if(last.compinationOf.size() > preLast.compinationOf.size()){
            return preLast;
        }else{
            return last;
        }
    }

    // nie usuwa element indexChosen z list
    private void addToBlock(WeightsBuckets buckets, WeightNode chosenNode){
        buckets.removeFromPul(chosenNode);
        for (Map.Entry<WeightNode, Integer> c : chosenNode.conection.entrySet()) {
            buckets.incresWeightOfNode(c.getKey(), c.getValue());
        }
    }

    private int fardesNode(ArrayList<WeightNode> weighGraf){
        WeightsBuckets buckets = new WeightsBuckets(weighGraf);
        buckets.incresWeightOfNode(weighGraf.get(0), 1);

        for(int i=1; i < weighGraf.size(); i++){
            WeightNode nextNode = getNextBestNode(buckets);
            addToBlock(buckets, nextNode);
            // System.out.println(buckets);
        }
        // System.out.println(indexToCheck);
        WeightNode nodeToReturn = getNextBestNode(buckets);
        int retVal;
        for(retVal = 0; weighGraf.get(retVal) != nodeToReturn; retVal+=1);
        return retVal;
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
        // System.out.println(aceptableNodes.size());
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
            System.out.print(weighGraf.size() + " ");
            long start = System.nanoTime();
            int fardesIndex = fardesNode(weighGraf);
            System.out.print((System.nanoTime() - start)/1000000 + "\r");
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
        // System.out.println(weighGraf);

        // weighGraf.get(3).addMerge(weighGraf.get(4));
        // System.out.println(retVal);
        return retVal;
        // throw new UnsupportedOperationException("Unimplemented method 'nodesFromSecentGraf'");
    }

}