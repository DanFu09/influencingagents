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
# Usage: bash runconvergence_multilarge.sh min max inc runs length [print_delta]
# min, max are the bounds of N, runs is the number of times to run each N,
# 	length is the number of steps to run each experiment, print_delta is the
# 	rate to print out flock counts

if [ "$#" -ne 8 ]; then
	echo "Usage: bash runlarge_vary_n.sh placement global_behavior local_behavior min max inc runs numConvergence"
	exit 1
fi

PLACEMENT=$1
GLOBAL_BEHAVIOR=$2
LOCAL_BEHAVIOR=$3
MIN=$4
MAX=$5
INC=$6
RUNS=$7
NUMCONVERGENCE=$8

LOG_FOLDER=~/Dropbox/large_correct_one_step
# if [ "${PWD##*/}" = "scripts" ]; then
# 	LOG_FOLDER="../logs/{$LOG_FOLDER}"
# fi
RUN_FOLDER="$LOG_FOLDER/0-100_N-300_${PLACEMENT}_${GLOBAL_BEHAVIOR}_${LOCAL_BEHAVIOR}"
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
			-globalBehavior $GLOBAL_BEHAVIOR -localBehavior $LOCAL_BEHAVIOR -prints 100 \
			-outputNumFlocks false -outputNumLoners false -outputNumSameDir true \
			-outputNumBoidsWithAdhoc true >> $OUTPUT
	done

done
