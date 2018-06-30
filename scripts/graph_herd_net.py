import matplotlib.pyplot as plt; plt.rcdefaults()
import numpy as np
import matplotlib.pyplot as plt
import csv
from collections import defaultdict 
import matplotlib.cm as cm
from matplotlib.font_manager import FontProperties
import sys

INCLUDE_EXTRA = False
INCLUDE_GENETIC = False
INCLUDE_GLOBALLOCAL = False
INCLUDE_STDERR = True
INCLUDE_COUZIN = True

# get file location
file_name = 'herd_aggregated.csv'
if len(sys.argv) > 1:
    file_name = sys.argv[1]

# definition
placements = ('Border 500', 'Border 750', 'Random 500', 'Random 750', 'Grid 500', 'Grid 750', 'K Means')
global_strategies = ('Direct', 'Random', 'Circle-Net')
genetic = ['GENETIC_ast01',
           'GENETIC_ast02', 'GENETIC_ast03', 'GENETIC_ast04', 'GENETIC_ast05', 'GENETIC_ast06',
           'GENETIC_ast07']
local_strategies = ['FACE_EAST', 'OFFSET_MOMENTUM', 'ONE_STEP_LOOKAHEAD', 'COORDINATED']
if INCLUDE_COUZIN:
    local_strategies = local_strategies + ['COUZIN']
if INCLUDE_GENETIC:
    local_strategies = local_strategies + genetic
y_pos = np.arange(len(placements))

# first do processing for herd data
# median time to convergence 

# gs = global strategies
gs1 = np.zeros((len(placements), len(local_strategies)))
gs2 = np.zeros((len(placements), len(local_strategies)))
gs3 = np.zeros((len(placements), len(local_strategies)))
gs4 = np.zeros((len(placements), len(local_strategies)))

gs1err = np.zeros((len(placements), len(local_strategies)))
gs2err = np.zeros((len(placements), len(local_strategies)))
gs3err = np.zeros((len(placements), len(local_strategies)))
gs4err = np.zeros((len(placements), len(local_strategies)))

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

            radius = row[1]

            global_strategy = row[2]
            local_strategy = row[3]
            time_50 = row[8]

            # net strategy == if global is FACE
            if global_strategy == 'FACE' or global_strategy == 'RANDOM'\
                    or global_strategy == 'MULTIHERD-CIRCLE-MIN_AVG_DIR_NEIGH-0-FACE-0':

                if local_strategy not in local_strategies:
                    continue
                ls = local_strategies.index(local_strategy)

                placement = 0
                print "radius", radius, "p", p
                radius = int(radius)
                if radius == 500 and p == 'BORDER_CIRCLE':
                    placement = 0
                    print "++++++++++++YAY"
                elif radius == 500 and p == 'RANDOM_CIRCLE':
                    print "++++++++++++YAY"
                    placement = 2
                elif radius == 500 and p == 'GRID_CIRCLE':
                    print "++++++++++++YAY"
                    placement = 4
                elif radius == 750 and p == 'BORDER_CIRCLE':
                    placement = 1
                elif radius == 750 and p == 'RANDOM_CIRCLE':
                    placement = 3
                elif radius == 750 and p == 'GRID_CIRCLE':
                    placement = 5
                elif p == 'K_MEANS':
                    placement = 6

                if local_strategy > -1:
                    print p, radius, global_strategy, local_strategy, time_50
                    print "placement", placement, "ls", ls
                    if global_strategy == 'FACE':
                        gs1[placement][ls] = time_50
                    elif global_strategy == 'RANDOM':
                        gs2[placement][ls] = time_50
                    elif global_strategy == 'MULTIHERD-CIRCLE-MIN_AVG_DIR_NEIGH-0-FACE-0':
                        gs3[placement][ls] = time_50

        if count == 3:
            # define each column
            p = row[0]

            radius = row[1]

            global_strategy = row[2]
            local_strategy = row[3]
            time_50 = row[8]

            # net strategy == if global is FACE
            if global_strategy == 'FACE' or global_strategy == 'RANDOM' \
                    or global_strategy == 'MULTIHERD-CIRCLE-MIN_AVG_DIR_NEIGH-0-FACE-0':

                if local_strategy not in local_strategies:
                    continue
                ls = local_strategies.index(local_strategy)

                placement = 0
                print "radius", radius, "p", p
                radius = int(radius)
                if radius == 500 and p == 'BORDER_CIRCLE':
                    placement = 0
                    print "++++++++++++YAY"
                elif radius == 500 and p == 'RANDOM_CIRCLE':
                    print "++++++++++++YAY"
                    placement = 2
                elif radius == 500 and p == 'GRID_CIRCLE':
                    print "++++++++++++YAY"
                    placement = 4
                elif radius == 750 and p == 'BORDER_CIRCLE':
                    placement = 1
                elif radius == 750 and p == 'RANDOM_CIRCLE':
                    placement = 3
                elif radius == 750 and p == 'GRID_CIRCLE':
                    placement = 5
                elif p == 'K_MEANS':
                    placement = 6

                if local_strategy > -1:
                    print p, radius, global_strategy, local_strategy, time_50
                    print "placement", placement, "ls", ls
                    if global_strategy == 'FACE':
                        gs1err[placement][ls] = time_50
                    elif global_strategy == 'RANDOM':
                        gs2err[placement][ls] = time_50
                    elif global_strategy == 'MULTIHERD-CIRCLE-MIN_AVG_DIR_NEIGH-0-FACE-0':
                        gs3err[placement][ls] = time_50


