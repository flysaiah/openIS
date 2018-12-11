## **USAGE**

To run the GUI, simply run: `python3 interfaceGUI.py`. See our `INSTALL.txt` for library requirements/etc. To compile & run the main project, open in your favorite IDE and run the main file, which is `Driver.java`. You should run the GUI before this, however, to select the appropriate database / workload / algorithm, and you will obviously need a database of the selected type running in order for everything to work. Workloads are not stored here but can be found on our Drive: https://drive.google.com/drive/folders/1icOOMaK_QJbBWMNTGi82x8QHaV4eV1QY?usp=sharing

## **Major Functions**

There are 4 main things that a user can do with this project:

### **1. Testing an algorithm on a database & workload**

A user can use the GUI (defined in interfaceGUI.java) to configure the next execution of the program in 4 different ways:

* **Select Database**

    This will cause our *Driver* class (`Driver.java`) to create a connection via JDBC driver to the chosen database, for the duration of the execution of the program. **NOTE**: In order for this to work, the user obviously has to have a database server running of the appropriate type with the username/password/DBName matching what is listed in `Config.java`

* **Select Workload**

    This selects the workload that the tests will be using for the execution of the program. To explain in more detail, this will cause our *Driver* class to invoke the chosen  user-defined database helper class (see "Select User-defined Functions") that provides instructions on how to load the specified workload for this database.

* **Select User-defined Functions**

    This selects which database helper class the user wishes to use for the duration of the program's execution. This class extends the `BasicTemplate` class and provides interfaces for database-specific tasks, such as cost estimation and index creation. They also specify how to load data into that particular database, and the developer creating these interfaces can choose whether to provide workload-specific data loading mechanisms or general versions. For reference, we have provided examples (`SQLServerHelper.java` and `MySQLHelper.java`) which make it relatively easy to provide a default mechanism for loading workload data, but users can write this however they wish as long as it has the desired result of creating the necessary tables & loading the data relevant to their workloads.

* **Select Algorithm**

    The user can select an index selection algorithm to test with. This causes the *DatabaseManager* class to run the *generateIndexes* function that every index selection algorithm must provide.

Once these configurations are set, the user can compile and run our main program (`Driver.java`) which will load the data into the chosen database, run the chosen workload once without any indexes, then run the chosen index selection algorithm, create the specified indexes, then run the workload again and create a `results.csv` file that contains the execution time of both runs.

### **2. Add an Algorithm**

A user can use our GUI to add an algorithm to our pool of index selection algorithms. The GUI will prompt the user to select a file--this must be a `.java` file that extends from the class *BasicAlgorithmTemplate*, defined in our algorithms package. The user can leverage the database helper functions (building indexes, simulating cost) via the database helper class *userDefinedClass*, which is guaranteed to contain functions related to whichever database the algorithm is being run against.

### **3. Add a Database**

A user can use our GUI to add support for a new database, as well. The user will be prompted to enter the details of the database's connection string, as well as a username/password for the database.


### **4. Add User-defined Functions**

Any index selection algorithm can interface with the database via a database helper class that we term "User-defined functions." To add these, a user can add a database helper `.java` file that will extend our *BasicTemplate* class. This file must contain the following functions:
  1. `public void loadData(Connection myConn, String fileDir, String workload)`

    This function must create the tables and load the data associated with the given workload; see `MySQLHelper.java` for an example.

  2. `public String createSingleIndex(Connection myConn, String tableName, String indexCol)`

    This function must create a single index on the given table/column, and returns the index name.

  3. `public void dropSingleIndex(Connection myConn, String tableName, String indexNames)`

    This function must drop the index given as a parameter.

  4.  `abstract public long getCost(Connection myConn, HashSet<String> indexes, ArrayList<String> workload)`

    This function calculates a cost estimate of the queries in the given workload, given a set of indexes.

  5.  `abstract public void buildAllIndexes(Connection myConn, HashSet<String> indexes)`

    This function creates all of the indexes given in the current database.

  6.  `abstract public void deleteAllIndexes(Connection myConn)`

    This function deletes all indexes that currently exist in a database.

### **4. Add a Workload**

Finally, a user can add a workload (specific to database) via the GUI. The GUI will ask for a folder (our standard is to have name_of_folder = workload_name). This folder should contain a subfolder called `loaddata/`, which contains all data / SQL queries / etc needed to load the data associated with that workload into the database. It should also have another subfolder called `query/`, which should contain the queries associated with testing the given workload in a file called `1.sql`. Due to space limitations, we don't store these workloads in this repo, and instead are keeping them in a Google Drive repository, which is located here: https://drive.google.com/drive/folders/1icOOMaK_QJbBWMNTGi82x8QHaV4eV1QY?usp=sharing

