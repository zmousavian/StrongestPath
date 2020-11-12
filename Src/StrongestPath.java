import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

class IdValuePair {
	int id;
	double value;

	public IdValuePair(int id, double value) {
		this.id = id;
		this.value = value;
	}
}

public class StrongestPath {
	String specie;
	DestSrcSampleGraph srcGraph, destGraph, expGraph;

	SampleGraph sampleGraph = null;
	SampleGraph invertedSampleGraph;
	Dijkstra srcDij;
	Dijkstra destDij;

	String[] proteins;
	Nomenclature nomenclature = null;

	ArrayList<Integer> path;
	String[] strongestPath;

	public StrongestPath(String specie, Nomenclature nomenclature,
			String databaseName) throws IOException {
		this.specie = specie;

		if(sampleGraph == null)
		{
				sampleGraph = new SampleGraph(specie, databaseName);
				invertedSampleGraph = new SampleGraph(specie,
				databaseName.split("\\.")[0] + "-Inverted.txt");
		}
		srcGraph = new DestSrcSampleGraph(sampleGraph);

		destGraph = new DestSrcSampleGraph(invertedSampleGraph);
		this.nomenclature = nomenclature;

		srcDij = new Dijkstra();
		destDij = new Dijkstra();
	}

	public void setSources(String[] sources) throws Exception {

		ArrayList<Integer> srcs = new ArrayList<Integer>();
		int proteinId;
		for (String proteinName : sources) {
			proteinId = nomenclature.NametoID(proteinName.trim());
			if (proteinId != -1)
				srcs.add(proteinId);
		}

		srcGraph.sources = new int[srcs.size()];
		srcGraph.subGraph.clear();
		for (int i = 0; i < srcs.size(); i++) {
			srcGraph.sources[i] = srcs.get(i);
			srcGraph.subGraph.put(srcs.get(i), 0);

		}

		destGraph.destinations = new int[srcs.size()];
		destGraph.subGraph.clear();
		for (int i = 0; i < srcs.size(); i++)
			destGraph.destinations[i] = srcs.get(i);

	}

	public void setDestinations(String[] destinations) throws Exception {
		ArrayList<Integer> dests = new ArrayList<Integer>();
		int proteinId;
		for (String proteinName : destinations) {
			proteinId = nomenclature.NametoID(proteinName.trim());

			if (proteinId != -1)
				dests.add(proteinId);
		}
		srcGraph.destinations = new int[dests.size()];
		srcGraph.subGraph.clear();
		for (int i = 0; i < dests.size(); i++) {
			srcGraph.destinations[i] = dests.get(i);
		}
		destGraph.sources = new int[dests.size()];

		for (int i = 0; i < dests.size(); i++)
			destGraph.sources[i] = dests.get(i);

	}
	/*
	public String[] getStrongestPath() {
		srcDij.shortestPaths(srcGraph, 0, true);
		path = srcDij.getPath(srcDij.dest);
		strongestPath = new String[path.size()];
		for (int i = 0; i < path.size(); i++) {
			strongestPath[i] = path.get(i).toString();// nomenclature.idToName(path.get(i));
		}
		return strongestPath;
	}

	public Vector<ArrayList<String>> getStrongestPaths(double threshold) {
		Vector<ArrayList<String>> paths = new Vector<ArrayList<String>>();
		srcDij.shortestPaths(srcGraph, 0, false);

		destDij.shortestPaths(destGraph, 0, false);
		ArrayList<IdValuePair> nodeValues = new ArrayList<IdValuePair>();
		double t;
		for (int i = 0; i < srcDij.result.size(); i++) {
			t = srcDij.result.get(i);
			t += destDij.result.get(i);
			nodeValues.add(new IdValuePair(i, t));
		}
		Collections.sort(nodeValues, new Comparator<IdValuePair>() {

			@Override
			public int compare(IdValuePair o1, IdValuePair o2) {
				if (o1.value < o2.value)
					return -1;
				else if (o1.value > o2.value)
					return 1;
				else
					return 0;
			}
		});
		ArrayList<Integer> pathToSrc;
		ArrayList<Integer> pathToDst;
		ArrayList<String> tempPath;
		double min = nodeValues.get(1).value;
		for (IdValuePair p : nodeValues) {
			if (p.value - min <= threshold) {
				pathToSrc = srcDij.getPath(p.id);
				pathToDst = destDij.getPath(p.id);
				tempPath = new ArrayList<String>();
				for (int i = 0; i < pathToSrc.size(); i++) {
					tempPath.add(pathToSrc.get(i).toString());// nomenclature.idToName(pathToSrc.get(i));
				}

				pathToDst.remove(pathToDst.size() - 1);
				if (pathToDst.size() >= 1) {
					pathToDst.remove(0);

					Collections.reverse(pathToDst);
					for (int i = 0; i < pathToDst.size(); i++) {
						tempPath.add(pathToDst.get(i).toString());// nomenclature.idToName(pathToDst.get(i));
					}
				}
				paths.add(tempPath);

			}
		}
		return paths;
	}
	*/

