# TSP Benchmarking Framework

This project provides a complete benchmarking framework for comparing multiple Traveling Salesman Problem (TSP) algorithms using multi-threading, Java thread pools, and speedup analysis. It supports TSPLIB datasets, parallel execution, and automated result reporting.

## Features

- **Multi-threaded Benchmarking System:** Utilizes `java.util.concurrent` to run algorithms in parallel.
- **Java ThreadPool (ExecutorService):** Efficiently manages threads for parallel execution (scaling from 1 to 16 threads).
- **Speedup Calculation:** Automatically calculates speedup metrics ($T_1 / T_n$) for each algorithm and thread count.
- **TSPLIB Support:** Parses standard `.tsp` datasets (specifically `NODE_COORD_SECTION`).
- **Automated Reporting:** Exports performance data to `tsp_results.txt` and saves the best routes to separate files.
- **Multiple Heuristics:** Includes implementations of Greedy, 2-Opt, Simulated Annealing, Genetic Algorithm, and Ant Colony Optimization.

## 🧠 Implemented Algorithms

The framework benchmarks the following algorithms:

1.  **Greedy (Nearest Neighbor):** A constructive heuristic using a Multi-Start strategy.
2.  **2-Opt (Local Search):** An iterative improvement algorithm that untangles crossed paths.
3.  **Simulated Annealing (SA):** A probabilistic technique to escape local optima using a cooling schedule.
4.  **Genetic Algorithm (GA):** An evolutionary approach using tournament selection and mutation.
5.  **Ant Colony Optimization (ACO):** A simplified construction heuristic based on ant behavior concepts.


## 📈Performance Analysis
The framework runs each algorithm with the following thread configurations to measure scalability: {1, 2, 4, 6, 8, 12, 16} threads.

This allows for the observation of Amdahl's Law and the efficiency of parallel execution for stochastic search algorithms.
