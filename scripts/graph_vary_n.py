import matplotlib.pyplot as plt; plt.rcdefaults()
import numpy as np
import matplotlib.pyplot as plt
import csv
from collections import defaultdict
import matplotlib.cm as cm
from matplotlib.font_manager import FontProperties
import sys
import os

'''
 usage: python graph_vary_n.py folder_name output_format scenario [xmin xmax]

 looks for folder_name/flockAvg.csv, folder_name/loneAvg.csv,
 creates output_format + flock.csv, output_format + lone.csv
'''

folder_name = ''
output_format = ''
scenario = 'Large'
xmin = 0
xmax = 0
if len(sys.argv) >= 4:
    folder_name = sys.argv[1]
    output_format = sys.argv[2]
    scenario = sys.argv[3]
if len(sys.argv) > 4:
    xmin = int(sys.argv[4])
    xmax = int(sys.argv[5])

'''
Creates a graph from file into output, title will be:
 "Average number of [data_term] in the [scenario] setting"

 Y axis will be: "Number of [data_term]"

 X axis will be: "Timesteps"

 Legend will be "50 Agents, 100 Agents, etc"
'''
def make_plot(file_avgs, file_sems, output_name, data_term, scenario, include_legend):
    labels = ('50', '100', '150', '200', '250', '300')
    legend_labels = [label + ' Agents' for label in labels]

    data = {'Step': []}
    yerr = {}
    for label in labels:
        data[label] = []
        yerr[label] = []

    with open(file_avgs) as csvfile:
        reader = csv.DictReader(csvfile)
        for row in reader:
            data['Step'].append(row['Step'])
            for label in labels:
                data[label].append(row[label])
    with open(file_sems) as csvfile:
        reader = csv.DictReader(csvfile)
        for row in reader:
            for label in labels:
                yerr[label].append(row[label])

    fig, ax = plt.subplots(1, figsize=(4,3))

    for label in labels:
        ax.plot(data['Step'], data[label], linewidth = 2, label=label)

    ax.set_title('Average # of {0}\n{1} Setting'.format(data_term, scenario), fontsize=16)
    ax.set_ylabel('Number of {0}'.format(data_term), fontsize=16)
    ax.set_xlabel('Timesteps', fontsize=16)
    if xmax != 500:
        ax.set_xticks(np.arange(xmin, xmax + 1, 2000))
    plt.xticks(fontsize=14)
    plt.yticks(fontsize=14)
    if xmin != 0 or xmax != 0:
        ax.set_xlim(xmin, xmax)
    handles, labels = ax.get_legend_handles_labels()
    text = fig.text(0, 0, '', va='center', rotation='vertical')
    if include_legend:
        lgd = ax.legend(handles, legend_labels, title='Number of Agents',
                        bbox_to_anchor=(1.05, 1), loc=2, borderaxespad=0.)
    ax.grid('on')
    plt.savefig(output_name, bbox_inches='tight')
    plt.show()
    plt.close()

make_plot(os.path.join(folder_name, 'flockAvg.csv'), os.path.join(folder_name, 'flockStd.csv'),
          output_format + 'flockWithLegend.png', 'Flocks', scenario, True)
make_plot(os.path.join(folder_name, 'loneAvg.csv'), os.path.join(folder_name, 'loneStd.csv'),
          output_format + 'loneWithLegend.png', 'Lone Agents', scenario, True)
make_plot(os.path.join(folder_name, 'flockAvg.csv'), os.path.join(folder_name, 'flockStd.csv'),
          output_format + 'flockNoLegend.png', 'Flocks', scenario, False)
make_plot(os.path.join(folder_name, 'loneAvg.csv'), os.path.join(folder_name, 'loneStd.csv'),
          output_format + 'loneNoLegend.png', 'Lone Agents', scenario, False)
