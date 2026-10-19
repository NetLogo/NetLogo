# Let's do simple stats, without bothering with NumPy...

def average(lst):
	return float(sum(lst)) / len(lst)

def stdError(lst):
	return stdev(lst) / (len(lst) ** 0.5)


def stdev(lst):
	avg = float(sum(lst)) / len(lst)
	sdsq = float(sum([(i - avg) ** 2 for i in lst]))
	return (sdsq / (len(lst) - 1 or 1)) ** .5

def median(lst):
	x = sorted(lst)
	return ((len(x) % 2) and (x[(len(x)>>1)])) or (sum(x[((len(x)>>1)-1):(len(x)>>1)+1])/2.0)
