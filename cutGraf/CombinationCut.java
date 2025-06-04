package cutGraf;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Stack;
import java.util.TreeSet;

public class CombinationCut implements Graf.GrafCutFinder{

    private class BitSetComp implements Comparator<BitSet>{
        @Override
        public int compare(BitSet o1, BitSet o2) {
            int i;
            for (i = 0; i < o1.length(); i++) {
                if( o1.get(i) !=  o2.get(i) ){
                    break;
                }
            }
            int io1 = o1.get(i) ? 1 : 0;
            int io2 = o2.get(i) ? 1 : 0;
            return io1 - io2;
        }
    }

    private class Step{
        static HashMap<Node, Integer> locationInGraf;
        static ArrayList<Node> graf;
        BitSet state;
        BitSet conected;
        Stack<Node> options;
        int cutStrenght;

        private void genereOption(){
            for (int i = 0; i < graf.size(); i++) {
                if( conected.get(i) ){
                    options.push( graf.get(i) );
                }
            }
        }

        private void flip(Node node){
            int flipI = locationInGraf.get(node);
            state.set(flipI, true);
            conected.set(flipI, false);
            for (Node c : node.getConection()) {
                int conI = locationInGraf.get(c);
                if( state.get(conI) ){
                    cutStrenght--;
                }else{
                    cutStrenght++;
                    conected.set( conI , true);
                }
            }
        }

        // zerbu zaczac
        void startSetup(ArrayList<Node> graf){
            this.graf = graf;
            this.locationInGraf = new HashMap<Node, Integer>();
            for (int i = 0; i < graf.size(); i++) {
                locationInGraf.put( graf.get(i), i );
            }
            state = new BitSet(graf.size());
            conected = new BitSet(graf.size());
            options = new Stack<Node>();
            cutStrenght = 0;
            flip( graf.get(0) );
            genereOption();
        }

        // usuwa elemnt z options
        Step nextMove(){
            Step retVal = new Step();
            retVal.state = (BitSet) this.state.clone();
            retVal.conected = (BitSet) this.conected.clone();
            retVal.options = new Stack<Node>(); 
            retVal.cutStrenght = this.cutStrenght;

            retVal.flip( this.options.pop() );
            retVal.genereOption();

            return retVal;
        }

    }

    @Override
    public ArrayList<Node> nodesForSecentGraf(ArrayList<Node> graf, int minSize) {
        int maxSize = graf.size() - minSize;
        // System.out.println(maxSize);

        TreeSet<BitSet> alerdyWass = new TreeSet<BitSet>( (o1, o2) -> {
            // System.out.print(o1);
            // System.out.print(" ");
            // System.out.print(o2);
            // System.out.print(o1.length() +" "+ o2.length());
            int len = (o1.length() >= o2.length()) ? o1.length() : o2.length();
            int i;
            for (i = 0; i < len; i++) {
                if( o1.get(i) !=  o2.get(i) ){
                    break;
                }
            }
            int io1 = o1.get(i) ? 1 : 0;
            int io2 = o2.get(i) ? 1 : 0;
            // System.out.println(": " + (io2 - io1));
            return io2 - io1;
        });

        BitSet bestState = null;
        int bestCut = Integer.MAX_VALUE;

        ArrayList<Step> stosAkcji = new ArrayList<Step>();

        Step s = new Step();
        s.startSetup(graf);
        stosAkcji.add(s);

        if(stosAkcji.size() >= minSize && bestCut > s.cutStrenght){
            bestCut = s.cutStrenght;
            bestState = s.state;
        }

        int skipt = 0;

        while ( !stosAkcji.isEmpty() ) {
            Step fardest = stosAkcji.getLast();
            if(fardest.options.isEmpty() || stosAkcji.size() >= maxSize){
                stosAkcji.removeLast();
                continue;
            }
            fardest = fardest.nextMove();
            if( alerdyWass.contains( fardest.state ) ){
                System.out.print("\r"+ (skipt++));
                // System.out.println(fardest.state);
                continue;
            }
            // System.out.println(fardest.state);
            alerdyWass.add(fardest.state);
            stosAkcji.add(fardest);
            // System.out.println(fardest.state);
            // System.out.println(fardest.cutStrenght);
            // System.out.println(stosAkcji.size() >= minSize);
            // if(stosAkcji.size() >= minSize){
            // }
            if(stosAkcji.size() >= minSize && bestCut > fardest.cutStrenght){
                bestCut = fardest.cutStrenght;
                bestState = (BitSet) fardest.state.clone();
            }
            // stosAkcji.removeLast();
        }

        System.out.println(bestState);
        ArrayList<Node> retVal = new ArrayList<Node>();
        for (int i = 0; i < graf.size(); i++) {
            if( bestState.get(i) ){
                retVal.add( graf.get(i) );
            }
        }

        return retVal;
    }
    
}
