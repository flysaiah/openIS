package eecs584.project.indexselection;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

// NOTE: This will be updated by the GUI, so no need to manually make your changes here!

public class Config {
    public static String sqlServer;
    public static String dataPath;
    
    public static HashMap<String, String> dbConnectionStrings;
    public static HashMap<String, String> dbUsers;
    public static HashMap<String, String> dbPasswd;

    public Config() {
        dbConnectionStrings = new HashMap<String, String>();
        dbUsers = new HashMap<String, String>();
        dbPasswd = new HashMap<String, String>();

        dataPath = "/data/";
        loadDataFromConfig("../config.json");
    }

    private void loadDataFromConfig(String fileName) {
        String result = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
            result = sb.toString();
        } catch(Exception e) {
            e.printStackTrace();
        }

        JSONObject jobj = new JSONObject(result);
        jobj = jobj.getJSONObject("databases");
        Iterator<String> keys = jobj.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject databaseInfo = jobj.getJSONObject(key);
            dbUsers.put(key, databaseInfo.getString("UserName"));
            dbPasswd.put(key, databaseInfo.getString("Passwd"));
            dbConnectionStrings.put(key, databaseInfo.getString("URL"));
            System.out.println(dbUsers.get(key));
            System.out.println(dbPasswd.get(key));
            System.out.println(dbConnectionStrings.get(key));
        }
    }
}
