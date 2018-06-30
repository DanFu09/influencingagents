#!/usr/bin/python

import numpy as np
import os
from scipy import stats

# def getBaseline():
#     baseline_data = "logs/CONVERGENCE_LARGE_VARY_NUMADHOC/0-100_N-300_GRID_RECT_FACE_EAST_2/0"
#     f = open(baseline_data, 'r')
#     data = []
#     for line in f:
#         data.append(int(line.split(" ")[0]))
#     return data

folder = "logs/large_correct_one_step"
placement_strategies = ["RANDOM_RECT", "GRID_RECT", "K_MEANS"]
genetic_behaviors = ["GENETIC_ast0" + str(i) for i in xrange(1, 8)]
local_behaviors = ["FACE_EAST", "OFFSET_MOMENTUM", "ONE_STEP_LOOKAHEAD", "COORDINATED"] + genetic_behaviors
global_behaviors_nonmulti = ["FACE", "RANDOM"]
global_behaviors_multi = ["MULTILARGE-150-MIN_AVG_DIR_NEIGH-", "MULTILARGE-200-MIN_AVG_DIR_NEIGH-",
                          "MULTILARGEFIXED-150-MIN_AVG_DIR_NEIGH-", "MULTILARGEFIXED-200-MIN_AVG_DIR_NEIGH-",
                          "MULTILARGELOCAL-150-MIN_AVG_DIR_NEIGH-", "MULTILARGELOCAL-200-MIN_AVG_DIR_NEIGH-",]

experiment_folder_format = "0-100_N-300_{0}_{1}_{2}"
strategy_name_format = "{0},{1},{2}"
# baseline_data = getBaseline()
# baseline_average = np.mean(baseline_data)
# baseline_median = np.median(baseline_data)

experiments = []
for placement in placement_strategies:
    experiment = {}
    for local_behavior in local_behaviors:
        for global_behavior in global_behaviors_nonmulti:
            experiment = {}
            experiment["placement"] = placement
            experiment["local_behavior"] = local_behavior
            experiment["global_behavior"] = global_behavior
            experiments.append(experiment)
        for global_behavior in global_behaviors_multi:
            experiment = {}
            experiment["placement"] = placement
            experiment["local_behavior"] = local_behavior
            experiment["global_behavior"] = global_behavior + local_behavior
            experiments.append(experiment)
    # add COUZIN + FACE
    experiment = {}
    experiment["placement"] = placement
    experiment["local_behavior"] = "COUZIN"
    experiment["global_behavior"] = "FACE"
    experiments.append(experiment)

    # add COUZIN + MULTILARGE
    experiment = {}
    experiment["placement"] = placement
    experiment["local_behavior"] = "COUZIN"
    experiment["global_behavior"] = "MULTILARGELOCAL-150-MIN_AVG_DIR_NEIGH-COUZIN"
    experiments.append(experiment)

def format_for_experiment(format_str, experiment):
    return format_str.format(
        experiment["placement"],
        experiment["global_behavior"],
        experiment["local_behavior"])

# Average time to convergence
out_string = "Placement,Global,Local"
for x in xrange(0, 101, 10):
    out_string += "," + str(x)
print out_string
for experiment in experiments:
    experiment_folder = format_for_experiment(experiment_folder_format, experiment)
    out_string = format_for_experiment(strategy_name_format, experiment)
    if not os.path.isdir(os.path.join(folder, experiment_folder)):
        continue;
    output_data = []
    for x in xrange(0, 101, 10):
        data = []
        filename = os.path.join(
            folder, os.path.join(experiment_folder, str(x)))
        collect_data = False
        f = open(filename, 'r')
        for line in f:
            if line[0] == 'S':
                collect_data = True
                continue
            line_data = line.split("\t")
            time = int(line_data[0])
            controlled = int(line_data[1])
            samedir = int(line_data[2])
            if samedir >= 150 and collect_data:
                data.append(time)
                collect_data = False
        output_data.append(np.mean(data))
    for average in output_data:
        out_string += "," + str(average)
    print out_string

# Standard deviation of average time to convergence
out_string = "Placement,Global,Local"
for x in xrange(0, 101, 10):
    out_string += "," + str(x)
