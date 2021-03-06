#!/usr/bin/env bash

# A script for running a batch of the LARGE experiment
# Runs the large experiment with constant size (1kx1k) and N (300), varying number of
#   ad hoc agents from min to max
# Runs the experiment until numConvergence boids are facing the same direction
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

if [ "$#" -ne 7 ]; then
	echo "Usage: bash runlarge_vary_n.sh placement behavior min max inc runs numConvergence"
	exit 1
fi

PLACEMENT=$1
BEHAVIOR=$2
MIN=$3
MAX=$4
INC=$5
RUNS=$6
NUMCONVERGENCE=$7

LOG_FOLDER=logs/CONVERGENCE_LARGE_VARY_NUMADHOC
if [ "${PWD##*/}" = "scripts" ]; then
	LOG_FOLDER=../logs/CONVERGENCE_LARGE_VARY_NUMADHOC
fi
RUN_FOLDER="$LOG_FOLDER/0-100_N-300_${PLACEMENT}_${BEHAVIOR}"
echo $RUN_FOLDER
mkdir $RUN_FOLDER

for NUMADHOC in $(seq $MIN $INC $MAX); do
	echo "$NUMADHOC adhoc agents"
	OUTPUT="$RUN_FOLDER/$NUMADHOC"

	for run in $(seq $RUNS); do
		echo "Run $run"
		java sim.app.flockers.Flockers -experiment LARGE -for 200000 -until $NUMCONVERGENCE -width 1000 -height 1000 \
			-numFlockers $((300+$NUMADHOC)) -numAdhoc $NUMADHOC \
			-placement $PLACEMENT -placementX 500 -placementY 500 -placementRadius 500 \
			-behavior $BEHAVIOR -prints 100 \
			-outputNumFlocks false -outputNumLoners false -outputNumSameDir true > tmp
		echo $(tail -1 tmp) >> $OUTPUT
	done
done