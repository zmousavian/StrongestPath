import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;

import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.app.swing.AbstractCySwingApp;
import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.*;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.property.*;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.osgi.framework.BundleContext;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

public class MyStrongestPathPlugin extends AbstractCySwingApp {

	String styleName = "myVisualStyle";
	public VisualStyle visualStyle;
	public ActionListener annotActionListenerE;
	public ActionListener userDBActionListener;
	private JFrame frame;

	private Integer[] proteins;
	private Integer[] newproteins;
	private Integer[][] neighbors=new Integer[2][];

	protected String DATAspecies;
	private String[] databases;
	private HashMap<String, String> databasesType = new HashMap<String, String>();
	private HashMap<String, Integer> speciesIndex = new HashMap<String, Integer>();
	private String[] databaseLabels;
    private DataDownloader data_downloader;
	public JPanel pluginMainPanel;
	public JPanel dataBaseDataPanel1;
	public JButton downloadUpdateDBs;
	public JButton loadDBs;
	public JComboBox<String> selectSpecies;
	public JPanel speciesDataPanel;
	public JTextField networkTextBox;
	public JTextField annotationTextBox;
	public JTextField sourceGenesTextField;
	public JTextField targetGenesTextField;
    public JTextField sourceGenesTextFieldRP;
    public JTextField targetGenesTextFieldRP;
	public JTextField geneListTextField;
	public JTextField numberExpandTextField;
	public JTextField genesListFromFile;

	private int numberExpand;

	class BFSInfos {
		public HashMap<Integer, Integer> heights;
		public HashMap<Integer, Integer> visitTimes;

	}

	public MyStrongestPathPlugin(CySwingAppAdapter adapter, CyApplicationManager cyApplicationManager,
								 CyNetworkViewManager cyNetworkViewManager, CyNetworkViewFactory cyNetworkViewFactory,
								 CyNetworkFactory cyNetworkFactory, CyNetworkManager cyNetworkManager,
								 CyEventHelper cyEventHelper, VisualStyleFactory visualStyleFactory) {

		super(adapter);
		adapter.getCySwingApplication().addAction(
				new MyPluginMenuAction(adapter, cyApplicationManager, cyNetworkViewManager, cyNetworkViewFactory,
						cyNetworkFactory, cyNetworkManager, cyEventHelper, visualStyleFactory));
	}

	public class MyPluginMenuAction extends AbstractCyAction {
		private JPanel topPanel;
		private CyAppAdapter adapter;
		final CyApplicationManager manager;
		final CyNetworkViewManager network_view_manager;
		final CyNetworkViewFactory network_view_factory;
		final CyNetworkManager cy_network_manager;
		final CyNetworkFactory network_factory;
		final CyEventHelper cy_event_helper;
		private JTabbedPane tabbedPane;
		private JPanel dataBaseDataPanel;
		private JPanel growthDataPanel;
		private JPanel strongestPathDataPanel;
        private JPanel regulatoryPathDataPanel;

		private JRadioButton genesFromFileCheckBox;
		private JTextField srcfileAddressE;
		private JTextField srcTextFieldE;
		private ActionListener srcActionListenerE;
		private boolean srcFromFileE = false;
		private JTextField srcfileAddress;
		private JTextField srcTextField;
		private JTextField dstfileAddress;
		private JTextField dstTextField;
        private JTextField srcfileAddressRP;
        private JTextField srcTextFieldRP;
        private JTextField dstfileAddressRP;
        private JTextField dstTextFieldRP;
		private boolean srcFromFile = false;
		private boolean dstFromFile = false;
        private boolean srcFromFileRP = false;
        private boolean dstFromFileRP = false;
		private ActionListener srcActionListener;
		private ActionListener dstActionListener;
        private ActionListener srcActionListenerRP;
        private ActionListener dstActionListenerRP;
		private JLabel download_state;
		private JLabel user_load_state;
		private JComboBox networkTypeComboBox;
		private JComboBox networkTypeExpandComboBox;
		private JScrollPane expandScrollPane;
        private JScrollPane scrollPane;
        private JScrollPane scrollPaneRP;
		private JList networkList;
        private JList networkListRP;
		private JList expandNetworkList;
		private GridBagConstraints gbc = new GridBagConstraints();
        private boolean userDatabase = false;
        private JComboBox speciesComboBox;
        private Nomenclature nomen;
        private boolean annotFromFile;
		private String annotFile;
		private boolean networkFromFile = false;
		private String networkFile;
		private ArrayList<String> DATAdatabaseNames;
		private int step;
		private ArrayList<String> current_networks;
		private int is_exc = 1;
		private int is_source_based = 1;
		private int complete_graph = 0;
		private int is_edge_connectivity_full = 0;
		protected double DATAthreshold;
		protected String DATAsrcfilePath;
		protected String DATAfilePath;
		protected String DATAsrctextField;
		protected String DATAdstfilePath;
		protected String DATAdsttextField;
        protected String DATAsrcfilePathRP;
        protected String DATAfilePathRP;
        protected String DATAsrctextFieldRP;
        protected String DATAdstfilePathRP;
        protected String DATAdsttextFieldRP;

        protected HashMap<String, StrongestPath> subNetworks = new HashMap<String, StrongestPath>();
		protected HashMap<String, RegulatoryPath> subNetworksRP = new HashMap<>();
		protected int selectDBWidth = 600;
		protected int selectDBHeight = 520;
		protected int pluginWidth = 600;
		protected int pluginHeight = 520;
		protected int tabbedPaneWidth = 600 - 60;
		protected int tabbedPaneHeight = 520 - 70;


		public MyPluginMenuAction(CySwingAppAdapter adapter, CyApplicationManager cyApplicationManager,
								  CyNetworkViewManager cyNetworkViewManager, CyNetworkViewFactory cyNetworkViewFactory,
								  CyNetworkFactory cyNetworkFactory, CyNetworkManager cyNetworkManager, CyEventHelper cyEventHelper, VisualStyleFactory visualStyleFactory) {
			super("StrongestPath", cyApplicationManager,
					"network", cyNetworkViewManager);
			System.out.println("HI");
			this.adapter = adapter;
			setPreferredMenu("Apps");
			manager = cyApplicationManager;
			network_view_manager = cyNetworkViewManager;
			network_view_factory = cyNetworkViewFactory;
			network_factory = cyNetworkFactory;
			cy_network_manager = cyNetworkManager;
			cy_event_helper = cyEventHelper;
			visualStyle = visualStyleFactory.createVisualStyle(styleName);
		}

		private void doFinalize() {
			System.gc();
		}

