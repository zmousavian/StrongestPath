import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;



public class DataBaseManager {
	private HashMap<Integer, Integer> globalIDtoGraphID = new HashMap<Integer, Integer>();
	private HashMap<Integer, Integer> graphIDtoGlobalID = new HashMap<Integer, Integer>();
	public int proteinsCount;
	public DataBaseManager(String species, String databaseName) throws FileNotFoundException {
		Scanner s = new Scanner(new File(getDataBasePath(species, databaseName)));
		s.next();
		int i = 0;
		int t;
		while(s.hasNext())
		{
			t = s.nextInt();
			globalIDtoGraphID.put(t, i);
			graphIDtoGlobalID.put(i, t);
			i++;
		}
		proteinsCount = i;
		s.close();
	}
	
	/*
	 * the result is 0-based
	 */
	public int globalToGraph(int globalId)
	{
		if(!globalIDtoGraphID.containsKey(globalId))
		{
			return -1;
		}
		return globalIDtoGraphID.get(globalId);
	}
	
	public int graphToGlobal(int graphId) {
		if (!graphIDtoGlobalID.containsKey(graphId))
			return -1;
		return graphIDtoGlobalID.get(graphId);
	}
	
	public static String getDataBasePath(String species, String databaseName)
	{
		String root = new File(Resources.getRoot(),"files").toString();
		return new File(new File(root, species).toString(), databaseName).toString();
	}
	
	

}
