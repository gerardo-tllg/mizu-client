/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.newgui.components;

import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.settings.Settings;

/**
 * A {@link MeteorGuiTheme} that does nothing visually special but <em>captures</em>
 * the {@link Settings} object the first time {@code settings(Settings)} is called.
 *
 * <p>This lets the new-theme block/generic editors reuse Meteor's existing
 * per-T {@code createScreen()} implementations without rendering their widget
 * trees. Meteor's concrete screens (e.g. {@code ESPBlockDataScreen}) build a
 * {@code Settings} object in {@code initWidgets()} then hand it to
 * {@code theme.settings(...)}; we intercept at that point, keep the reference,
 * and render it via {@link SettingGroupRenderer} in our own theme.</p>
 *
 * <p>Falls back gracefully: if a T's {@code initWidgets()} doesn't call
 * {@code theme.settings(...)} (e.g. a custom screen that builds widgets
 * manually), {@link #captured} stays {@code null} and the caller should
 * open the widget screen itself.</p>
 */
public class CapturingGuiTheme extends MeteorGuiTheme {
    /** The first {@link Settings} passed to {@code settings(...)} while using this theme. */
    public Settings captured;

    @Override
    public WWidget settings(Settings settings) {
        if (captured == null) captured = settings;
        return super.settings(settings);
    }

    @Override
    public WWidget settings(Settings settings, String filter) {
        if (captured == null) captured = settings;
        return super.settings(settings, filter);
    }
}
