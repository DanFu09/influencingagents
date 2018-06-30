# Influencing Agents for Flocking in Low-Density Settings

This directory contains the code used to run experiments for the paper
[Influencing Agents for Flocking in Low-Density Settings](https://arxiv.org/abs/1804.08667),
as well as the undergraduate thesis
[Design of Influencing Agents for Flocking in Low-Density Settings](http://www.danfu.org/files/SeniorThesis.pdf).

The seven ast files in this base directory are the seven genetic algorithms referred to
in the undergraduate thesis.
The algorithms discussed in the paper are, for the most part, implemented in the Flocker
class.

A few notes:
* Generally speaking, "ad hoc" agents refer to influencing agents.

* In the code, influencing agent behaviors are split into "global behaviors" and
"local behaviors". This comes from an earlier organizational distinction; for an
explanation of the differences between them, please refer to the appendix of the
undergraduate thesis.

## Running the Code:

First, download the [MASON](https://cs.gmu.edu/~eclab/projects/mason/) source code
and replace the "flockers" folder with this folder.
Next, download [Weka](https://www.cs.waikato.ac.nz/ml/weka/).
Finally, you will need to set CLASSPATH to include the mason base directory and weka.jar.

To compile:
```
make
```

To run:
```
bash runsim.sh
```