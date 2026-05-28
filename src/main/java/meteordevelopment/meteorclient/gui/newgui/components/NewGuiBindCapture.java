/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.newgui.components;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.meteor.ModuleBindChangedEvent;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import org.lwjgl.glfw.GLFW;

/**
 * Routes keybind-capture events for the new clickgui.
 *
 * <p>Module keybinds delegate to {@link Modules#setModuleToBind} so that
 * Meteor's existing {@link Modules#onKeyBinding} flow runs — that handler
 * respects {@link Keybind#canBindTo}, normalizes modifiers, and posts
 * {@link ModuleBindChangedEvent}. We then clear our own "listening" flag
 * off that event.</p>
 *
 * <p>Per-setting {@link KeybindSetting} capture doesn't have a
 * corresponding service helper, so we bind directly on key release here
 * and cancel the event so the screen doesn't also see the key. ESC clears
 * the bind.</p>
 */
public final class NewGuiBindCapture {
    private static final NewGuiBindCapture INSTANCE = new NewGuiBindCapture();
    private static boolean subscribed = false;

    private KeybindSetting listeningSetting = null;

    private NewGuiBindCapture() {}

    public static NewGuiBindCapture get() {
        ensureSubscribed();
        return INSTANCE;
    }

    /** Idempotent subscription so the bus gets our handler exactly once. */
    public static void ensureSubscribed() {
        if (subscribed) return;
        MeteorClient.EVENT_BUS.subscribe(INSTANCE);
        subscribed = true;
    }

    // ---- Per-setting KeybindSetting ----

    public void startListeningForSetting(KeybindSetting setting) {
        this.listeningSetting = setting;
    }

    public boolean isListeningForSetting() {
        return listeningSetting != null;
    }

    public KeybindSetting getListeningSetting() {
        return listeningSetting;
    }

    public void cancelSettingListen() {
        listeningSetting = null;
    }

    // ---- Event handlers (setting-level only; module handled by Modules) ----

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onKey(KeyEvent event) {
        if (listeningSetting == null) return;
        if (event.action == KeyAction.Press && event.key == GLFW.GLFW_KEY_ESCAPE) {
            listeningSetting.get().set(true, GLFW.GLFW_KEY_UNKNOWN, 0);
            listeningSetting = null;
            event.cancel();
            return;
        }
        if (event.action == KeyAction.Release) {
            Keybind kb = listeningSetting.get();
            if (kb.canBindTo(true, event.key, event.modifiers)) {
                kb.set(true, event.key, event.modifiers);
                listeningSetting.onChanged();
            }
            listeningSetting = null;
            event.cancel();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onMouseButton(MouseButtonEvent event) {
        if (listeningSetting == null) return;
        if (event.action == KeyAction.Release) {
            Keybind kb = listeningSetting.get();
            if (kb.canBindTo(false, event.button, 0)) {
                kb.set(false, event.button, 0);
                listeningSetting.onChanged();
            }
            listeningSetting = null;
            event.cancel();
        }
    }

    /** Clear the module-listening flag on ModuleButton whenever a bind completes. */
    @EventHandler
    private void onModuleBindChanged(ModuleBindChangedEvent event) {
        ModuleButton.clearKeyListeningIfMatches(event.module);
    }
}
