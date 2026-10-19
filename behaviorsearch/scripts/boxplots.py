#!/usr/bin/env python

import glob
from optparse import OptionParser
from pylab import *

def uniq(alist):   # remove duplicates, preserve order
	set = {}
	return [set.setdefault(e,e) for e in alist if e not in set]

def stripQuotes(s):
	return s.strip(" \n\t").strip('"')


# quick&dirty enum for python...
class PlotType:
	NA=-1
	CONSTANT=0
	WHISKER=1
	BAR=2


def getPlotType(paramName, dat):
	## TODO: Intelligently detect plot type from the data, 
	## and/or from the searchConfig.xml file
	items = dat[paramName]
	if (is_numlike(items)):
		return PlotType.WHISKER
	else:
		return PlotType.NA

	#if len(set(items)) == 1:
	#	return PlotType.CONSTANT
	#else if len(set(items)) < 5:
	#	return PlotType.BAR
	#return PlotType.
	

def plotFromFile(fname, graphTitle, options):
	# first, read the real column header names from the file,
	# (csv2rec mangles them)and check which ones are params (marked with *)
	with open(fname,"r") as fin:
		headerLine = fin.readline()
		headerNames = [stripQuotes(s).strip() for s in headerLine.split(",")]
		modelParamIndices = [ i for i in range(len(headerNames)) if (headerNames[i][-1] == '*')]
		modelParams = [ s[0:-1] for s in headerNames if (s[-1] == '*')]

	dat = csv2rec(fname)
	paramColumnNames = [dat.dtype.names[i] for i in modelParamIndices]
	originalNameMap = {}
	for i in range(len(paramColumnNames)):
		originalNameMap[paramColumnNames[i]] = modelParams[i]
	
	plotParams = [ x for x in paramColumnNames if getPlotType(x,dat) != PlotType.NA ]

	#HACK
	#plotParams = [ x for x in plotParams if ("strat" in x) or ("seeding" in x) ]

	numSubPlots = len(plotParams)	

	figure(figsize=(8,0.6*numSubPlots))
	subplots_adjust(hspace=0.8, left=0.28, right=0.95, top=0.92)
	subplot(numSubPlots,1,1)
	suptitle(graphTitle, fontsize=15)
	#Note at the bottom
	#figtext(0.05,0.02,"[Each plot represents %s data points]"%len(dat), ha='left', fontstyle='italic')
	#axislims = [(255,257),(0,100),(3,5),(0,2),(0,1.0),(-1,1),(0,64)] #linguistics?
	axislims = [(0,1),(0,2),(-1,1),(-1,1),(-1,1),(-1,1),(-1,1),(-1,1),(-1,1),(-1,1),(-1,1),(-1,1),(-1,1),(-1,1),(-1,1),(-1,1),(-1,1),(-1,1),(-1,1),(-1,1),(-1,1),(-1,1),(-1,1)]
	
	for i in range(numSubPlots):
		param = plotParams[i]
		plotType = getPlotType(param,dat)
		if (plotType == PlotType.WHISKER):
			subplot(numSubPlots,1,(i+1))
			boxplot([dat[param]],0,'bx',0,hold=False,widths=0.4)
			yticks([1],[originalNameMap[param]])
			#axis(xmin=0,xmax=1)
			axis(xmin=axislims[i][0],xmax=axislims[i][1])
			
			#hack
			#if (originalNameMap[param] == "strat2-probability"):
			#	axis(xmin=0,xmax=0.5)
	
	xlabel("parameter range", fontsize=14)
	
	outFileName = fname.replace(".finalBests.csv",".boxplot." + options.filetype)
	outFileName = outFileName.replace(".finalCheckedBests.csv",".boxplot." + options.filetype)
	if (not ".boxplot." in outFileName):
		outFileName = outFileName.replace(".csv","." + options.filetype)
	
	savefig(outFileName,dpi=options.dpi)
	if (options.show):
		show()


def main(options, args):
	if (len(args) < 1):
		parser.print_help()
		sys.exit(1)
	if (options.filetype == None):
		options.filetype = "pdf"
		
	filenames = []
	for fPat in args:
		filenames.extend(glob.glob(fPat))
	filenames = uniq(filenames)

	if (len(filenames) < 1):
		print "input files not found."
		sys.exit(1)
		
	for f in filenames:
		graphTitle = options.title
		if (graphTitle == None):
			graphTitle = f.replace(".finalBests.csv","").replace(".finalCheckedBests.csv","")
		plotFromFile(f, graphTitle, options)

	

if __name__ == '__main__':
	parser = OptionParser()
	parser.set_usage(parser.get_prog_name() + " file1.finalBests.csv ...")
	parser.set_description("""Takes xxx.finalBests.csv or xxx.finalCheckedBests.csv files and 
for each file creates a corresponding xxx.finalBests.{png,pdf,eps,svg} boxplot of the parameter settings
(Note filename wildcards are allowed for input files.)""")
	parser.add_option("-o", action="store", type="choice", choices=["png","pdf","eps","svg"], dest="filetype", help="type of output file to be created")
	parser.add_option("--dpi", action="store", type="int", dest="dpi", help="DPI to use when exporting graphics (applicable to raster formats, like PNG)")
	parser.add_option("--title", action="store", dest="title", help="title for all graphs created")
	parser.add_option("-s", "--show", action="store_true", dest="show", help="display the created plots")
	
	options , args = parser.parse_args()
	main(options, args)





