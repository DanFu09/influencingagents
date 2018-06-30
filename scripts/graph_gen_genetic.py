import numpy as np
import matplotlib.pyplot as plt
import sys

file_name = 'data'
if len(sys.argv) > 1:
    file_name = sys.argv[1]



with open(file_name, 'rb') as file:
    header = file.readline()
    # Population size: 25, generations: 50, reproductions: 10, alg: Top
    params = header.split(', ')
    population_size = int(params[0].split(' ')[-1])

    data = file.readline()

    history = eval(data)

    average_fitness = [np.average(epoch[1]) for epoch in history]
    best_fitness = [min(epoch[1]) for epoch in history]
    face_fitness = [66.08 for i in xrange(0, len(history))]
    offset_fitness = [69.19 for i in xrange(0, len(history))]
    one_step_fitness = [52.32 for i in xrange(0, len(history))]
    couzin_fitness = [72.3 for i in xrange(0, len(history))]

    plt.plot(average_fitness, label="Average for epoch")
    plt.plot(best_fitness, label="Best for epoch")
    plt.plot(face_fitness, label="Face")
    plt.plot(offset_fitness, label="Offset Momentum")
    plt.plot(one_step_fitness, label="One Step Lookahead")
    plt.plot(couzin_fitness, label="Couzin")

    plt.title('Angle difference from goal over time, population size {0}'.format(population_size))
    plt.xlabel('Generation')
    plt.ylabel('Difference in angle from the goal')
    plt.ylim((40, 100))
    plt.legend()
    plt.savefig('genetic_fitness.png')
    plt.show()

