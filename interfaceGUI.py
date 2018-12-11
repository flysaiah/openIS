import sys
import json
import os.path
from PyQt5.QtWidgets import *
from PyQt5.QtCore import Qt, pyqtSlot
from PyQt5.QtGui import QIcon
import PyQt5
from shutil import copyfile, copytree

class PopUp(PyQt5.QtWidgets.QDialog):
    
    def __init__(self):
        super().__init__()
        self.config_data = json.load(open('config.json'))
        self.initUI()
        
    
    def initUI(self):
        self.setWindowTitle("Add new database")
        self.center()
        self.resize(500, 150)
        self.show_dialog()
        self.show()
    
    def center(self):
        qr = self.frameGeometry()
        cp = QDesktopWidget().availableGeometry().center()
        qr.moveCenter(cp)
        self.move(qr.topLeft())

    def update_json_database(self):
        self.config_data["databases"][self.textbox_0.text()] = {
            "URL": self.textbox_1.text(), 
            "Workloads": [],
            "UserDefinedFunctions": [],
            "UserName": self.textbox_2.text(),
            "Passwd": self.textbox_3.text()
        }
        json.dump(self.config_data, open('config.json', 'w'))
        self.close()

    def show_dialog(self):
        self.label_0 = QLabel("Database Name:")
        self.textbox_0 = QLineEdit()

        self.label_1 = QLabel("Server URL:")
        self.textbox_1 = QLineEdit()

        self.label_2 = QLabel("User Name:")
        self.textbox_2 = QLineEdit()

        self.label_3 = QLabel("Password:")
        self.textbox_3 = QLineEdit()
        
        self.button_1 = QPushButton('Finished')
        self.button_1.clicked.connect(self.update_json_database)
        '''
        self.textbox.move(20, 20)
        self.textbox.resize(280, 40)
        '''
        self.grid_layout = QGridLayout()
        self.grid_layout.addWidget(self.label_0, 0, 0)
        self.grid_layout.addWidget(self.textbox_0, 0, 2)
        

        self.grid_layout.addWidget(self.label_1, 1, 0)
        self.grid_layout.addWidget(self.textbox_1, 1, 2)
        
        self.grid_layout.addWidget(self.label_2, 2, 0)
        self.grid_layout.addWidget(self.textbox_2, 2, 2)

        self.grid_layout.addWidget(self.label_3, 3, 0)
        self.grid_layout.addWidget(self.textbox_3, 3, 2)
        self.grid_layout.addWidget(self.button_1, 4, 1)

        self.setLayout(self.grid_layout)

