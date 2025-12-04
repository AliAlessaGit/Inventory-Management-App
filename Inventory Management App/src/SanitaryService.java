import java.util.*;

public class SanitaryService {
    private List<SanitaryItem> sanitaryItems = new ArrayList<>();
    private final String dataPath;
    private List<Warehouse> warehouses;


    public SanitaryService(String dataPath, List<Warehouse> warehouses) {
        this.dataPath = dataPath; // e.g., "data/sanitary.json"
        this.warehouses = warehouses;
        load();
    }

    // --- دوال CRUD ---
    public List<SanitaryItem> getAll() { return sanitaryItems; }
    public void add(SanitaryItem item) { sanitaryItems.add(item); save(); }
    public void remove(SanitaryItem item) { sanitaryItems.remove(item); save(); }
    public void update(SanitaryItem oldItem, SanitaryItem updatedValues) {
        if (oldItem == null || updatedValues == null) return;
        int idx = sanitaryItems.indexOf(oldItem);
        if (idx >= 0) {
            oldItem.applyUpdatesFrom(updatedValues);
            // تأكد أن عداد المعرّفات يبقى في حالة متوافقة (اختياري لكنه آمن)
            InventoryItem.bumpIdCounterIfNeeded(oldItem.getIdNumber());
            save();
        }
    }
    // تم التعديل: الحفظ باستخدام JSON
    public void save() {
        JSONUtil.writeAll(dataPath, sanitaryItems);
    }

    // تم التعديل: التحميل باستخدام JSON
    public void load() {
        this.sanitaryItems = JSONUtil.readAll(dataPath, SanitaryItem.class);

        // تحديث عداد ID العام
        for (SanitaryItem item : this.sanitaryItems) {
            InventoryItem.bumpIdCounterIfNeeded(item.getIdNumber());
        }
    }  // اختياري: تسهيل البحث بالـ idNumber
    public SanitaryItem findById(long id) {
        for (SanitaryItem s : sanitaryItems) {
            if (s.getIdNumber() == id) return s;
        }
        return null;
    }

    public List<SanitaryItem> filter(String type) {
        List<SanitaryItem> filtered = new ArrayList<>();
        for (SanitaryItem s : sanitaryItems) {
            boolean match = (type == null || type.equals("كل الأنواع") || s.getType().getArabicName().equals(type));
            if (match) filtered.add(s);
        }
        return filtered;
    }

    /**
     * NEW: دالة للبحث الدقيق عن الأدوات الصحية باستخدام الاسم والنخب.
     * @param name اسم العنصر
     * @param grade النخب
     * @return SanitaryItem إذا تم العثور عليه، وإلا null
     */
    public SanitaryItem findByNameAndGrade(String name, String grade) {
        if (name == null || grade == null) {
            return null;
        }
        String nameTrim = name.trim();
        String gradeTrim = grade.trim();

        for (SanitaryItem item : sanitaryItems) {
            if (item.getName() != null && item.getGrade() != null &&
                    item.getName().equalsIgnoreCase(nameTrim) &&
                    item.getGrade().equalsIgnoreCase(gradeTrim)) {
                return item;
            }
        }
        return null; // لم يتم العثور على العنصر
    }
}