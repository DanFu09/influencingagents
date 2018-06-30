import matplotlib.pyplot as plt; plt.rcdefaults()
import numpy as np
import matplotlib.pyplot as plt
import csv
from collections import defaultdict 
import matplotlib.cm as cm
from matplotlib.font_manager import FontProperties
import math
import sys
from matplotlib.transforms import blended_transform_factory
from matplotlib.lines import Line2D

INCLUDE_EXTRAS=False
INCLUDE_GENETIC=False
INCLUDE_STDDEV=True
SINGLE_ROW=True
SINGLE_ROW_MULTISTEP=True
INCLUDE_COUZIN=True
INCLUDE_GLOBALLOCAL = False
LOGSCALE = True

# get file location
file_name = 'large_aggregated.csv'
if len(sys.argv) > 1:
    file_name = sys.argv[1]

# definitions
placements = ('Random', 'Grid', 'K-Means')
global_strategies = ('FACE', 'RANDOM', 'MULTILARGELOCAL-150-MIN_AVG_DIR_NEIGH', 'MULTILARGELOCAL-200-MIN_AVG_DIR_NEIGH',
                     'MULTILARGEFIXED-150-MIN_AVG_DIR_NEIGH', 'MULTILARGEFIXED-200-MIN_AVG_DIR_NEIGH')
local_strategies=['FACE_EAST', 'OFFSET_MOMENTUM', 'ONE_STEP_LOOKAHEAD', 'COORDINATED']
if INCLUDE_COUZIN:
    local_strategies = local_strategies + ['COUZIN']
if SINGLE_ROW and not SINGLE_ROW_MULTISTEP:
    local_strategies = local_strategies + ['MULTISTEP']
if SINGLE_ROW and SINGLE_ROW_MULTISTEP:
    global_strategies = ['MULTILARGELOCAL-150-MIN_AVG_DIR_NEIGH']
if INCLUDE_GENETIC:
    local_strategies = local_strategies +  ['GENETIC_ast01',
                                            'GENETIC_ast02', 'GENETIC_ast03', 'GENETIC_ast04', 'GENETIC_ast05', 'GENETIC_ast06',
                                            'GENETIC_ast07']
y_pos = np.arange(len(placements))

# first do processing for large data
# median time to convergence 

# gs = global strategies
gs1 = np.zeros((len(placements), len(local_strategies)))
gs2 = np.zeros((len(placements), len(local_strategies)))
gs3 = np.zeros((len(placements), len(local_strategies)))
gs4 = np.zeros((len(placements), len(local_strategies)))
gs5 = np.zeros((len(placements), len(local_strategies)))
gs6 = np.zeros((len(placements), len(local_strategies)))

gs1stddev = np.zeros((len(placements), len(local_strategies)))
gs2stddev = np.zeros((len(placements), len(local_strategies)))
gs3stddev = np.zeros((len(placements), len(local_strategies)))
gs4stddev = np.zeros((len(placements), len(local_strategies)))
gs5stddev = np.zeros((len(placements), len(local_strategies)))
gs6stddev = np.zeros((len(placements), len(local_strategies)))

