package meteordevelopment.meteorclient.utils.network;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/network/ItemData.class */
public class ItemData {
    public String name;
    public int quantity;
    public String category;
    public String notes;

    public ItemData(String name, int quantity, String category) {
        this.name = name;
        this.quantity = quantity;
        this.category = category;
        this.notes = null;
    }

    public ItemData(String name, int quantity, String category, String notes) {
        this.name = name;
        this.quantity = quantity;
        this.category = category;
        this.notes = notes;
    }
}
