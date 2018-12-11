package eecs584.project.algorithms;
import eecs584.project.userfunctions.BasicTemplate;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class MicrosoftIS extends BasicAlgTemplate{
	private class CandidateIndexSelector {
		private ConfigurationEnumerator configurationEnumerator;

		public CandidateIndexSelector() {
			this.configurationEnumerator = new ConfigurationEnumerator();
		}

		private HashSet<String> getStartingCandidateIndexes(String query, Connection conn) throws SQLException {
			// Get all possible indexes from query & return in set form

			// Start by getting list of all column names
			HashSet<String> columnNames = new HashSet<String>();
			HashMap<String, ArrayList<String>> possibleIndexes = new HashMap<String, ArrayList<String>>();
			String getAllQuery = "select TABLE_NAME, COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS";
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(getAllQuery);
			while (rs.next()) {
				String columnName = rs.getString("COLUMN_NAME").toLowerCase();
				String tableName = rs.getString("TABLE_NAME").toLowerCase();
				columnNames.add(columnName);
				if (possibleIndexes.containsKey(columnName)) {
					if (!possibleIndexes.get(columnName).contains(tableName)) {
						possibleIndexes.get(columnName).add(tableName);
					}
				} else {
					ArrayList<String> tmp = new ArrayList<String>();
					tmp.add(tableName);
					possibleIndexes.put(columnName, tmp);
				}
			}

			HashSet<String> res = new HashSet<String>();
			// Strategy: Wait until we see the WHERE, ORDER BY or GROUP keyword, then grab every column name we see & keep it
			boolean sentinelFound = false;
			String[] queryWords = query.split("\\s+");
			for (int i = 0; i < queryWords.length; i++) {
				String word = queryWords[i].trim().toLowerCase().replaceAll(",", "").replaceAll(";", "");
				if (sentinelFound && columnNames.contains(word)) {
					for (String tableName : possibleIndexes.get(word)) {
						res.add(tableName + "|" + word);
					}
				}
				if (!sentinelFound && (word.equals("where")|| word.equals("order") || word.equals("group"))) {
					sentinelFound = true;
					// ORDER BY & GROUP BY are two words so need to skip a word in those cases
					if (word.equals("order") || word.equals("group")) {
						i++;
					}
				}
			}
			return res;
		}

		public HashSet<String> getCandidateIndexSet(ArrayList<String> workload, Connection conn, BasicTemplate userDefinedClass, int k, int c) throws Exception {
			HashSet<String> res = new HashSet<String>();
			// Step 1: Separate workload into N individual queries (already done by parameter)
			// Step 2: For each workload (query), feed all possible indexes for that query into Configuration Enumeration Module
			for (int i = 0; i < workload.size() ; i++) {
				String query = workload.get(i);
				HashSet<String> candidateIndexes = this.getStartingCandidateIndexes(query, conn);
//				System.out.println("Starting configuration for query " + String.valueOf(i) + ": " + candidateIndexes.toString());
				ArrayList<String> tmp = new ArrayList<String>();
				tmp.add(query);
				HashSet<String> refinedIndexes = this.configurationEnumerator.getBestConfiguration(conn, userDefinedClass, candidateIndexes, tmp, k, c);
//				System.out.println("Best configuration for query " + String.valueOf(i) + ": " + refinedIndexes.toString());
				// Step 3: Candidate Index set = Union of all results from #2
				res.addAll(refinedIndexes);
			}
			return res;
		}

	}

	private class ConfigurationEnumerator {

		private String getBestIndex(Connection conn, BasicTemplate userDefinedClass, HashSet<String> currentConfig, ArrayList<String> workload, HashSet<String> candidateIndexes, int numColumns) throws Exception {
			String res = null;
			long minCost = userDefinedClass.getCost(conn, currentConfig, workload);
			// Iterate over all combinations of (config U new-index) and choose the one with lowest cost
			// Return null if no indexes cause minCost to change
			if (numColumns == 1) {
				for (String newIndex : candidateIndexes) {
					HashSet<String> tmp = new HashSet<String>(currentConfig);
					tmp.add(newIndex);
					long cost = userDefinedClass.getCost(conn, tmp, workload);
					if (cost < minCost) {
						minCost = cost;
						res = newIndex;
					}
				}
			} else if (numColumns == 2) {
				// For multi-column indexes, best index starts with going through each currentConfig index and trying to add additional columns
				for (String existingIndex : currentConfig) {
					for (String newPartialIndex : candidateIndexes) {
						// Only works if they're from the same table
						if (existingIndex.split("|")[0].equals(newPartialIndex.split("|")[0])) {
							String newIndex = existingIndex + "," + newPartialIndex.split("|")[1];
							HashSet<String> tmp = new HashSet<String>(currentConfig);
							tmp.add(newIndex);
							long cost = userDefinedClass.getCost(conn, tmp, workload);
							if (cost < minCost) {
								minCost = cost;
								res = newIndex;
							}
						}
					}
				}
			} else {
				// TODO: Right now only support 1- or 2-column indexes; but if more are desired then that case can be added here
				throw new Exception("Only support 1- or 2-column indexes is currently available for this algorithm");
			}

			return res;
		}

		// TODO: This function assumes m = 0, could possibly make this a parameter
		public HashSet<String> getBestConfiguration(Connection conn, BasicTemplate userDefinedClass, HashSet<String> candidateIndexes, ArrayList<String> workload, int k, int c) throws Exception {
			// Get best configuration of candidate indexes
			HashSet<String> optimalConfiguration = new HashSet<String>();
			for (int i = 1; i <= c; i++) {
				// Step 1: Start by assuming we have optimal configuration of size m (= 0)
				HashSet<String> tmp = new HashSet<String>();
				// Step 2: While length(OC) < k, greedily add best index to configuration
				while (tmp.size() < k) {
					String bestIndex = this.getBestIndex(conn, userDefinedClass, tmp, workload, candidateIndexes, i);
					if (bestIndex == null) {
						// There is no best index -- adding any more indexes actually worsens our performance, so stop here
						break;
					} else {
						tmp.add(bestIndex);
					}
				}
				optimalConfiguration.addAll(tmp);
			}
			return optimalConfiguration;
		}
	}

	private CandidateIndexSelector candidateIndexSelector;
	private ConfigurationEnumerator configurationEnumerator;

	public MicrosoftIS() {
		this.candidateIndexSelector = new CandidateIndexSelector();
		this.configurationEnumerator = new ConfigurationEnumerator();
	}
	public HashSet<String> generateIndexes(BasicTemplate userDefinedClass, ArrayList<String> workload, Connection conn) throws Exception {
		int k = 2, c = 1;
		HashSet<String> candidateIndexSet = this.candidateIndexSelector.getCandidateIndexSet(workload, conn, userDefinedClass, k, c);
		HashSet<String> finalIndexes = this.configurationEnumerator.getBestConfiguration(conn, userDefinedClass, candidateIndexSet, workload, k, c);
		return finalIndexes;
	}
}