with open(file_name, 'rb') as file:
    reader = csv.reader(file.read().splitlines())
    count = 0
    for row in reader:
        if count == 4:
            # finished
            break

        if row[0] == 'Placement':
            count += 1
            continue

        if count == 1:
            # define each column
            p = row[0]
            global_strategy = row[1]
            local_strategy = row[2]
            time_50 = row[8]

            if local_strategy not in local_strategies:
                continue
            ls = local_strategies.index(local_strategy)

            if p == 'RANDOM_RECT':
                placement = 0
            elif p == 'GRID_RECT':
                placement = 1
            elif p == 'K_MEANS':
                placement = 2

            string_length1 = len('MULTILARGELOCAL-150-MIN_AVG_DIR_NEIGH')
            string_length2 = len('MULTILARGEFIXED-150-MIN_AVG_DIR_NEIGH')
            if local_strategy > -1:
                if SINGLE_ROW:
                    if (SINGLE_ROW_MULTISTEP and
                                global_strategy[:string_length1] == 'MULTILARGELOCAL-150-MIN_AVG_DIR_NEIGH'):
                        gs1[placement][ls] = time_50
                    elif (not SINGLE_ROW_MULTISTEP and
                                  global_strategy[:string_length1] == 'MULTILARGELOCAL-150-MIN_AVG_DIR_NEIGH' and
                                  local_strategy == 'FACE_EAST'):
                        gs1[placement][ls] = time_50
                else:
                    if global_strategy == 'FACE':
                        gs1[placement][ls] = time_50
                    elif global_strategy == 'RANDOM':
                        gs2[placement][ls] = time_50
                    elif global_strategy[:string_length1] == 'MULTILARGELOCAL-150-MIN_AVG_DIR_NEIGH':
                        gs3[placement][ls] = time_50
                    elif global_strategy[:string_length1] == 'MULTILARGELOCAL-200-MIN_AVG_DIR_NEIGH':
                        gs4[placement][ls] = time_50
                    elif global_strategy[:string_length2] == 'MULTILARGEFIXED-150-MIN_AVG_DIR_NEIGH':
                        gs5[placement][ls] = time_50
                    elif global_strategy[:string_length2] == 'MULTILARGEFIXED-200-MIN_AVG_DIR_NEIGH':
                        gs6[placement][ls] = time_50
        elif count == 3:
            # define each column
            p = row[0]
            global_strategy = row[1]
            local_strategy = row[2]
            time_50 = row[8]

            if local_strategy not in local_strategies:
                continue
            ls = local_strategies.index(local_strategy)

            if p == 'RANDOM_RECT':
                placement = 0
            elif p == 'GRID_RECT':
                placement = 1
            elif p == 'K_MEANS':
                placement = 2

            string_length1 = len('MULTILARGELOCAL-150-MIN_AVG_DIR_NEIGH')
            string_length2 = len('MULTILARGEFIXED-150-MIN_AVG_DIR_NEIGH')
            if local_strategy > -1:
                if SINGLE_ROW:
                    if (SINGLE_ROW_MULTISTEP and
                                global_strategy[:string_length1] == 'MULTILARGELOCAL-150-MIN_AVG_DIR_NEIGH'):
                        gs1stddev[placement][ls] = time_50
                    elif (not SINGLE_ROW_MULTISTEP and
                                  global_strategy[:string_length1] == 'MULTILARGELOCAL-150-MIN_AVG_DIR_NEIGH' and
                                  local_strategy == 'FACE_EAST'):
                        gs1stddev[placement][ls] = time_50
                else:
                    if global_strategy == 'FACE':
                        gs1stddev[placement][ls] = time_50
                    elif global_strategy == 'RANDOM':
                        gs2stddev[placement][ls] = time_50
                    elif global_strategy[:string_length1] == 'MULTILARGELOCAL-150-MIN_AVG_DIR_NEIGH':
                        gs3stddev[placement][ls] = time_50
                    elif global_strategy[:string_length1] == 'MULTILARGELOCAL-200-MIN_AVG_DIR_NEIGH':
                        gs4stddev[placement][ls] = time_50
                    elif global_strategy[:string_length2] == 'MULTILARGEFIXED-150-MIN_AVG_DIR_NEIGH':
                        gs5stddev[placement][ls] = time_50

if INCLUDE_GENETIC:
    space = 0.2
    width = 0.2 * (1.0 - space) / len(y_pos)
else:
    space = 0.5
    numbars = 4
    if INCLUDE_COUZIN:
        numbars = numbars + 1
    if SINGLE_ROW and not SINGLE_ROW_MULTISTEP:
        numbars = numbars + 1
    if numbars == 6:
        width = 0.6*(1.0 - space) / len(y_pos)
    elif numbars == 5:
        width = 0.8*(1.0 - space) / len(y_pos)
    else:
        width = (1.0 - space) / len(y_pos)

