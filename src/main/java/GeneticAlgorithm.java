import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GeneticAlgorithm implements Callable<Long> {

    private City[] cities;
    private int threadCount;
    private final int GENERATIONS = 1000;

    public GeneticAlgorithm(City[] cities, int threadCount) {
        this.cities = cities;
        this.threadCount = threadCount;
    }

    @Override
    public Long call() throws Exception {
        long startTime = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                runEvolutionSimulation();
            });
        }
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        return (endTime - startTime);
    }
    private void runEvolutionSimulation() {
        double tempSum = 0;
        for (int i = 0; i < GENERATIONS; i++) {
            for (City c : cities) {
                tempSum += Math.sqrt(c.getX() * c.getY()) + Math.pow(c.distanceTo(c), 2);
            }
        }
    }
}