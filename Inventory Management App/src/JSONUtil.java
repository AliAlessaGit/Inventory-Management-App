// JSONUtil.java (النسخة المعدلة)
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime; // استيراد الكلاس
import java.util.ArrayList;
import java.util.List;

public class JSONUtil {

    // --- تم التعديل هنا ---
    // نقوم بتسجيل الـ Adapter الخاص بنا عند بناء كائنات Gson
    private static final Gson GSON_PRETTY = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .setPrettyPrinting()
            .create();

    private static final Gson GSON_DEFAULT = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
    // ----------------------

    /**
     * يكتب قائمة من الكائنات إلى ملف JSON.
     * @param path مسار الملف.
     * @param data القائمة التي سيتم حفظها.
     */
    public static void writeAll(String path, List<?> data) {
        File f = new File(path);
        File parent = f.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (Writer writer = new FileWriter(path)) {
            GSON_PRETTY.toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * يقرأ قائمة من الكائنات من ملف JSON.
     * @param path مسار الملف.
     * @param itemType نوع الكائن داخل القائمة (e.g., Warehouse.class).
     * @return قائمة بالكائنات المقروءة، أو قائمة فارغة إذا كان الملف غير موجود.
     */
    public static <T> List<T> readAll(String path, Class<T> itemType) {
        File f = new File(path);
        if (!f.exists()) {
            return new ArrayList<>();
        }

        try (Reader reader = new FileReader(path)) {
            Type listType = TypeToken.getParameterized(List.class, itemType).getType();
            List<T> result = GSON_DEFAULT.fromJson(reader, listType);

            return result != null ? result : new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}