	public Vector<PairLR> getStrongestPathsGraph(double threshold, int is_edge_connectivity_full) {


		srcDij.shortestPaths(srcGraph, 0, false);
		destDij.shortestPaths(destGraph, 0, false);

		ArrayList<IdValuePair> nodeValues = new ArrayList<IdValuePair>();
		double t;
		for (int i = 0; i < srcDij.result.size(); i++) {
			t = srcDij.result.get(i);
			t += destDij.result.get(i);
			nodeValues.add(new IdValuePair(i, t));
//			if (i < 10)
//				System.out.println("node :"+ i + " = " + t);
		}
		Collections.sort(nodeValues, new Comparator<IdValuePair>() {

			@Override
			public int compare(IdValuePair o1, IdValuePair o2) {
				if (o1.value < o2.value)
					return -1;
				else if (o1.value > o2.value)
					return 1;
				else
					return 0;
			}
		});
		ArrayList<HashSet<Integer>> shortestPathGraphs = new ArrayList<HashSet<Integer>>();
		HashMap<Integer, Integer> shortestPathGraphInds = new HashMap<Integer, Integer>();
		shortestPathGraphs.clear();
		shortestPathGraphInds.clear();
		//mahduf
		double min = nodeValues.get(1).value;
//		System.out.println("min = " + min);
		HashSet<Integer> tempSet;

		tempSet = new HashSet<Integer>();
		tempSet.add(destGraph.proteinsCount);
		shortestPathGraphInds.put(0, shortestPathGraphs.size());
		shortestPathGraphs.add(tempSet);

		tempSet = new HashSet<Integer>();
		tempSet.add(0) ;
		shortestPathGraphInds.put(destGraph.proteinsCount, shortestPathGraphs.size());
		shortestPathGraphs.add(tempSet);

		for (IdValuePair p : nodeValues) {
			if (p.value == 0) {
				continue;
			}

//			if(p.id < 10)
//				System.out.println(p.id+" "+p.value +" " + min);
			if (p.value - min <= java.lang.Double.max(threshold, 1e-9)) {

				//** add parent of node 'p' (in sources graph) to its neighbors
				int srcP = srcDij.resultsParent.get(p.id);
				int dstP = destDij.resultsParent.get(p.id);
//				System.out.println("node = " + p.id + " src = " + srcP + " dest = " +dstP);

				if(shortestPathGraphInds.get(srcP) == null)
				{
					tempSet = new HashSet<Integer>();
					tempSet.add(p.id);
					shortestPathGraphInds.put(srcP, shortestPathGraphs.size());
					shortestPathGraphs.add(tempSet);
				}
				else
					shortestPathGraphs.get(shortestPathGraphInds.get(srcP)).add(p.id);

				//** add parent of node 'p' (in destinations graph) to its neighbors
				if (dstP == 0) {
					dstP = destGraph.proteinsCount;
				}
				if(shortestPathGraphInds.get(dstP) == null)
				{
					tempSet = new HashSet<Integer>();
					tempSet.add(p.id);
					shortestPathGraphInds.put(dstP, shortestPathGraphs.size());
					shortestPathGraphs.add(tempSet);
				}
				else
					shortestPathGraphs.get(shortestPathGraphInds.get(dstP)).add(p.id);

				if(shortestPathGraphInds.get(p.id) == null)
				{
					tempSet = new HashSet<Integer>();
					tempSet.add(srcP);
					if (dstP != 0)
						tempSet.add(dstP);
					shortestPathGraphInds.put(p.id, shortestPathGraphs.size());
					shortestPathGraphs.add(tempSet);
				}
				else
				{
					shortestPathGraphs.get(shortestPathGraphInds.get(p.id)).add(srcP);
					if (dstP != 0)
						shortestPathGraphs.get(shortestPathGraphInds.get(p.id)).add(dstP);
				}

				if (is_edge_connectivity_full == 1){
					Map<Integer, Double> srcGraphNeighbors = srcGraph.edgesFrom(p.id);
					for (Integer key : srcGraphNeighbors.keySet()) {
						int u = p.id, v = key;
						if (v == 0)
							continue;
						double weight = srcGraphNeighbors.get(key);
						double pvalue_edge = srcDij.result.get(u) + weight + destDij.result.get(v);
						if (pvalue_edge - min <= java.lang.Double.max(threshold, 1e-9)) {
	//						System.out.println(u+" ----> "+v + " = " + weight + " pvalue = " + pvalue_edge );
							if (shortestPathGraphInds.get(v) == null) {
								tempSet = new HashSet<Integer>();
								tempSet.add(u);
								shortestPathGraphInds.put(v, shortestPathGraphs.size());
								shortestPathGraphs.add(tempSet);
							}
							else
								shortestPathGraphs.get(shortestPathGraphInds.get(v)).add(u);

							if (shortestPathGraphInds.get(u) == null) {
								tempSet = new HashSet<Integer>();
								tempSet.add(v);
								shortestPathGraphInds.put(u, shortestPathGraphs.size());
								shortestPathGraphs.add(tempSet);
							}
							else
								shortestPathGraphs.get(shortestPathGraphInds.get(u)).add(v);
						}
					}
				}
			}
		}

		BiConnected bc = new BiConnected(shortestPathGraphs.size());
		HashMap<Integer, Integer> nodeToId = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> idToNode = new HashMap<Integer, Integer>();

		Iterator<Entry<Integer, Integer>> gIterator = shortestPathGraphInds.entrySet().iterator();
		Entry<Integer, Integer> node;
		int i = 1;
		while (gIterator.hasNext()) {
			node = gIterator.next();
			nodeToId.put(node.getKey(), i);
			idToNode.put(i, node.getKey());
			i++;
		}

		gIterator = shortestPathGraphInds.entrySet().iterator();
		Iterator<Integer> adjsIterator;
		int a;
		while (gIterator.hasNext()) {
			node = gIterator.next();
			bc.e[nodeToId.get(node.getKey())] = new Vector<Integer>();
			bc.e[nodeToId.get(node.getKey())].clear();

			adjsIterator = shortestPathGraphs.get(node.getValue()).iterator();

			for (; adjsIterator.hasNext();) {
				a = adjsIterator.next();
				bc.e[nodeToId.get(node.getKey())].add(nodeToId.get(a));
			}
		}

		Vector<Vector<PairLR>> comps = bc.componentsOf(1);
		Vector<PairLR> vp = new Vector<PairLR>();
		boolean hasDst;

		int dst = nodeToId.get(destGraph.proteinsCount);
		String leftString, rightString;
		for (Vector<PairLR> v : comps) {
			hasDst = false;
			vp.clear();
			for (PairLR p : v) {
				if (idToNode.get(p.r) == 0)
					rightString = "source";
				else if (idToNode.get(p.r) == sampleGraph.proteinsCount)
					rightString = "destination";
				else
					rightString = nomenclature.IDtoName(idToNode.get(p.r));

				if (idToNode.get(p.r) == 0)
					leftString = "source";
				else if (idToNode.get(p.r) == sampleGraph.proteinsCount)
					leftString = "destination";
				else
					leftString = nomenclature.IDtoName(idToNode.get(p.l));

				vp.add(new PairLR(idToNode.get(p.l), idToNode.get(p.r),
						leftString, rightString)

				);
				if (p.l == dst || p.r == dst) {
					hasDst = true;
				}
			}
			if (hasDst)
				break;
		}

		return vp;

	}

