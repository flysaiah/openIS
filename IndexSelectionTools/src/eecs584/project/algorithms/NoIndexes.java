package eecs584.project.algorithms;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;

import eecs584.project.userfunctions.BasicTemplate;

public class NoIndexes extends BasicAlgTemplate {
	
	// Test algorithm

	@Override
	public HashSet<String> generateIndexes(BasicTemplate userDefinedClass, ArrayList<String> workload, Connection conn) throws Exception {
		return new HashSet<String>();
	}

}
