#!/usr/bin/env python

import glob
import os, sys
from optparse import OptionParser

def uniq(alist):   # remove duplicates, preserve order
	set = {}
	return [set.setdefault(e,e) for e in alist if e not in set]

def stripQuotes(s):
	return s.strip(" \n\t").strip('"')

if __name__ == '__main__':

	parser = OptionParser()
	parser.set_usage(parser.get_prog_name() + " input_file_or_wildcard_pattern")
	parser.set_description("""For each input file (should be .finalBest.csv or finalCheckedBest.csv) 
creates a new .singleBest that contains only the single row that had the best fitness. 
 Uses 'rechecked' if the 'best-fitness-rechecked' column is present in the file.""")
	parser.add_option("-m", "--minimize", action="store_true", dest="minimize", help="choose the line with the smallest (rather than largest) fitness value.")
	#parser.add_option("-r", "--rechecked", action="store_true", dest="rechecked", help="use the rechecked fitness values to choose the best.")
	
	options , filepatterns = parser.parse_args()
	if (len(filepatterns) == 0):
		parser.print_help()
		sys.exit(0)

	filenames = []
	for fPat in filepatterns:
		filenames.extend(glob.glob(fPat))
	filenames = uniq(filenames)
	
	for inName in filenames:
		if (not ".finalBests.csv" in inName) and (not ".finalCheckedBests.csv" in inName):
			print "Warning: skipping file %s because it doesn't end in either finalBests.csv or finalCheckedBests.csv\n"%inName
		else:
			outName = inName.replace(".finalBests.", ".").replace(".finalChecked.",".").replace(".csv",".singleBest.csv")
		
		with open(outName,"w") as fout:
			with open(inName, "r") as fin:
				headerLine = fin.readline()
				headers = [stripQuotes(x) for x in headerLine.split(",")]
				if ("best-fitness-rechecked" in headers):
					colIndex = headers.index("best-fitness-rechecked")
				elif ("best-fitness-so-far" in headers):
					colIndex = headers.index("best-fitness-so-far")
				else:
					raise Exception("Can't find a column header for the best fitness in file %s. Quitting."%inName )

				fout.write(headerLine)
				lines = []
				bestFitnesses = []
				for line in fin:
					lines.append(line)
					bestFitnesses.append(float(stripQuotes(line.split(',')[colIndex])))
				if (options.minimize):
					bestIndex = bestFitnesses.index(min(bestFitnesses))
				else:
					bestIndex = bestFitnesses.index(max(bestFitnesses))
				
				fout.write(lines[bestIndex])
		
		
