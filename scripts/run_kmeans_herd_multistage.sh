#!/usr/bin/env bash

if [ "$#" -lt 4 ]; then
	echo "Usage: bash runlarge_vary_n.sh min max inc runs [LOCAL]"
	echo "Usage: bash runlarge_vary_n.sh 0 100 10 50"
	exit 1
fi

MIN=$1
MAX=$2
INC=$3
RUNS=$4

PLACEMENT_ARRAY=(K_MEANS)
PLACEMENT_RADIUS_ARRAY=(500)

LOCAL_BEHAVIORS=(
FACE_EAST
COUZIN
GENETIC_ast01
GENETIC_ast02
GENETIC_ast03
GENETIC_ast04
GENETIC_ast05
GENETIC_ast06
GENETIC_ast07)

if [ "$#" -eq 5 ]; then
	LOCAL_BEHAVIORS=($5)
fi

GLOBAL_BEHAVIOR_500=(
FACE
CIRCLE
MULTIHERD-CIRCLE-MIN_AVG_DIR_NEIGH-750-CIRCLE-900
MULTIHERD-CIRCLE-MIN_AVG_DIR_NEIGH-750-CIRCLE-750
MULTIHERD-CIRCLE-MIN_AVG_DIR_NEIGH-900-CIRCLE-900
POLYGON10
RANDOM
MULTIHERD-CIRCLE-MIN_AVG_DIR_NEIGH-0-FACE-0)

for PLACEMENT_RADIUS in "${PLACEMENT_RADIUS_ARRAY[@]}"
do 
	for PLACEMENT in "${PLACEMENT_ARRAY[@]}"
	do
		if [ ${PLACEMENT_RADIUS} -eq "500" ]; then
			for GLOBAL_BEHAVIOR in "${GLOBAL_BEHAVIOR_500[@]}"
			do
				for LOCAL_BEHAVIOR in "${LOCAL_BEHAVIORS[@]}"
				do
					echo "${PLACEMENT_RADIUS} ${PLACEMENT} ${GLOBAL_BEHAVIOR} ${LOCAL_BEHAVIOR}"
					bash scripts/run_herd_multistage.sh ${PLACEMENT} ${PLACEMENT_RADIUS} ${GLOBAL_BEHAVIOR} \
						${LOCAL_BEHAVIOR} $MIN $MAX $INC $RUNS 
				done
			done
		fi
	done
done 
	
