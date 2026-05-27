/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.utils.network;

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
