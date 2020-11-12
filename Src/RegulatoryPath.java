import javafx.util.Pair;
import org.cytoscape.model.CyNode;

import java.io.IOException;
import java.util.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.lang.Math;


class RNode
{
    double is_exc;
    int depth ;
    int id;
    RNode parent;
    int is_destination = 0 ;
    int is_source = 0 ;
    int x , y ;
    int cnt ;

    CyNode cy_node;
    public RNode(int id, int depth, double is_exc, RNode parent) {
        this.id = id;
        this.depth = depth;
        this.is_exc = is_exc;
        this.parent = parent;
        this.y = depth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RNode rNode = (RNode) o;
        return id == rNode.id;
    }
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}

public class RegulatoryPath
{
    String specie;
    SampleGraph sampleGraph ;
    SampleGraph invertedSampleGraph ;
    Nomenclature nomenclature ;

    public RegulatoryPath(String specie, Nomenclature nomenclature, String databaseName) throws IOException
    {
        this.specie = specie;
        sampleGraph = new SampleGraph(specie, databaseName);
        invertedSampleGraph = new SampleGraph(specie,
                databaseName.split("\\.")[0] + "-Inverted.txt");
        this.nomenclature = nomenclature;
    }

    public Vector<RNode> __getRegulatoryPath(String source, String[] dests, double is_exc, int is_source_based) throws Exception
    {
        System.out.println(source + " " + dests[0]);
        Vector<RNode> nodes= new Vector<>();

        int source_id = this.nomenclature.NametoID(source);
        nodes.add(new RNode(source_id, 0, 1, null ));
        nodes.get(0).is_source = 1;

        HashSet<Integer> dest_ids = new HashSet<>();
        for (String dest : dests)
            dest_ids.add(this.nomenclature.NametoID(dest));

        HashSet<Integer> mark_bfs = new HashSet<>();
        mark_bfs.add(source_id);
        for(int i = 0 ; i < nodes.size(); i ++ )
        {
            RNode node = nodes.get(i);
            Map<Integer, Double> neighbors;
            if(is_source_based == 1)
                neighbors = sampleGraph.get_neighbors_of(node.id);
            else
                neighbors = invertedSampleGraph.get_neighbors_of(node.id);

            if(dest_ids.contains(node.id))
                node.is_destination = 1;
            for(int neighbor : neighbors.keySet())
                if(!mark_bfs.contains(neighbor))
                {
                    RNode n_node =new RNode(neighbor, node.depth+1,
                                        node.is_exc*Math.signum(neighbors.get(neighbor)), node);
                    nodes.add(n_node);
                    mark_bfs.add(neighbor);
                }
        }
        for (String dest : dests) {
            int id = this.nomenclature.NametoID(dest);
            if (!mark_bfs.contains(id))
            {
                RNode node = new RNode(id,1,0,null);
                node.is_destination = 1;
                nodes.add(node);
            }
        }
        return nodes;
    }

    public Vector<Vector<RNode>> getRegulatoryPath(String[] sources, String[] dests, int is_exc,
                                                   int is_source_based) throws Exception
    {
        Vector<Vector<RNode>> rgraphs = new Vector<>();
        if(is_source_based == 1)
            for(String source: sources)
                rgraphs.add(__getRegulatoryPath(source, dests, is_exc, is_source_based));
        else
            for(String destination: dests)
                rgraphs.add(__getRegulatoryPath(destination, sources, is_exc, is_source_based));

        return rgraphs;
    }
}
