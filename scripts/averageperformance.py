#!/usr/bin/env python

import sys, os, glob
import csv
import simplestats
from optparse import OptionParser

def uniq(alist):   # remove duplicates, preserve order
	set = {}
	return [set.setdefault(e,e) for e in alist if e not in set]

def stripQuotes(s):
	return s.strip(" \n\t").strip('"')

def csvline(lst):
	return '"' + '","'.join(lst) + '"\n'

####################################################
## Main program starts here...
####################################################


def put(n, history, searchIndex, fitnessVal, options):
	if (n > options.minEvalCount) and ((n - options.minEvalCount) % options.increment == 0):
		if (not n in history):
			history[n] = {}		
		history[n][searchIndex] = fitnessVal	

def avgPerf(inFname, outFname, options):
	with open(inFname, "r") as fin:
		dr = csv.DictReader(fin, quoting=csv.QUOTE_ALL)	
		bestFitnessHistory = {}
		checkedFitnessHistory = {}

		lastEvalCount = None
		lastFitness = None
		lastCheckedFitness = None
		lastSearchIndex = None
		for row in dr:
			evalCount = int(row['evaluation'])
			searchIndex = int(row['search-number'])
			fitness = float(row['best-fitness-so-far'])
			if ('best-fitness-rechecked' in row):
				checkedFitness = float(row['best-fitness-rechecked'])
			else:
				checkedFitness = None
			
			if (lastSearchIndex != None and searchIndex != lastSearchIndex):
				for n in range(lastEvalCount+1,options.maxEvalCount+1):
					put(n,bestFitnessHistory,lastSearchIndex,lastFitness,options)
					if (lastCheckedFitness != None):
						put(n,checkedFitnessHistory,lastSearchIndex,lastCheckedFitness,options)
				lastEvalCount = None
				lastFitness = None
				lastCheckedFitness = None
			
			if (lastEvalCount != None):
				for n in range(lastEvalCount+1,min(evalCount,options.maxEvalCount+1)):
					put(n,bestFitnessHistory,searchIndex,lastFitness,options)
					if (lastCheckedFitness != None):
						put(n,checkedFitnessHistory,searchIndex,lastCheckedFitness,options)

			if (evalCount <= options.maxEvalCount):
				put(evalCount,bestFitnessHistory,searchIndex,fitness,options)
				if (checkedFitness != None):
					put(evalCount,checkedFitnessHistory,searchIndex,checkedFitness,options)
					
			lastFitness = fitness
			lastCheckedFitness = checkedFitness
			lastEvalCount = evalCount
			lastSearchIndex = searchIndex

		for n in range(lastEvalCount+1,options.maxEvalCount+1):
			put(n,bestFitnessHistory,lastSearchIndex,lastFitness,options)	
			if (lastCheckedFitness != None):
				put(n,checkedFitnessHistory,lastSearchIndex,lastCheckedFitness,options)
	
	with open(outFname, "w") as fout:
		out = csv.writer(fout, quoting=csv.QUOTE_ALL)
		headers = ["evaluations","num_searches","mean_fitness","stdev_fitness","stderr_fitness"]
		if len(checkedFitnessHistory) > 0:
			headers += ["mean_checked_fitness","stdev_checked_fitness","stderr_checked_fitness"]
		out.writerow(headers)
		
		for evalCount in sorted(bestFitnessHistory.keys()):
			fitnesses = bestFitnessHistory[evalCount].values()
			row = [ evalCount,
					len(fitnesses),
					simplestats.average(fitnesses),
					simplestats.stdev(fitnesses),
					simplestats.stdError(fitnesses)]
			if len(checkedFitnessHistory) > 0:
				cfitnesses = checkedFitnessHistory[evalCount].values()
				assert (len(cfitnesses) == len(fitnesses)), "Error: some 'rechecked-fitness' data wasn't in the input files, or something else is wrong."
				row += [ simplestats.average(cfitnesses),
						 simplestats.stdev(cfitnesses),
						 simplestats.stdError(cfitnesses) ]
			out.writerow(row)
		fout.flush()

def determineMaxCountFromData(inName):
	try:
		finalBest = inName.replace(".bestHistory.csv", ".finalBests.csv").replace(".objectiveFunctionHistory.csv", ".finalBests.csv")
		with open(finalBest,"r") as fin:
			dr = csv.DictReader(fin, quoting=csv.QUOTE_ALL)	
			return int(dr.next()['evaluation'])
	except:
		return None
		
if __name__ == '__main__':
	parser = OptionParser()
	parser.set_usage(parser.get_prog_name() + " input_file_or_wildcard_pattern")
	parser.set_description("""Takes BehaviorSearch CSV output files (either xxx.bestHistory.csv or 
xxx.objectiveFunctionHistory.csv) that contains data from multiple search runs, 
and processes them into a new xxx.performance.csv file which gives a record of 
'average fitness' as the search progresses.""")
	parser.add_option("--min", action="store", type="int", dest="minEvalCount", default=0, help="# of evaluations to start at")
	parser.add_option("--inc", action="store", type="int", dest="increment", default=1, help="increment for the # of evaluations")
	parser.add_option("--max", action="store", type="int", dest="maxEvalCount", help="highest number of evaluations to go up to.")

	options , filepatterns = parser.parse_args()
	if (len(filepatterns) == 0): # or not options.maxEvalCount):
		parser.print_help()
		sys.exit(0)

	filenames = []
	for fPat in filepatterns:
		filenames.extend(glob.glob(fPat))
	filenames = uniq(filenames)
	
	for inName in filenames:
		if (not ".bestHistory.csv" in inName) and (not ".objectiveFunctionHistory.csv" in inName):
			print "Warning: skipping file %s because it doesn't end in either bestHistory.csv or objectiveFunctionHistory.csv\n"%inName
		else:
			outName = inName.replace(".bestHistory.", ".").replace(".objectiveFunctionHistory.",".").replace(".csv",".performance.csv")
			print "Creating file %s"%outName 
			if (options.maxEvalCount == None):
				options.maxEvalCount = determineMaxCountFromData(inName)
			
			if (options.maxEvalCount == None): #even after attempting to auto-detect it.
				print "Warning: skipping file %s because the maximum # of evals could not be determined from the .finalBests.csv file.  You could also specify --max N manually. \n"%inName
			else:
				avgPerf(inName, outName, options)
	
