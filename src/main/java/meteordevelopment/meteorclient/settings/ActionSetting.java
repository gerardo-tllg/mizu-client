/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.settings;

import net.minecraft.nbt.NbtCompound;

import java.util.List;

/**
 * A button-style setting: shows a row with a label (the setting's name) and a
 * "{@code hint \u203A}" affordance on the right. Clicking the row invokes
 * {@link #action}. No value is stored — the setting ignores load/save.
 */
public class ActionSetting extends Setting<Boolean> {
    public final Runnable action;
    public final String buttonLabel;

    private ActionSetting(String name, String description, Runnable action, String buttonLabel, IVisible visible) {
        super(name, description, false, null, null, visible);
        this.action = action;
        this.buttonLabel = buttonLabel;
    }

    @Override
    protected Boolean parseImpl(String str) { return false; }

    @Override
    protected boolean isValueValid(Boolean value) { return true; }

    @Override
    public List<String> getSuggestions() { return List.of(); }

    @Override
    public NbtCompound save(NbtCompound tag) { return tag; }

    @Override
    public Boolean load(NbtCompound tag) { return false; }

    public static class Builder extends SettingBuilder<Builder, Boolean, ActionSetting> {
        private Runnable action = () -> {};
        private String buttonLabel = "open";

        public Builder() {
            super(false);
        }

        public Builder action(Runnable action) {
            this.action = action;
            return this;
        }

        public Builder buttonLabel(String label) {
            this.buttonLabel = label;
            return this;
        }

        @Override
        public ActionSetting build() {
            return new ActionSetting(name, description, action, buttonLabel, visible);
        }
    }
}
