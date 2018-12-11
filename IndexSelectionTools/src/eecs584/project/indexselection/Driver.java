package eecs584.project.indexselection;

import eecs584.project.algorithms.*;
import eecs584.project.userfunctions.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Scanner;

public class Driver {

	private static String workload;
	private static String database;
	private static Connection myConn;
	private static String result_file;

	private static String createAbsoluteDir(String fileDir, String database, String workload) {
		return System.getProperty("user.dir").replace("\\", "/") + fileDir + database + "/" + workload + "/";
	}

	public static void runQuery(Connection myConn){
		String dataPath = createAbsoluteDir(Config.dataPath, database, workload) + "query/";
		DatabaseManager.runQuery(myConn, dataPath);
	}

	public static void writeToCSV(String userClassName, String algName,
                                  double queryTime_DefaultIndex, double queryTime_AlgIndex) throws IOException

    {
		// Used to write results of our tests to a CSV
        File resultCSV = new File(result_file);
        StringBuilder sb = new StringBuilder();
        if (!resultCSV.exists()) {
            resultCSV.createNewFile();
            sb.append("algName,");
            sb.append("database,");
            sb.append("workload,");
            sb.append("queryTime_DefaultIndex(ms),");
            sb.append("queryTime_AlgIndex(ms)\n");
        }
        sb.append(algName + ",");
        sb.append(database + ",");
        sb.append(workload + ",");
        sb.append(String.valueOf(queryTime_DefaultIndex) + ",");
        sb.append(String.valueOf(queryTime_AlgIndex) + "\n");

        PrintWriter pw = new PrintWriter(new FileOutputStream(
                new File(result_file), true));

        pw.write(sb.toString());
        pw.close();

    }

    public static void main(String[] args) {
    	
    	// Main function -- shouldn't be any reason to change this

        Config config = new Config();
        //SPACIALMAKR, DO NOT CHANGE
		BasicTemplate userDefinedClass = new SQLServerHelper();
		BasicAlgTemplate alg = new MicrosoftIS();
		database = "sqlserver";
		workload = "tpch";
		String userClassName = "SQLServerHelper";
		String algName = "MicrosoftIS";
		result_file = "result.csv";
        try {
            double queryTime_DefaultIndex = 0, queryTime_AlgIndex = 0;

            myConn = Connector.getConnection(database);
            String dataPath = createAbsoluteDir(Config.dataPath, database, workload) + "loaddata/";
            userDefinedClass.loadData(myConn, dataPath, workload);

            long startTime = System.currentTimeMillis();
            runQuery(myConn);
            queryTime_DefaultIndex = System.currentTimeMillis() - startTime;

            dataPath = createAbsoluteDir(Config.dataPath, database, workload) + "query/";
            DatabaseManager.buildIndex(myConn, dataPath, userDefinedClass, alg);

            startTime = System.currentTimeMillis();
            runQuery(myConn);
            queryTime_AlgIndex = System.currentTimeMillis() - startTime;
            userDefinedClass.deleteAllIndexes(myConn);

            writeToCSV(userClassName, algName, queryTime_DefaultIndex, queryTime_AlgIndex);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