plt.close('all')
# Three subplots sharing X axes
figsize=(12, 6)
if INCLUDE_EXTRAS:
    f, (ax1, ax2, ax3, ax4, ax5, ax6) = plt.subplots(6, sharex=True, sharey=False, figsize=(12, 10))
elif SINGLE_ROW:
    f, (ax1) = plt.subplots(1, sharex=True, sharey=False, figsize=(12, 2.5))
else:
    f, (ax1, ax2, ax3, ax4) = plt.subplots(4, sharex=True, sharey=False, figsize=(12, 6))


# a1 = face 
# a2 = Random
# a3 = multi t=150
# a4 = multi t=200
n = len(local_strategies)
for i, local in enumerate(local_strategies):
    print placement
    if INCLUDE_GENETIC:
        pos = [j - (1 - space) / 2. + i * width -.95 for j in range(1,1+len(placements))]
    else:
        if INCLUDE_COUZIN and SINGLE_ROW:
            pos = [j - (1 - space) / 2. + i * width -1.05 for j in range(1,1+len(placements))]
    if INCLUDE_STDDEV:
        ax1.bar(pos, gs1[:, i], width=width, label=local, color=cm.Accent(float(i) / n), yerr = gs1stddev[:, i])
        if not SINGLE_ROW:
            ax2.bar(pos, gs2[:, i], width=width, label=local, color=cm.Accent(float(i) / n), yerr = gs2stddev[:, i])
            ax3.bar(pos, gs3[:, i], width=width, label=local, color=cm.Accent(float(i) / n), yerr = gs3stddev[:, i])
            if INCLUDE_EXTRAS:
                ax4.bar(pos, gs5[:, i], width=width, label=local, color=cm.Accent(float(i) / n), yerr = gs5stddev[:, i])
                ax5.bar(pos, gs3[:, i], width=width, label=local, color=cm.Accent(float(i) / n), yerr = gs3stddev[:, i])
                ax6.bar(pos, gs5[:, i], width=width, label=local, color=cm.Accent(float(i) / n), yerr = gs5stddev[:, i])
            else:
                ax4.bar(pos, gs3[:, i], width=width, label=local, color=cm.Accent(float(i) / n), yerr = gs3stddev[:, i])
    else:
        ax1.bar(pos, gs1[:, i], width=width, label=local, color=cm.Accent(float(i) / n))
        if not SINGLE_ROW:
            ax2.bar(pos, gs2[:, i], width=width, label=local, color=cm.Accent(float(i) / n))
            ax3.bar(pos, gs3[:, i], width=width, label=local, color=cm.Accent(float(i) / n))
            if INCLUDE_EXTRAS:
                ax4.bar(pos, gs5[:, i], width=width, label=local, color=cm.Accent(float(i) / n))
                ax5.bar(pos, gs3[:, i], width=width, label=local, color=cm.Accent(float(i) / n))
                ax6.bar(pos, gs5[:, i], width=width, label=local, color=cm.Accent(float(i) / n))
            else:
                ax4.bar(pos, gs3[:, i], width=width, label=local, color=cm.Accent(float(i) / n))

# printing the legend
handles, labels = ax1.get_legend_handles_labels()
if INCLUDE_GLOBALLOCAL:
    titletext = 'Average Convergence Time for Global and Local Strategies'
else:
    titletext = 'Average Convergence Time'
if SINGLE_ROW:
    ax1.set_title(titletext)
else:
    title = f.suptitle(titletext, fontsize=18, fontweight='bold', x=.55, y=1.03)

ymax = 3500
ylim = (0, ymax)

# print the ylabels
if SINGLE_ROW:
    if LOGSCALE:
        ax1.set_yscale("log")
    else:
        ax1.set_ylim(ylim)
    ax1.set_ylabel('Average Convergence Time')
    text = f.text(0, 0, '', va='center', rotation='vertical')
