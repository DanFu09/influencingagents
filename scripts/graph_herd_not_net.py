import matplotlib.pyplot as plt; plt.rcdefaults()
import numpy as np
import matplotlib.pyplot as plt
import csv
from collections import defaultdict 
import matplotlib.cm as cm
from matplotlib.font_manager import FontProperties
import sys

INCLUDE_STDERR = True
INCLUDE_GLOBALLOCAL = False

# get file location
file_name = 'herd_aggregated.csv'
if len(sys.argv) > 1:
    file_name = sys.argv[1]

# definition
placements = ('Border 500', 'Border 750', 'Random 500', 'Random 750', 'Grid 500', 'Grid 750', 'K Means')
global_strategies = ("CIRCLE",
    "MULTIHERD-CIRCLE LARGE",
    "POLYGON10")
local_strategies = ('FACE_EAST')
y_pos = np.arange(len(placements))

# first do processing for herd data
# median time to convergence 

# ls = global strategies
ls1 = np.zeros((len(placements), len(global_strategies)))

lserr1 = np.zeros((len(placements), len(global_strategies)))

with open(file_name, 'rb') as file:
    reader = csv.reader(file.read().splitlines())
    count = 0
    for row in reader:
        if row[0] == 'Placement':
            count += 1

        if count == 4:
            # finished
            break

        if count == 1:
            if row[1] == 'Radius':
                continue
            p = row[0]

            radius = row[1]

            global_strategy = row[2]
            local_strategy = row[3]
            time_50 = row[8]

            # net strategy == if global is FACE
            if global_strategy != 'FACE' and local_strategy in local_strategies:
                print "global", global_strategy, "local", local_strategy, "time_50", time_50

                placement = 0
            #    print "radius", radius, "p", p
                radius = int(radius)
                if radius == 500 and p == 'BORDER_CIRCLE':
                    placement = 0
                #    print "++++++++++++YAY"
                elif radius == 500 and p == 'RANDOM_CIRCLE':
                #    print "++++++++++++YAY"
                    placement = 2
                elif radius == 500 and p == 'GRID_CIRCLE':
                #    print "++++++++++++YAY"
                    placement = 4
                elif radius == 750 and p == 'BORDER_CIRCLE':
                    placement = 1
                elif radius == 750 and p == 'RANDOM_CIRCLE':
                    placement = 3
                elif radius == 750 and p == 'GRID_CIRCLE':
                    placement = 5
                elif p == 'K_MEANS':
                    placement = 6

                l = len("MULTIHERD-CIRCLE-MIN_AVG_DIR_NEIGH-")

                b = str(global_strategy[-3:])
                a = global_strategy[l:]
                a = a[:3]
            #    if global_strategy[0] == 'M':
            #        print "first", a, "second", b

                c = "1"
                d = "1"
                if radius == 500:
                    c = "750"
                    d = "900"

                elif radius == 750:

                    c = "900"
                    d = "110"

            #    print "C", c, "D", d, "a", a, "b", b
            #    print "ls", ls
                print a, "a", b, "b"
                gs = 1
                if global_strategy == "CIRCLE":
                    gs = 0

                elif global_strategy == "POLYGON10":
                    gs = 2


                elif radius == 500:
                    if a == '750' and b == '750':
                        continue
                    elif a == '750' and b == '900':
                        continue
                    elif a == '900' and b == '900':
                        gs = 1
                    else:
                        continue

                elif radius == 750:
                    if a == '900' and b == '900':
                        continue
                    elif a == '900' and b == '100':
                        continue
                    elif a == '110' and b == '100':
                        gs = 1
                    else:
                        continue

                ls1[placement][gs] = float(time_50)

        elif count == 3:
            if row[1] == 'Radius':
                continue
            p = row[0]

            radius = row[1]

            global_strategy = row[2]
            local_strategy = row[3]
            time_50 = row[8]

            # net strategy == if global is FACE
            if global_strategy != 'FACE' and local_strategy in local_strategies:
                print "global", global_strategy, "local", local_strategy, "time_50", time_50

                placement = 0
                #    print "radius", radius, "p", p
                radius = int(radius)
                if radius == 500 and p == 'BORDER_CIRCLE':
                    placement = 0
                #    print "++++++++++++YAY"
                elif radius == 500 and p == 'RANDOM_CIRCLE':
                    #    print "++++++++++++YAY"
                    placement = 2
                elif radius == 500 and p == 'GRID_CIRCLE':
                    #    print "++++++++++++YAY"
                    placement = 4
                elif radius == 750 and p == 'BORDER_CIRCLE':
                    placement = 1
                elif radius == 750 and p == 'RANDOM_CIRCLE':
                    placement = 3
                elif radius == 750 and p == 'GRID_CIRCLE':
                    placement = 5
                elif p == 'K_MEANS':
                    placement = 6

                l = len("MULTIHERD-CIRCLE-MIN_AVG_DIR_NEIGH-")

                b = str(global_strategy[-3:])
                a = global_strategy[l:]
                a = a[:3]
                #    if global_strategy[0] == 'M':
                #        print "first", a, "second", b

                c = "1"
                d = "1"
                if radius == 500:
                    c = "750"
                    d = "900"

                elif radius == 750:

                    c = "900"
                    d = "110"

                    #    print "C", c, "D", d, "a", a, "b", b
                    #    print "ls", ls
                print a, "a", b, "b"
                gs = 1
                if global_strategy == "CIRCLE":
                    gs = 0

                elif global_strategy == "POLYGON10":
                    gs = 2


                elif radius == 500:
                    if a == '750' and b == '750':
                        continue
                    elif a == '750' and b == '900':
                        continue
                    elif a == '900' and b == '900':
                        gs = 1

                elif radius == 750:
                    if a == '900' and b == '900':
                        continue
                    elif a == '900' and b == '100':
                        continue
                    elif a == '110' and b == '100':
                        gs = 1

                lserr1[placement][gs] = float(time_50)