		protected void finalize() throws Throwable {
			doFinalize();
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					// Turn off metal's use of bold fonts
					UIManager.put("swing.boldMetal", Boolean.FALSE);
					Font f = new Font(Font.SERIF,Font.BOLD, 12);
					UIManager.put("ComboBox.font", f);
					UIManager.put("Button.font", f);
					UIManager.put("TextField.font", f);
					UIManager.put("JPanel.font", f);
					UIManager.put("TextPane.font", f);
					UIManager.put("TableHeader.font", f);
					UIManager.put("ScrollPane.font", f);
					UIManager.put("Label.font", f);
					UIManager.put("TitledBorder.font", f);
					UIManager.put("JTabbedPane.font", f);
					UIManager.put("JLabel.font", f);
					UIManager.put("JScrollPane.font", f);
					UIManager.put("JTabbedPane.font", f);
					UIManager.put("CheckBox.font", f);
					UIManager.put("RadioButton.font", f);
					UIManager.put("ToggleButton.font", f);
					UIManager.put("Page.font", f);

					initPlugin();
					showFrame();


				}
			});
		}

		public void initPlugin() {

			topPanel = new JPanel(false);
			initializePluginMainPanel();
			topPanel.setPreferredSize(new Dimension(selectDBWidth,
					selectDBHeight));
			pluginMainPanel.setPreferredSize(new Dimension(selectDBWidth,
					selectDBHeight));
			pluginMainPanel.setVisible(true);
			topPanel.add(pluginMainPanel);
        }
		private void make_species_file(String root)
		{
			(new File(root +"/files")).mkdir();
			File species_file = new File(root+"/files", "species.txt");

			try{
				 DataDownloader species_downloader =  new DataDownloader(root);
				 species_downloader.download_species(root);
				 if (species_downloader.state.equals("failed"))
				 	throw new Throwable();
			}
			catch (Throwable T)
			{
				if(species_file.exists()) {
					return;
				}
				try {
					FileWriter fw = new FileWriter(species_file);
					fw.write("Human"+"\n"+"Mouse");
					fw.close();
				}
				catch(Exception e){System.out.println(e);}
			}
		}
		private void initializePluginMainPanel() {
			pluginMainPanel = new JPanel(false);
			tabbedPane = new JTabbedPane();
			tabbedPane.setFont( new Font(Font.SERIF,Font.BOLD, 12) );
			tabbedPane.setBounds(1, 1, tabbedPaneWidth, tabbedPaneHeight);

			JPanel dataBaseTabPanel = new JPanel();
			JPanel strongestPathTabPanel = new JPanel();
			JPanel growthTabPanel = new JPanel();
            JPanel regulatoryPathTabPanel = new JPanel();

			tabbedPane.setVisible(true);

			createDataBaseDataPanel();
			createStrongestPathDataPanel();
			createGrowthDataPanel();
            createRegulatoryPathPanel();

			JScrollPane dataBaseDataScrollPane = new JScrollPane(dataBaseDataPanel);
			dataBaseDataScrollPane.setVisible(true);
			dataBaseDataScrollPane.setPreferredSize(new Dimension(
					tabbedPaneWidth, tabbedPaneHeight));
			JScrollPane growthDataScrollPane = new JScrollPane(growthDataPanel);
			growthDataScrollPane.setVisible(true);
			growthDataScrollPane.setPreferredSize(new Dimension(
					tabbedPaneWidth, tabbedPaneHeight));
			JScrollPane strongestPathDataScrollPane = new JScrollPane(strongestPathDataPanel);
			strongestPathDataScrollPane.setVisible(true);
			strongestPathDataScrollPane.setPreferredSize(new Dimension(
					tabbedPaneWidth, tabbedPaneHeight));
            JScrollPane regulatoryPathDataScrollPane = new JScrollPane(regulatoryPathDataPanel);
            regulatoryPathDataScrollPane.setVisible(true);
            regulatoryPathDataScrollPane.setPreferredSize(new Dimension(
                    tabbedPaneWidth, tabbedPaneHeight));
			dataBaseTabPanel.add(dataBaseDataScrollPane);
			growthTabPanel.add(growthDataScrollPane);
			strongestPathTabPanel.add(strongestPathDataScrollPane);
            regulatoryPathTabPanel.add(regulatoryPathDataScrollPane);

			tabbedPane.addTab("Select Databases", dataBaseTabPanel);
			tabbedPane.addTab("Strongest Path", strongestPathTabPanel);
			tabbedPane.addTab("Expand", growthTabPanel);
            tabbedPane.addTab("Regulatory Path", regulatoryPathTabPanel);

			dataBaseDataPanel.setVisible(true);
			growthDataPanel.setVisible(true);
			strongestPathDataPanel.setVisible(true);
            regulatoryPathDataPanel.setVisible(true);

			dataBaseDataPanel.setBounds(1, 1, tabbedPaneWidth, tabbedPaneHeight);
			growthDataPanel.setBounds(1, 1, tabbedPaneWidth, tabbedPaneHeight);
			strongestPathDataPanel.setBounds(1, 1, tabbedPaneWidth,	tabbedPaneHeight);
            regulatoryPathDataPanel.setBounds(1, 1, tabbedPaneWidth,	tabbedPaneHeight);

			pluginMainPanel.add(tabbedPane);
			tabbedPane.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					download_state.setText(" ");
					user_load_state.setText(" ");
				}
			});
		}

		private void createDataBaseDataPanel() {
			dataBaseDataPanel=new JPanel(false);
			dataBaseDataPanel.setLayout(new GridLayout(2, 1, 0, 0));

			JPanel panel_1 = new JPanel();
			panel_1.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 1, true), "Download and load the databases:", TitledBorder.LEFT, TitledBorder.TOP, null, Color.BLACK));

			dataBaseDataPanel.add(panel_1);
			panel_1.setLayout(null);
			try {
				BufferedImage myPicture = ImageIO.read(getClass().getResource("Logo.png"));
				Image logo = myPicture.getScaledInstance(180, 160, BufferedImage.SCALE_SMOOTH);
				JLabel picLabel = new JLabel(new ImageIcon(logo));
				picLabel.setBounds(50,37,180,160);
				panel_1.add(picLabel);
			}
			catch (IOException ex)
			{
				System.out.println("Logo problem");
			}

			JLabel lblSelectTheSpecies = new JLabel("Select the species:");
			lblSelectTheSpecies.setBounds(263, 47, 106, 14);
			panel_1.add(lblSelectTheSpecies);

			JButton btnDownloadupdateDatabases = new JButton("Download/Update Databases");
			btnDownloadupdateDatabases.setBounds(280, 91, 190, 23);
			panel_1.add(btnDownloadupdateDatabases);

			JButton btnLoadDatabases = new JButton("Load Databases");
			btnLoadDatabases.setBounds(280, 138, 190, 23);
			panel_1.add(btnLoadDatabases);

			download_state= new JLabel(" ", SwingConstants.CENTER);
			download_state.setBounds(280, 185, 190, 23 );
			panel_1.add(download_state);

			speciesComboBox = new JComboBox();
			make_species_file(Resources.getRoot());

			System.out.println(Resources.getRoot());

			String root = new File(Resources.getRoot(),"files").toString();
			System.out.println(root);
			root=new File(root, "species.txt").toString();
			String line;
			int s=0;
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(root)));
				try {
					while ((line = br.readLine()) != null) {
						speciesComboBox.addItem(line);
						speciesIndex.put(line, s);
						s++;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					JOptionPane.showMessageDialog(null,e.getMessage());
				}

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				JOptionPane.showMessageDialog(null,e.getMessage());
			}

			speciesComboBox.setBounds(376, 46, 99, 20);
			panel_1.add(speciesComboBox);
			speciesComboBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setInternalNetworkPanelEnabled(false);
					download_state.setText("");
					user_load_state.setText("");
				}
			});


			JPanel panel_2 = new JPanel();
			panel_2.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 1, true), "Load local databases:", TitledBorder.LEFT, TitledBorder.TOP, null, Color.BLACK));
			dataBaseDataPanel.add(panel_2);
			panel_2.setLayout(null);

			JPanel panel = new JPanel();
			panel.setBounds(199, 27, 1, 1);
			panel_2.add(panel);
			panel.setLayout(null);

			JLabel lblNewLabel = new JLabel("Annotation file:");
			lblNewLabel.setBounds(76, 66, 89, 14);
			panel_2.add(lblNewLabel);

			networkTextBox = new JTextField();
			networkTextBox.setBounds(175, 100, 199, 20);
			panel_2.add(networkTextBox);
			networkTextBox.setColumns(10);

			JButton btnAnnotBrowse = new JButton("Browse");
			btnAnnotBrowse.setBounds(384, 64, 72, 23);
			panel_2.add(btnAnnotBrowse);

			JLabel lblNetworkFile = new JLabel("Network file:");
			lblNetworkFile.setBounds(76, 101, 89, 14);
			panel_2.add(lblNetworkFile);

			annotationTextBox = new JTextField();
			annotationTextBox.setColumns(10);
			annotationTextBox.setBounds(175, 65, 199, 20);
			panel_2.add(annotationTextBox);

			JButton btnDBBrowse = new JButton("Browse");
			btnDBBrowse.setBounds(384, 99, 72, 23);
			panel_2.add(btnDBBrowse);

			JButton btnLoadLocalDatabase = new JButton("Load local database");
			btnLoadLocalDatabase.setBounds(179, 145, 190, 23);
			panel_2.add(btnLoadLocalDatabase);

			user_load_state= new JLabel(" ", SwingConstants.CENTER);
			user_load_state.setBounds(179, 185, 190, 23 );
			panel_2.add(user_load_state);

			btnDownloadupdateDatabases.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					try
                    {
                    	download_state.setText("Downloading...");
						data_downloader =  new DataDownloader(getRoot());
					    String current_specie = speciesComboBox.getSelectedItem().toString();
					    System.out.println(current_specie);

					    frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                        data_downloader.download_data_for_specie(current_specie,getRoot());

						if(data_downloader.state == "OK") {
							download_state.setText("Data downloaded.");
							System.out.println("Data downloaded successfully");
						}
						else
							throw new Exception();

						frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					}
					catch (Exception e)
                    {
						frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
						download_state.setText("Download failed.");
						System.out.println("Download Failed.");
                    }
				}
			});

			btnLoadDatabases.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					userDatabase = false;
					try
					{
						download_state.setText("Loading...");
						frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
						loadAllDatabasesAndNomen();
						setInternalNetworkPanelEnabled(true);
						frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
						download_state.setText("Data loaded.");
					}
					catch (Exception e)
					{
						frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
						download_state.setText("Loading failed.");
						System.out.println("Load Failed.");
					}
				}

			});

			//zaynab

			final JFileChooser fc = new JFileChooser();

			// User Annotation
			annotationTextBox.addFocusListener(new FocusListener() {
				@Override
				public void focusLost(FocusEvent e) {
					annotFromFile = true;
					annotFile = annotationTextBox.getText();
				}
				@Override
				public void focusGained(FocusEvent e) {
					// TODO Auto-generated method stub
				}
			});
			btnAnnotBrowse.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					int returnVal = fc.showOpenDialog(null);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						annotationTextBox.setText(file.getAbsolutePath());
						annotFromFile = true;
						annotFile = file.getAbsolutePath();
					}
				}
			});
			annotActionListenerE = btnAnnotBrowse.getActionListeners()[0];

			// User Network
			networkTextBox.addFocusListener(new FocusListener() {
				@Override
				public void focusLost(FocusEvent e) {
					networkFromFile = true;
					networkFile = networkTextBox.getText();
				}
				@Override
				public void focusGained(FocusEvent e) {
					// TODO Auto-generated method stub
				}
			});
			btnDBBrowse.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					int returnVal = fc.showOpenDialog(null);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						networkTextBox.setText(file.getAbsolutePath());
						networkFromFile = true;
						networkFile = file.getAbsolutePath();
					}
				}
			});
			userDBActionListener = btnDBBrowse.getActionListeners()[0];


			btnLoadLocalDatabase.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {

					if (annotationTextBox.getText().equals("")) {
						JOptionPane.showMessageDialog(null,
								" Please select your annotation file ");
						annotActionListenerE.actionPerformed(arg0);
					} else if (networkTextBox.getText().equals("")) {
						JOptionPane.showMessageDialog(null,
								" Please select your database ");
						userDBActionListener.actionPerformed(arg0);
					} else {
						userDatabase = true;

						try
						{
							user_load_state.setText("Loading...");

							frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
							loadUserDatabasesAndNomen();
							setInternalNetworkPanelEnabled(false);
							user_load_state.setText("Data loaded.");
							System.out.println("Data loaded successfully");
							frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
						}
						catch (Exception e)
						{
							System.out.print(step);
							frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
							user_load_state.setText("Loading failed.");
							System.out.println("Loading Failed.");
						}
					}
				}
			});


		}

		private void createStrongestPathDataPanel() {
			strongestPathDataPanel=new JPanel(false);
			strongestPathDataPanel.setLayout(null);

			JLabel lblSourceGenes = new JLabel("Source genes:");
			lblSourceGenes.setBounds(71, 28, 82, 20);
			strongestPathDataPanel.add(lblSourceGenes);

			sourceGenesTextField = new JTextField();
			sourceGenesTextField.setBounds(163, 30, 179, 20);
			strongestPathDataPanel.add(sourceGenesTextField);
			sourceGenesTextField.setColumns(10);

			final JButton btnBrowseSrc = new JButton("Browse");
			btnBrowseSrc.setBounds(352, 60, 72, 23);
			strongestPathDataPanel.add(btnBrowseSrc);

			JLabel lblTargetGenes = new JLabel("Target genes:");
			lblTargetGenes.setBounds(71, 90, 82, 20);
			strongestPathDataPanel.add(lblTargetGenes);

			final JTextField sourceGenesBrowse = new JTextField();
			sourceGenesBrowse.setColumns(10);
			sourceGenesBrowse.setBounds(163, 61, 179, 20);
			strongestPathDataPanel.add(sourceGenesBrowse);

			final JButton btnBrowseDest = new JButton("Browse");
			btnBrowseDest.setBounds(352, 120, 72, 23);
			strongestPathDataPanel.add(btnBrowseDest);

			JLabel lblSelectNetworkType = new JLabel("Select network type:");
			lblSelectNetworkType.setBounds(88, 168, 124, 20);
			strongestPathDataPanel.add(lblSelectNetworkType);

			String[] networkTypes = new String[] {"Protein-Protein Interaction Network", "Signaling Network"};
			String[] networkType_abbrivations = new String[] {"PPI", "Signaling"};

			networkTypeComboBox = new JComboBox(networkTypes);
			networkTypeComboBox.setBounds(223, 170, 196, 20);

            networkTypeComboBox.setSelectedIndex(-1);
			strongestPathDataPanel.add(networkTypeComboBox);

			JSeparator separator = new JSeparator();
			separator.setBounds(15, 154, 507, 8);
			strongestPathDataPanel.add(separator);

			current_networks = new ArrayList<String>();
			networkList=new JList(current_networks.toArray(new String[current_networks.size()]));
			scrollPane = new JScrollPane(networkList);
			scrollPane.setBounds(66, 198, 400, 85);


			networkTypeComboBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String type = networkType_abbrivations[networkTypeComboBox.getSelectedIndex()];
					current_networks = new ArrayList<String>();
					for (String net : subNetworks.keySet())
						if(databasesType.get(net).equals(type))
							current_networks.add(net);

					networkList.setListData(current_networks.toArray(new String[current_networks.size()]));
					scrollPane.updateUI();
				}
			});
			strongestPathDataPanel.add(scrollPane);


			JSeparator separator_1 = new JSeparator();
			separator_1.setBounds(15, 294, 507, 8);
			strongestPathDataPanel.add(separator_1);

			JLabel edge_connectivity = new JLabel("Connectivity:");
			edge_connectivity.setBounds(150, 391, 100, 20);
			strongestPathDataPanel.add(edge_connectivity);

			JRadioButton sparse_edge_connectivity_rdbtn = new JRadioButton("Sparse");
			sparse_edge_connectivity_rdbtn.setSelected(true);
			JRadioButton full_edge_connectivity_rdbtn = new JRadioButton("Full");
			ButtonGroup path_type_radioGroup = new ButtonGroup();
			path_type_radioGroup.add(sparse_edge_connectivity_rdbtn);
			path_type_radioGroup.add(full_edge_connectivity_rdbtn);
			sparse_edge_connectivity_rdbtn.setBounds(250, 391, 80, 20);
			full_edge_connectivity_rdbtn.setBounds(330, 391, 100, 20);

			sparse_edge_connectivity_rdbtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					is_edge_connectivity_full = 0;
				}
			});
			full_edge_connectivity_rdbtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					is_edge_connectivity_full = 1;
				}
			});
			strongestPathDataPanel.add(sparse_edge_connectivity_rdbtn);
			strongestPathDataPanel.add(full_edge_connectivity_rdbtn);

			JButton btnStrongestPath = new JButton("Show Strongest Path");
			btnStrongestPath.setBounds(194, 416, 148, 23);
			strongestPathDataPanel.add(btnStrongestPath);

			final JSlider thresholdSlider = new JSlider(0, 1000, 0);
			thresholdSlider.setMajorTickSpacing(50);
			thresholdSlider.setPaintTicks(true);
			thresholdSlider.setPaintTrack(true);
			thresholdSlider
					.setModel(new DefaultBoundedRangeModel(0, 0, 0, 1000));
			Hashtable<Integer, JLabel> ht = new Hashtable<Integer, JLabel>();
			for (int i = 0; i < 1000; i++) {
				if (i % 100 == 0) {
					if (Math.pow(i / 1000.0, 3) > 0.3)
						ht.put(i,
								new JLabel(String.format("%.1f",
										Math.pow(i / 1000.0, 3))));
					else if (Math.pow(i / 1000.0, 3) > 0.03)
						ht.put(i,
								new JLabel(String.format("%.2f",
										Math.pow(i / 1000.0, 3))));
					else
						ht.put(i,
								new JLabel(String.format("%.3f",
										Math.pow(i / 1000.0, 3))));

				}
			}
			ht.put(999, new JLabel(String.format("%.1f", 1000 / 1000.0)));
			thresholdSlider.setLabelTable(ht);
			thresholdSlider.setPaintLabels(true);
			thresholdSlider.setBounds(15, 338, 497, 37);
			strongestPathDataPanel.add(thresholdSlider);

			JLabel lblSelectThreshold = new JLabel("Select threshold:");
			lblSelectThreshold.setBounds(25, 313, 120, 20);
			strongestPathDataPanel.add(lblSelectThreshold);

			JSeparator separator_2 = new JSeparator();
			separator_2.setBounds(15, 386, 507, 8);
			strongestPathDataPanel.add(separator_2);

			targetGenesTextField = new JTextField();
			targetGenesTextField.setColumns(10);
			targetGenesTextField.setBounds(163, 92, 179, 20);
			strongestPathDataPanel.add(targetGenesTextField);

			final JTextField targetGenesBrowse = new JTextField();
			targetGenesBrowse.setColumns(10);
			targetGenesBrowse.setBounds(163, 123, 179, 20);
			strongestPathDataPanel.add(targetGenesBrowse);

			final JRadioButton rdbtnFromFileSrc = new JRadioButton("From file:");
			rdbtnFromFileSrc.setBounds(64, 59, 87, 23);
			strongestPathDataPanel.add(rdbtnFromFileSrc);

			final JRadioButton rdbtnFromFileDest = new JRadioButton("From file:");
			rdbtnFromFileDest.setBounds(64, 120, 87, 23);
			strongestPathDataPanel.add(rdbtnFromFileDest);



			//Input source genes
			rdbtnFromFileSrc.setToolTipText(" There should be one name per line in the input file ");
			sourceGenesTextField.setToolTipText(" Genes/Proteins should be separated by comma ");
			rdbtnFromFileSrc.setSelected(false);
			sourceGenesBrowse.setEnabled(false);
			btnBrowseSrc.setEnabled(false);
			//zaynab commented this codes: because the app couldn't be installed
			/*BalloonTipStyle btStyle = new RoundedBalloonStyle(5, 5, new Color(255, 255, 204), new Color(204, 204, 0));
			btStyle.setHorizontalOffset(20);
			btStyle.setVerticalOffset(10);
			BalloonTip bt;
			bt = new BalloonTip(btnBrowseSrc,
					"<html>There should be one name per line in the input file. <br><br><i>e.g.<i> <br>pou5f1 <br>nanog <br>sall4</html>",
					btStyle, false);
			ToolTipUtils.balloonToToolTip(bt, 1, 7000);
			BalloonTip bt2;
			bt2 = new BalloonTip(sourceGenesTextField,
					"<html>Genes/Proteins sholud be separated by comma. <br><i>e.g.<i> pou5f1, nanog, sall4</html>",
					btStyle, false);
			ToolTipUtils.balloonToToolTip(bt2, 1, 7000);
			*/
			srcTextField = sourceGenesTextField;
			srcfileAddress = sourceGenesBrowse;
			final JFileChooser fc = new JFileChooser();
			btnBrowseSrc.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					int returnVal = fc.showOpenDialog(null);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						sourceGenesBrowse.setText(file.getAbsolutePath());
					}
				}
			});
			srcActionListener = btnBrowseSrc.getActionListeners()[0];
			ChangeListener myChangeListener = new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent arg0) {
					if (rdbtnFromFileSrc.isSelected()) {
						sourceGenesTextField.setEnabled(false);
						srcFromFile = true;
						sourceGenesBrowse.setEnabled(true);
						btnBrowseSrc.setEnabled(true);
					} else {
						sourceGenesTextField.setEnabled(true);
						srcFromFile = false;
						sourceGenesBrowse.setEnabled(false);
						btnBrowseSrc.setEnabled(false);
					}
				}
			};
			rdbtnFromFileSrc.addChangeListener(myChangeListener);


			//Input target genes
			rdbtnFromFileDest.setToolTipText(" There should be one name per line in the input file ");
			targetGenesTextField.setToolTipText(" Genes/Proteins should be separated by comma ");
			rdbtnFromFileDest.setSelected(false);
			targetGenesBrowse.setEnabled(false);
			btnBrowseDest.setEnabled(false);
			//zaynab commented this codes: because the app couldn't be installed
			/*BalloonTipStyle btStyle = new RoundedBalloonStyle(5, 5, new Color(255, 255, 204), new Color(204, 204, 0));
			btStyle.setHorizontalOffset(20);
			btStyle.setVerticalOffset(10);
			BalloonTip bt;
			bt = new BalloonTip(btnBrowseDest,
					"<html>There should be one name per line in the input file. <br><br><i>e.g.<i> <br>pou5f1 <br>nanog <br>sall4</html>",
					btStyle, false);
			ToolTipUtils.balloonToToolTip(bt, 1, 7000);
			BalloonTip bt2;
			bt2 = new BalloonTip(targetGenesTextField,
					"<html>Genes/Proteins sholud be separated by comma. <br><i>e.g.<i> pou5f1, nanog, sall4</html>",
					btStyle, false);
			ToolTipUtils.balloonToToolTip(bt2, 1, 7000);
			*/
			dstTextField = targetGenesTextField;
			dstfileAddress = targetGenesBrowse;

			btnBrowseDest.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					int returnVal = fc.showOpenDialog(null);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						targetGenesBrowse.setText(file.getAbsolutePath());
					}
				}
			});
			dstActionListener = btnBrowseSrc.getActionListeners()[0];
			ChangeListener myChangeListener2 = new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent arg0) {
					if (rdbtnFromFileDest.isSelected()) {
						targetGenesTextField.setEnabled(false);
						dstFromFile = true;
						targetGenesBrowse.setEnabled(true);
						btnBrowseDest.setEnabled(true);
					} else {
						targetGenesTextField.setEnabled(true);
						dstFromFile = false;
						targetGenesBrowse.setEnabled(false);
						btnBrowseDest.setEnabled(false);
					}
				}
			};
			rdbtnFromFileDest.addChangeListener(myChangeListener2);

			//Action for button show results
			btnStrongestPath.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {

					DATAdatabaseNames = new ArrayList<String>();
					System.out.println(";D");
					for (Object net: networkList.getSelectedValuesList()) {
						System.out.println(net.toString());
						DATAdatabaseNames.add(net.toString());
					}
					if(userDatabase && !DATAdatabaseNames.contains("User"))
						DATAdatabaseNames.add("User");
					if (DATAdatabaseNames.size() == 0 && !networkFromFile) {
						JOptionPane.showMessageDialog(null,
								" Please select at least one database ");
					} else if (srcFromFile
							&& srcfileAddress.getText().equals("")) {
						JOptionPane.showMessageDialog(null,
								" Please select your source genes ");
						srcActionListener.actionPerformed(arg0);
					} else if (!srcFromFile
							&& srcTextField.getText().equals("")) {
						JOptionPane.showMessageDialog(null,
								" Please select your source genes ");
						srcTextField.requestFocus();
					} else if (dstFromFile
							&& dstfileAddress.getText().equals("")) {
						JOptionPane.showMessageDialog(null,
								" Please select your target genes ");
						dstActionListener.actionPerformed(arg0);
					} else if (!dstFromFile
							&& dstTextField.getText().equals("")) {
						JOptionPane.showMessageDialog(null,
								" Please select your target genes ");
						dstTextField.requestFocus();
					} else {


						DATAsrcfilePath = srcfileAddress.getText();
						DATAsrctextField = srcTextField.getText();
						DATAdstfilePath = dstfileAddress.getText();
						DATAdsttextField = dstTextField.getText();
						DATAspecies=speciesComboBox.getSelectedItem().toString();

						DATAthreshold = Math.pow(
								thresholdSlider.getValue() / 1000.0, 3);



						// ***** Do the Job ******
						try {
							frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
							doKStrongestPath();
							frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
						}
						catch (Exception e) {
							frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
						}

					}
				}
			});

		}
        private void createRegulatoryPathPanel() {
            regulatoryPathDataPanel=new JPanel(false);
            regulatoryPathDataPanel.setLayout(null);

            JLabel lblSourceGenes = new JLabel("Source genes:");
            lblSourceGenes.setBounds(71, 28, 82, 20);
            regulatoryPathDataPanel.add(lblSourceGenes);

            sourceGenesTextFieldRP = new JTextField();
            sourceGenesTextFieldRP.setBounds(163, 30, 179, 20);
            regulatoryPathDataPanel.add(sourceGenesTextFieldRP);
            sourceGenesTextFieldRP.setColumns(10);

            final JButton btnBrowseSrcRP = new JButton("Browse");
            btnBrowseSrcRP.setBounds(352, 60, 72, 23);
            regulatoryPathDataPanel.add(btnBrowseSrcRP);

            JLabel lblTargetGenes = new JLabel("Target genes:");
            lblTargetGenes.setBounds(71, 90, 82, 20);
            regulatoryPathDataPanel.add(lblTargetGenes);

            final JTextField sourceGenesBrowseRP = new JTextField();
            sourceGenesBrowseRP.setColumns(10);
            sourceGenesBrowseRP.setBounds(163, 61, 179, 20);
            regulatoryPathDataPanel.add(sourceGenesBrowseRP);

            final JButton btnBrowseDestRP = new JButton("Browse");
            btnBrowseDestRP.setBounds(352, 120, 72, 23);
            regulatoryPathDataPanel.add(btnBrowseDestRP);

            JSeparator separator = new JSeparator();
            separator.setBounds(15, 154, 507, 8);
            strongestPathDataPanel.add(separator);

            current_networks = new ArrayList<String>();
            for (String net : subNetworksRP.keySet())
                if(databasesType.get(net).equals("RP"))
                    current_networks.add(net);

            networkListRP=new JList(current_networks.toArray(new String[current_networks.size()]));
            scrollPaneRP = new JScrollPane(networkListRP);
            scrollPaneRP.setBounds(66, 198, 400, 85);

            regulatoryPathDataPanel.add(scrollPaneRP);

            JSeparator separator_1 = new JSeparator();
            separator_1.setBounds(15, 294, 507, 8);
            regulatoryPathDataPanel.add(separator_1);

            JButton btnRegulatoryPathRP = new JButton("Show Regulatory Path");
            btnRegulatoryPathRP.setBounds(184, 405, 168, 23);
            regulatoryPathDataPanel.add(btnRegulatoryPathRP);


            JSeparator separator_2 = new JSeparator();
            separator_2.setBounds(15, 386, 507, 8);
            regulatoryPathDataPanel.add(separator_2);

            targetGenesTextFieldRP = new JTextField();
            targetGenesTextFieldRP.setColumns(10);
            targetGenesTextFieldRP.setBounds(163, 92, 179, 20);
            regulatoryPathDataPanel.add(targetGenesTextFieldRP);

            final JTextField targetGenesBrowseRP = new JTextField();
            targetGenesBrowseRP.setColumns(10);
            targetGenesBrowseRP.setBounds(163, 123, 179, 20);
            regulatoryPathDataPanel.add(targetGenesBrowseRP);

            final JRadioButton rdbtnFromFileSrcRP = new JRadioButton("From file:");
            rdbtnFromFileSrcRP.setBounds(64, 59, 87, 23);
            regulatoryPathDataPanel.add(rdbtnFromFileSrcRP);

            final JRadioButton rdbtnFromFileDestRP = new JRadioButton("From file:");
            rdbtnFromFileDestRP.setBounds(64, 120, 87, 23);
            regulatoryPathDataPanel.add(rdbtnFromFileDestRP);

			JLabel layout_type = new JLabel("Layout:");
			layout_type.setBounds(350, 312, 150, 20);
			regulatoryPathDataPanel.add(layout_type);
			JRadioButton src_based_layout_rdbtn = new JRadioButton("Source based");
			src_based_layout_rdbtn.setSelected(true);
			JRadioButton dest_based_layout_rdbtn = new JRadioButton("Target based");
			JRadioButton complete_graph_layout_rdbtn = new JRadioButton("Complete Graph");
			ButtonGroup layout_radioGroup = new ButtonGroup();
			layout_radioGroup.add(src_based_layout_rdbtn);
			layout_radioGroup.add(dest_based_layout_rdbtn);
			layout_radioGroup.add(complete_graph_layout_rdbtn);

			src_based_layout_rdbtn.setBounds(270, 336, 100, 20);
			dest_based_layout_rdbtn.setBounds(375, 336, 150, 20);
			complete_graph_layout_rdbtn.setBounds(310, 356, 150, 20);
			complete_graph_layout_rdbtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					is_source_based = 1;
					complete_graph = 1;
					System.out.println(":D");
				}
			});
			src_based_layout_rdbtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					is_source_based = 1;
					complete_graph = 0;
					System.out.println(":D");
				}
			});
			dest_based_layout_rdbtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					is_source_based = 0;
					complete_graph = 0;
					System.out.println(":-)");
				}
			});
			regulatoryPathDataPanel.add(src_based_layout_rdbtn);
			regulatoryPathDataPanel.add(dest_based_layout_rdbtn);
			regulatoryPathDataPanel.add(complete_graph_layout_rdbtn);

			JLabel path_type = new JLabel("Path type:");
			path_type.setBounds(90, 312, 150, 20);
			regulatoryPathDataPanel.add(path_type);
			JRadioButton exc_rdbtn = new JRadioButton("Activatory");
			exc_rdbtn.setSelected(true);
			JRadioButton inh_rdbtn = new JRadioButton("Inhibitory");
			ButtonGroup path_type_radioGroup = new ButtonGroup();
			path_type_radioGroup.add(exc_rdbtn);
			path_type_radioGroup.add(inh_rdbtn);
			exc_rdbtn.setBounds(40, 336, 90, 20);
			inh_rdbtn.setBounds(130, 336, 100, 20);

			exc_rdbtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					is_exc = 1;
				}
			});
			inh_rdbtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					is_exc = -1;
				}
			});
			regulatoryPathDataPanel.add(exc_rdbtn);
			regulatoryPathDataPanel.add(inh_rdbtn);


			//Input source genes
            rdbtnFromFileSrcRP.setToolTipText(" There should be one name per line in the input file ");
            sourceGenesTextFieldRP.setToolTipText(" Genes/Proteins should be separated by comma ");
            rdbtnFromFileSrcRP.setSelected(false);
            sourceGenesBrowseRP.setEnabled(false);
            btnBrowseSrcRP.setEnabled(false);

            srcTextFieldRP = sourceGenesTextFieldRP;
            srcfileAddressRP = sourceGenesBrowseRP;
            final JFileChooser fc = new JFileChooser();
            btnBrowseSrcRP.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    int returnVal = fc.showOpenDialog(null);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        sourceGenesBrowseRP.setText(file.getAbsolutePath());
                    }
                }
            });
            srcActionListenerRP = btnBrowseSrcRP.getActionListeners()[0];
            ChangeListener myChangeListener = new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent arg0) {
                    if (rdbtnFromFileSrcRP.isSelected()) {
                        sourceGenesTextFieldRP.setEnabled(false);
                        srcFromFileRP = true;
                        sourceGenesBrowseRP.setEnabled(true);
                        btnBrowseSrcRP.setEnabled(true);
                    } else {
                        sourceGenesTextFieldRP.setEnabled(true);
                        srcFromFileRP = false;
                        sourceGenesBrowseRP.setEnabled(false);
                        btnBrowseSrcRP.setEnabled(false);
                    }
                }
            };
            rdbtnFromFileSrcRP.addChangeListener(myChangeListener);



            rdbtnFromFileDestRP.setToolTipText(" There should be one name per line in the input file ");
            targetGenesTextFieldRP.setToolTipText(" Genes/Proteins should be separated by comma ");
            rdbtnFromFileDestRP.setSelected(false);
            targetGenesBrowseRP.setEnabled(false);
            btnBrowseDestRP.setEnabled(false);

			//Input target genes
            dstTextFieldRP = targetGenesTextFieldRP;
            dstfileAddressRP = targetGenesBrowseRP;

            btnBrowseDestRP.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    int returnVal = fc.showOpenDialog(null);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        targetGenesBrowseRP.setText(file.getAbsolutePath());
                    }
                }
            });
            dstActionListenerRP = btnBrowseSrcRP.getActionListeners()[0];
            ChangeListener myChangeListener2 = new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent arg0) {
                    if (rdbtnFromFileDestRP.isSelected()) {
                        targetGenesTextFieldRP.setEnabled(false);
                        dstFromFileRP = true;
                        targetGenesBrowseRP.setEnabled(true);
                        btnBrowseDestRP.setEnabled(true);
                    } else {
                        targetGenesTextFieldRP.setEnabled(true);
                        dstFromFileRP = false;
                        targetGenesBrowseRP.setEnabled(false);
                        btnBrowseDestRP.setEnabled(false);
                    }
                }
            };
            rdbtnFromFileDestRP.addChangeListener(myChangeListener2);

            //Action for button show results
            btnRegulatoryPathRP.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {

                    DATAdatabaseNames = new ArrayList<String>();
                    System.out.println(";D");
                    for (Object net: networkListRP.getSelectedValuesList()) {
                        System.out.println(net.toString());
                        DATAdatabaseNames.add(net.toString());
                    }
                    if(userDatabase && !DATAdatabaseNames.contains("User"))
                        DATAdatabaseNames.add("User");
                    if (DATAdatabaseNames.size() == 0 && !networkFromFile) {
                        JOptionPane.showMessageDialog(null,
                                " Please select at least one database ");
                    } else if (srcFromFileRP
                            && srcfileAddressRP.getText().equals("")) {
                        JOptionPane.showMessageDialog(null,
                                " Please select your source genes ");
                        srcActionListenerRP.actionPerformed(arg0);
                    } else if (!srcFromFileRP
                            && srcTextFieldRP.getText().equals("")) {
                        JOptionPane.showMessageDialog(null,
                                " Please select your source genes ");
                        srcTextFieldRP.requestFocus();
                    } else if (dstFromFileRP
                            && dstfileAddressRP.getText().equals("")) {
                        JOptionPane.showMessageDialog(null,
                                " Please select your target genes ");
                        dstActionListenerRP.actionPerformed(arg0);
                    } else if (!dstFromFileRP
                            && dstTextFieldRP.getText().equals("")) {
                        JOptionPane.showMessageDialog(null,
                                " Please select your target genes ");
                        dstTextFieldRP.requestFocus();
                    } else {

                        DATAsrcfilePathRP = srcfileAddressRP.getText();
                        DATAsrctextFieldRP = srcTextFieldRP.getText();
                        DATAdstfilePathRP = dstfileAddressRP.getText();
                        DATAdsttextFieldRP = dstTextFieldRP.getText();
                        DATAspecies=speciesComboBox.getSelectedItem().toString();
						try {
							frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
							getRegulatoryPath();
							frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
						}
						catch (Exception e)
						{
							frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
						}
                    }
                }
            });

        }
        private void getRegulatoryPath()
        {
            int species = 0;
            species=speciesComboBox.getSelectedIndex();
            step = 0;
            try
            {
                if (networkFromFile)
                    DATAdatabaseNames.add("User");

                step = 1;
                String[] sources = getGenes(DATAsrcfilePathRP, DATAsrctextFieldRP, srcFromFileRP);
                step = 2;
                String[] destinations = getGenes(DATAdstfilePathRP, DATAdsttextFieldRP, dstFromFileRP);

                for (int i = 0; i < sources.length; i++)
                    for (int j = 0; j < destinations.length; j++)
                        if (sources[i].equals(destinations[j]))
                            throw new Exception("'" + sources[i] + "' is in both sources and destination genes.");
				System.out.println("****"+"data fetched successfully");
				getRegulatoryPathForEachDatabase(species, nomen, sources,
                        destinations, DATAdatabaseNames);
                bringToTheFront();
            }
            catch (Exception e) {
                String message = "[getRegulatoryPath: Step " + step
                        + "] Error: ";
				JOptionPane.showMessageDialog(null,
						e.getMessage());

				e.printStackTrace();
            }
        }
		private void getRegulatoryPathForEachDatabase(int species, Nomenclature nomen, String[] sources,
													  String[] destinations, ArrayList<String> dATAdatabaseNames) throws Exception
		{

			RegulatoryPath regulatory_path = null;
			for (String databaseName : dATAdatabaseNames) {
				if(!userDatabase && databaseName.equals("User"))
					continue;
				regulatory_path = subNetworksRP.get(databaseName);
				step = 1;
				Vector<Vector<RNode>> rp_graphs = regulatory_path.getRegulatoryPath(sources, destinations,
																					is_exc, is_source_based);

				rp_visualize(rp_graphs, databaseName);
			}

		}
		private int get_width_of_graph(Vector< RNode > graph)
		{
			HashMap <Integer, Integer> n_depth = new HashMap<>();
			for(RNode node :  graph)
			{
				if (!n_depth.containsKey(node.y))
					n_depth.put(node.y , 0);
				node.x = n_depth.get(node.y);
				n_depth.put(node.y, n_depth.get(node.y)+1);
			}
			int width = 0;
			for(int x : n_depth.values())
				width = Math.max(width, x);
			return width;
		}
		private void set_y_of_destinations(Vector< RNode > graph)
		{
			int max_depth = 0 ;
			for(RNode node :  graph)
				max_depth = Math.max(max_depth, node.depth);
			for(RNode node :  graph)
				if(node.is_destination == 1)
					node.y = max_depth;
		}
		private Vector<RNode> get_valid_rp_graph(Vector<RNode> rp_graph)
		{
			Vector<RNode> valid_graph= new Vector<>();
			int path_exists = 0 ;
			for(RNode node:rp_graph)
				if((node.is_destination == 1 && node.is_exc * is_exc > 0) || node.is_source == 1)
					valid_graph.add(node);

			int cnt = 0;
			for(int i = 0 ; i < valid_graph.size() ; i ++)
			{
				RNode node = valid_graph.get(i);
				node.cnt = cnt;
				cnt+=1;
                if(node.is_destination == 1)
    				path_exists = 1;
                RNode pnode = node.parent;
				if(pnode != null  && !valid_graph.contains(pnode))
					valid_graph.add(pnode);
			}

            for(RNode node:rp_graph)
                if(node.is_destination == 1 && !valid_graph.contains(node)) {
                    valid_graph.add(node);
                    node.parent = null;
                    node.is_exc = 0;
                }
			if(path_exists == 0 )
            {
                for(RNode node:valid_graph)
                    if(node.is_destination == 1 )
                        node.depth = 1;
                if(is_source_based !=1)
                {
                    for(RNode node:valid_graph)
                        if(node.is_source == 1 )
                            node.is_exc = 0;
                }
            }
			return valid_graph;
		}
		private NodeShape getShape(RNode node)
		{
			if ((node.is_source == 1 && is_source_based ==1) || (node.is_destination == 1 && is_source_based !=1))
				return NodeShapeVisualProperty.HEXAGON;

			if ((node.is_destination == 1 && is_source_based ==1) || (node.is_source == 1 && is_source_based !=1))
				return NodeShapeVisualProperty.RECTANGLE;

			return NodeShapeVisualProperty.ELLIPSE;
		}
		private void __rp_visualize(CyNetwork network, Vector<RNode> rp_graph, String base_id, int base_x, String db) throws Exception
		{
			HashMap<Integer, CyNode> mark = new HashMap<>();
			for(RNode node : rp_graph)
			{
				CyNode cy_node;
				if(!mark.containsKey(node.id)) {
					cy_node = network.addNode();
					step = 4;
					network.getRow(cy_node).set("nodeID",
							(nomen.IDtoEntrezID(node.id)) + base_id);
					step = 5;
					mark.put(node.id, cy_node);
				}
				else
					cy_node = mark.get(node.id);
				node.cy_node = cy_node;
			}
			cy_event_helper.flushPayloadEvents();

			step =  6 ;
			HashMap<CyEdge, Boolean> edges_type = new HashMap<CyEdge, Boolean>();

			for(RNode node : rp_graph)
				if(node.parent != null)
				{
					CyNode node1 = node.parent.cy_node;
					CyNode node2 = node.cy_node;
					if(is_source_based != 1)
					{
						CyNode temp = node1;
						node1 = node2;
						node2 = temp;
					}
					if (!network.containsEdge(node1, node2)) {
						CyEdge edge = network.addEdge(node1, node2, true);
                        network.getRow(edge).set("interaction",
                                (nomen.IDtoEntrezID(node.parent.id))+base_id+"-"+(nomen.IDtoEntrezID(node.id))+base_id);
						network.getRow(edge).set("Database", db);
                		edges_type.put(edge, node.parent.is_exc == node.is_exc);
                    }
				}
			cy_event_helper.flushPayloadEvents();
			for (Entry<CyEdge, Boolean> e: edges_type.entrySet()) {
				if(e.getValue())
					setEdgeStyle(e.getKey(), Color.green, ArrowShapeVisualProperty.ARROW);
				else
					setEdgeStyle(e.getKey(), Color.red, ArrowShapeVisualProperty.T);
			}

			int max_depth = 0 ;
			for(RNode node :  rp_graph)
				max_depth = Math.max(max_depth, node.depth);

			for(RNode node: rp_graph)
			{

				step = 7;
				CyNode cy_node = node.cy_node;

                Color color = Color.white;
                if(node.is_destination == 1)
                {
                    if (node.is_exc == 1)
                        color = Color.green;
                    else if (node.is_exc == -1)
                        color = Color.red;
                }
				nodeStyleWithShape(cy_node,
						nomen.Convert(nomen.IDtoName(node.id), "Official_Gene_Symbol"),
						getShape(node),
						color);
				step = 8;

				double y = (double) node.y * 80;
				if( is_source_based !=1)
					y = max_depth-y;

				manager.getCurrentNetworkView()
					   .getNodeView(cy_node)
					   .setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, y);

				manager.getCurrentNetworkView()
						.getNodeView(cy_node)
						.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION,
								(double) (base_x + node.x) * 80);
			}

		}
		private Vector< RNode > merge_rp_graphs(Vector<Vector< RNode >> valid_graphs)
		{
			Vector< RNode > merged_graph = new Vector<>();
			for(Vector< RNode >valid_graph: valid_graphs)
				for(RNode node: valid_graph)
					merged_graph.add(node);
			return merged_graph;
		}
		private void fix_node_colors(Vector< RNode > merged_graph)
		{
			HashMap<Integer, Double> node_type = new HashMap<Integer, Double>();
			for(RNode node: merged_graph)
				if(node.is_destination == 1 && node.is_exc !=0)
					node_type.put(node.id, node.is_exc);
			for(RNode node: merged_graph)
				if(node.is_destination == 1 && node.is_exc ==0 && node_type.containsKey(node.id))
					node.is_exc = node_type.get(node.id);
		}
        private void rp_visualize(Vector<Vector< RNode >> rp_graphs, String title) throws Exception
        {
            String networkTitle = "Regulatory Path network view";
            CyNetwork network = getNetwork(true, title);
            CyNetworkView networkView = getNetworkView(true, network);
			cy_network_manager.addNetwork(network);
			network_view_manager.addNetworkView(networkView);
			try {

				int base_id = 1;
				int base_x = 0;
				Vector<Vector<RNode>> valid_graphs = new Vector<Vector<RNode>>();
				for (Vector<RNode> rp_graph : rp_graphs) {
					Vector<RNode> valid_graph = get_valid_rp_graph(rp_graph);
					valid_graphs.add(valid_graph);
				}
				if (complete_graph == 0){
					for (Vector<RNode> valid_graph : valid_graphs) {
						step = 1;
						String base_id_str = "(" + (Integer.toString(base_id)) + ")";
						step = 2;
						set_y_of_destinations(valid_graph);
						int width = get_width_of_graph(valid_graph) + 2;
						step = 3;
						__rp_visualize(network, valid_graph, base_id_str, base_x, title);
						base_id += 1;
						base_x += width;
					}
				}
				else {
					step = 1;
					Vector<RNode> merged_graph = merge_rp_graphs(valid_graphs);
					step = 2;
					set_y_of_destinations(merged_graph);
					get_width_of_graph(merged_graph);
					fix_node_colors(merged_graph);
					step = 3;
					__rp_visualize(network, merged_graph, "", 0, title);
				}
				manager.getCurrentNetworkView().updateView();

				cy_event_helper.flushPayloadEvents();
			}
			catch (Exception e)
			{
				System.out.println(step);
				System.out.println(":|||||||||||||||||||||||||||||");
				e.printStackTrace();
			}

    	}
		private void doExpand() {
			try {
				expandAndShowNetwork(nomen, DATAdatabaseNames, numberExpand);
				bringToTheFront();
			} catch (Exception e) {
				String message = "[doExpand: Step " + step + "] Error: ";
				JOptionPane.showMessageDialog(null,
						e.getMessage());
				e.printStackTrace();

			}
		}

		private void doDisplayNetwork() {

			step = 0;
			try {

				// ***** Converts the database according to the nomenClature
				// *****//
				if (networkFromFile) {

					DATAdatabaseNames.add("User");
				}
				// *** to here *** //
				step = 1;
				String[] sources = getGenes(DATAsrcfilePath, DATAsrctextField,
						srcFromFileE);

				step = 2;
				showNetwork(nomen, sources, DATAdatabaseNames);
				bringToTheFront();

			} catch (Exception e) {
				String message = "[doDisplayNetwork: Step " + step
						+ "] Error: ";
				JOptionPane.showMessageDialog(null, e.getMessage());
				e.printStackTrace();

			}

		}

		private void showNetwork(Nomenclature nomen,
								 String[] sources, ArrayList<String> dATAdatabaseNames)
				throws Exception {

			for (String databaseName : dATAdatabaseNames) {
				StrongestPath strongestPath = null;
				if(!userDatabase && databaseName.equals("User"))
					continue;
				strongestPath = subNetworks.get(databaseName);
				step = 3;
				strongestPath.setSources(sources);

				step = 4;
				Vector<PairLR> edges = strongestPath.getSubNetwork(databaseName);
				step = 6;
				visaulizeNetwork(edges, sources, nomen, databaseName);

			}
		}

		private void expandAndShowNetwork(Nomenclature nomen,
										  ArrayList<String> dATAdatabaseNames, int numberExpand)
				throws Exception {

			CyNetwork network = manager.getCurrentNetwork();
			String databaseName = network.getRow(network).get(CyNetwork.NAME,
					String.class);
			StrongestPath strongestPath = subNetworks.get(databaseName);
			step = 3;
			Vector<PairLR> edges = null;
			try {
				edges = strongestPath.expandAndGetSubNetwork(databaseName,
						numberExpand);
			} catch (NullPointerException e) {
				throw new Exception("Please select the correct network");

			}
			step = 4;
			if (edges.size() != 0)
				visaulizeNetwork(edges, strongestPath.getSubGraph(), nomen,
						databaseName, false);
			else if (numberExpand > 0)
				throw new Exception("These genes have no neighbors in "
						+ databaseName + "!");
		}

		private void createGrowthDataPanel() {
			growthDataPanel = new JPanel(false);
			growthDataPanel.setLayout(null);

			JLabel lblSourceGenes = new JLabel("Gene list:");
			lblSourceGenes.setBounds(89, 23, 82, 14);
			growthDataPanel.add(lblSourceGenes);

			geneListTextField = new JTextField();
			geneListTextField.setBounds(163, 23, 179, 20);
			growthDataPanel.add(geneListTextField);
			geneListTextField.setColumns(10);
			genesFromFileCheckBox = new JRadioButton("From file: ");
			genesFromFileCheckBox.setBounds(70, 50, 87, 14);
			growthDataPanel.add(genesFromFileCheckBox);

			genesListFromFile = new JTextField();
			geneListTextField.setColumns(10);
			genesListFromFile.setBounds(163, 47, 179, 20);
			growthDataPanel.add(genesListFromFile);

			genesFromFileCheckBox.setToolTipText(" There should be one name per line in the input file ");
			genesFromFileCheckBox.setToolTipText(" Genes should be separated by comma ");
			genesFromFileCheckBox.setSelected(false);
			genesListFromFile.setEnabled(false);
			JButton btnBrowseExpand = new JButton("Browse");
			btnBrowseExpand.setEnabled(false);
			btnBrowseExpand.setBounds(352, 47, 72, 20);
			growthDataPanel.add(btnBrowseExpand);

			final JFileChooser fc = new JFileChooser();
			btnBrowseExpand.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int returnVal = fc.showOpenDialog(null);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						genesListFromFile.setText(file.getAbsolutePath());
					}
				}
			});



			genesFromFileCheckBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					btnBrowseExpand.setEnabled(genesFromFileCheckBox.isSelected());
					genesListFromFile.setEnabled(genesFromFileCheckBox.isSelected());
					srcFromFileE = genesFromFileCheckBox.isSelected();
					geneListTextField.setEnabled(!genesFromFileCheckBox.isSelected());
				}
			});
			JLabel lblSelectNetworkType = new JLabel("Select network type:");
			lblSelectNetworkType.setBounds(89, 117, 124, 20);
			growthDataPanel.add(lblSelectNetworkType);

			String[] networkTypes = new String[] {"Protein-Protein Interaction Network", "Signaling Network"};
			String[] networkType_abbrivations = new String[] {"PPI", "Signaling"};

			networkTypeExpandComboBox = new JComboBox(networkTypes);
			networkTypeExpandComboBox.setBounds(223, 119, 196, 20);
            networkTypeExpandComboBox.setSelectedIndex(-1);
			growthDataPanel.add(networkTypeExpandComboBox);
			JSeparator separator = new JSeparator();
			separator.setBounds(15, 100, 507, 8);
			growthDataPanel.add(separator);

			current_networks = new ArrayList<String>();
			expandNetworkList=new JList(current_networks.toArray(new String[current_networks.size()]));
			expandScrollPane = new JScrollPane(expandNetworkList);
			expandScrollPane.setBounds(66, 161, 400, 85);
			setInternalNetworkPanelEnabled(false);

			growthDataPanel.add(expandScrollPane);

			networkTypeExpandComboBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String type = networkType_abbrivations[networkTypeExpandComboBox.getSelectedIndex()];
					current_networks = new ArrayList<String>();
					for (String net : subNetworks.keySet())
						if(databasesType.get(net).equals(type))
							current_networks.add(net);

					expandNetworkList.setListData(current_networks.toArray(new String[current_networks.size()]));
					expandScrollPane.updateUI();
				}

			});

			JSeparator separator_1 = new JSeparator();
			separator_1.setBounds(15, 267, 507, 8);
			growthDataPanel.add(separator_1);

			JButton btnShow = new JButton("Show Network");
			btnShow.setBounds(181, 298, 148, 23);
			growthDataPanel.add(btnShow);

			btnShow.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					DATAdatabaseNames = new ArrayList<String>();
					System.out.println(";D");
					for (Object net: expandNetworkList.getSelectedValuesList()) {
						System.out.println(net.toString());
						DATAdatabaseNames.add(net.toString());
					}
					if(userDatabase && !DATAdatabaseNames.contains("User"))
						DATAdatabaseNames.add("User");

					if (DATAdatabaseNames.size() == 0 && !networkFromFile) {
						JOptionPane.showMessageDialog(null,
								" Please select at least one database ");
					}
					else if (srcFromFileE && genesListFromFile.getText().equals("")) {
						JOptionPane.showMessageDialog(null,
								" Please select your input genes ");
					}
					else if (!srcFromFileE && geneListTextField.getText().equals("")) {
						JOptionPane.showMessageDialog(null,
								" Please select your input genes ");
						geneListTextField.requestFocus();
					}
					else {
						DATAsrcfilePath = genesListFromFile.getText();
						DATAsrctextField = geneListTextField.getText();
						doDisplayNetwork();
					}
				}
			});

			JLabel lblSelectThreshold = new JLabel("Input number of genes:");
			lblSelectThreshold.setBounds(120, 363, 150, 20);
			growthDataPanel.add(lblSelectThreshold);

			JSeparator separator_2 = new JSeparator();
			separator_2.setBounds(15, 349, 500, 8);
			growthDataPanel.add(separator_2);

			numberExpandTextField = new JTextField();
			numberExpandTextField.setBounds(274, 365, 55, 20);
			growthDataPanel.add(numberExpandTextField);
			numberExpandTextField.setColumns(10);

			JButton btnExpandNetwork = new JButton("Expand Network");
			btnExpandNetwork.setBounds(182, 406, 148, 23);
			growthDataPanel.add(btnExpandNetwork);
			btnExpandNetwork.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						numberExpand = Integer.parseInt(numberExpandTextField.getText());
					} catch (NumberFormatException exception) {
						numberExpand = 0;
						JOptionPane
								.showMessageDialog(null,
										" Please input number of genes to expand current network.");
					}
					try {
						if (geneListTextField.getText().isEmpty())
							throw new Exception();
						else {
							frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
							doExpand();
							frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
						}
					} catch (Exception ex) {
						frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
						JOptionPane
								.showMessageDialog(null,
										" Please select your input genes.");
					}
				}
			});


		}

		private void setBorder(JPanel panel, String text) {
			TitledBorder title = new TitledBorder(
					BorderFactory.createTitledBorder(text));
			title.setTitlePosition(TitledBorder.LEFT);
			panel.setBorder(title);
		}

		private void loadUserDatabasesAndNomen() throws Exception {

			DATAspecies=speciesComboBox.getSelectedItem().toString();
			nomen = new Nomenclature(DATAspecies, annotFile, true);

			convertFile(DATAspecies, nomen);// file
			// binary-species-User-PPI.txt
			// should be created from
			// nomenClature

			String databaseName = "User";

			subNetworks = new HashMap<String, StrongestPath>();
			try {
				subNetworks.put(databaseName, new StrongestPath(DATAspecies, nomen,
						DATAspecies + "-User-" + databaseName + ".txt"));
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, e.getMessage());
				// TODO: handle exception
			}
		}

		public void convertFile(String species, Nomenclature nomen)
				throws Exception {
			String root = new File(Resources.getRoot(), "files").toString();
			File file = new File(new File(root, species).toString(), "/"
					+ species + "-User-User.txt");
			File file2 = new File(new File(root, species).toString(),
					"/" + species + "-User-User-Inverted.txt");

			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(networkFile)));
			BufferedWriter bw = new BufferedWriter(new FileWriter(
					file.getAbsoluteFile()));
			BufferedWriter bw2 = new BufferedWriter(new FileWriter(
					file2.getAbsoluteFile()));
			String line;
			String[] listGeneID;
			LineNumberReader lnr = new LineNumberReader(new FileReader(
					new File(networkFile)));
			lnr.skip(Long.MAX_VALUE);
			int lines = lnr.getLineNumber() - 1;
			if (lines < 0)
				throw new Exception("Database file is corrupted!");
			int[][] fileArray;
			lines+=5;
			fileArray = new int[lines][3];
			String[] weights = new String[lines];
			int i = 0;
			System.out.println(lines);
			br.readLine(); // skip the header
			while ((line = br.readLine()) != null) {
				try {
					listGeneID = line.split("\t");
					if(listGeneID.length < 3)
					    continue;
					fileArray[i][0] = nomen.NametoID(listGeneID[0]);
					fileArray[i][1] = nomen.NametoID(listGeneID[1]);
                    fileArray[i][2]=i;
					weights[i] = listGeneID[2];
					i++;
				} catch (Exception e) {
					throw new Exception(
							"Network file is not appropriate @line " + i + " ("
									+ e.getMessage() + ")");
				}
			}

			java.util.Arrays.sort(fileArray, new Comparator<int[]>() {
				@Override
				public int compare(int[] o1, int[] o2) {
					return ((Integer) o2[0]).compareTo(o1[0]);
				}
			});
			lines = i;
			i--;
			while (i >= 0) {
//			    System.out.println(fileArray[i][0] + "\t" + fileArray[i][1] + "\t"
//                        + weights[fileArray[i][2]] + "\n");
				bw.write(fileArray[i][0] + "\t" + fileArray[i][1] + "\t"
						+ weights[fileArray[i][2]] + "\n");
				i--;
			}
			for (int j = 0; j < lines; j++) {
				swap(fileArray, j, 0, 1);

			}
			i = lines;
			java.util.Arrays.sort(fileArray, new Comparator<int[]>() {
				@Override
				public int compare(int[] o1, int[] o2) {
					return ((Integer) o2[0]).compareTo(o1[0]);
				}
			});
			i--;
			while (i >= 0) {
				bw2.write(fileArray[i][0] + "\t" + fileArray[i][1] + "\t"
						+ weights[fileArray[i][2]] + "\n");
				i--;
			}
			bw.close();
			bw2.close();
			br.close();
			lnr.close();

		}

		public void swap(int[][] arr, int line, int pos1, int pos2) {
			int temp = arr[line][pos1];
			arr[line][pos1] = arr[line][pos2];
			arr[line][pos2] = temp;
		}

		private void loadAllDatabasesAndNomen(){

			DATAspecies=speciesComboBox.getSelectedItem().toString();
			networkFromFile = false;
			try {
				nomen = new Nomenclature(DATAspecies, null, false);
			} catch (Exception e) {
				e.printStackTrace();
			}

			subNetworks = new HashMap<String, StrongestPath>();
			subNetworksRP = new HashMap<String, RegulatoryPath>();
			setDatabases(DATAspecies);

			for (String databaseName : databases) {
				try {
					String type=databasesType.get(databaseName);
					if(!type.equals("RP")) {
						subNetworks.put(databaseName, new StrongestPath(DATAspecies,
								nomen, DATAspecies + "-" + type + "-" + databaseName
								+ ".txt"));
					}
					else {
						subNetworksRP.put(databaseName, new RegulatoryPath(DATAspecies,
								nomen, DATAspecies + "-" + type + "-" + databaseName
								+ ".txt"));
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					JOptionPane.showMessageDialog(null, e.getMessage());
					e.printStackTrace();
				}

			}
			current_networks = new ArrayList<String>();
			for (String net : subNetworksRP.keySet())
				if(databasesType.get(net).equals("RP"))
					current_networks.add(net);
			networkListRP.setListData(current_networks.toArray(new String[current_networks.size()]));
			scrollPaneRP.updateUI();

		}


		private void setDatabases(String species) {

			String root = new File(Resources.getRoot(),"files").toString();
			root= new File(root, species).toString();
			File folder = new File(root);
			File[] listOfFiles = folder.listFiles();
			String name;
            String[] line;
            List<String> names = new ArrayList<String>();

		    for (int i = 0; i < listOfFiles.length; i++) {
		      if (listOfFiles[i].isFile()) {
		    	  name=listOfFiles[i].getName();
		    	  line = name.split("-");
		    	  if (line.length>2)
		    	  {
		    	      String current_name = line[2].replace(".txt", "");
		    		  if( current_name.equals("User"))
		    		  	continue;
		    	      names.add(current_name);
                      databasesType.put(current_name, line[1]);
		    	  }


		      } else if (listOfFiles[i].isDirectory()) {
		    	  JOptionPane.showMessageDialog(null,"Directory " + listOfFiles[i].getName());
		      }
		    }

		    databases = names.toArray(new String[0]);
		    //convert to an array with unique elements
		    Set<String> stringSet = new HashSet<>(Arrays.asList(databases));
		    databases= stringSet.toArray(new String[0]);
		    databaseLabels=databases;
            for (int i = 0 ; i < databases.length ; i ++ )
                System.out.println(databases[i]+" "+databasesType.get(databases[i]));
		}
		private void setInternalNetworkPanelEnabled(boolean b)
		{
			scrollPane.setEnabled(b);
			expandScrollPane.setEnabled(b);
			networkTypeComboBox.setEnabled(b);
			networkList.clearSelection();
			networkList.setEnabled(b);
			expandNetworkList.setEnabled(b);
			expandNetworkList.clearSelection();
			networkTypeExpandComboBox.setEnabled(b);
		}
		public void showFrame() {
			frame = new JFrame("StrongestPath");
			frame.setLocation(100, 20);
			// Add content to the window.
			frame.add(topPanel, BorderLayout.CENTER);
			// Display the window.
			frame.pack();
			frame.setVisible(true);
			frame.addWindowListener(new java.awt.event.WindowAdapter() {
			    @Override
			    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
			        try {
			        	doFinalize();

					} catch (Exception e) {
						// TODO: handle exception
						JOptionPane.showMessageDialog(null, "Didn't manage to finalize the app.");
					}

			    }
			});
		}

        private void doKStrongestPath() {
			int species = 0;
			species=speciesComboBox.getSelectedIndex();
			step = 0;

			try {

				// ***** Converts the database according to the nomenClature
				// *****//
				if (networkFromFile) {
					DATAdatabaseNames.add("User");

				}



				// *** to here *** //
				step = 1;
				String[] sources = getGenes(DATAsrcfilePath, DATAsrctextField,
						srcFromFile);
				step = 2;
				String[] destinations = getGenes(DATAdstfilePath,
						DATAdsttextField, dstFromFile);
				for (int i = 0; i < sources.length; i++)
					for (int j = 0; j < destinations.length; j++) {
						if (sources[i].equals(destinations[j]))
							throw new Exception(
									"'" + sources[i]
											+ "' is in both sources and destination genes.");
					}

				doStrongestPathOnEachDatabase(species, nomen, sources,
						destinations, DATAdatabaseNames);
				bringToTheFront();
			} catch (Exception e) {
				String message = "[doKStrongestPath: Step " + step
						+ "] Error: ";

				JOptionPane.showMessageDialog(null, e.getMessage());
				e.printStackTrace();

			}

		}

		private String[] getGenes(String dATAdstfilePath,
				String dATAdsttextField, boolean dstFromFile)
				throws FileNotFoundException {
			String s;
			ArrayList<String> genes = new ArrayList<String>();
			if (dstFromFile) {
				Scanner sc = new Scanner(new File(dATAdstfilePath));
				while (sc.hasNext()) {
					s = sc.nextLine().trim();
					if (!"".equals(s))
						genes.add(s);
				}
				return genes.toArray(new String[genes.size()]);

			} else {
				String[] splited = dATAdsttextField.split(",");
				for (int i = 0; i < splited.length; i++) {
					splited[i] = splited[i].trim();
					if (!"".equals(splited[i]))
						genes.add(splited[i]);

				}
				return genes.toArray(new String[genes.size()]);
			}

		}

		private void doStrongestPathOnEachDatabase(int species,
				Nomenclature nomen, String[] sources, String[] destinations,
				ArrayList<String> dATAdatabaseNames) throws Exception {


			Map<String, Double> confidences = new HashMap<String, Double>();
			StrongestPath strongestPath = null;
			for (String databaseName : dATAdatabaseNames) {
				if(!userDatabase && databaseName.equals("User"))
					continue;
				strongestPath = subNetworks.get(databaseName);
				step = 3;
				strongestPath.setSources(sources);
				step = 4;
				strongestPath.setDestinations(destinations);
				step = 5;
				Vector<PairLR> edges = strongestPath
						.getStrongestPathsGraph(DATAthreshold, is_edge_connectivity_full);
				step = 6;
				Vector<Vector<PairLR>> vecTemp = new Vector<Vector<PairLR>>();
				vecTemp.add(edges);
				HashSet<Integer> nodeset = new BiConnected(1).edgeToVertex(
						vecTemp).get(0);
				nodeset.remove(0);

				nodeset.remove(strongestPath.destGraph.proteinsCount);
				confidences = strongestPath.getConfidences(nodeset);
				step = 7;
				BFSInfos heights = getHeightsByBFS(edges, nodeset, sources,
						destinations, nomen, strongestPath);
				step = 8;
				visualizeEdges(sources, destinations, edges, heights,
						confidences, true, nomen, databaseName, strongestPath);

				step = 9;
			}

		}
		private void visaulizeNetwork(Vector<PairLR> edges,
									  HashMap<Integer, Integer> subGraph, Nomenclature nomen,
									  String title, boolean createNetwork)
				throws NumberFormatException, Exception {
			String networkTitle = "Strongest path network view";
			CyNetwork network = getNetwork(createNetwork, title);
			CyNetworkView networkView = getNetworkView(createNetwork, network);
			cy_network_manager.addNetwork(network);
			network_view_manager.addNetworkView(networkView);

			Integer max = Collections.max(subGraph.values());

			/* find nodes ids' */

			ArrayList<Entry<Integer, Integer>> allNodes = new ArrayList<Entry<Integer, Integer>>(
					subGraph.entrySet());
			Collections.sort(allNodes,
					new Comparator<Entry<Integer, Integer>>() {
						@Override
						public int compare(Entry<Integer, Integer> arg0,
										   Entry<Integer, Integer> arg1) {
							if (arg0.getValue() > arg1.getValue())
								return 1;
							else if (arg0.getValue() < arg1.getValue())
								return -1;
							else
								return 0;
						}

					});
			Integer[] nodes = new Integer[allNodes.size()];
			for (int i = 0; i < allNodes.size(); i++) {
				nodes[i] = allNodes.get(i).getKey();

			}
			String[] nodeIds = new String[nodes.length];
			for (int i = 0; i < nodes.length; i++) {
				nodeIds[i] = nodes[i].toString();
			}

			/* add nodes to network */
			ArrayList<CyNode> newNodes = new ArrayList<CyNode>();
			ArrayList<String> newStrings = new ArrayList<String>();
			for (String s : nodeIds) {

				// Added the nodes to the network with name equal to node ID
				CyNode node;

				node = getNodeWithValue(network, "nodeID", nomen.IDtoEntrezID(new Integer(s)));
				if (node == null) {
					node = network.addNode();
					setNodeId(network, node, nomen, s);
					newNodes.add(node);
					newStrings.add(s);
				}

				/* Change color and shape of the node in the network */

			}
			cy_event_helper.flushPayloadEvents();

			for (int i = 0; i < allNodes.size(); i++) {
				int g = (255 / (max)) * (max - allNodes.get(i).getValue());

				nodeStyleWithShape(
						getNodeWithValue(network, "nodeID", nomen.IDtoEntrezID(allNodes.get(i)
								.getKey())), nomen.Convert(
								nomen.IDtoName(allNodes.get(i).getKey()),
								"Official_Gene_Symbol"),
						NodeShapeVisualProperty.ELLIPSE, new Color(255, g, g));

				/*nodeStyleWithShape(newNodes.get(i), nomen.Convert(
						nomen.IDtoName(Integer.parseInt(s)),
						"Official_Gene_Symbol"),
						NodeShapeVisualProperty.ELLIPSE, null);*/
			}

			/* add edges to the network */
			CyEdge edge;
			CyNode node1, node2;

			for (PairLR p : edges) {
                String nodeId1 = nomen.IDtoEntrezID(nomen.NametoID(p.left));
                String nodeId2 = nomen.IDtoEntrezID(nomen.NametoID(p.right));
				node1 = getNodeWithValue(network, "nodeID", nodeId1);
				node2 = getNodeWithValue(network, "nodeID", nodeId2);

				if (!network.containsEdge(node1, node2)) {
					edge = network.addEdge(node1, node2, true);
                    network.getRow(edge).set("interaction", nodeId1+"-"+nodeId2);
                    network.getRow(edge).set("Database", p.dataBaseName);
				}

			}

			/*************************************/

			// Create the visual style
			buildNetwork(network, networkView, networkTitle + title, false);
			applyTableLayout(network, nodeIds, nomen);

		}

		private void visaulizeNetwork(Vector<PairLR> edges, String[] nodes,
									  Nomenclature nomen, String title) throws Exception {
			// String networkTitle = "Strongest path network view: ";
			/*
			 * Old 2.x CyNetwork network = Cytoscape.createNetwork(title);
			 */
			// New 3.x
			CyNetwork network = getNetwork(true, title);
			CyNetworkView networkView = getNetworkView(true, network);
			cy_network_manager.addNetwork(network);
			network_view_manager.addNetworkView(networkView);

			/* find nodes ids' */
			String[] srcIds = new String[nodes.length];
			Integer tempId;
			for (int i = 0; i < srcIds.length; i++) {
				tempId = nomen.NametoID(nodes[i].trim());
				if (tempId == -1)
					throw new Exception("There is no gene with this id: "
							+ nodes[i]);
				srcIds[i] = tempId.toString();
			}

			/* add nodes to network */

			ArrayList<CyNode> newNodes = new ArrayList<CyNode>();
			ArrayList<String> newStrings = new ArrayList<String>();
			for (String s : srcIds) {
				/*
				 * Old 2.x node = Cytoscape.getCyNode(s, true);
				 * network.addNode(node);
				 */
				// New 3.x
				CyNode node;
				node = network.addNode();
				setNodeId(network, node, nomen, s);
				/* change shape and color of the nodes */
				step = 40;
				newStrings.add(s);
				newNodes.add(node);
			}
			cy_event_helper.flushPayloadEvents();
			String s;

			for (int i = 0; i < newNodes.size(); i++) {
				s = newStrings.get(i);
				if (!s.equals("source") && !s.equals("destination")) {
					nodeStyleWithShape(newNodes.get(i), nomen.Convert(
							nomen.IDtoName(Integer.parseInt(s)),
							"Official_Gene_Symbol"),
							NodeShapeVisualProperty.ELLIPSE, null);
				}

			}

			/* add edges to the network */
			CyEdge edge;
			/*
			 * Old 2.x CyAttributes cyNodeAttrs; CyAttributes cyEdgeAttrs;
			 */
			CyNode node1, node2;

			for (PairLR p : edges) {
                String nodeId1 = nomen.IDtoEntrezID(nomen.NametoID(p.left));
                String nodeId2 = nomen.IDtoEntrezID(nomen.NametoID(p.right));
                node1 = getNodeWithValue(network, "nodeID", nodeId1);
                node2 = getNodeWithValue(network, "nodeID", nodeId2);

				if (!network.containsEdge(node1, node2)) {
					edge = network.addEdge(node1, node2, true);
                    network.getRow(edge).set("interaction", nodeId1+"-"+nodeId2);
					network.getRow(edge).set("Database", p.dataBaseName);
				}


			}

			/*************************************/

			// Create the visual style
			buildNetwork(network, networkView, title, false);
			applyTableLayout(network, srcIds, nomen);

		}
		private void applyTableLayout(CyNetwork network, String[] srcIds,
									  Nomenclature nomen) {
			CyNode node;
			int r = 0, c = 0;
			int maxRow = (int) Math.ceil(Math.sqrt(srcIds.length));

			for (String nodeID : srcIds) {

				if (c == maxRow) {
					c = 0;
					r++;
				}
				/*
				 * Old 2.x node = Cytoscape.getCyNode(nodeID, false);
				 * Cytoscape.getCurrentNetworkView().getNodeView(node)
				 * .setYPosition(r * 80);
				 * Cytoscape.getCurrentNetworkView().getNodeView(node)
				 * .setXPosition(c * 80);
				 */
				/* New 3.x */
				node = getNodeWithValue(network, "nodeID", nomen.IDtoEntrezID(new Integer(nodeID.toString())));
				manager.getCurrentNetworkView()
						.getNodeView(node)
						.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION,
								(double) r * 80);
				manager.getCurrentNetworkView()
						.getNodeView(node)
						.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION,
								(double) c * 80);

				c++;
			}
			// Old 2.x
			// Cytoscape.getCurrentNetworkView().redrawGraph(false, true);
			manager.getCurrentNetworkView().updateView();
			cy_event_helper.flushPayloadEvents();
		}

		private void setNodeId(CyNetwork network, CyNode node, Nomenclature nomen, String id)
		{
			network.getRow(node).set("nodeID", nomen.IDtoEntrezID(new Integer(id)));
		}
		private void visualizeEdges(String[] srcIds, String[] dstIds,
				Vector<PairLR> edges, BFSInfos heights,
				Map<String, Double> confidences, boolean createNetwork,
				Nomenclature nomen, String title, StrongestPath strongestPath)
				throws Exception {

			String networkTitle = "Strongest path network: ";

			CyNetwork network = getNetwork(createNetwork, title);
			CyNetworkView networkView = getNetworkView(createNetwork, network);

			cy_network_manager.addNetwork(network);
			network_view_manager.addNetworkView(networkView);
			/*******/
			/* change style of nodes */

			// graphStyle.setNodeSizeLocked(false);

			// set some visual property for two nodes
			Integer tempId;
			String[] srcIds2 = new String[srcIds.length];
			for (int i = 0; i < srcIds2.length; i++) {
				tempId = nomen.NametoID(srcIds[i].trim());
				if (tempId == -1)
					throw new Exception("There is no gene with this id: "
							+ srcIds[i]);
				srcIds2[i] = tempId.toString();
			}
			String[] dstIds2 = new String[dstIds.length];
			for (int i = 0; i < dstIds2.length; i++) {
				tempId = nomen.NametoID(dstIds[i].trim());
				if (tempId == -1)
					throw new Exception("There is no gene with this name: "
							+ dstIds[i]);
				dstIds2[i] = tempId.toString();
			}
			/**********/
			if (createNetwork) {
				step = 22;
				ArrayList<CyNode> newNodes = new ArrayList<CyNode>();
				ArrayList<String> newStrings = new ArrayList<String>();
				for (String s : srcIds2) {
					CyNode node;
					/*
					 * Old 2.x node = Cytoscape.getCyNode(s, true);
					 * network.addNode(node);
					 */
					// New 3.x
					node = network.addNode();
					setNodeId(network, node, nomen, s);
					newNodes.add(node);
					newStrings.add(s);

				}
				cy_event_helper.flushPayloadEvents();
				String s;
				for (int i = 0; i < newNodes.size(); i++) {
					s = newStrings.get(i);
					if (!s.equals("source") && !s.equals("destination")) {
						nodeStyleWithShape(newNodes.get(i), nomen.Convert(
								nomen.IDtoName(Integer.parseInt(s)),
								"Official_Gene_Symbol"),
								NodeShapeVisualProperty.ELLIPSE, null);
					}

				}

				newNodes.clear();
				newStrings.clear();
				for (String s1 : dstIds2) {
					CyNode node;
					/*
					 * Old 2.x node = Cytoscape.getCyNode(s, true);
					 * network.addNode(node);
					 */
					// New 3.x
					node = network.addNode();
					setNodeId(network, node, nomen, s1);
					newNodes.add(node);
					newStrings.add(s1);
				}
				cy_event_helper.flushPayloadEvents();
				String s1;
				for (int i = 0; i < newNodes.size(); i++) {
					s1 = newStrings.get(i);
					if (!s1.equals("source") && !s1.equals("destination")) {
						nodeStyleWithShape(newNodes.get(i), nomen.Convert(
								nomen.IDtoName(Integer.parseInt(s1)),
								"Official_Gene_Symbol"),
								NodeShapeVisualProperty.ELLIPSE, null);
					}

				}
			}

			CyEdge edge;
			/*
			 * Old 2.x CyAttributes cyNodeAttrs; CyAttributes cyEdgeAttrs;
			 */

			int src = 0;
			int dst = strongestPath.destGraph.proteinsCount;

			/********** DRAW EDGES ***************/
			step = 25;
			int nodeID1, nodeID2;
			ArrayList<CyNode> newNodes = new ArrayList<CyNode>();
			ArrayList<Integer> newNodesIDs = new ArrayList<Integer>();
			for (PairLR p : edges) {
				if (p.l == src || p.l == dst || p.r == src || p.r == dst)
					continue;
				/*
				 * Old 2.x node1 =
				 * Cytoscape.getCyNode(nomen.NametoID(p.left).toString(), true);
				 * node2 =
				 * Cytoscape.getCyNode(nomen.NametoID(p.right).toString(),
				 * true);
				 */
				CyNode node1, node2;
				nodeID1 = nomen.NametoID(p.left);
				nodeID2 = nomen.NametoID(p.right);
				step = 26;
				Integer.toString(nodeID1);

				node1 = getNodeWithValue(network, "nodeID",
						nomen.IDtoEntrezID(nodeID1));
				node2 = getNodeWithValue(network, "nodeID",
						nomen.IDtoEntrezID(nodeID2));

				// if (!network.containsNode(node1)) {
				if (node1 == null) {
					/*
					 * Old 2.x network.addNode(node1);
					 */
					// TODO add node1 to the network
					step = 27;
					node1 = network.addNode();
					setNodeId(network, node1, nomen, Integer.toString(nodeID1));
					step = 271;
					newNodes.add(node1);
					newNodesIDs.add(nodeID1);

					/*
					 * cy_event_helper.flushPayloadEvents(); step =
					 * 272; nodeStyle3(node1, nomen, nodeID1); step = 273;
					 */
				}
				// if (!network.containsNode(node2)) {
				if (node2 == null) {
					/*
					 * Old 2.x network.addNode(node2);
					 */

					// TODO add node2 to the network
					node2 = network.addNode();
					setNodeId(network, node2, nomen, Integer.toString(nodeID2));
					newNodes.add(node2);
					newNodesIDs.add(nodeID2);
					// cy_event_helper.flushPayloadEvents();
					// step = 28;
					// nodeStyle3(node2, nomen, nodeID2);
				}

				/*** here ***/

				/*
				 * Old 2.x cyNodeAttrs = Cytoscape.getNodeAttributes();
				 * cyNodeAttrs.setAttribute(node1.getSUID(), "PathConfidence",
				 * 1.0 / Math.pow(2, confidences.get(p.left)));
				 * cyNodeAttrs.setAttribute(node2.getSUID(), "PathConfidence",
				 * 1.0 / Math.pow(2, confidences.get(p.right)));
				 */
				// New 3.x
				step = 29;
				/*
				network.getRow(node1).set(
						"Path Confidence",
						Double.toString(1.0 / Math.pow(2,
								confidences.get(p.left))));
				network.getRow(node2).set(
						"Path Confidence",
						Double.toString(1.0 / Math.pow(2,
								confidences.get(p.right))));
*/
				/*
				 * Old 2.x edge = Cytoscape.getCyEdge(node1, node2,
				 * Semantics.INTERACTION, "pp", true); cyEdgeAttrs =
				 * Cytoscape.getEdgeAttributes(); if (title.equals(""))
				 * cyEdgeAttrs.setAttribute(edge.getSUID(), "Database",
				 * p.dataBaseName); if (!network.containsEdge(edge))
				 * network.addEdge(edge);
				 */
				// New 3.x
				if (!network.containsEdge(node1, node2)) {
					edge = network.addEdge(node1, node2, true);
					String eid1 = nomen.IDtoEntrezID(new Integer(nodeID1));
					String eid2 = nomen.IDtoEntrezID(new Integer(nodeID2));
					network.getRow(edge).set("interaction", eid1+"-"+eid2);
					network.getRow(edge).set("Database", p.dataBaseName);
				}

			}
			cy_event_helper.flushPayloadEvents();

			for (int i = 0; i < newNodes.size(); i++) {
				nodeStyle3(newNodes.get(i), nomen, newNodesIDs.get(i));
			}
			/*************************************/
			// Create the visual style
			buildNetwork(network, networkView, networkTitle + title, true);

			applyColor(network, networkView, heights, nomen);
			applyBFSLayout(network, networkView, heights, nomen);
			// JOptionPane.showMessageDialog(null, "BFS");

		}

        private CyNode getNodeWithValue(final CyNetwork network,
				final String colname, final Object value) {
			CyTable table = network.getDefaultNodeTable();
			final Collection<CyRow> matchingRows = table.getMatchingRows(
					colname, value);
			final Set<CyNode> nodes = new HashSet<CyNode>();
			final String primaryKeyColname = table.getPrimaryKey().getName();
			if (matchingRows.size() != 0) {
				for (final CyRow row : matchingRows) {
					final Long nodeId = row.get(primaryKeyColname, Long.class);
					if (nodeId == null)
						continue;
					final CyNode node = network.getNode(nodeId);
					if (node == null)
						continue;
					nodes.add(node);
				}
				return nodes.iterator().next();
			} else
				return null;
		}

		private void nodeStyle3(CyNode node, Nomenclature nomen, int nodeId)
				throws NumberFormatException, Exception {


			String officialSymbol = nomen.Convert(nomen.IDtoName(nodeId),
					"Official_Gene_Symbol");

			Color color = new Color(0, 250, 250);
			step = 281;
			if (manager.getCurrentNetworkView() == null)
				JOptionPane.showMessageDialog(null, "network view is null");

			int exp_cnt = 0;
			while(manager.getCurrentNetworkView().getNodeView(node) == null) {
				if(exp_cnt > 10) {
					JOptionPane.showMessageDialog(null, officialSymbol
							+ " node view is null " + node.toString());
					break;
				}
				exp_cnt++;
				System.out.println(exp_cnt);
				Thread.sleep(10000);
			}
			manager.getCurrentNetworkView()
					.getNodeView(node)
					.setVisualProperty(BasicVisualLexicon.NODE_LABEL,
							officialSymbol);
			step = 282;
			manager.getCurrentNetworkView().getNodeView(node)
					.setVisualProperty(BasicVisualLexicon.NODE_WIDTH, 50.0);
			step = 283;
			manager.getCurrentNetworkView().getNodeView(node)
					.setVisualProperty(BasicVisualLexicon.NODE_HEIGHT, 50.0);
			step = 284;
			manager.getCurrentNetworkView()
					.getNodeView(node)
					.setVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR,
							color);
			step = 285;
			manager.getCurrentNetworkView()
					.getNodeView(node)
					.setVisualProperty(BasicVisualLexicon.NODE_SHAPE,
							NodeShapeVisualProperty.ELLIPSE);
		}

		private void applyColor(CyNetwork network, CyNetworkView networkView,
				BFSInfos bfsInfos, Nomenclature nomen) {
			HashMap<Integer, Integer> heights = bfsInfos.heights;
			int max = 0;
			for (Entry<Integer, Integer> e : heights.entrySet())
				if (e.getValue() > max)
					max = e.getValue();

			CyNode node;
			step = 26;
			for (Entry<Integer, Integer> e : heights.entrySet()) {
				/*
				 * Old 2.x CyNode node1 =
				 * Cytoscape.getCyNode(e.getKey().toString(), true);
				 */
				node = getNodeWithValue(network, "nodeID",
						nomen.IDtoEntrezID(e.getKey()));

				String red = Integer.toHexString((255 / max)
						* (max - e.getValue()));
				if (red.length() == 1)
					red = "0" + red;
				networkView.getNodeView(node).setVisualProperty(
						BasicVisualLexicon.NODE_FILL_COLOR,
						Color.decode("#ff" + red + red));
			}
			// Old 2.x
			// graphStyle.buildStyle();
		}

		private void applyBFSLayout(CyNetwork network,
				CyNetworkView networkView, BFSInfos bfsInfos, Nomenclature nomen) {
			CyNode node;
			HashMap<Integer, Integer> heights = bfsInfos.heights;
			HashMap<Integer, Integer> visitTimes = bfsInfos.visitTimes;

			for (Integer nodeID : heights.keySet()) {
				/*
				 * Old 2.x node = Cytoscape.getCyNode(nodeID.toString(), false);
				 * Cytoscape.getCurrentNetworkView().getNodeView(node)
				 * .setYPosition(heights.get(nodeID) * 80);
				 * Cytoscape.getCurrentNetworkView().getNodeView(node)
				 * .setXPosition(visitTimes.get(nodeID) * 80);
				 */
				// JOptionPane.showMessageDialog(null, "x: "+
				// heights.get(nodeID)+ ", y: "+visitTimes.get(nodeID));
				node = getNodeWithValue(network, "nodeID", nomen.IDtoEntrezID(nodeID));
				networkView.getNodeView(node).setVisualProperty(
						BasicVisualLexicon.NODE_Y_LOCATION,
						(double) heights.get(nodeID) * 80);
				networkView.getNodeView(node).setVisualProperty(
						BasicVisualLexicon.NODE_X_LOCATION,
						(double) visitTimes.get(nodeID) * 80);
			}
			// Old 2.x
			// Cytoscape.getCurrentNetworkView().redrawGraph(false, true);
			// networkView.updateView();
			networkView.updateView();
			cy_event_helper.flushPayloadEvents();

		}
        private void setEdgeStyle(CyEdge edge, Color color, ArrowShape arrow_type) {
			step = 65;
            manager.getCurrentNetworkView().getEdgeView(edge)
                    .setVisualProperty(BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE, arrow_type);
			step = 66;
			manager.getCurrentNetworkView().getEdgeView(edge)
					.setVisualProperty(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, color);
			manager.getCurrentNetworkView().getEdgeView(edge)
					.setVisualProperty(BasicVisualLexicon.EDGE_STROKE_SELECTED_PAINT, color.YELLOW);

//			Range<Paint> PAINT_RANGE = new ContinuousRange(Paint.class, color.black, color.white, true, true);
//
//			PaintVisualProperty EDGE_PAINT = new PaintVisualProperty(color, PAINT_RANGE, "EDGE_PAINT",
//					"Edge Paint", CyEdge.class);
//			PaintVisualProperty EDGE_SELECTED_PAINT = new PaintVisualProperty(Color.YELLOW, PAINT_RANGE, "EDGE_SELECTED_PAINT",
//					"Edge Color (Selected)", CyEdge.class);
//			PaintVisualProperty EDGE_UNSELECTED_PAINT = new PaintVisualProperty(color, PAINT_RANGE,
//					"EDGE_UNSELECTED_PAINT", "Edge Color (Unselected)", CyEdge.class);
//
//			manager.getCurrentNetworkView().getEdgeView(edge)
//					.setVisualProperty(BasicVisualLexicon.EDGE_PAINT, EDGE_PAINT);
//			manager.getCurrentNetworkView().getEdgeView(edge)
//					.setVisualProperty(BasicVisualLexicon.EDGE_SELECTED_PAINT, EDGE_SELECTED_PAINT);
//			manager.getCurrentNetworkView().getEdgeView(edge)
//					.setVisualProperty(BasicVisualLexicon.EDGE_UNSELECTED_PAINT, EDGE_UNSELECTED_PAINT);
        }

		private void nodeStyleWithShape(CyNode node, String label,
				NodeShape shape, Color color) {
			// New 3.x
			if (color == null) {
				color = new Color(0, 250, 250);
			}
			step = 41;
			manager.getCurrentNetworkView().getNodeView(node)
					.setVisualProperty(BasicVisualLexicon.NODE_LABEL, label);
			step = 42;
			manager.getCurrentNetworkView().getNodeView(node)
					.setVisualProperty(BasicVisualLexicon.NODE_WIDTH, 50.0);
			manager.getCurrentNetworkView().getNodeView(node)
					.setVisualProperty(BasicVisualLexicon.NODE_HEIGHT, 50.0);
			manager.getCurrentNetworkView()
					.getNodeView(node)
					.setVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR,
							color);
			manager.getCurrentNetworkView().getNodeView(node)
					.setVisualProperty(BasicVisualLexicon.NODE_SHAPE, shape);

			/*
			 * Old 2.x graphStyle.addProperty(nodeIdentifier,
			 * VisualPropertyType.NODE_LABEL, label);
			 * graphStyle.addProperty(nodeIdentifier,
			 * VisualPropertyType.NODE_WIDTH, "50");
			 * graphStyle.addProperty(nodeIdentifier,
			 * VisualPropertyType.NODE_HEIGHT, "50");
			 * graphStyle.addProperty(nodeIdentifier,
			 * VisualPropertyType.NODE_FILL_COLOR, "#00FAFA");
			 * graphStyle.addProperty(nodeIdentifier,
			 * VisualPropertyType.NODE_SHAPE, shape);
			 */
		}

		private CyNetworkView getNetworkView(boolean createNetwork,
				CyNetwork network) {
			CyNetworkView networkView;
			if (createNetwork)
				networkView = network_view_factory.createNetworkView(network);
			else
				networkView = manager.getCurrentNetworkView();
			return networkView;
		}

		private CyNetwork getNetwork(boolean createNetwork, String title) {
			CyNetwork network;
			if (createNetwork) {
				network = network_factory.createNetwork();
				network.getRow(network).set(CyNetwork.NAME, title);

			} else
				network = manager.getCurrentNetwork();
			addNodeIDColumn(network);
			return network;
		}

    	protected void addNodeIDColumn(CyNetwork network) {
			CyTable nodeTable = network.getDefaultNodeTable();
			CyTable edgeTable = network.getDefaultEdgeTable();
			if (nodeTable.getColumn("nodeID") == null) {
				nodeTable.createColumn("nodeID", String.class, false);
			}
			/*if (nodeTable.getColumn("Path Confidence") == null) {
				nodeTable.createColumn("Path Confidence", String.class, false);
			}*/
			if (edgeTable.getColumn("Database") == null) {
				edgeTable.createColumn("Database", String.class, false);
			}
		}

		private BFSInfos getHeightsByBFS(Vector<PairLR> edges,
				HashSet<Integer> nodeset, String[] sources,
				String[] destinations, Nomenclature nomen, StrongestPath sp)
				throws Exception {
			HashMap<Integer, Integer> heights = new HashMap<Integer, Integer>();
			HashMap<Integer, HashSet<Integer>> graph = new HashMap<Integer, HashSet<Integer>>();
			int src = 0;
			int dst = sp.destGraph.proteinsCount;

			for (PairLR p : edges) {
				if (p.l == src || p.l == dst || p.r == src || p.r == dst)
					continue;
				if (!graph.containsKey(p.r))
					graph.put(p.r, new HashSet<Integer>());
				if (!graph.containsKey(p.l))
					graph.put(p.l, new HashSet<Integer>());

				HashSet<Integer> neighbors, neighbors2;
				neighbors = graph.get(p.r);
				neighbors.add(p.l);
				graph.put(p.r, neighbors);
				neighbors2 = graph.get(p.l);
				neighbors2.add(p.r);
				graph.put(p.l, neighbors2);
			}

			ArrayList<Integer> queue = new ArrayList<Integer>();
			HashSet<Integer> marked = new HashSet<Integer>();
			int start, end;
			int current;
			// TODO: 1e5 -> proteins count
			int visitTime[] = new int[100000];

			HashMap<Integer, Integer> visitTimes = new HashMap<Integer, Integer>();
			int temp = sources.length - 1;
			for (String srcGene : sources) {
				current = nomen.NametoID(srcGene);
				heights.put(current, 0);
				marked.add(current);
				if (graph.containsKey(current)) {
					queue.add(current);
					visitTimes.put(current, visitTime[0]++);
				} else
					visitTimes.put(current, temp--);
			}
			end = queue.size();
			int current_height;
			start = 0;
			int max = 0;

			while (end != start) {
				current = queue.get(start);
				current_height = heights.get(current);
				start++;
				for (Integer child : graph.get(current)) {
					if (!marked.contains(child)) {
						heights.put(child, current_height + 1);
						visitTimes.put(child, visitTime[current_height + 1]++);
						if (max < current_height + 1)
							max = current_height + 1;
						queue.add(child);
						marked.add(child);
						end++;
					}
				}
			}
			int count = 0;
			for (Entry<Integer, Integer> e : heights.entrySet()) {
				if (e.getValue() == max)
					count++;
			}
			if (max == 0 || count > destinations.length)
				max++;
			//else
			//	visitTime[max]--;
			for (String dstGene : destinations) {
				heights.put(nomen.NametoID(dstGene), max);
				visitTimes.put(nomen.NametoID(dstGene), visitTime[max]++);
			}

			BFSInfos bfs = new BFSInfos();
			bfs.heights = heights;
			bfs.visitTimes = visitTimes;

			return bfs;
		}

		protected void bringToTheFront()
		{
			java.awt.EventQueue.invokeLater(new Runnable() {
			    @Override
			    public void run() {
			        frame.toFront();
			        frame.repaint();
			    }
			});
		}



	}
	private void buildNetwork(CyNetwork network, CyNetworkView networkView,
			String networkTitle, boolean disposeFrame) {


		/*
		 * //TODO Convert next few lines to the new 3.x
		 * CyLayoutAlgorithm layoutAlgorithm =
		 * getCyLayoutAlgorithmManager().getAllLayouts().iterator()
		 * .next(); TaskIterator itr =
		 * layoutAlgorithm.createTaskIterator(networkView,
		 * layoutAlgorithm.createLayoutContext(),
		 * CyLayoutAlgorithm.ALL_NODE_VIEWS, null);
		 * getTaskManager().execute(itr);
		 */
	}

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
			String cwd =   System.getProperty("user.home");
			return (new File (cwd, "CytoscapeConfiguration")).toString();

//			return getHigherFolder(new File(MyStrongestPathPlugin.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent());
		}


}
