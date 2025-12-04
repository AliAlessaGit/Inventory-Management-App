import java.io.*;
import java.util.*;

public class CSVUtil {
    public static List<String[]> readAll(String path) {
        List<String[]> rows = new ArrayList<>();
        File f = new File(path);
        if (!f.exists()) return rows;
        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = r.readLine()) != null) {
                rows.add(line.split(","));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rows;
    }

    public static void writeAll(String path, List<String[]> rows) {
        try (PrintWriter w = new PrintWriter(new FileWriter(path))) {
            for (String[] row : rows) {
                w.println(String.join(",", row));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
