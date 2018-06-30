#!/usr/bin/python

# Take in a folder where files are labeled 1, 2, 3, etc
# Go from file min to file max, inclusive, with increment inc
# Each file has some number of runs in it; each run has some number of lines in it,
#   with each line having two numbers
# The first is the number of steps to "convergence," and the second is
#   the size of the number of birds facing the same direction at convergence
# 
# This script outputs one table:
#   Trials,min,min+inc,min+inc*2,min+inc*3,...,max
#   trial_num,num,num,num,num,num,...,num
#   ...
# 
# The data in the nums are the number of steps until convergence

import sys
import os
import numpy as np

if len(sys.argv) != 5:
    print "Usage: python aggregate_results.py folder min max inc"

folder = sys.argv[1]
minNum = int(sys.argv[2])
maxNum = int(sys.argv[3])
inc = int(sys.argv[4])

fileNums = []
stepsPerFile = {}

for i in xrange(minNum, maxNum+inc, inc):
    path = os.path.join(folder, str(i))
    f = open(path, 'r')

    fileNums.append(i)
    stepsPerFile[i] = []

    for line in f:
    	data = line.split(" ")
    	stepsPerFile[i].append(data[0])

def printData(fileNums, data):
    outStr = "Trials,"+",".join(map(str, fileNums))
    print outStr

    for trial in xrange(len(data[fileNums[0]])):
        outStr = str(trial)
        for fileNum in fileNums:
        	outStr += "," + str(data[fileNum][trial])
        print outStr

printData(fileNums, stepsPerFile)