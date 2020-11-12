import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

class DestSrcSampleGraph {
	HashMap<Integer, Integer> subGraph = new HashMap<>();
    SampleGraph sampleGraph;
    int depth = 1, proteinsCount;
	int[] sources, destinations;

	DestSrcSampleGraph(SampleGraph sampleGraph) {

		proteinsCount = sampleGraph.proteinsCount;
		this.sampleGraph = sampleGraph;
	}


	void addNeighbors() {
		Object[] subGraphSet = subGraph.keySet().toArray();
        for (Object node : subGraphSet) {
            // Here we get the neighbors of ith node, in the database
            // We add all the candidate neighbors to the set of all neighbors
            for (Entry<Integer, Double> neighbor : neighborsOf((Integer) node).entrySet()) {
                if (!subGraph.containsKey(neighbor.getKey()))
                    subGraph.put(neighbor.getKey(), depth);
            }
        }

		if (subGraph.keySet().size() != subGraphSet.length)
			depth += 1;
	}

	void expand(int numberOfNewNodes) {
		// subGraphSet is the list of nodes in the current network (visible
		// nodes).
		Object[] subGraphSet = subGraph.keySet().toArray();
		Map<Integer, Double> neighborsEdgeSum = new HashMap<>();
		Double tempSum;
        for (Object node : subGraphSet) {
            // Here we get the neighbors of ith node, in the database
            // We add all the candidate neighbors to the set of all neighbors
            for (Entry<Integer, Double> neighbor : neighborsOf(
                    (Integer) node).entrySet()) {
                if (!subGraph.containsKey(neighbor.getKey())) {
                    tempSum = neighborsEdgeSum.get(neighbor.getKey());

                    if (tempSum == null)
                        neighborsEdgeSum.put(neighbor.getKey(),
                                neighbor.getValue());
                    else
                        neighborsEdgeSum.put(neighbor.getKey(),
                                neighbor.getValue() + tempSum);

                }
            }
        }
		// Iterate over all candidate neighbors and for each of them find all
		// the neighbors
		// amongst those neighbors sum over those that are in the subGraph and
		// save this summation

		// in neighborsEdgeSum we have the sum of edges from neighbors to the
		// subGraph for each neighbor
		// we just need to sort them and take the top 10
		ArrayList<Entry<Integer, Double>> neighborsFinalList = new ArrayList<>(
				neighborsEdgeSum.entrySet());
		Collections.sort(neighborsFinalList,
				new Comparator<Entry<Integer, Double>>() {
					@Override
					public int compare(Entry<Integer, Double> arg0,
							Entry<Integer, Double> arg1) {
						if (arg0.getValue() > arg1.getValue())
							return -1;
						else if (arg0.getValue() < arg1.getValue())
							return 1;
						else
							return 0;
					}

				});
		if (neighborsFinalList.size() > 0)
			depth += 1;
		int l = Math.min(numberOfNewNodes, neighborsFinalList.size());
		for (int i = 0; i < l; i++)
			if (!subGraph.containsKey(neighborsFinalList.get(i).getKey()))
				subGraph.put(neighborsFinalList.get(i).getKey(), depth);

	}

	Map<Integer, Double> neighborsOf(int index) {
		Map<Integer, Double> arcs = new HashMap<>();

		if(index == 0 ) {
            for (int source : sources)
                arcs.put(source, SampleGraph.logD);
		}
		else {
			Map<Integer, Double> neighbors = sampleGraph.get_neighbors_of(index);

			if(neighbors == null)
				return arcs;
			for (Integer key : neighbors.keySet()) {
				arcs.put(key, Math.log(neighbors.get(key) / 1000.0) );
			}
		}
		return arcs;

	}

	Map<Integer, Double> edgesFrom(int index) {
		Map<Integer, Double> arcs = new HashMap<>();

		if (index == 0)
			for(int source : sources)
				arcs.put(source, SampleGraph.logD);
		else {

			Map<Integer, Double> neighbors = sampleGraph.get_neighbors_of(index);
			if (neighbors == null)
				return arcs;

			for (Integer key : neighbors.keySet()) {
				arcs.put(key, Math.log(1000.0 / neighbors.get(key)));
			}
		}
		return arcs;
	}
}