class GUI(QMainWindow):
    def __init__(self):
        super().__init__()
        self.initUI()

    def initUI(self):
        self.setWindowTitle("OpenIS")
        self.config_data = json.load(open('config.json'))
        self.center()
        self.resize(500, 400)
        self.add_grid_layout()
        self.show()
        
    
    
    def center(self):
        qr = self.frameGeometry()
        cp = QDesktopWidget().availableGeometry().center()
        qr.moveCenter(cp)
        self.move(qr.topLeft())

    def add_grid_layout(self):
        
        self.w = None

        self.label_1 = QLabel("select database")
        self.label_2 = QLabel("select workload")
        self.label_3 = QLabel("select user defined function")
        self.label_4 = QLabel("select algorithms")

        self.button_1 = QPushButton('add new database')
        self.button_1.clicked.connect(self.add_database)
        self.button_2 = QPushButton('add new workload')
        self.button_2.clicked.connect(self.add_workload)
        self.button_3 = QPushButton('add new user defined function')
        self.button_3.clicked.connect(self.add_user_function)
        self.button_4 = QPushButton('add new algorithms')
        self.button_4.clicked.connect(self.add_algorithms)

        self.comboBox_1 = QComboBox(self)
        self.generate_combo_box(self.config_data["databases"], self.comboBox_1)
        self.comboBox_1.currentTextChanged.connect(self.combo_update)
        
        self.comboBox_2 = QComboBox(self)
        if self.comboBox_1.currentText() != "":
            print(self.comboBox_1.currentText())
            self.generate_combo_box(self.config_data["databases"][self.comboBox_1.currentText()]["Workloads"], self.comboBox_2)

        self.comboBox_3 = QComboBox(self)
        if self.comboBox_1.currentText() != "":
            self.generate_combo_box(self.config_data["databases"][self.comboBox_1.currentText()]["UserDefinedFunctions"], self.comboBox_3)

        self.comboBox_4 = QComboBox(self)
        self.generate_combo_box(self.config_data["algorithms"], self.comboBox_4)

        self.button_run = QPushButton('Set Parameters')
        self.button_run.clicked.connect(self.run_program)
        
        grid_layout = QGridLayout()
        grid_layout.addWidget(self.label_1, 1, 0)
        grid_layout.addWidget(self.comboBox_1, 1, 1)
        grid_layout.addWidget(self.button_1, 1, 3)

        grid_layout.addWidget(self.label_2, 2, 0)
        grid_layout.addWidget(self.comboBox_2, 2, 1)
        grid_layout.addWidget(self.button_2, 2, 3)

        grid_layout.addWidget(self.label_3, 3, 0)
        grid_layout.addWidget(self.comboBox_3, 3, 1)
        grid_layout.addWidget(self.button_3, 3, 3)

        grid_layout.addWidget(self.label_4, 4, 0)
        grid_layout.addWidget(self.comboBox_4, 4, 1)
        grid_layout.addWidget(self.button_4, 4, 3)

        grid_layout.addWidget(self.button_run, 5, 1)

        layout_widget = QWidget()
        layout_widget.setLayout(grid_layout)
        self.setCentralWidget(layout_widget)

    def combo_update(self):
        if self.comboBox_1.currentText() == '':
            return
        self.generate_combo_box(self.config_data["databases"][self.comboBox_1.currentText()]["Workloads"], self.comboBox_2)
        self.generate_combo_box(self.config_data["databases"][self.comboBox_1.currentText()]["UserDefinedFunctions"], self.comboBox_3)
        
    @staticmethod
    def generate_combo_box(data, combo):
        combo.clear()
        for key in data:
            combo.addItem(key)
    
    def update_first_3_combo(self):        
        self.generate_combo_box(self.config_data["databases"], self.comboBox_1)
        self.combo_update()

    def add_database(self):
        self.w = PopUp()
        self.w.exec_()
        self.config_data = json.load(open('config.json'))
        self.update_first_3_combo()

    def add_workload(self):
        options = QFileDialog.Options()
        options |= QFileDialog.DontUseNativeDialog
        dir_name = QFileDialog.getExistingDirectory(self, "select workload directory")

        if dir_name == "":
            return
        workload_name = dir_name.split('/')[-1].split('\\')[-1]
        new_dir_name = './IndexSelectionTools/data/{}/{}'.format(self.comboBox_1.currentText(), workload_name)

        if not os.path.isdir(new_dir_name):
            copytree(dir_name, './IndexSelectionTools/data/{}/{}'.format(self.comboBox_1.currentText(), workload_name))

        if workload_name not in self.config_data["databases"][self.comboBox_1.currentText()]["Workloads"]:
            self.config_data["databases"][self.comboBox_1.currentText()]["Workloads"].append(workload_name)
        
        json.dump(self.config_data, open('config.json', 'w'))
        self.generate_combo_box(self.config_data["databases"][self.comboBox_1.currentText()]["Workloads"], self.comboBox_2)
        
    def add_user_function(self):
        options = QFileDialog.Options()
        options |= QFileDialog.DontUseNativeDialog
        file_name, _ = QFileDialog.getOpenFileName(self, "select user function file", "", "All Files (*);;Java Files (*.java)", options=options)
        if file_name == "":
            return
        class_name = file_name.split('/')[-1].split('\\')[-1].split('.')[0]
        
        new_file_name = './IndexSelectionTools/src/eecs584/project/userfunctions/{}.java'.format(class_name)
        if not os.path.isfile(new_file_name):
            copyfile(file_name, new_file_name)
        
        if class_name not in self.config_data["databases"][self.comboBox_1.currentText()]["UserDefinedFunctions"]:
            self.config_data["databases"][self.comboBox_1.currentText()]["UserDefinedFunctions"].append(class_name)
            json.dump(self.config_data, open('config.json', 'w'))
            self.generate_combo_box(self.config_data["databases"][self.comboBox_1.currentText()]["UserDefinedFunctions"], self.comboBox_3)
        

    def add_algorithms(self):
        options = QFileDialog.Options()
        options |= QFileDialog.DontUseNativeDialog
        file_name, _ = QFileDialog.getOpenFileName(self, "select algorithm file", "", "All Files (*);;Java Files (*.java)", options=options)
        if file_name == "":
            return
        class_name = file_name.split('/')[-1].split('\\')[-1].split('.')[0]
        
        new_file_name = './IndexSelectionTools/src/eecs584/project/algorithms/{}.java'.format(class_name)
        
        if not os.path.isfile(new_file_name):
            copyfile(file_name, new_file_name)
        
        if class_name not in self.config_data["algorithms"]:
            self.config_data["algorithms"].append(class_name)
            json.dump(self.config_data, open('config.json', 'w'))
            self.generate_combo_box(self.config_data["algorithms"], self.comboBox_4)


    def run_program(self):
        replace_file(self.comboBox_3.currentText(), self.comboBox_4.currentText(), self.comboBox_1.currentText(), self.comboBox_2.currentText())
    
def replace_file(user_class_name, alg_class_name, database, workload):
    f = open("./IndexSelectionTools/src/eecs584/project/indexselection/Driver.java", "r")
    lines = f.readlines()
    for i, line in enumerate(lines):
        if line.strip() == "//SPACIALMAKR, DO NOT CHANGE":
            lines[i + 1] = "\t\tBasicTemplate userDefinedClass = new {}();\n".format(user_class_name)
            lines[i + 2] = "\t\tBasicAlgTemplate alg = new {}();\n".format(alg_class_name)
            lines[i + 3] = "\t\tdatabase = \"{}\";\n".format(database)
            lines[i + 4] = "\t\tworkload = \"{}\";\n".format(workload)
            lines[i + 5] = "\t\tString userClassName = \"{}\";\n".format(user_class_name)
            lines[i + 6] = "\t\tString algName = \"{}\";\n".format(alg_class_name)
            break
    f.close()
    f = open("./IndexSelectionTools/src/eecs584/project/indexselection/Driver.java", "w")
    f.writelines(lines)
    f.close()
    


if __name__ == "__main__":
    app = QApplication(sys.argv)
    gui = GUI()
    gui.show()

    sys.exit(app.exec_())

