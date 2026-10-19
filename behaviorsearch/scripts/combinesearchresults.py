#!/usr/bin/env python

import sys, os, fnmatch, glob
import xml.dom.minidom
from optparse import OptionParser


def uniq(alist):   # remove duplicates, preserve order
	set = {}
	return [set.setdefault(e,e) for e in alist if e not in set]

def stripQuotes(s):
	return s.strip(" \n\t").strip('"')

def chooseCombinedFileName(filenames, prefix="", stripEndNumbers=True):
	minLen = min([len(s) for s in filenames])
	start = ""
	for i in range(minLen):
		if (len(set([s[i] for s in filenames])) != 1):
			break;
		start += filenames[0][i]
	if (stripEndNumbers):
		start = start.rstrip("0123456789")
		start = start.rstrip("_")
	if (start == ""):
		raise Exception("No common starting filename stem for " + filenames)
	ending = ""
	for i in range(minLen):
		if (len(set([s[-1 - i] for s in filenames])) != 1):
			break;
		ending = filenames[0][-1 - i] + ending
	fname = start + ending
	dirname,filename = os.path.split(fname)
	if (dirname == ""):
		return prefix + filename
	else:
		return dirname + os.path.sep + prefix + filename

def combine(filenames, preserveNumbering, deleteInputFiles, outFile):
	firstFile = True
	renumberIndex = -1
	
	for filename in filenames:
		inputfile = file(filename, "r")
		firstLine = inputfile.readline()
		if (firstFile):
			outFile.write(firstLine)
			firstFile = False
		lastSearchIndex = None
		for line in inputfile:
			items = line.split(",")
			searchIndex = items[0]
			if (searchIndex != lastSearchIndex):
				renumberIndex = renumberIndex + 1
				lastSearchIndex = searchIndex
			if (not preserveNumbering):
				items[0]='"' + str(renumberIndex) + '"'
			newLine = ",".join(items)
			if newLine[-1] != '\n':
				newLine = newLine + '\n'
			outFile.write(newLine)
	outFile.flush()
	if (deleteInputFiles):
		for filename in filenames:
			os.remove(filename)  

def autosort_and_combine(filenames,preserveNumbering, deleteInputFiles):
	oldLen = len(filenames)
	filenames = fnmatch.filter(filenames, "*.searchConfig.xml")
	if (oldLen != len(filenames)):
		print "Warning: input files not named xxxx.searchConfig.xml are being ignored."
	nameMap = {}
	for fname in filenames:
		#with open(fname,"r") as f:
			#config = f.read()
			#if config in nameMap:
				#nameMap[config].append(fname)
			#else:
				#nameMap[config] = [ fname ]
		config = xml.dom.minidom.parse(fname).toxml().replace(" ", "")
		if config in nameMap:
			nameMap[config].append(fname)
		else:
			nameMap[config] = [ fname ]
	for flist in nameMap.values():
		if (len(flist) == 1):
			print "Skipping %s, only 1 source file."%flist[0]
			continue
		combinedFileName = chooseCombinedFileName(flist)

		for ext in [".bestHistory.csv", ".finalBests.csv", ".finalCheckedBests.csv", ".modelRunHistory.csv", ".objectiveFunctionHistory.csv"]:
			outName = combinedFileName.replace(".searchConfig.xml",ext)
			inFileNames = [s.replace(".searchConfig.xml",ext) for s in flist]
			if (all(os.path.exists(s) for s in inFileNames)):
				print "Creating %s from %s source files."%(outName, len(inFileNames))
				with open(outName, "w") as fout:
					combine(inFileNames, preserveNumbering, deleteInputFiles, fout)
			else:
				print "Warning: not creating %s (source .CSV missing)"%outName
				
		#make a xxx.searchConfig.xml file to match the combined file
		with open(flist[0], "r") as f:
			fullText = f.read()
		with open(combinedFileName, "w") as fout:
			fout.write(fullText)

		if (deleteInputFiles):
			#delete all the input searchConfig.xml files.
			for f in flist:
				os.remove(f)

####################################################
## Main program starts here...
####################################################

def main():
	parser = OptionParser()
	parser.set_usage(parser.get_prog_name() + " -a exp1.searchConfig.xml exp2.searchConfig\n" 
				+ " OR " + parser.get_prog_name() + " -m exp1.YYY.csv exp2.YYY.csv [...]")
	parser.set_description("""Combines BehaviorSearch .csv files (that were created using the same search configuration.)

In -a (auto mode), it will use the *.searchConfig.xml files you specify to find all of the matching CSV search results files, and combine them
into a new file, named based on the common filename stem of the combined files. (i.e. the files: xxxx_00.yyy.csv, xxxx_01.yyy.csv,  => xxxx.yyy.csv)

In -m (manual mode), only those CSV files that you manually specify will be combined, and the results will go to stdout.
(Note that you can specify wildcards, such as "data*.xml") 
""")
	parser.add_option("-m", "--manual", action="store_true", dest="manual", help="manual mode")
	parser.add_option("-a", "--autosort", action="store_true", dest="autosort", help="(auto-sort mode) use XXX.searchConfig.xml files to automatically choose which CSV files should be combined.")
	parser.add_option("-p", "--preserve", action="store_true", dest="preserve", help="keep the original search number indexes, instead of renumbering consecutively.")
	parser.add_option("-d", "--delete", action="store_true", dest="delete", help="delete the input files, after combining")

	options , filepatterns = parser.parse_args()
	if (options.manual == options.autosort):
		print "ERROR: You must specify EITHER -m (manual) or -a (autosort) mode."
		print
		parser.print_help()
		sys.exit(0)

	if (len(filepatterns) == 0):
		parser.print_help()
		sys.exit(0)
		
	filenames = []
	for fPat in filepatterns:
		filenames.extend(glob.glob(fPat))
	
	filenames = uniq(filenames)
	
	if (len(filenames) < 1):
		parser.print_help()
		sys.exit(1)

	if (options.autosort):
		autosort_and_combine(filenames, options.preserve, options.delete)
	else: # (options.manual == True)
		combine(filenames, options.preserve, options.delete, sys.stdout)
		

if __name__ == '__main__':
	main()
	
