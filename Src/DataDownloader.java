import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.HttpURLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.*;
import javafx.util.Pair;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class DataDownloader {
    Map<String, ArrayList<Pair<String, String>>> dataset_names_for_species =  new HashMap<String, ArrayList<Pair<String, String>>>();
    ArrayList<String> species = new ArrayList<String>();
    String state = "OK";
    String root ;
    boolean hash_exists = false;
    HashMap<String, String> current_hashes = new HashMap<String, String>();
    HashMap<String, String> new_hashes = new HashMap<String, String>();

    DataDownloader(String rt)
    {
        root = rt;
    }
    private void check_hash_map_exists(String root, String specie)
    {

        File file = new File(root + "/files", "hashes-"+specie+".txt");
        if (!file.exists())
            return ;
        hash_exists = true;
    }
    private HashMap<String,String> make_hash_map_from_file(String file, HashMap<String,String> hashes)
    {
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String [] line;
            String st;
            while ((st = br.readLine()) != null) {
                line = st.split(" ");
                hashes.put(line[0], line[1]);
            }
            br.close();

        }
        catch(Exception e)
        {
            state = "failed";
        }
        return hashes;

    }
    private boolean isRedirected(Map<String, List<String>> header) {
        for( String hv : header.get( null )) {
            if(   hv.contains( " 301 " )
                    || hv.contains( " 302 " )) return true;
        }
        return false;
    }

    private void download_file(String link, String file_name) throws Throwable
    {
        //String link ="https://raw.githubusercontent.com/Alir3za97/SSP/master/Human/Human-PPI-Reactome.txt";
        //String fileName = "files/Human-PPI-Reactome.txt";
        URL url  = new URL( link );
        HttpURLConnection http = (HttpURLConnection)url.openConnection();
        Map< String, List< String >> header = http.getHeaderFields();
        while( isRedirected( header )) {
            link = header.get( "Location" ).get( 0 );
            url    = new URL( link );
            http   = (HttpURLConnection)url.openConnection();
            header = http.getHeaderFields();
        }
        InputStream  input  = http.getInputStream();
        byte[]       buffer = new byte[4096];
        int          n      = -1;
        OutputStream output = new FileOutputStream( new File( file_name));
        while ((n = input.read(buffer)) != -1) {
            output.write( buffer, 0, n );
        }
        output.close();
    }
    public void download_species(String root)
    {
        try {
            download_file("https://raw.githubusercontent.com/zmousavian/StrongestPath/main/Files/species.txt",
                    root + "/files/species.txt");
        }
        catch (Throwable T)
        {
            state = "failed";
            System.out.println("Download failed in species");
        }
    }
    private void download_initial_files(String root)
    {
        try
        {
            download_file("https://raw.githubusercontent.com/zmousavian/StrongestPath/main/Files/species.txt",
                    root + "/files/species.txt");
            download_file("https://raw.githubusercontent.com/zmousavian/StrongestPath/main/Files/names.txt",
                    root + "/files/names.txt");

            File file = new File(root + "/files", "species.txt");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String st;
            while ((st = br.readLine()) != null) {
                species.add(st);
            }
            br.close();

            for (String specie : species)
                dataset_names_for_species.put(specie, new ArrayList<Pair<String, String>>());

            file = new File(root + "/files", "names.txt");
            br = new BufferedReader(new FileReader(file));
            String [] line;
            while ((st = br.readLine()) != null) {
                line = st.split(" ");
                dataset_names_for_species.get(line[0]).add(new Pair<String, String>(line[1],line[2]));
            }
            br.close();

        }
        catch(Throwable t)
        {
            t.printStackTrace();
            state = "failed";
            System.out.println("Download failed in initials");
        }
    }
    void download_data_for_specie(String specie, String root )
    {

        (new File(root +"/files")).mkdir();
        (new File(root +"/files/"+specie)).mkdir();
        check_hash_map_exists(root, specie);
        download_initial_files(root);

        if(hash_exists)
        {
            try
            {
                download_file("https://raw.githubusercontent.com/zmousavian/StrongestPath/main/Files/hashes-"+specie+".txt",
                        root + "/files/hashes-tmp.txt");
                make_hash_map_from_file(root + "/files/hashes-"+specie+".txt", current_hashes);
                make_hash_map_from_file(root + "/files/hashes-tmp.txt", new_hashes);

                Files.deleteIfExists(Paths.get(root + "/files/hashes-tmp.txt"));

            }
            catch(Throwable t)
            {
                state = "failed";
                System.out.println("Download failed in hashes");
            }
        }

        String base = "https://raw.githubusercontent.com/zmousavian/StrongestPath/main/Files/"+specie+"/";
        String address = root+"/files/"+specie+"/";
        try
        {
            if(should_download(specie + "-annotations"))
            {
                download_file(base + specie + "-Annotations.txt.zip", address + specie + "-Annotations.txt.zip");
                unzip(address + specie + "-Annotations.txt.zip", address);
                delet_zip(address + specie + "-Annotations.txt.zip");
            }
            for (Pair<String, String> data : dataset_names_for_species.get(specie))
            {
                String dataset_name = data.getKey();
                String type = data.getValue();
                String name = specie+"-"+type+"-"+dataset_name+".txt";
                String name_inverted = specie+"-"+type+"-"+dataset_name+"-Inverted.txt";
                if(should_download((specie+"-"+dataset_name)))
                {
                    download_file(base+name+".zip",address+name+".zip");
                    unzip(address+name+".zip",address);
                    delet_zip(address+name+".zip");
                    download_file(base+name_inverted+".zip",address+name_inverted+".zip");
                    unzip(address+name_inverted+".zip",address);
                    delet_zip(address+name_inverted+".zip");
                }
            }
            download_file("https://raw.githubusercontent.com/zmousavian/StrongestPath/main/Files/hashes-"+specie+".txt",
                    root + "/files/hashes-"+specie+".txt");

        }
        catch(Throwable t) {
            state = "failed";
            System.out.println("Download failed in dbs");
        }

    }
    private boolean should_download(String file_name)
    {
        if(!hash_exists)
            return true;
        if(!current_hashes.containsKey(file_name))
            return true;

        return !current_hashes.get(file_name).equals(new_hashes.get(file_name));
    }

    public void unzip(String zipFile, String outputFolder){

        byte[] buffer = new byte[1024];

        try{


            //get the zip file content
            ZipInputStream zis =
                    new ZipInputStream(new FileInputStream(zipFile));
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while(ze!=null){

                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);

                System.out.println("file unzip : "+ newFile.getAbsoluteFile());

                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();

            System.out.println("Done");

        }catch(IOException ex){
            state = "failed";
            System.out.println("Download failed in unziping");
        }
    }

    private void delet_zip(String name) throws IOException {
        Files.deleteIfExists(Paths.get(name));
    }
}
