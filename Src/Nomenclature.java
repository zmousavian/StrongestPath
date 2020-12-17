import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import javax.swing.JOptionPane;

public class Nomenclature {

	public final static String HUMANSPEICEID = "human";
	public final static String MOUSESPEICEID = "mouse";
	public final static String RATSPEICEID = "rat";
	public static int PROTEINSCOUNTRAT = 16902;
	public static int PROTEINSCOUNTMOUSE = 17919;
	public static int PROTEINSCOUNTHUMAN = 18600;

	public String species;

	String[] attributes;

	boolean userNomen = false;
	Integer maxAttributes;
	String fileName;
	HashMap<String, Integer>[] nameToIdMap;
	HashMap<Integer, String>[] annotationtoIDMap;

	public Nomenclature(String species, String fileAddress, boolean userNomen) throws Exception {

		String root =  DataBaseManager.getDataBasePath(species, species + "-Annotations" + ".txt");
		String line;
		BufferedReader brr = null;
		try {
			brr = new BufferedReader(new InputStreamReader(new FileInputStream(root)));
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if(userNomen)
				JOptionPane.showMessageDialog(null,e.getMessage());
			else
				JOptionPane.showMessageDialog(null, "Please download the databases.");
		}
		try {
				line = brr.readLine();
				attributes = line.split("\t");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				JOptionPane.showMessageDialog(null,e.getMessage());
			}


		maxAttributes=attributes.length;
		annotationtoIDMap = new HashMap[maxAttributes + 1];
		this.userNomen = userNomen;
		for (int i = 0; i < annotationtoIDMap.length; i++) {
			annotationtoIDMap[i] = new HashMap<Integer, String>();
		}
		nameToIdMap = new HashMap[maxAttributes + 1];
		for (int i = 0; i < nameToIdMap.length; i++) {
			nameToIdMap[i] = new HashMap<>();
		}
		this.species = species;

		if (fileAddress != null && !fileAddress.equals(""))
			fileName = fileAddress;
		else
			fileName = DataBaseManager.getDataBasePath(species, species + "-Annotations" + ".txt");
        System.out.println(fileName);
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));


		String[] listGeneID;
		String[] GeneID;

		int lineNumber = 0;
		int counter = 0;
		int currentID = 0;
		try {

			while ((line = br.readLine()) != null) {
				listGeneID = line.split("\t");

				for (int i = 1; i < annotationtoIDMap.length; i++) {
					if (listGeneID.length >= i) {
						annotationtoIDMap[i].put(lineNumber,
								listGeneID[i-1].toLowerCase());
					}
				}

				int cnt = 1;
				for (String list : listGeneID) {

					GeneID = list.split(",");
					for (String ID : GeneID) {
						nameToIdMap[cnt].put(ID.toLowerCase(), lineNumber);
					}
					cnt+=1;
				}
				lineNumber++;
			}
		} catch (Exception e) {
			throw new Exception("Annotation file is corrupted!");
		}

		br.close();
	}

	Integer AttributetoID(String attr) {
		if(userNomen)
			return 1;
		for (int i = 1; i < attributes.length; i++)
			if (attributes[i].toLowerCase().equals(attr.toLowerCase()))
				return i;
		return 1;
	}

	Integer NametoID(String name) throws Exception {

		if(name.equals(""))
			throw new Exception("There is no gene with the name '"+ name +"' in annotations file.");

		for (int i = 1; i < attributes.length; i++)
			if (nameToIdMap[i].containsKey(name.toLowerCase()))
				return nameToIdMap[i].get(name.toLowerCase());
		throw new Exception("There is no gene with the name '"+ name +"' in annotations file.");


	}

	String IDtoName(Integer ID) {
		for (int i = 1; i < attributes.length; i++)
			if (annotationtoIDMap[i].get(ID) != null)
				return annotationtoIDMap[i].get(ID).toString();
		return null;

	}
	String IDtoEntrezID(Integer ID)
	{
		int x = 1;
		if (annotationtoIDMap[x].get(ID) != null) {
			return annotationtoIDMap[x].get(ID).toString();
		}
		return "NA";
	}

	String Convert(String name, String destType) throws Exception {
		//Zaynab
		Integer destIndex = userNomen ? 1 : 2;
		//AttributetoID(destType); // We can use this function to show the results with any attribute we want,
								   // but for now we just use first column for user nomen and 8th column for our nomen.
		Integer line = NametoID(name);
		String st1;
		if (destIndex != -1) {
			st1 = annotationtoIDMap[destIndex].get(line);
			if (st1 != null)
//				return annotationtoIDMap[destIndex].get(line).toString();
				return st1.toString().toUpperCase();
		}

		return "";
	}

}
