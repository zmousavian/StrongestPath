

/**************************************************************************
 * File: Dijkstra.java
 * Author: Keith Schwarz (htiek@cs.stanford.edu)
 *
 * An implementation of Dijkstra's single-source shortest path algorithm.
 * The algorithm takes as input a directed graph with non-negative edge
 * costs and a source node, then computes the shortest path from that node
 * to each other node in the graph.
 *
 * The algorithm works by maintaining a priority queue of nodes whose
 * priorities are the lengths of some path from the source node to the
 * node in question.  At each step, the algortihm dequeues a node from
 * this priority queue, records that node as being at the indicated
 * distance from the source, and then updates the priorities of all nodes
 * in the graph by considering all outgoing edges from the recently-
 * dequeued node to those nodes.
 *
 * In the course of this algorithm, the code makes up to |E| calls to
 * decrease-key on the heap (since in the worst case every edge from every
 * node will yield a shorter path to some node than before) and |V| calls
 * to dequeue-min (since each node is removed from the prioritiy queue
 * at most once).  Using a Fibonacci heap, this gives a very good runtime
 * guarantee of O(|E| + |V| lg |V|).
 *
 * This implementation relies on the existence of a FibonacciHeap class, also
 * from the Archive of Interesting Code.  You can find it online at
 *
 *         http://keithschwarz.com/interesting/code/?dir=fibonacci-heap
 */


import java.util.*; // For HashMap

import javax.swing.JOptionPane;
public final class Dijkstra {
	public Map<Integer, Integer> resultsParent;
	public Map<Integer, Double> result;
	public Integer source;
	public int dest = -1;
    /**
     * Given a directed, weighted graph G and a source node s, produces the
     * distances from s to each other node in the graph.  If any nodes in
     * the graph are unreachable from s, they will be reported at distance
     * +infinity.
     *
     * @param graph The graph upon which to run Dijkstra's algorithm.
     * @param source The source node in the graph.
     * @return A map from nodes in the graph to their distances from the source.
     */
    public <T> void shortestPaths(DestSrcSampleGraph graph, Integer source, boolean untilReachDest) {
    	this.source = source;
        /* Create a Fibonacci heap storing the distances of unvisited nodes
         * from the source node.
         */
        FibonacciHeap<Integer> pq = new FibonacciHeap<Integer>();

        /* The Fibonacci heap uses an internal representation that hands back
         * Entry objects for every stored element.  This map associates each
         * node in the graph with its corresponding Entry.
         */
        Map<Integer, FibonacciHeap.Entry<Integer>> entries = new HashMap<Integer, FibonacciHeap.Entry<Integer>>();

        /* Maintain a map from nodes to their distances.  Whenever we expand a
         * node for the first time, we'll put it in here.
         */
        result = new HashMap<Integer, Double>();
        resultsParent = new HashMap<Integer, Integer>();
        /* Add each node to the Fibonacci heap at distance +infinity since
         * initially all nodes are unreachable.
         */
//        JOptionPane.showMessageDialog(null, "before");
        for (int i = 0; i < graph.proteinsCount; i++)
            entries.put(i, pq.enqueue(i, Double.POSITIVE_INFINITY));
//        JOptionPane.showMessageDialog(null, "after");

        /* Update the source so that it's at distance 0.0 from itself; after
         * all, we can get there with a path of length zero!
         */
//        JOptionPane.showMessageDialog(null, "before");
        pq.decreaseKey(entries.get(source), 0.0);
//        JOptionPane.showMessageDialog(null, "after");


        /* Keep processing the queue until no nodes remain. */
        while (!pq.isEmpty()) {
//        	 JOptionPane.showMessageDialog(null,pq.size());
            /* Grab the current node.  The algorithm guarantees that we now
             * have the shortest distance to it.
             */
            FibonacciHeap.Entry<Integer> curr = pq.dequeueMin();
            //System.out.println(curr.getPriority() + " " + curr.getValue());
            /* Store this in the result table. */
            result.put(curr.getValue(), curr.getPriority());
//            JOptionPane.showMessageDialog(null, "before");
            if(untilReachDest)
            {
	            for(int i = 0; i < graph.destinations.length; i++)
	            	if(curr.getValue() == graph.destinations[i])
	            	{
	            		dest = curr.getValue();
	            		return;
	            	}
            }
//            JOptionPane.showMessageDialog(null, "after");
//            JOptionPane.showMessageDialog(null, curr.getValue());

            /* Update the priorities of all of its edges. */
            for (Map.Entry<Integer, Double> arc : graph.edgesFrom((Integer)curr.getValue()).entrySet()) {
                /* If we already know the shortest path from the source to
                 * this node, don't add the edge.
                 */

//            	JOptionPane.showMessageDialog(null, "*");
                if (result.containsKey(arc.getKey())) continue;

                /* Compute the cost of the path from the source to this node,
                 * which is the cost of this node plus the cost of this edge.
                 */

                double pathCost = curr.getPriority() + arc.getValue();

                /* If the length of the best-known path from the source to
                 * this node is longer than this potential path cost, update
                 * the cost of the shortest path.
                 */

                FibonacciHeap.Entry<Integer> dest = entries.get(arc.getKey());

                if (pathCost < dest.getPriority())
                {
                    pq.decreaseKey(dest, pathCost);

                    resultsParent.put(dest.getValue(), curr.getValue());

                }

            }
        }

        /* Finally, report the distances we've found. */

    }


}