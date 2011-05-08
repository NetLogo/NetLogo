############################
# Module:   modellibrary2csv.py
# Authors:  Daniel Kornhauser <dkor@media.mit.edu > 
#            Josh Unterman <junterman@ccl.northwestern.edu>
# Date:     2006/02/03
# Version:  0.1
#
# Description: This script walks through the models library directory 
#                and creates a CSV file which can be imported to SQL 
#                with phpmyadmin.
#

# TODO :
#       * Verify path to make sure it works on cvs. 
#       * Debug /n /n/r issue ...
#       * Normalize stuff:
#           Versions
#           Categories
#           Related Models
#       * Finish commenting.


import sys
import string
import csv
import re
import os
   
class Model:
    """ Encapsulates a NetLogo model file  and parses and saves to a csv file """
    # Separator string used in nlogo files to divide each secion
    SEPARATOR = "@#$#@#$#@\r\n" 

    # These atributes contain the nlogo file name and directory
    name = None
    category = None
    unverified = None
    
    # These atributes contain the nlogo file divided by sections
    source = None
    widgets = None
    info = None
    shapes = None
    version = None
    preview_commands = None
    aggregate = None
    experiments = None
    client = None
    
    # array that will contain all the text in the nlogo file
    file_array = []
    
    def __init__(self, root, fname):
        self.name = fname.split(".")[0]
        last_dir_name = string.split(root,"/")[-1]
        if last_dir_name == 'Unverified':
            self.category = string.split(root,"/")[-2]
            self.unverified = True
        else:
            self.category = string.split(root,"/")[-1]
            self.unverified = False
        fullname = os.path.join(root, fname)
        file = open(fullname, "r")
        # read nlogo file
        self.file_array = file.readlines()

        self.parse()

    def parse(self):
        """ This methods goes thru an nlogo file array and 
        finds each section and assigns it to an atribute.
        """
        SOURCE_INDEX = 0
        WIDGETS_INDEX = 1
        INFO_INDEX = 2
        SHAPES_INDEX = 3
        VERSION_INDEX = 4
        PREVEW_COMMANDS_INDEX = 5
        AGGREGATE_INDEX = 6
        EXPERIMENTS_INDEX = 7
        CLIENT_INDEX = 8   
        
        separator_indices_list = [ ]
        for i in range(len(self.file_array)):
            if self.file_array[i] == self.SEPARATOR :
                separator_indices_list.append(i)
        self.source =  FileSection(self.file_array[:separator_indices_list[SOURCE_INDEX]])
        self.widgets = FileSection(self.file_array[separator_indices_list[SOURCE_INDEX]+1:separator_indices_list[WIDGETS_INDEX ]])
        self.info =    InfoSection(self.file_array[separator_indices_list[WIDGETS_INDEX]+1:separator_indices_list[INFO_INDEX]])
        self.shapes =  FileSection(self.file_array[separator_indices_list[INFO_INDEX]+1:separator_indices_list[SHAPES_INDEX]]) 
        self.version = FileSection(self.file_array[separator_indices_list[SHAPES_INDEX]+1:separator_indices_list[VERSION_INDEX]]) 
        self.preview_commands = FileSection(self.file_array[separator_indices_list[VERSION_INDEX]+1:separator_indices_list[PREVEW_COMMANDS_INDEX]]) 
        self.aggregate =   FileSection(self.file_array[separator_indices_list[PREVEW_COMMANDS_INDEX]+1:separator_indices_list[AGGREGATE_INDEX]]) 
        self.experiments = FileSection(self.file_array[separator_indices_list[AGGREGATE_INDEX]+1:separator_indices_list[EXPERIMENTS_INDEX]]) 
        if len(separator_indices_list)> 8:
            self.client =      FileSection(self.file_array[separator_indices_list[EXPERIMENTS_INDEX]+1:separator_indices_list[CLIENT_INDEX]]) 
        else:
            self.client = FileSection([])
            
    def append_csv(self):
        """ writes the Models atrributes to a csv file readable by myadminphp """
        csv.register_dialect("TSR", TSR)
        output_file = open(output_file_name, "a")
        writer = csv.writer(output_file, "TSR", quoting=csv.QUOTE_ALL) # csv.QUOTE_ALL option is necesary
        data =  (
                 self.name,
                 self.category,
                 self.unverified,
                 self.source.section_str,
                 self.widgets.section_str,
                 #self.info.section_str,
                 self.info.subsection_str("WHAT IS IT?"),
                 self.info.subsection_str("HOW IT WORKS"),
                 self.info.subsection_str("HOW TO USE IT"), 
                 self.info.subsection_str("THINGS TO NOTICE"), 
                 self.info.subsection_str("THINGS TO TRY"), 
                 self.info.subsection_str("NETLOGO FEATURES"), 
                 self.info.subsection_str("RELATED MODELS"),
                 self.info.subsection_str("CREDITS AND REFERENCES"),
                 self.shapes.section_str,
                 self.version.section_str,
                 self.preview_commands.section_str,
                 self.aggregate.section_str,
                 self.experiments.section_str,        
                 self.client.section_str
                )
        writer.writerow(data)

                   
