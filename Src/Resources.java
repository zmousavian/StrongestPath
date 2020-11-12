import java.io.File;
import java.io.UnsupportedEncodingException;

import javax.swing.ImageIcon;


public class Resources {
	private static String firstRootPath = new File(getRoot(),"files").toString();
	private static String rootPath = new File(firstRootPath,"static").toString();
	
	private static String humanIconPath = new File(rootPath, "human.jpg").toString();
	private static String mouseIconPath = new File(rootPath, "mouse.jpg").toString();
	private static String ratIconPath = new File(rootPath, "rat.jpg").toString();
	private static String pagerankIconPath = new File(rootPath, "pagerank-icon.jpg").toString();

	public final static ImageIcon humanIcon = new ImageIcon(humanIconPath);
	public final static ImageIcon mouseIcon = new ImageIcon(mouseIconPath);
	public final static ImageIcon ratIcon = new ImageIcon(ratIconPath);
	public final static ImageIcon pagerankIcon = new ImageIcon(pagerankIconPath);
	
	public static String getHigherFolder(String path)
	{
		int lastSepIndex = path.indexOf(File.separator, path.indexOf("Cytoscape"));
		String path1 = path.substring(0, lastSepIndex+1);
		String result;
		try {
			result = java.net.URLDecoder.decode(path1, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			result = path1.replace("%20", " ");
			e.printStackTrace();
		}
		return result;
	}
	public static String getRoot()
	{
		
		return getHigherFolder(new File(MyStrongestPathPlugin.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent());
	}
}
