#!/usr/bin/env bash

# A script for running a batch of the HERD experiment
# Runs the large experiment with constant size (5kx5k), varying N from
# 	min to max
# Creates a run folder in LOG_FOLDER with the current date and time
# In the run folder, outputs min, min+1, ..., max
# Each file contains the output of all the runs concatenated:
# 	The output is one line containing the random seed, then one line with labels,
# 	then length/print_delta lines of the step_num, number of flocks, and number
# 	of lone boids
# 
# Usage: bash runlarge_vary_n.sh min max inc runs length [print_delta]
# min, max are the bounds of N, runs is the number of times to run each N,
# 	length is the number of steps to run each experiment, print_delta is the
# 	rate to print out flock counts

if [ "$#" -lt 5 ]; then
	echo "Usage: bash runherd_vary_n.sh min max inc runs length [folder]"
	exit 1
fi

MIN=$1
MAX=$2
INC=$3
RUNS=$4
LENGTH=$5

LOG_FOLDER=logs/HERD_VARY_N
if [ "${PWD##*/}" = "scripts" ]; then
	LOG_FOLDER="logs/HERD_VARY_N"
fi

if [ "$#" -eq 6 ]; then
	LOG_FOLDER=$6
fi

RUN_FOLDER="$LOG_FOLDER"
mkdir $RUN_FOLDER

for N in $(seq $MIN $INC $MAX); do
	echo $N
	OUTPUT="$RUN_FOLDER/$N"

	for run in $(seq $RUNS); do
		echo $run
		java sim.app.flockers.Flockers -experiment HERD -for $LENGTH -width 5000 -height 5000 \
			-numFlockers $N -radius 500 -toroidal false >> $OUTPUT
	done
done
