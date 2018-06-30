#!/usr/bin/env python3

import ast
import numpy as np
import subprocess
import re
import sys
import matplotlib.pyplot as plt
import random

FITNESS_PROPORTIONAL = 0
RANK_PROPORTIONAL = 1
TOP = 2

memoization = {}

def alg_to_string(alg):
    if alg == FITNESS_PROPORTIONAL:
        return "Fitness Proportional"
    elif alg == RANK_PROPORTIONAL:
        return "Rank Proportional"
    elif alg == TOP:
        return "Top"
    else:
        return "Unknown"

def evaluate_genome(genome):
    # run 10 trials of each cardinal direction
    # return fitness

    genome_str = str(genome)
    if genome_str in memoization:
        return memoization[genome_str]

    with open('tmpgenome', 'w') as genome_file:
        genome_file.write('AST\n')
        genome_file.write(genome_str + '\n')

    command = ('java sim.app.flockers.Flockers -experiment SMALL-{0} -for 100 -width 200 -height 200 '
               '-numFlockers 7 -numAdhoc 1 -placementX 100 -placementY 100 -placementRadius 0 '
               '-radius 10 -localBehavior GENETIC_tmpgenome -outputNumflocks false '
               '-outputNumLoners false -outputNumSameDir false -outputAverageAngleDiffFromGoal true')

    fitness = []

    processes = []
    for direction in ('NORTH', 'SOUTH', 'EAST', 'WEST'):
        processes.append(subprocess.Popen(command.format(direction), shell=True, stdout=subprocess.PIPE,
                                              stderr=subprocess.PIPE))
    for p in processes:
        lines = p.stdout.readlines()
        fitness.append(float(re.split('\t|\n', lines[2].decode('utf-8'))[1]))
        for line in p.stderr.readlines():
            print(line)

    memoization[genome_str] = np.average(fitness) + len(genome.nodes()) / 10.

    return memoization[genome_str]

    # return genome

def run_genomes(population):
    # run each genome once, returning new fitness values
    return [evaluate_genome(genome) for genome in population]

def evolve(num_epochs, population, num_reproductions, genome_method):
    # run population for num_epochs
    # reproduce num_reproductions every time; choose individuals to reproduce proportional
    #   to fitness value
    # have offspring replace individuals inversely proportional to fitness values
    # return populations and fitness values over time

    history = []

    for i in range(0, num_epochs):
        print(i, file=sys.stderr)
        fitness = run_genomes(population)
        history.append(([str(genome) for genome in population], fitness))
        print(history[len(history)-1], file=sys.stderr)

        if genome_method == FITNESS_PROPORTIONAL:
            fitness_converted = [180 - f for f in fitness]
            parents = np.random.choice(len(fitness), num_reproductions, fitness_converted, repalce=False)
            offspring = []
            for parent in parents:
                old_genome = population[parent].copy()
                offspring.append(ast.mutate(old_genome))
                # offspring.append(population[parent])
            replacements = np.random.choice(len(fitness), num_reproductions, fitness)
            for j in range(0, num_reproductions):
                population[replacements[j]] = offspring[j]
        elif genome_method == RANK_PROPORTIONAL:
            order = np.array(fitness).argsort()
            ranks = order.argsort().tolist()
            ranks_inverted = [len(ranks) - rank for rank in ranks]
            parents = np.random.choice(len(fitness), num_reproductions, ranks_inverted, replace=False)
            offspring = []
            for parent in parents:
                old_genome = population[parent].copy()
                offspring.append(ast.mutate(old_genome))
                # offspring.append(population[parent])
            replacements = np.random.choice(len(fitness), num_reproductions, ranks)
            for j in range(0, num_reproductions):
                population[replacements[j]] = offspring[j]
        elif genome_method == TOP:
            order = np.array(fitness).argsort()
            ranks = order.argsort().tolist()
            parents = []
            for j in range(0, len(ranks)):
                if ranks[j] < num_reproductions:
                    parents.append(j)
            offspring = []
            new_population = []
            for parent in parents:
                old_genome = population[parent].copy()
                offspring.append(old_genome)
                # offspring.append(population[parent])
                if len(new_population) < len(population):
                    new_population.append(old_genome)
            while len(new_population) < len(population):
                if random.random() < 0.5:
                    # select one child and mutate it
                    new_child = np.random.choice(offspring)
                    new_offspring = ast.mutate(new_child.copy())
                    # print('Mutating {0} into {1}'.format(str(new_child), str(new_offspring)))
                    new_population.append(new_offspring)
                else:
                    # select two children for crossover
                    new_parents = np.random.choice(offspring, 2, replace=False)
                    new_offspring = ast.crossover(new_parents[0], new_parents[1])
                    # print('Crossing {0} and {1} into {2} and {3}'.format(str(new_parents[0]), str(new_parents[1]),
                    #                                              str(new_offspring[0]), str(new_offspring[1])))
                    new_population.append(new_offspring[0])
                    if len(new_population) < len(population):
                        new_population.append(new_offspring[1])
            population = new_population

    return history

def main():
    random.seed()

    population_size = 100
    generations = 300
    num_reproductions = 20
    algorithm = TOP

    population = [ast.seed_genome() for i in range(0, population_size)]
    # population = [random.randrange(0, 360) for i in range(0, population_size)]

    history= evolve(generations, population, num_reproductions, TOP)

    print("Population size: {0}, generations: {1}, reproductions: {2}, alg: {3}".format(
        population_size, generations, num_reproductions, alg_to_string(algorithm)))
    print(history)

    average_fitness = [np.average(epoch[1]) for epoch in history]
    best_fitness = [min(epoch[1]) for epoch in history]
    plt.plot(average_fitness, label="Average for epoch")
    plt.plot(best_fitness, label="Best for epoch")
    plt.title('Difference from goal over time, population size {0}'.format(population_size))
    plt.xlabel('Generation')
    plt.ylabel('Difference in angle from the goal')
    plt.legend()
    plt.savefig('genetic_fitness.png')
    plt.show()

if __name__ == "__main__":
    main()