print out_string
for experiment in experiments:
    experiment_folder = format_for_experiment(experiment_folder_format, experiment)
    out_string = format_for_experiment(strategy_name_format, experiment)
    if not os.path.isdir(os.path.join(folder, experiment_folder)):
        continue;
    output_data = []
    for x in xrange(0, 101, 10):
        data = []
        filename = os.path.join(
            folder, os.path.join(experiment_folder, str(x)))
        collect_data = False
        f = open(filename, 'r')
        for line in f:
            if line[0] == 'S':
                collect_data = True
                continue
            line_data = line.split("\t")
            time = int(line_data[0])
            controlled = int(line_data[1])
            samedir = int(line_data[2])
            if samedir >= 150 and collect_data:
                data.append(time)
                collect_data = False
        output_data.append(np.std(data))
    for stddev in output_data:
        out_string += "," + str(stddev)
    print out_string

# Standard error of average time to convergence
out_string = "Placement,Global,Local"
for x in xrange(0, 101, 10):
    out_string += "," + str(x)
print out_string
for experiment in experiments:
    experiment_folder = format_for_experiment(experiment_folder_format, experiment)
    out_string = format_for_experiment(strategy_name_format, experiment)
    if not os.path.isdir(os.path.join(folder, experiment_folder)):
        continue;
    output_data = []
    for x in xrange(0, 101, 10):
        data = []
        filename = os.path.join(
            folder, os.path.join(experiment_folder, str(x)))
        collect_data = False
        f = open(filename, 'r')
        for line in f:
            if line[0] == 'S':
                collect_data = True
                continue
            line_data = line.split("\t")
            time = int(line_data[0])
            controlled = int(line_data[1])
            samedir = int(line_data[2])
            if samedir >= 150 and collect_data:
                data.append(time)
                collect_data = False
        output_data.append(stats.sem(data))
    for sem in output_data:
        out_string += "," + str(sem)
    print out_string

# Median time to convergence
out_string = "Placement,Global,Local"
for x in xrange(0, 101, 10):
    out_string += "," + str(x)
print out_string
for experiment in experiments:
    experiment_folder = format_for_experiment(experiment_folder_format, experiment)
    out_string = format_for_experiment(strategy_name_format, experiment)
    if not os.path.isdir(os.path.join(folder, experiment_folder)):
        continue;
    output_data = []
    for x in xrange(0, 101, 10):
        data = []
        filename = os.path.join(
            folder, os.path.join(experiment_folder, str(x)))
        collect_data = False
        f = open(filename, 'r')
        for line in f:
            if line[0] == 'S':
                collect_data = True
                continue
            line_data = line.split("\t")
            time = int(line_data[0])
            controlled = int(line_data[1])
            samedir = int(line_data[2])
            if samedir >= 150 and collect_data:
                data.append(time)
                collect_data = False
        output_data.append(np.median(data))
    for median in output_data:
        out_string += "," + str(median)
    print out_string

# Average time to 50% of boids in flocks with ad hoc
out_string = "Placement,Global,Local"
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
        collect_data = False
        f = open(filename, 'r')
        for line in f:
            if line[0] == 'S':
                collect_data = True
                continue
            line_data = line.split("\t")
            time = int(line_data[0])
            controlled = int(line_data[1])
            samedir = int(line_data[2])
            if controlled >= 150 and collect_data:
                data.append(time)
                collect_data = False
        output_data.append(np.mean(data))
    for average in output_data:
        out_string += "," + str(average)
    print out_string

# Median time to 50% of boids in flocks with ad hoc
out_string = "Placement,Global,Local"
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
        collect_data = False
        f = open(filename, 'r')
        for line in f:
            if line[0] == 'S':
                collect_data = True
                continue
            line_data = line.split("\t")
            time = int(line_data[0])
            controlled = int(line_data[1])
            samedir = int(line_data[2])
            if controlled >= 150 and collect_data:
                data.append(time)
                collect_data = False
        output_data.append(np.median(data))
    for median in output_data:
        out_string += "," + str(median)
    print out_string