space = 0
width = (1.0 - space) / len(y_pos)

plt.close('all')
# Three subplots sharing both x/y axes
f, (ax1) = plt.subplots(1, sharex=True, sharey=True, figsize=(12, 2.5))


# a1 = face 
# a2 = Random
# a3 = multi t=150
# a4 = multi t=200
n = len(global_strategies)
print ls1
print "number of local strategies", n
for i, local in enumerate(global_strategies):
    print local
    pos = [j - (1 - space) / 2. + i * width -0.71 for j in range(1,1+len(placements))]
    print cm.Accent(float(i) / n)
    if INCLUDE_STDERR:
        ax1.bar(pos, ls1[:, i], width=width, label=local, color=cm.Accent(float(i) / n), yerr=lserr1[:, i])
    else:
        ax1.bar(pos, ls1[:, i], width=width, label=local, color=cm.Accent(float(i) / n))

# printing the legend
handles, labels = ax1.get_legend_handles_labels()
ax1.set_title("Average Number of Agents Under Influencing Agent Control after 15K Steps (Stationary)")

# print the ylabels
ax1.set_ylabel('Average Number of Agents')
text = f.text(0, 0, '', va='center', rotation='vertical')


plt.xlabel("Placement Strategy")
plt.xticks(y_pos, placements)

# tune how close the plots are to one another
f.subplots_adjust(hspace=0.4)

# hide the x-axes that is the not the last
plt.setp([a.get_xticklabels() for a in f.axes[:]], visible=True)

if INCLUDE_GLOBALLOCAL:
    legend = "Global Behaviors"
    figname = 'herd_stationary'
else:
    legend = "Behaviors"
    figname = 'herd_stationary_nogloballocal'

# legends
labels = ['Circle', 'Multicircle', 'Polygon']
lgd = ax1.legend(handles, labels, ncol=3, loc=8, bbox_to_anchor=(0., -0.65, 1., .102),
    title=legend)
ax1.grid('on')
plt.savefig(figname, bbox_extra_artists=(lgd, text), bbox_inches='tight')
plt.show()
