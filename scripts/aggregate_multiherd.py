#!/usr/bin/python

import numpy as np
from scipy import stats
import os
import sys

folder = "logs/herd_correct_one_step"
placement_strategies = ["BORDER_CIRCLE", "RANDOM_CIRCLE", "GRID_CIRCLE", "K_MEANS"]
placement_radii = ["500", "750"]
genetic_behaviors = ["GENETIC_ast0" + str(i) for i in xrange(1, 8)]
local_behaviors = [
    "FACE_EAST",
    "OFFSET_MOMENTUM",
    "ONE_STEP_LOOKAHEAD",
    "COORDINATED"
] + genetic_behaviors
global_net_behaviors = ["FACE", "RANDOM", "MULTIHERD-CIRCLE-MIN_AVG_DIR_NEIGH-0-FACE-0"]
global_stationary_behaviors_500 = [
    "CIRCLE",
    "MULTIHERD-CIRCLE-MIN_AVG_DIR_NEIGH-750-CIRCLE-750",
    "MULTIHERD-CIRCLE-MIN_AVG_DIR_NEIGH-750-CIRCLE-900",
    "MULTIHERD-CIRCLE-MIN_AVG_DIR_NEIGH-900-CIRCLE-900",
    "POLYGON10"
]
global_stationary_behaviors_750 = [
    "CIRCLE",
    "MULTIHERD-CIRCLE-MIN_AVG_DIR_NEIGH-900-CIRCLE-900",
    "MULTIHERD-CIRCLE-MIN_AVG_DIR_NEIGH-900-CIRCLE-1100",
    "MULTIHERD-CIRCLE-MIN_AVG_DIR_NEIGH-1100-CIRCLE-1100",
    "POLYGON10"
]

experiment_folder_format = "0-100_N-300_{0}_{1}_{2}_{3}"
strategy_name_format = "{0},{1},{2},{3}"

def format_for_experiment(format_str, experiment):
    return format_str.format(
        experiment["placement"],
        experiment["placement_radius"],
        experiment["global_behavior"],
        experiment["local_behavior"])

experiments = []
for placement_radius in placement_radii:
    experiment = {}
    for local_behavior in local_behaviors:
        for placement in placement_strategies:
            if placement_radius == "750" and placement == "K_MEANS":
                continue
            global_stationary_behavior = []
            if placement_radius == "500":
                global_stationary_behavior = global_stationary_behaviors_500
            else:
                global_stationary_behavior = global_stationary_behaviors_750
            for global_behavior in global_stationary_behavior:
                experiment = {}
                experiment["placement"] = placement
                experiment["placement_radius"] = placement_radius
                experiment["local_behavior"] = local_behavior
                experiment["global_behavior"] = global_behavior
                experiments.append(experiment)
            for global_behavior in global_net_behaviors:
                experiment = {}
                experiment["placement"] = placement
                experiment["placement_radius"] = placement_radius
                experiment["local_behavior"] = local_behavior
                experiment["global_behavior"] = global_behavior
                experiments.append(experiment)

    for placement in placement_strategies:
        # Add COUZIN + Face
        experiment = {}
        experiment["placement"] = placement
        experiment["placement_radius"] = placement_radius
        experiment["local_behavior"] = "COUZIN"
        experiment["global_behavior"] = "FACE"
        experiments.append(experiment)

# Print averages
out_string = "Placement,Radius,Global,Local"
for x in xrange(10, 101, 10):
    out_string += "," + str(x)
print out_string
for experiment in experiments:
    experiment_folder = format_for_experiment(experiment_folder_format, experiment)
    out_string = format_for_experiment(strategy_name_format, experiment)
    if not os.path.isdir(os.path.join(folder, experiment_folder)):
        continue;
    output_data = []
    for x in xrange(10, 101, 10):
        data = []
        filename = os.path.join(
            folder, os.path.join(experiment_folder, str(x)))
        if not os.path.exists(filename):
            break
        f = open(filename, 'r')
        for line in f:
            if line[0] == 'S':
                continue
            line_data = line.split("\t")
            try:
                time = int(line_data[0])
                controlled = int(line_data[1])
                onscreen = int(line_data[2])
                inboundingbox = int(line_data[3])
            except:
                print line_data
                print out_string
                sys.exit(0)
            if time == 10000:
                data.append(controlled)
        output_data.append(np.mean(data))
    for average in output_data:
        out_string += "," + str(average)
    print out_string

# Print standard deviations
out_string = "Placement,Radius,Global,Local"
for x in xrange(10, 101, 10):
    out_string += "," + str(x)
print out_string
for experiment in experiments:
    experiment_folder = format_for_experiment(experiment_folder_format, experiment)
    out_string = format_for_experiment(strategy_name_format, experiment)
    if not os.path.isdir(os.path.join(folder, experiment_folder)):
        continue;
    output_data = []
    for x in xrange(10, 101, 10):
        data = []
        filename = os.path.join(
            folder, os.path.join(experiment_folder, str(x)))
        if not os.path.exists(filename):
            break
        f = open(filename, 'r')
        for line in f:
            if line[0] == 'S':
                continue
            line_data = line.split("\t")
            try:
                time = int(line_data[0])
                controlled = int(line_data[1])
                onscreen = int(line_data[2])
                inboundingbox = int(line_data[3])
            except:
                print line_data
                print out_string
                sys.exit(0)
            if time == 10000:
                data.append(controlled)
        output_data.append(np.std(data))
    for stddev in output_data:
        out_string += "," + str(stddev)
    print out_string

# Print standard errors
out_string = "Placement,Radius,Global,Local"
for x in xrange(10, 101, 10):
    out_string += "," + str(x)
print out_string
for experiment in experiments:
    experiment_folder = format_for_experiment(experiment_folder_format, experiment)
    out_string = format_for_experiment(strategy_name_format, experiment)
    if not os.path.isdir(os.path.join(folder, experiment_folder)):
        continue;
    output_data = []
    for x in xrange(10, 101, 10):
        data = []
        filename = os.path.join(
            folder, os.path.join(experiment_folder, str(x)))
        if not os.path.exists(filename):
            break
        f = open(filename, 'r')
        for line in f:
            if line[0] == 'S':
                continue
            line_data = line.split("\t")
            try:
                time = int(line_data[0])
                controlled = int(line_data[1])
                onscreen = int(line_data[2])
                inboundingbox = int(line_data[3])
            except:
                print line_data
                print out_string
                sys.exit(0)
            if time == 10000:
                data.append(controlled)
        output_data.append(stats.sem(data))
    for sem in output_data:
        out_string += "," + str(sem)
    print out_string

# Print medians
out_string = "Placement,Radius,Global,Local"
for x in xrange(10, 101, 10):
    out_string += "," + str(x)
print out_string
for experiment in experiments:
    experiment_folder = format_for_experiment(experiment_folder_format, experiment)
    out_string = format_for_experiment(strategy_name_format, experiment)
    if not os.path.isdir(os.path.join(folder, experiment_folder)):
        continue;
    output_data = []
    for x in xrange(10, 101, 10):
        data = []
        filename = os.path.join(
            folder, os.path.join(experiment_folder, str(x)))
        if not os.path.exists(filename):
            break
        f = open(filename, 'r')
        for line in f:
            if line[0] == 'S':
                continue
            line_data = line.split("\t")
            time = int(line_data[0])
            controlled = int(line_data[1])
            onscreen = int(line_data[2])
            inboundingbox = int(line_data[3])
            if time == 10000:
                data.append(controlled)
        output_data.append(np.median(data))
    for median in output_data:
        out_string += "," + str(median)
    print out_string