	public Vector<PairLR> getStrongestPathsDirectedGraph(double threshold) {

		srcDij.shortestPaths(srcGraph, 0, false);
		destDij.shortestPaths(destGraph, 0, false);
		ArrayList<IdValuePair> nodeValues = new ArrayList<IdValuePair>();
		double t;
		for (int i = 0; i < srcDij.result.size(); i++) {
			t = srcDij.result.get(i);
			t += destDij.result.get(i);
			nodeValues.add(new IdValuePair(i, t));
		}
		Collections.sort(nodeValues, new Comparator<IdValuePair>() {

			@Override
			public int compare(IdValuePair o1, IdValuePair o2) {
				if (o1.value < o2.value)
					return -1;
				else if (o1.value > o2.value)
					return 1;
				else
					return 0;
			}
		});
		HashMap<Integer, HashSet<Integer>> shortestPathGraph = new HashMap<Integer, HashSet<Integer>>();
		shortestPathGraph.clear();
		double min = nodeValues.get(1).value;
		Integer srcP, dstP;
		HashSet<Integer> tempSet, tempSet2;

		tempSet = new HashSet<Integer>();
		tempSet.add(destGraph.proteinsCount);
		shortestPathGraph.put(0, tempSet);

		tempSet = new HashSet<Integer>();
		tempSet.add(0);
		shortestPathGraph.put(destGraph.proteinsCount, tempSet);

		for (IdValuePair p : nodeValues) {
			if (p.value == 0) {
				continue;
			}
			if (p.value - min <=java.lang.Double.max(threshold, 1e-9)) {
				srcP = srcDij.resultsParent.get(p.id);
				dstP = destDij.resultsParent.get(p.id);
				tempSet = new HashSet<Integer>();
				tempSet.add(p.id);
				tempSet2 = shortestPathGraph.get(srcP);
				tempSet2 = (tempSet2 == null ? new HashSet<Integer>()
						: tempSet2);
				tempSet.addAll(tempSet2);
				shortestPathGraph.put(srcP, tempSet);
				if (dstP == 0) {
					dstP = destGraph.proteinsCount;
				}
				if (dstP != 0)
					tempSet.add(dstP);
				tempSet2 = shortestPathGraph.get(p.id);
				tempSet2 = (tempSet2 == null ? new HashSet<Integer>()
						: tempSet2);
				tempSet.addAll(tempSet2);
				shortestPathGraph.put(p.id, tempSet);

			}
		}

		BiConnected bc = new BiConnected(shortestPathGraph.size());
		HashMap<Integer, Integer> nodeToId = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> idToNode = new HashMap<Integer, Integer>();

		Iterator<Entry<Integer, HashSet<Integer>>> gIterator = shortestPathGraph
				.entrySet().iterator();
		Entry<Integer, HashSet<Integer>> node;
		int i = 1;
		while (gIterator.hasNext()) {
			node = gIterator.next();
			nodeToId.put(node.getKey(), i);
			idToNode.put(i, node.getKey());
			i++;
		}

		gIterator = shortestPathGraph.entrySet().iterator();
		Iterator<Integer> adjsIterator;
		int a;
		while (gIterator.hasNext()) {
			node = gIterator.next();
			bc.e[nodeToId.get(node.getKey())] = new Vector<Integer>();
			bc.e[nodeToId.get(node.getKey())].clear();

			adjsIterator = node.getValue().iterator();

			for (; adjsIterator.hasNext();) {
				a = adjsIterator.next();
				bc.e[nodeToId.get(node.getKey())].add(nodeToId.get(a));
			}
		}
		Vector<Vector<PairLR>> comps = bc.componentsOf(1);
		Vector<PairLR> vp = new Vector<PairLR>();
		boolean hasDst;

		int dst = nodeToId.get(destGraph.proteinsCount);
		String leftString, rightString;
		for (Vector<PairLR> v : comps) {
			hasDst = false;
			vp.clear();
			for (PairLR p : v) {
				if (idToNode.get(p.r) == 0)
					rightString = "source";
				else if (idToNode.get(p.r) == sampleGraph.proteinsCount)
					rightString = "destination";
				else
					rightString = nomenclature.IDtoName(idToNode.get(p.r));

				if (idToNode.get(p.r) == 0)
					leftString = "source";
				else if (idToNode.get(p.r) == sampleGraph.proteinsCount)
					leftString = "destination";
				else
					leftString = nomenclature.IDtoName(idToNode.get(p.l));

				vp.add(new PairLR(idToNode.get(p.l), idToNode.get(p.r),
						leftString, rightString)

				);
				if (p.l == dst || p.r == dst) {
					hasDst = true;
				}
			}
			if (hasDst)
				break;
		}

		return vp;
	}

