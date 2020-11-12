import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.io.LineNumberReader;
import java.io.File;
import java.io.FileReader;

class SampleGraph {

	public final String HUMANSPEICEID = Nomenclature.HUMANSPEICEID;
	public final String MOUSESPEICEID = Nomenclature.MOUSESPEICEID;
	public final String RATSPEICEID = Nomenclature.RATSPEICEID;
	public static int PROTEINSCOUNTRAT = 139498;
	public static int PROTEINSCOUNTMOUSE = 87999;
	public static int PROTEINSCOUNTHUMAN = 139498;

	public static double D = 0.95;
	public static double logD = Math.log(1/D);

	public int proteinsCount;
	public String species;

	public int last_node= 0 ;
	public int cnt = 1 ;
	public int last_indx = 0;
	public int line_count ;
	Map<Integer, Integer>lefts = new HashMap<>();
	Map<Integer, Integer>rights = new HashMap<>();
	int[] neighbors;
	double[] weights;

	Map<Integer, Map<Integer, Double>> graph = new HashMap<>();
	public SampleGraph(String species, String databaseName) throws IOException
	{
        this.species = species;
        this.proteinsCount = PROTEINSCOUNTHUMAN;

		initialize(DataBaseManager.getDataBasePath(species, databaseName));
	}

	private void addEdgeToGraph(int firstNode, int secondNode, double weight)
	{
		//System.out.println(firstNode + " " + secondNode+" "+weight);
		if(firstNode != last_node)
		{
			lefts.put(firstNode, cnt);
		}
		last_node = firstNode;
		if(last_node > 0)
			rights.put(last_node,cnt);


		weights[cnt] = weight*1000;
		if (weights[cnt] == 1000) {
			if(cnt % 10000 == 0)
				System.out.println("hi");
			weights[cnt] -= 0.1;
		}
		neighbors[cnt] = secondNode;
		cnt+=1;
	}
	public Map<Integer, Double> get_neighbors_of(int index)
	{
		Map<Integer, Double> current_neighbors = new HashMap<>();
		if(!lefts.containsKey(index))
			return current_neighbors;
		int cleft = lefts.get(index), cright = rights.get(index);
		for(int i = cleft ; i <= cright ; i ++ )
			current_neighbors.put(neighbors[i],weights[i]);
		return current_neighbors;
	}

	private void initialize(String databasePath) throws IOException
	{
		String strLine;
		String[] line;
		try
		{
			File file = new File(databasePath);
			LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file));
			lineNumberReader.skip(Long.MAX_VALUE);
			line_count=lineNumberReader.getLineNumber();
			lineNumberReader.close();
		}
		catch(Throwable t)
		{
			System.out.println("line reader in sample_graph");
			return;
		}
		System.out.println(line_count);
		neighbors =new int[line_count+5];
		weights =new double[line_count+5];

		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(
					new FileInputStream(databasePath)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		int xx = 0 ;
        try {
			while ((strLine = br.readLine())!=null ) {

				line = strLine.split("\t");
				addEdgeToGraph(Integer.parseInt(line[0]), Integer.parseInt(line[1]), Double.parseDouble(line[2]));
				this.proteinsCount = Math.max(
						this.proteinsCount,
						Math.max(Integer.parseInt(line[0]), Integer.parseInt(line[1]))
				);
				xx ++;
				if(xx%1000000 == 0)
					System.out.println(xx);
			}
			br.close();
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
	}

}
