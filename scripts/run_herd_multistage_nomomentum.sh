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

if [ "$#" -ne 8 ]; then
	echo "Usage: bash runlarge_vary_n.sh placement placement_radius global_behavior local_behavior min max inc runs"
	exit 1
fi

PLACEMENT=$1
PLACEMENT_RADIUS=$2
GLOBAL_BEHAVIOR=$3
LOCAL_BEHAVIOR=$4
MIN=$5
MAX=$6
INC=$7
RUNS=$8


LOG_FOLDER=~/Dropbox/herd_nomomentum
# if [ "${PWD##*/}" = "scripts" ]; then
# 	LOG_FOLDER=../logs/HERD_MULTISTAGE
# fi
RUN_FOLDER="$LOG_FOLDER/0-100_N-300_${PLACEMENT}_${PLACEMENT_RADIUS}_${GLOBAL_BEHAVIOR}_${LOCAL_BEHAVIOR}"
echo $RUN_FOLDER
mkdir $RUN_FOLDER

for NUMADHOC in $(seq $MIN $INC $MAX); do
	echo "$NUMADHOC adhoc agents"
	OUTPUT="$RUN_FOLDER/$NUMADHOC"

	for run in $(seq $RUNS); do
		echo "Run $run"
		java sim.app.flockers.Flockers -experiment HERD -for 15000 -width 5000 -height 5000 \
			-numFlockers $((300+$NUMADHOC)) -numAdhoc $NUMADHOC -radius 500 -toroidal false -momentum 0 \
			-placement $PLACEMENT -placementX 2500 -placementY 2500 -placementRadius ${PLACEMENT_RADIUS} \
			-globalBehavior $GLOBAL_BEHAVIOR -localBehavior $LOCAL_BEHAVIOR -prints 100 \
			-outputNumFlocks false -outputNumLoners false -outputNumSameDir false \
			-outputNumBoidsWithAdhoc true -outputNumOnScreen true -outputNumBoidsInBoundingBox true >> $OUTPUT
	done
done
