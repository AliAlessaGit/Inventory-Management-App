public class Warehouse {
    public enum Type { OWNED, EXTERNAL }

    private int id;
    private String name;
    private Type type;
    private String ownerName; // اسم التاجر إذا خارجي
    private String ownerContact; // بيانات التاجر (اختياري)

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public void setOwnerContact(String ownerContact) {
        this.ownerContact = ownerContact;
    }

    public Warehouse(int id, String name, Type type, String ownerName, String ownerContact) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.ownerName = ownerName;
        this.ownerContact = ownerContact;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public Type getType() { return type; }
    public String getOwnerName() { return ownerName; }
    public String getOwnerContact() { return ownerContact; }


    public String getDisplayName() {
        if (type == Type.EXTERNAL) {
            return name + " (تاجر: " + ownerName + ")";
        }
        return name + " (خاص)";
    }
} 