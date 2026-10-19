# Let's do fancy stats, with some help from scipy
from scipy.stats import distributions
from numpy import sqrt
from numpy import corrcoef

def ttest_greaterthan(mean1,stdev1,n1,mean2,stdev2,n2):
	"""Gives the p-value (1.0 - confidence) that the true mean1 is greater than the true mean2.
	(This is a one-tailed t-test, and it assumes normal distributions with equal variance in the two distributions.
	mean1 and mean2 are the observed means
	stdev1 and stdev2 are the observed standard deviations
	n1 and n2 are the number of points sampled
	"""
	if (n1 < 2 or n2 < 2):
		raise Exception("Both distributions need to have been sampled at least twice (preferably > 30 times) to apply a t-test.")
	num = mean1 - mean2  #difference in means
	df = n1 + n2 - 2 #degrees of freedom
	#denom is the standard error of the difference
	denom = sqrt(((n1-1)*stdev1*stdev1+(n2-1)*stdev2*stdev2) / float(df) * (1.0/n1 + 1.0/n2))
	if (num == 0):
		t = 0
	else:
		t = num / denom
	return distributions.t.sf(t,df)

def ttest_notequal(mean1,stdev1,n1,mean2,stdev2,n2):
	"""Gives the p-value (1.0 - confidence) that mean1 is statistically different from mean2.
	(This is a two-tailed t-test, and it assumes normal distributions with equal variance in the two distributions.
	mean1 and mean2 are the observed means
	stdev1 and stdev2 are the observed standard deviations
	n1 and n2 are the number of points sampled
	"""
	pgreater = ttest_greaterthan(mean1,stdev1,n1,mean2,stdev2,n2)
	if (pgreater > 0.5):
		pgreater = 1.0 - pgreater
	return 2 * pgreater


def gini(dd):
	""" Gini inequality coefficient
	Ranges between 0.0 (completely equally distributed) and 
	1.0 (all wealth/value concentrated at a single individual)"""
	dd = sorted(dd,reverse=True)
	N = len(dd)
	sum1 = sum([dd[i] * (i + 1) for i in range(N)])
	return float(N+1)/float(N-1) - 2.0 / (N * (N - 1) * mean(dd)) * sum1

def linearRegressionR2Val(xs,ys):
	"""Returns the R^2 value for a linear regression"""
	return (corrcoef(xs,ys)[0,1])**2
	
