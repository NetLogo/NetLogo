#!/bin/python

from pylab import *

def plotit(dat,interval,lab):
	errorbar(dat[::interval]['evaluations'],dat[::interval]['mean_fitness'],yerr=dat[::interval]['stderr']*1.96,label=lab)
# for 90% confidence interval, could use 1.65
#	errorbar(dat[::interval]['evaluations'],dat[::interval]['mean_fitness'],yerr=dat[::interval]['stderr']*1.65,label=lab)
	

datGA = csv2rec("flocking_noconverge_ga.processed.csv")
datHC = csv2rec("flocking_noconverge_hc.processed.csv")
datRand = csv2rec("flocking_noconverge_rand.processed.csv")
figure()
#clf()
plotit(datRand,5,"RandomSearch")
plotit(datHC,5,"HillClimber")
plotit(datGA,5,"GeneticAlgorithm")
xlabel("# of model runs")
#ylabel("variation in birds' movement directions")
ylabel("veeness metric across all flocks ")
title("Search performance for 'veeness' task in Vee Flocking")
legend(loc = 'lower right')
axis(ymax=0.6)

fname = "fveeness"
savefig(fname + "_performance.png",dpi=300)
savefig(fname + "_performance.pdf")
savefig(fname + "_performance.eps")
savefig(fname + "_performance.svg")

###########################



