package eecs584.project.userfunctions;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;

abstract public class BasicTemplate {
    public int indexNum = 0;

    abstract public void loadData(Connection myConn, String fileDir, String workload);

    abstract public String createSingleIndex(Connection myConn, String tableName, String indexCol);

    abstract public void dropSingleIndex(Connection myConn, String tableName, String indexName);

    abstract public long getCost(Connection myConn, HashSet<String> indexes, ArrayList<String> workload);
    
    abstract public void buildAllIndexes(Connection myConn, HashSet<String> indexes);
    
    abstract public void deleteAllIndexes(Connection myConn);
}
