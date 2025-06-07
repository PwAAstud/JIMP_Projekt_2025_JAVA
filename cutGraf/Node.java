package cutGraf;

import java.util.ArrayList;

public class Node implements Comparable<Node>{
    private int x,y, id;
    private ArrayList<Node> conection = new ArrayList<Node>();

    Node(int id,int x,int y){
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public int getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public ArrayList<Node> getConection() {
        return new ArrayList<Node>(conection);
    }

    public void conectTo(Node conectWith){
        conection.remove(conectWith);
        conectWith.conection.remove(this);
        conection.add(conectWith);
        conectWith.conection.add(this);
    }

    public void removeConectTo(Node conectWith){
        conection.remove(conectWith);
        conectWith.conection.remove(this);
    }

    @Override
    public int compareTo(Node o) {
        return this.id - o.id;
    }

    @Override
    public String toString() {
        String conString = "";
        for (Node node : conection) {
            conString += node.id + " ";
        }
        if(conString != ""){
            conString = conString.substring(0, conString.length()-1);
        }
        conString = "(" + conString + ")";
        return String.valueOf(id) + ":"+conString;
    }
<<<<<<< Updated upstream
=======

    @Override
    public int hashCode() {
        return id;
    }
    @Override
public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    Node other = (Node) obj;
    return id == other.id;
}

>>>>>>> Stashed changes
}