	public Map<String, Double> getConfidences() {
		Map<String, Double> confidences = new HashMap<String, Double>();
		for (int i = 0; i < path.size(); i++) {
			confidences.put(strongestPath[i], srcDij.result.get(path.get(i)));
		}
		return confidences;
	}

	public Map<String, Double> getConfidences(HashSet<Integer> nodes) {
		Map<String, Double> confidences = new HashMap<String, Double>();
		for (Integer a : nodes) {
			confidences.put(nomenclature.IDtoName(a), srcDij.result.get(a));
		}
		return confidences;
	}

	public Vector<PairLR> getSubNetwork(String dataBaseName) {
		Vector<PairLR> edges = new Vector<PairLR>();
		Object[] nodes = srcGraph.subGraph.keySet().toArray();
		for (int i = 0; i < nodes.length; i++) {

			Map<Integer, Double> m = srcGraph.edgesFrom((Integer) nodes[i]);
			for (int j = 0; j < nodes.length; j++) {

				if (m.containsKey(nodes[j]))
					edges.add(new PairLR((Integer) nodes[i], (Integer) nodes[j],
							nomenclature.IDtoName((Integer) nodes[i]),
							nomenclature.IDtoName((Integer) nodes[j]),
							dataBaseName));
			}
		}
		return edges;
	}

	public Vector<PairLR> expandAndGetSubNetwork(String dataBaseName,
												 int numberOfNewNodes) {
		srcGraph.expand(numberOfNewNodes);
		return getSubNetwork(dataBaseName);
	}

	public HashMap<Integer, Integer> getSubGraph() {

		return srcGraph.subGraph;
	}

}
