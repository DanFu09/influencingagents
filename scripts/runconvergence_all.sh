#!/usr/bin/env bash

if [ "$#" -ne 5 ]; then
	echo "Usage: bash runlarge_vary_n.sh min max inc runs numConvergence"
	exit 1
fi

MIN=$1
MAX=$2
INC=$3
RUNS=$4
NUMCONVERGENCE=$5

PLACEMENTS=(RANDOM_RECT GRID_RECT K_MEANS)
BEHAVIORS=(FACE_EAST OFFSET_MOMENTUM ONE_STEP_LOOKAHEAD COORDINATED)

for BEHAVIOR in "${BEHAVIORS[@]}"
do
	for PLACEMENT in "${PLACEMENTS[@]}"
	do
		echo "$PLACEMENT $BEHAVIOR"

		FOLDER="logs/CONVERGENCE_LARGE_VARY_NUMADHOC/0-100_N-300_${PLACEMENT}_${BEHAVIOR}"
		bash scripts/runconvergence_large_vary_numadhoc.sh $PLACEMENT $BEHAVIOR \
			$MIN $MAX $INC $RUNS $NUMCONVERGENCE

		python scripts/aggregate_convergence.py $FOLDER $MIN $MAX $INC > "${FOLDER}/all.csv"
	done
done