package eecs584.project.userfunctions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

public class SQLServerHelper extends BasicTemplate {
	
	public SQLServerHelper() {
        indexNames = new ArrayList<String>();
	}
    public void loadData(Connection myConn, String fileDir, String workload) {
    	// This needs to instantiate all the tables / data needed for testing
    	
    	switch (workload) {
    		// Allow custom loading of workload data as well as default mechanism
    		// No custom workloads at this time
    		default:
    			System.out.println("No custom loadData function available for this workload--using default mechanism.");
    			List<String> tableNames;
    	        try {
    	        	System.out.println(fileDir);
    	            tableNames = Files.readAllLines(Paths.get(fileDir + workload + ".txt"), StandardCharsets.UTF_8);
    	        } catch (IOException e) {
    	            System.out.println("ERROR: Invalid workload table names file");
    	            return;
    	        }
    	        try {
    	            if (createTables(myConn, fileDir, workload) == 0) {
    	                System.out.println("ERROR: Invalid workload create tables flie");
    	                return;
    	            }

    	            System.out.println("--Loading data into tables--");
    	            String query = "";
    	            Statement stmt = null;

    	            for (int i = 0; i < tableNames.size(); ++ i) {
    	                query = "BULK" + 
    	                " INSERT " + tableNames.get(i) + 
    	                "\nFROM " + "'" + fileDir + tableNames.get(i) + ".txt'\n" + 
    	                " WITH\n" + 
    	                " (" + 
    	                " FIELDTERMINATOR = '|'," + 
    	                " ROWTERMINATOR = '0x0a'" + 
    	                ");";
    	                
    	                stmt = myConn.createStatement();
    	                stmt.executeUpdate(query);
    	                stmt.close();
    	            }

    	            System.out.println("--Done loading data into tables--");
    	        } catch (SQLException e) {
    	            e.printStackTrace();
    	            System.out.println("ERROR: Loading data failed");
    	        } catch (FileNotFoundException e) {
    	            e.printStackTrace();
    	            System.out.println("ERROR: No table creation file found");
    	        }
    	}
        
    }

    public String createSingleIndex(Connection myConn, String tableName, String indexCol) {
        String query = String.format("CREATE INDEX index_%d ON %s(%s);", indexNum, tableName, indexCol);
        try {
            Statement stmt = myConn.createStatement();
            stmt.executeUpdate(query);
            indexNum += 1;
            stmt.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return "index_" + String.valueOf(indexNum - 1);
    }

    public void dropSingleIndex(Connection myConn, String tableName, String indexName) {
        String query = String.format("DROP INDEX %s on %s;", indexName, tableName);
        try {
            Statement stmt = myConn.createStatement();
            stmt.executeUpdate(query);;
            stmt.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int createTables(Connection myConn, String filePath, String workload) throws SQLException, FileNotFoundException {
        // Create tables specified by <dataDirectory>/<workload>/<workload>create.txt file, which holds SQL queries separated by semicolons
        System.out.println("--Creating tables in database--");
        Scanner read;

        read = new Scanner (new File(filePath + workload + "create.txt"));

        read.useDelimiter(";");
        String query;
        Statement stmt = myConn.createStatement();
        while (read.hasNext()) {
            query = read.next().trim();
            if (!query.isEmpty()) {
                stmt.execute(query);
            }
        }
        read.close();
        stmt.close();
        System.out.println("--Done creating tables in database--");
        return 1;
    }

    List<String> indexNames;
    private void runQueriesGivenWorkload(Connection myConn, ArrayList<String> workload) {
        try {
            Statement stmt = myConn.createStatement();
            for (String query : workload) {
                stmt.execute(query);
            }
        } catch(SQLException e) {
            e.printStackTrace();
            System.out.println("Cost Estimator Error: run query");
        }
    }

    public long getCost(Connection myConn, HashSet<String> indexes, ArrayList<String> workload) {
    	// Cost estimator -- right now, we're naively just running the query & keeping track of time
        buildAllIndexes(myConn, indexes);
        long startTime = System.currentTimeMillis();
        runQueriesGivenWorkload(myConn, workload);
        long cost =  System.currentTimeMillis() - startTime;
        deleteAllIndexes(myConn);
        return cost;
    }

    public void buildAllIndexes(Connection myConn, HashSet<String> indexes) {
        for (String index : indexes) {
            String arrOfString[] = index.split("\\|", 2);
            indexNames.add(arrOfString[0] + "|" + createSingleIndex(myConn, arrOfString[0], arrOfString[1]));
        }
    }

    public void deleteAllIndexes(Connection myConn) {
        for (String index : indexNames) {
            String arrOfString[] = index.split("\\|", 2);
            dropSingleIndex(myConn, arrOfString[0], arrOfString[1]);
        }
        indexNames.clear();
    }

}