space = 0
if INCLUDE_GENETIC:
    width = 0.5 * (1.0 - space) / len(y_pos)
else:
    width = (1.0 - space) / len(y_pos)

plt.close('all')
# Three subplots sharing both x/y axes
if INCLUDE_EXTRA:
    f, (ax1, ax2, ax3) = plt.subplots(3, sharex=True, sharey=True, figsize=(12, 6))
else:
    f, (ax1) = plt.subplots(1, sharex=True, sharey=True, figsize=(12, 2.5))


# a1 = face 
# a2 = Random
# a3 = multi t=150
# a4 = multi t=200
n = len(local_strategies)
for i, local in enumerate(local_strategies):
    if INCLUDE_GENETIC:
        pos = [j - (1 - space) / 2. + i * width -0.92 for j in range(1,1+len(placements))]
    elif INCLUDE_COUZIN:
        pos = [j - (1 - space) / 2. + i * width -0.85 for j in range(1,1+len(placements))]
    else:
        pos = [j - (1 - space) / 2. + i * width -0.78 for j in range(1,1+len(placements))]
    if INCLUDE_STDERR:
        ax1.bar(pos, gs1[:, i], width=width, label=local, color=cm.Accent(float(i) / n),yerr=gs1err[:,i])
        if INCLUDE_EXTRA:
            ax2.bar(pos, gs2[:, i], width=width, label=local, color=cm.Accent(float(i) / n),yerr=gs2err[:,i])
            ax3.bar(pos, gs3[:, i], width=width, label=local, color=cm.Accent(float(i) / n),yerr=gs3err[:,i])
        # ax4.bar(pos, gs4[:, i], width=width, label=local, color=cm.Accent(float(i) / n))
    else:
        ax1.bar(pos, gs1[:, i], width=width, label=local, color=cm.Accent(float(i) / n))
        if INCLUDE_EXTRA:
            ax2.bar(pos, gs2[:, i], width=width, label=local, color=cm.Accent(float(i) / n))
            ax3.bar(pos, gs3[:, i], width=width, label=local, color=cm.Accent(float(i) / n))
            # ax4.bar(pos, gs4[:, i], width=width, label=local, color=cm.Accent(float(i) / n))

# printing the legend
handles, labels = ax1.get_legend_handles_labels()
if INCLUDE_EXTRA:
    title = f.suptitle('Average Number of Agents Under Influencing Agent Control after 15K Steps (Net)', fontsize=18, fontweight='bold', x=.55, y=1.03)
else:
    ax1.set_title("Average Number of Agents Under Influencing Agent Control after 15K Steps (Net)")

# print the ylabels
if INCLUDE_EXTRA:
    ax1.set_title("Direct")
    ax2.set_title("Random")
    ax3.set_title("Circle-Net")
    # ax4.set_ylabel("Multi T=200")
    text = f.text(0.04, 0.50, 'Average Number of Agents', va='center', rotation='vertical')
else:
    ax1.set_ylabel("Average Number of Agents")
    text = f.text(0, 0, '', va='center', rotation='vertical')
ax1.set_ylim(0, 140)

plt.xlabel("Placement Strategy")
plt.xticks(y_pos, placements)

# tune how close the plots are to one another
#f.subplots_adjust(hspace=0.4)
if INCLUDE_EXTRA:
    plt.tight_layout(rect=(0.05, 0, 1, 1))

# hide the x-axes that is the not the last
plt.setp([a.get_xticklabels() for a in f.axes[:]], visible=True)


# legends
labels = ['Face', 'Offset Momentum', 'One Step Lookahead', 'Coordinated']
if INCLUDE_COUZIN:
    labels = labels + ['COUZIN']
genetic_labels = ['G1', 'G2', 'G3', 'G4', 'G5', 'G6', 'G7']
if INCLUDE_GENETIC:
    labels = labels + genetic_labels
if INCLUDE_GLOBALLOCAL:
    legend_text = 'Local Behaviors'
else:
    legend_text = 'Behaviors'
if INCLUDE_EXTRA:
    lgd = ax3.legend(handles, labels, ncol=5, loc=8, bbox_to_anchor=(0., -1.2, 1., .102),
        title=legend_text)
else:
    ncol = 2
    if INCLUDE_COUZIN:
        ncol = 3
    anchor_box=(0., -0.8, 1., .102)
    if INCLUDE_GENETIC:
        ncol=4
        anchor_box = bbox_to_anchor=(0., -0.95, 1., .102)
    lgd = ax1.legend(handles, labels, ncol=ncol, loc=8, bbox_to_anchor=anchor_box,
        title=legend_text)
ax1.grid('on')
if INCLUDE_EXTRA:
    ax2.grid('on')
    ax3.grid('on')
if INCLUDE_GENETIC:
    figname = 'herd_net_genetic'
else:
    figname = 'herd_net'
if INCLUDE_EXTRA:
    plt.savefig(figname, bbox_extra_artists=(lgd, text, title), bbox_inches='tight')
else:
    plt.savefig(figname, bbox_extra_artists=(lgd, text), bbox_inches='tight')
plt.show()
