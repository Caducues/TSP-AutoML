import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainGenerator {
    private static final int[] THREAD_COUNTS = {1, 2, 4, 8, 16,};

    public static void main(String[] args) {
        System.out.println("Programın Çalıştığı Dizin: " + System.getProperty("user.dir"));

        File folder = new File("datasets");
        System.out.println("Aranan Klasörün Tam Yolu: " + folder.getAbsolutePath());
        System.out.println("Klasör Var mı?: " + folder.exists());
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles == null || listOfFiles.length == 0) {
            System.out.println("HATA: 'datasets' klasörü boş veya bulunamadı!");
            return;
        }
        writeToCSV("CityCount,Algorithm,ThreadCount,RuntimeMS");

        for (File file : listOfFiles) {
            if (file.isFile()) {
                System.out.print("Dosya bulundu: " + file.getName());
                if (!file.getName().endsWith(".tsp")) {
                    System.out.println(" -> ATLANDI! (Uzantısı .tsp değil)");
                    continue;
                }
                City[] cities = TSPUtils.loadDataset(file.getPath());
                int cityCount = cities.length;

                if (cityCount == 0) {
                    System.out.println(" -> HATA! Dosya okundu ama şehir bulunamadı (Format sorunu olabilir).");
                    continue;
                }

                System.out.println(" -> BAŞARILI. (" + cityCount + " şehir) Benchmark başlıyor...");
                for (int threadCount : THREAD_COUNTS) {
                    GeneticAlgorithm ga = new GeneticAlgorithm(cities, threadCount);
                    try {
                        long duration = ga.call();
                        String line = cityCount + ",Genetic," + threadCount + "," + duration;
                        writeToCSV(line);
                        System.out.println("      -> Thread: " + threadCount + " | Süre: " + duration + "ms");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                System.out.println("   " + file.getName() + " tamamlandı.\n");
            }
        }
        System.out.println("Benchmark tamamlandı! 'tsp_training_data.csv' dosyasını kontrol et.");
    }

    private static void writeToCSV(String text) {
        try (FileWriter fw = new FileWriter("tsp_training_data.csv", true)) {
            fw.write(text + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}