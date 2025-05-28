package cutGraf;

import java.security.KeyStore.Entry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class cutUnconected implements Graf.GrafCutFinder{

    @Override
    public ArrayList<Node> nodesForSecentGraf(ArrayList<Node> graf, int minSize) {
        HashMap<Node, Boolean> isVisited = new HashMap<Node, Boolean>();
        for (Node node : graf) {
            isVisited.put(node, false);
        }
        Stack<Node> toVisit = new Stack<Node>();
        toVisit.push(graf.get(0));

        while (!toVisit.empty()) {
            Node toFlip = toVisit.pop();
            isVisited.put(toFlip, true);
            for (Node node : toFlip.getConection()) {
                if(isVisited.get(node)){
                    continue;
                }
                toVisit.push(node);
            }
        }

        ArrayList<Node> retList = new ArrayList<Node>();
        for (Node node : graf) {
            if(isVisited.get(node)){
                continue;
            }
            retList.add(node);
        }
        
        return retList;
    }
    
}
