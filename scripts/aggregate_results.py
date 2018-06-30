#!/usr/bin/python

# Take in a folder where files are labeled 1, 2, 3, etc
# Go from file min to file max, inclusive, with increment inc
# Each file has some number of runs in it; each run is structured as:
#   Seed: num
#   Step    Flocks  Lone    SameDir
#   num     num     num     num
#   ...
# For some number of steps (same number of steps for each run)
# 
# This script outputs four tables: flockAvg, flockStd, loneAvg, loneStd, sameDirAvg, sameDirStd
# Each file is structured as follows:
#   Step,min,min+inc,min+inc*2,min+inc*3,...,max
#   num,num,num,num,num,...,num
#   ...

import sys
import os
import numpy as np
from scipy import stats

if len(sys.argv) != 5:
    print "Usage: python aggregate_results.py folder min max inc"

folder = sys.argv[1]
minNum = int(sys.argv[2])
maxNum = int(sys.argv[3])
inc = int(sys.argv[4])

flockAvgPerStep = {}
flockStdPerStep = {}
loneAvgPerStep = {}
loneStdPerStep = {}
sameDirAvgPerStep = {}
sameDirStdPerStep = {}

stepNums = []
fileNums = []

for i in xrange(minNum, maxNum+inc, inc):
    path = os.path.join(folder, str(i))
    f = open(path, 'r')

    fileNums.append(i)

    flockValsPerStep = {} # Each step gets an array of entries
    loneValsPerStep = {} # Each step gets an array of entries
    sameDirValsPerStep = {} # Each step gets an array of entries

    for line in f:
        if line[0] == "S":
            continue
        data = line.split("\t")
        stepNum = int(data[0])
        flockNum = int(data[1])
        loneNum = int(data[2])
        sameDirNum = 0

        if not stepNum in stepNums:
            stepNums.append(stepNum)

        if stepNum in flockValsPerStep:
            flockValsPerStep[stepNum].append(flockNum)
        else:
            flockValsPerStep[stepNum] = [flockNum]

        if stepNum in loneValsPerStep:
            loneValsPerStep[stepNum].append(loneNum)
        else:
            loneValsPerStep[stepNum] = [loneNum]

        if stepNum in sameDirValsPerStep:
            sameDirValsPerStep[stepNum].append(sameDirNum)
        else:
            sameDirValsPerStep[stepNum] = [sameDirNum]

    for stepNum in stepNums:
        if stepNum in flockAvgPerStep:
            flockAvgPerStep[stepNum].append(np.mean(flockValsPerStep[stepNum]))
        else:
            flockAvgPerStep[stepNum] = [np.mean(flockValsPerStep[stepNum])]
        if stepNum in flockStdPerStep:
            flockStdPerStep[stepNum].append(stats.sem(flockValsPerStep[stepNum]))
        else:
            flockStdPerStep[stepNum] = [stats.sem(flockValsPerStep[stepNum])]
        if stepNum in loneAvgPerStep:
            loneAvgPerStep[stepNum].append(np.mean(loneValsPerStep[stepNum]))
        else:
            loneAvgPerStep[stepNum] = [np.mean(loneValsPerStep[stepNum])]
        if stepNum in loneStdPerStep:
            loneStdPerStep[stepNum].append(stats.sem(loneValsPerStep[stepNum]))
        else:
            loneStdPerStep[stepNum] = [stats.sem(loneValsPerStep[stepNum])]
        if stepNum in sameDirAvgPerStep:
            sameDirAvgPerStep[stepNum].append(np.mean(sameDirValsPerStep[stepNum]))
        else:
            sameDirAvgPerStep[stepNum] = [np.mean(sameDirValsPerStep[stepNum])]
        if stepNum in sameDirStdPerStep:
            sameDirStdPerStep[stepNum].append(stats.sem(sameDirValsPerStep[stepNum]))
        else:
            sameDirStdPerStep[stepNum] = [stats.sem(sameDirValsPerStep[stepNum])]

def printData(stepNums, fileNums, data):
    outStr = "Step,"+",".join(map(str, fileNums))
    print outStr

    for i in stepNums:
        outStr = str(i) + "," + ",".join(map(str, data[i]))
        print outStr

printData(stepNums, fileNums, flockAvgPerStep)
printData(stepNums, fileNums, flockStdPerStep)
printData(stepNums, fileNums, loneAvgPerStep)
printData(stepNums, fileNums, loneStdPerStep)
#printData(stepNums, fileNums, sameDirAvgPerStep)
#printData(stepNums, fileNums, sameDirStdPerStep)