package cutGraf;

import java.util.ArrayList;

public interface GrafCutFinder {
    ArrayList<Node> nodesForSecentGraf(ArrayList<Node> graf, int minSize);
}
