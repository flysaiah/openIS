package eecs584.project.algorithms;

import eecs584.project.userfunctions.BasicTemplate;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;

public abstract class BasicAlgTemplate {
    abstract public HashSet<String> generateIndexes(BasicTemplate userDefinedClass, ArrayList<String> workload, Connection conn) throws Exception;
}