class TSR(csv.excel):
    """ csv dialect options to make the cvs readable by myphdadmin
    The class is named TSR because as in name of the company in the office space movie
    In myphpadmin you need the following settings in the Import options:
    Format of import file
    [X] CSV                     Fields terminated by [;]
    [] CSv using LOAD_DATA     Fields enclosed by ["]
    [ ]  SQL                    Fields escaped by [\]
                                Fields terminated by [~]
    """    
    # like excel, but uses semicolons
    lineterminator = "~"
    delimiter = ';'
    doublequote = False;


class FileSection:
    """This class can just contain just the string attribute with escaped control characters """
    section_str =  ""
    section_array = None
    
    def __init__(self, section_array):
        # creates one string replacing the array separators by \n
        self.section_str = string.join(section_array,'\n')
        self.section_array = section_array
        
        # escapes the characters used as control or separator characters in the csv files
        self.section_str = self.section_str.replace('\\','\\\\')
        self.section_str = self.section_str.replace(';','\\;')
        self.section_str = self.section_str.replace('"','\\"')
        self.section_str = self.section_str.replace('~','\\~')
        if self.section_str == "":
            self.section_str = ' '
            
            
class InfoSection (FileSection) :
    """ Inherits the sections string attribute from FileSection but futher subdivides 
    the string into subsections in the case of the Infotab"""

    infotabSubsectionDic = { "WHAT IS IT?": None, 
                              "HOW IT WORKS": None, 
                              "HOW TO USE IT": None, 
                              "THINGS TO NOTICE": None, 
                              "THINGS TO TRY": None, 
                              "EXTENDING THE MODEL": None, 
                              "NETLOGO FEATURES": None, 
                              "RELATED MODELS": None, 
                              "CREDITS AND REFERENCES": None  }
    
    def __init__(self, section_array):
        FileSection.__init__(self, section_array)
        for subsection in self.infotabSubsectionDic.keys():
            for line_number in range(len(section_array)):
                if section_array[line_number].strip("\r\n ") == subsection:
                    self.infotabSubsectionDic [subsection] = line_number


    def subsection_str(self, subsection_separator):
        start_line = self.infotabSubsectionDic[subsection_separator]
        if start_line:
                infotabSubsectionList = self.infotabSubsectionDic.values()
                infotabSubsectionList.sort()
                start_line_index =  infotabSubsectionList.index(start_line)
                if start_line_index == len(infotabSubsectionList) - 1:
                    subsection_array = self.section_array[start_line+2:]
                else:
                    end_line = infotabSubsectionList[start_line_index + 1]
                    subsection_array = self.section_array[start_line+2:end_line]
                return_string = string.join(subsection_array,'\n')
                # escapes the characters used as control or separator characters in the csv files
                return_string = return_string.replace('\\','\\\\')
                return_string = return_string.replace(';','\\;')
                return_string = return_string.replace('"','\\"')
                return_string = return_string.replace('~','\\~')
                if return_string == "":
                    return_string = ' '
                return return_string
        else:
            return ' '



modelDir = "..\\models\\Sample Models"
output_file_name = "testeroo2.csv"
if __name__ == "__main__":
    # clear the file away before we append to it
    output_file = open(output_file_name, "w")
    output_file.close()

    for root, dirs, files in os.walk(modelDir):
        for fname in files:
            if re.compile(r'\.nlogo').search(fname):
                m = Model(root, fname)
                m.parse()
                m.append_csv()