else:
    ax1.set_title("Direct", fontsize=16)
    ax1.set_ylim(ylim)
    ax2.set_title("Random", fontsize=16)
    ax2.set_ylim(ylim)
    ax3.set_title("Multistep", fontsize=16)
    ax3.set_ylim(ylim)
    if INCLUDE_EXTRAS:
        ax4.set_title("Multistep Fixed", fontsize=16)
        ax4.set_ylim(ylim)
        ax5.set_title("Multistep, log scale", fontsize=16)
        ax5.set_yscale("log")
        ax6.set_title("Multistep, log scale", fontsize=16)
        ax6.set_yscale("log")
    else:
        ax4.set_title("Multistep, log scale", fontsize=16)
        ax4.set_yscale("log")
    text = f.text(0.04, 0.50, 'Average Convergence Time', va='center', rotation='vertical')

min = 0
max = 3500
interval = 500
if not LOGSCALE:
    ax1.set_yticks(np.arange(min, max+1, interval))
if not SINGLE_ROW:
    ax2.set_yticks(np.arange(min, max+1, interval))
    ax3.set_yticks(np.arange(min, max+1, interval))
    if INCLUDE_EXTRAS:
        ax4.set_yticks(np.arange(min, max+1, interval))

plt.xlabel("Placement Strategy")
plt.xticks(y_pos, placements)

# tune how close the plots are to one another
if not SINGLE_ROW:
    plt.tight_layout(rect=(0.05, 0, 1, 1))
else:
    f.subplots_adjust(hspace=0.4)

# hide the x-axes that is the not the last
plt.setp([a.get_xticklabels() for a in f.axes[:]], visible=True)

# legends
labels = ['Face', 'Offset Momentum', 'One Step Lookahead', 'Coordinated']
if INCLUDE_COUZIN:
    labels = labels + ['Couzin']
if SINGLE_ROW:
    labels = labels + ['Multistep']
if INCLUDE_GENETIC:
    labels = labels + ['G1', 'G2', 'G3', 'G4', 'G5', 'G6', 'G7']

if SINGLE_ROW:
    last_axis = ax1
else:
    last_axis = ax4

legendname = 'Local Behaviors'
if SINGLE_ROW:
    legendname = 'Behaviors'

ncol = 2
if INCLUDE_GENETIC:
    ncol=5
elif INCLUDE_COUZIN or SINGLE_ROW:
    ncol=3
anchor_box = (0., -1.9, 1., .102)
if SINGLE_ROW and not INCLUDE_GENETIC:
    anchor_box = (0., -.8, 1., .102)
elif SINGLE_ROW:
    anchor_box = (0., -.95, 1., .102)
if SINGLE_ROW:
    lgd = last_axis.legend(handles, labels, ncol=ncol, loc=8, bbox_to_anchor=anchor_box, title=legendname)
else:
    lgd = last_axis.legend(handles, labels, ncol=ncol, loc=8, bbox_to_anchor=anchor_box, title=legendname)
ax1.grid('on')
if not SINGLE_ROW:
    ax2.grid('on')
    ax3.grid('on')
    ax4.grid('on')
if INCLUDE_EXTRAS:
    ax5.grid('on')
    ax6.grid('on')
if INCLUDE_GENETIC:
    figname = 'largegenetic'
else:
    figname = 'large'
if SINGLE_ROW:
    figname = figname + 'singlerow'
if SINGLE_ROW_MULTISTEP:
    figname = figname + 'multistep'
if LOGSCALE:
    figname = figname + 'log'
if SINGLE_ROW:
    plt.savefig(figname, bbox_extra_artists=(lgd, text), bbox_inches='tight')
else:
    plt.savefig(figname, bbox_extra_artists=(lgd, text, title), bbox_inches='tight')
plt.show()
