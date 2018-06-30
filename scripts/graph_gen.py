import matplotlib.pyplot as plt; plt.rcdefaults()
import numpy as np
import matplotlib.pyplot as plt
import csv
from collections import defaultdict 
import matplotlib.cm as cm

# definitions
placements = ('Random', 'Grid', 'K-Means')
global_strategies = ('FACE', 'RANDOM', 'MULTILARGE-150-MIN_AVG_DIR_NEIGH', 'MULTILARGE-200-MIN_AVG_DIR_NEIGH')
local_strategies = ('FACE_EAST', 'OFFSET_MOMENTUM', 'ONE_STEP_LOOKAHEAD', 'COORDINATED')
y_pos = np.arange(len(placements))

# first do processing for large data
# median time to convergence 

# gs = global strategies
gs1 = np.zeros((len(placements), len(local_strategies)))
gs2 = np.zeros((len(placements), len(local_strategies)))
gs3 = np.zeros((len(placements), len(local_strategies)))
gs4 = np.zeros((len(placements), len(local_strategies)))

with open('sample_flocking_data.csv', 'rb') as file:
    reader = csv.reader(file.read().splitlines())
    count = 0
    for row in reader:
        if count == 2:
            # finished
            break

        if count == 1:
            # define each column
            p = row[0]
            global_strategy = row[1]
            local_strategy = row[2]
            time_50 = row[8]

            if local_strategy == 'FACE_EAST':
                ls = 0
            elif local_strategy == 'OFFSET_MOMENTUM':
                ls = 1
            elif local_strategy == 'ONE_STEP_LOOKAHEAD':
                ls = 2
            elif local_strategy == 'COORDINATED':
                ls = 3

            if p == 'RANDOM_RECT':
                placement = 0
            elif p == 'GRID_RECT':
                placement = 1
            elif p == 'K_MEANS':
                placement = 2

            string_length = len('MULTILARGE-150-MIN_AVG_DIR_NEIGH')
            if local_strategy > -1:
                if global_strategy == 'FACE':
                    gs1[placement][ls] = time_50
                elif global_strategy == 'RANDOM':
                    gs2[placement][ls] = time_50
                elif global_strategy[:string_length] == 'MULTILARGE-150-MIN_AVG_DIR_NEIGH':
                    gs3[placement][ls] = time_50
                elif global_strategy[:string_length] == 'MULTILARGE-150-MIN_AVG_DIR_NEIGH':
                    gs4[placement][ls] = time_50
        if row[0] == 'Placement':
            count += 1

space = 0.5
width = (1.0 - space) / len(y_pos)

plt.close('all')
# Three subplots sharing both x/y axes
f, (ax1, ax2, ax3, ax4) = plt.subplots(4, sharex=True, sharey=True)


# a1 = face 
# a2 = Random
# a3 = multi t=150
# a4 = multi t=200
n = len(local_strategies)
for i, local in enumerate(local_strategies):
    print placement
    pos = [j - (1 - space) / 2. + i * width -1 for j in range(1,1+len(placements))]
    ax1.bar(pos, gs1[:, i], width=width, label=local, color=cm.Accent(float(i) / n))
    ax2.bar(pos, gs2[:, i], width=width, label=local, color=cm.Accent(float(i) / n))
    ax3.bar(pos, gs3[:, i], width=width, label=local, color=cm.Accent(float(i) / n))
    ax4.bar(pos, gs4[:, i], width=width, label=local, color=cm.Accent(float(i) / n))

# printing the legend
handles, labels = ax1.get_legend_handles_labels()
ax1.set_title("Median Convergence Time for Global and Local Strategies")

# print the ylabels
ax1.set_ylabel("Face")
ax2.set_ylabel("Random")
ax3.set_ylabel("Multi T=150")
ax4.set_ylabel("Multi T=200")
text = f.text(0, 0.5, 'Global Strategy', va='center', rotation='vertical')


plt.xlabel("Placement Strategy")
plt.xticks(y_pos, placements)

# tune how close the plots are to one another
f.subplots_adjust(hspace=0.4)

# hide the x-axes that is the not the last
plt.setp([a.get_xticklabels() for a in f.axes[:]], visible=True)

# legends
lgd = ax4.legend(handles, labels, loc='upper center', bbox_to_anchor=(0.5,-0.5), title='Local Strategies')
ax4.grid('on')
plt.savefig('samplefigure', bbox_extra_artists=(lgd, text), bbox_inches='tight')
plt.show()
