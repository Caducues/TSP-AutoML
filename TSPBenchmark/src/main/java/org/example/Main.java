package org.example;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

interface TSPAlgorithm {
    Tour solve(Tour initialTour, Random random);
    String getName();
}

class City {
    private int x, y;

    public City(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public double distanceTo(City c) {
        int dx = this.x - c.x;
        int dy = this.y - c.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public int getX() { return x; }
    public int getY() { return y; }

    @Override
    public String toString() { return "(" + x + "," + y + ")"; }
}

class Tour {
    private List<City> tour = new ArrayList<>();
    private double distance = 0;

    public Tour() {}

    public Tour(List<City> tour) {
        this.tour = new ArrayList<>(tour);
    }

    public List<City> getTour() {
        return tour;
    }

    public void generateIndividual() {
        Collections.shuffle(tour);
        distance = 0;
    }

    public void addCity(City c) {
        tour.add(c);
        distance = 0;
    }

    public City getCity(int index) {
        return tour.get(index);
    }

    public void setCity(int index, City c) {
        tour.set(index, c);
        distance = 0;
    }

    public int tourSize() {
        return tour.size();
    }

    public double getDistance() {
        if (distance == 0) {
            double d = 0;
            for (int i = 0; i < tourSize(); i++) {
                City from = getCity(i);
                City to = (i + 1 < tourSize()) ? getCity(i + 1) : getCity(0);
                d += from.distanceTo(to);
            }
            distance = d;
        }
        return distance;
    }

    @Override
    public Tour clone() {
        return new Tour(new ArrayList<>(this.tour));
    }
}

// --- Greedy (Nearest Neighbor) ---
class GreedyAlgorithm implements TSPAlgorithm {
    @Override
    public String getName() { return "Greedy (Multi-Start)"; }

    @Override
    public Tour solve(Tour initialTour, Random random) {
        List<City> unvisited = new ArrayList<>(initialTour.getTour());
        Tour t = new Tour();

        City current = unvisited.remove(random.nextInt(unvisited.size()));
        t.addCity(current);

        while (!unvisited.isEmpty()) {
            City nearest = null;
            double minDist = Double.MAX_VALUE;
            for (City c : unvisited) {
                double d = current.distanceTo(c);
                if (d < minDist) {
                    minDist = d;
                    nearest = c;
                }
            }
            unvisited.remove(nearest);
            t.addCity(nearest);
            current = nearest;
        }
        return t;
    }
}

// --- 2-Opt ---
class TwoOptAlgorithm implements TSPAlgorithm {
    @Override
    public String getName() { return "2-Opt (Random-Start)"; }

    @Override
    public Tour solve(Tour initialTour, Random random) {
        Tour current = initialTour.clone();
        current.generateIndividual();

        boolean improved = true;
        int maxIterations = 1000;
        int iteration = 0;

        while (improved && iteration < maxIterations) {
            improved = false;
            iteration++;

            int size = current.tourSize();

            for (int i = 0; i < size - 1; i++) {
                for (int k = i + 1; k < size; k++) {
                    City c1 = current.getCity(i);
                    City c2 = current.getCity((i + 1) % size);
                    City c3 = current.getCity(k);
                    City c4 = current.getCity((k + 1) % size);

                    double oldDist = c1.distanceTo(c2) + c3.distanceTo(c4);
                    double newDist = c1.distanceTo(c3) + c2.distanceTo(c4);

                    if (newDist < oldDist) {
                        twoOptReverse(current, i + 1, k);
                        improved = true;
                    }
                }
            }
        }
        return current;
    }

    private void twoOptReverse(Tour tour, int i, int k) {
        while (i < k) {
            City temp = tour.getCity(i);
            tour.setCity(i, tour.getCity(k));
            tour.setCity(k, temp);
            i++;
            k--;
        }
    }
}

// --- Simulated Annealing (DÜZELTİLMİŞ) ---
class SimulatedAnnealingAlgorithm implements TSPAlgorithm {
    @Override
    public String getName() { return "Simulated Annealing"; }

    @Override
    public Tour solve(Tour initialTour, Random random) {
        Tour current = initialTour.clone();
        current.generateIndividual();
        Tour best = current.clone();

        int n = current.tourSize();

        // Sabit iterasyon sayısı
        int maxIterations = 50000;
        double startTemp = 100000; // Yüksek başlangıç sıcaklığı
        double endTemp = 0.001;
        double coolingRate = Math.pow(endTemp / startTemp, 1.0 / maxIterations);
        double temp = startTemp;

        for (int iter = 0; iter < maxIterations; iter++) {
            // 2-opt hamlesi
            int i = random.nextInt(n - 1);
            int k = i + 1 + random.nextInt(n - i - 1);

            // Mevcut mesafeyi hesapla (sadece değişen kenarlar)
            City c1 = current.getCity(i);
            City c2 = current.getCity(i + 1);
            City c3 = current.getCity(k);
            City c4 = current.getCity((k + 1) % n);

            double oldDist = c1.distanceTo(c2) + c3.distanceTo(c4);
            double newDist = c1.distanceTo(c3) + c2.distanceTo(c4);
            double delta = newDist - oldDist;

            // Kabul et veya reddet
            if (delta < 0 || Math.exp(-delta / temp) > random.nextDouble()) {
                // Reverse yap
                int left = i + 1;
                int right = k;
                while (left < right) {
                    City tmp = current.getCity(left);
                    current.setCity(left, current.getCity(right));
                    current.setCity(right, tmp);
                    left++;
                    right--;
                }

                // En iyiyi güncelle
                if (current.getDistance() < best.getDistance()) {
                    best = current.clone();
                }
            }

            temp *= coolingRate;
        }
        return best;
    }
}

// --- Genetic Algorithm (Basitleştirilmiş - Sadece mutation) ---
class GeneticAlgorithm implements TSPAlgorithm {
    @Override
    public String getName() { return "Genetic Algorithm"; }

    @Override
    public Tour solve(Tour initialTour, Random random) {
        int popSize = 50;
        int generations = 500;
        double mutationRate = 0.15;

        List<Tour> population = new ArrayList<>();
        for (int i = 0; i < popSize; i++) {
            Tour t = initialTour.clone();
            t.generateIndividual();
            population.add(t);
        }

        Tour best = population.get(0);

        for (int gen = 0; gen < generations; gen++) {
            List<Tour> nextGen = new ArrayList<>();

            for (int i = 0; i < popSize; i++) {
                // Tournament selection
                Tour parent = tournamentSelect(population, random);
                Tour child = parent.clone();

                // Mutation: Swap iki şehir
                if (random.nextDouble() < mutationRate) {
                    int a = random.nextInt(child.tourSize());
                    int b = random.nextInt(child.tourSize());
                    City temp = child.getCity(a);
                    child.setCity(a, child.getCity(b));
                    child.setCity(b, temp);
                }

                // 2-opt mutation (bazen)
                if (random.nextDouble() < 0.1) {
                    int start = random.nextInt(child.tourSize());
                    int end = random.nextInt(child.tourSize());
                    if (start > end) { int tmp = start; start = end; end = tmp; }
                    if (start != end) {
                        while (start < end) {
                            City tmp = child.getCity(start);
                            child.setCity(start, child.getCity(end));
                            child.setCity(end, tmp);
                            start++;
                            end--;
                        }
                    }
                }

                nextGen.add(child);

                if (child.getDistance() < best.getDistance()) {
                    best = child.clone();
                }
            }
            population = nextGen;
        }

        return best;
    }

    private Tour tournamentSelect(List<Tour> pop, Random rand) {
        Tour best = pop.get(rand.nextInt(pop.size()));
        for (int i = 0; i < 3; i++) {
            Tour competitor = pop.get(rand.nextInt(pop.size()));
            if (competitor.getDistance() < best.getDistance()) {
                best = competitor;
            }
        }
        return best;
    }
}

// --- ACO (Basit Greedy-based) ---
class ACOAlgorithm implements TSPAlgorithm {
    @Override
    public String getName() { return "Ant Colony Opt."; }

    @Override
    public Tour solve(Tour initialTour, Random random) {
        int n = initialTour.tourSize();
        Tour best = null;

        // Pheromone matrix yerine basit greedy + randomness
        int numAnts = 20;
        int iterations = 50;

        for (int iter = 0; iter < iterations; iter++) {
            for (int ant = 0; ant < numAnts; ant++) {
                Tour tour = buildAntTour(initialTour, random);

                if (best == null || tour.getDistance() < best.getDistance()) {
                    best = tour.clone();
                }
            }
        }
        return best;
    }

    // Greedy + randomness (ACO'nun basitleştirilmiş versiyonu)
    private Tour buildAntTour(Tour base, Random rand) {
        List<City> unvisited = new ArrayList<>(base.getTour());
        Tour tour = new Tour();

        City current = unvisited.remove(rand.nextInt(unvisited.size()));
        tour.addCity(current);

        while (!unvisited.isEmpty()) {
            // %70 greedy, %30 random
            City next;
            if (rand.nextDouble() < 0.7) {
                // Greedy: en yakın şehir
                double minDist = Double.MAX_VALUE;
                City nearest = null;
                for (City c : unvisited) {
                    double d = current.distanceTo(c);
                    if (d < minDist) {
                        minDist = d;
                        nearest = c;
                    }
                }
                next = nearest;
            } else {
                // Random exploration
                next = unvisited.get(rand.nextInt(unvisited.size()));
            }

            unvisited.remove(next);
            tour.addCity(next);
            current = next;
        }

        return tour;
    }
}

// --- Task ---
class TSPTask implements Callable<Tour> {
    private Tour startTour;
    private TSPAlgorithm algorithm;
    private long seed;

    public TSPTask(Tour t, TSPAlgorithm algorithm, long seed) {
        this.startTour = t.clone();
        this.algorithm = algorithm;
        this.seed = seed;
    }

    @Override
    public Tour call() throws Exception {
        Random random = new Random(seed);
        return algorithm.solve(startTour, random);
    }
}

// --- MAIN ---
public class Main {
    static final String DATASET_FOLDER = "C:\\Users\\Taha\\IdeaProjects\\TSPBenchmark\\datasets";
    static final int[] THREAD_COUNTS = {1, 2, 4, 6, 8, 12, 16};
    static final String OUTPUT_FILE = "tsp_results.txt";

    public static void main(String[] args) throws IOException {
        File folder = new File(DATASET_FOLDER);
        if (!folder.exists()) {
            System.err.println("HATA: Klasör bulunamadı: " + DATASET_FOLDER);
            return;
        }

        File[] listOfFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".tsp"));
        if (listOfFiles == null || listOfFiles.length == 0) {
            System.out.println("UYARI: .tsp dosyası bulunamadı.");
            return;
        }

        System.out.println("Bulunan Dataset Sayısı: " + listOfFiles.length);
        BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_FILE));
        writer.write("Dataset;Algoritma;ThreadSayisi;Sure(ms);Speedup;EnIyiMesafe\n");

        for (File file : listOfFiles) {
            System.out.println("\n==========================================");
            System.out.println("DATASET İŞLENİYOR: " + file.getName());

            List<City> cities = loadDataset(file);
            if (cities.isEmpty()) continue;

            runBenchmarkForDataset(cities, file.getName(), writer);
        }

        writer.close();
        System.out.println("\n------------------------------------------");
        System.out.println("TÜM TESTLER BİTTİ.");
    }

    private static void runBenchmarkForDataset(List<City> cities, String datasetName, BufferedWriter writer) throws IOException {
        Tour baseTour = new Tour(cities);
        Tour globalBestTour = null;
        String bestAlgoName = "";
        long bestAlgoDuration = 0;
        List<TSPAlgorithm> algorithms = new ArrayList<>();
        algorithms.add(new GreedyAlgorithm());
        algorithms.add(new TwoOptAlgorithm());
        algorithms.add(new SimulatedAnnealingAlgorithm());
        algorithms.add(new GeneticAlgorithm());
        algorithms.add(new ACOAlgorithm());

        for (TSPAlgorithm algo : algorithms) {
            System.out.println("\n   -> Algoritma: " + algo.getName());
            double timeSingleThread = 0;

            for (int threadCount : THREAD_COUNTS) {
                System.gc();
                ExecutorService executor = Executors.newFixedThreadPool(threadCount);
                List<Callable<Tour>> tasks = new ArrayList<>();

                for(int i = 0; i < 16; i++) {
                    tasks.add(new TSPTask(baseTour, algo, System.nanoTime() + i));
                }

                long startTime = System.nanoTime();
                List<Future<Tour>> results = null;
                try {
                    results = executor.invokeAll(tasks);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                long endTime = System.nanoTime();
                executor.shutdown();

                double durationMs = (endTime - startTime) / 1_000_000.0;
                double currentBestDist = Double.MAX_VALUE;

                if (results != null) {
                    for(Future<Tour> f : results) {
                        try {
                            Tour t = f.get();
                            double d = t.getDistance();
                            if(d < currentBestDist) currentBestDist = d;

                            if (globalBestTour == null || d < globalBestTour.getDistance()) {
                                globalBestTour = t.clone();
                                bestAlgoName = algo.getName();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                if (threadCount == 1) timeSingleThread = durationMs;
                double speedup = (durationMs > 0) ? timeSingleThread / durationMs : 0;

                String log = String.format("%s;%s;%d;%.2f;%.2f;%.2f",
                        datasetName, algo.getName(), threadCount, durationMs, speedup, currentBestDist);
                System.out.println("      Thr: " + threadCount + " | Süre: " + (int)durationMs +
                        " ms | Spd: " + String.format("%.2f", speedup) +
                        " | Mesafe: " + String.format("%.0f", currentBestDist));
                writer.write(log + "\n");
                writer.flush();
                bestAlgoDuration = (int)durationMs;

            }
        }

        if (globalBestTour != null) {
            saveBestRouteToFile(globalBestTour, datasetName, bestAlgoName, (int)bestAlgoDuration);
        }
    }

    private static void saveBestRouteToFile(Tour tour, String datasetName, String algoName,int durationMs) {
        String filename = "solution_" + datasetName + ".txt";
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            bw.write("Dataset: " + datasetName + "\n");
            bw.write("En Iyi Bulan Algoritma: " + algoName + "\n");
            bw.write("Toplam Mesafe: " + String.format("%.2f", tour.getDistance()) + "\n");
            bw.write("Süre: "+durationMs);
            System.out.println("      >>> EN İYİ ROTA KAYDEDİLDİ: " + filename);
        } catch (IOException e) {
            System.err.println("Rota kaydedilemedi: " + e.getMessage());
        }
    }

    private static List<City> loadDataset(File file) {
        List<City> cities = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean coordSectionFound = false;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.equals("EOF")) break;
                if (!coordSectionFound) {
                    if (line.startsWith("NODE_COORD_SECTION")) coordSectionFound = true;
                    continue;
                }
                String[] parts = line.split("\\s+");
                if (parts.length < 3) continue;
                try {
                    double valX = Double.parseDouble(parts[1]);
                    double valY = Double.parseDouble(parts[2]);
                    cities.add(new City((int)valX, (int)valY));
                } catch (NumberFormatException e) {}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cities;
    }
}