package eecs584.project.indexselection;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

import eecs584.project.algorithms.BasicAlgTemplate;
import eecs584.project.algorithms.MicrosoftIS;
import eecs584.project.userfunctions.BasicTemplate;

public class DatabaseManager {

    public static void buildIndex(Connection myConn, String filePath, BasicTemplate userDefinedClass, BasicAlgTemplate alg) throws Exception {
        // Build the index
    	ArrayList<String> workloadQueries = new ArrayList<String>();        
		try {
			Scanner s = new Scanner(new File(filePath + "/1.sql"));
			s.useDelimiter(";");
			while (s.hasNext()) {
				String query = s.next().trim();
				if (!query.isEmpty())
					workloadQueries.add(query);
			}
			s.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
        
    	HashSet<String> finalIndexes = alg.generateIndexes(userDefinedClass, workloadQueries, myConn);
    	userDefinedClass.buildAllIndexes(myConn, finalIndexes);
    }

    public static void runQuery(Connection myConn, String filePath) {
    	
    		System.out.println("--Executing tests--");

			Statement stmt = null;
			try {
				Scanner s = new Scanner(new File(filePath + "1.sql"));
				s.useDelimiter("/\\*[\\s\\S]*?\\*/|--[^\\r\\n]*|;");

				stmt = myConn.createStatement();
				while (s.hasNext()) {
					String query = s.next().trim();

					if (!query.isEmpty())
						stmt.execute(query);
				}
				s.close();
				stmt.close();
				System.out.println("--Done executing tests--");
			}
			catch(Exception e) {
				e.printStackTrace();
			}
    }
    

}
