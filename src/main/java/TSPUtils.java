import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class TSPUtils {

    public static City[] loadDataset(String filePath) {
        List<City> cities = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean coordSectionFound = false;
            int autoIdCounter = 1;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (line.equals("EOF")) break;
                if (line.startsWith("NAME") || line.startsWith("COMMENT") ||
                        line.startsWith("TYPE") || line.startsWith("DIMENSION") ||
                        line.startsWith("EDGE_WEIGHT_TYPE")) {
                    continue;
                }
                if (line.contains("NODE_COORD_SECTION")) {
                    coordSectionFound = true;
                    continue;
                }
                String[] parts = line.split("\\s+");

                try {
                    if (parts.length == 1) {
                        continue;
                    }
                    if (parts.length == 2) {
                        double x = Double.parseDouble(parts[0]);
                        double y = Double.parseDouble(parts[1]);
                        cities.add(new City(autoIdCounter++, x, y));
                    }
                    else if (parts.length >= 3) {
                        int id = Integer.parseInt(parts[0]);
                        double x = Double.parseDouble(parts[1]);
                        double y = Double.parseDouble(parts[2]);
                        cities.add(new City(id, x, y));
                    }

                } catch (NumberFormatException e) {
                }
            }
        } catch (Exception e) {
            System.err.println("Dosya okuma hatası: " + filePath + " -> " + e.getMessage());
        }

        return cities.toArray(new City[0]);
    }
}