/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.newgui;

import java.awt.Color;

public class GuiColors {
    // Background
    public static final int BG_DARK = new Color(18, 18, 24).getRGB();
    public static final int BG_PANEL = new Color(24, 24, 32).getRGB();
    public static final int BG_MODULE = new Color(30, 30, 40).getRGB();
    public static final int BG_MODULE_HOVER = new Color(38, 38, 50).getRGB();
    public static final int BG_SETTINGS = new Color(22, 22, 30).getRGB();
    public static final int BG_SETTING_HOVER = new Color(34, 34, 46).getRGB();

    // Accent
    public static final int ACCENT = new Color(130, 90, 255).getRGB();
    public static final int ACCENT_LIGHT = new Color(160, 130, 255).getRGB();

    // Text
    public static final int TEXT_PRIMARY = new Color(240, 240, 250).getRGB();
    public static final int TEXT_SECONDARY = new Color(140, 140, 165).getRGB();
    public static final int TEXT_DISABLED = new Color(80, 80, 100).getRGB();
    public static final int TEXT_SETTING_VALUE = new Color(170, 170, 200).getRGB();

    // Stub row (unsupported setting types) - use a muted grey like TEXT_DISABLED
    public static final int TEXT_STUB = new Color(110, 110, 130).getRGB();

    // Status
    public static final int ENABLED = new Color(90, 255, 130).getRGB();
    public static final int DISABLED = new Color(255, 70, 70).getRGB();

    // Borders
    public static final int BORDER = new Color(50, 50, 65).getRGB();
    public static final int BORDER_LIGHT = new Color(70, 70, 90).getRGB();

    // Scrollbar
    public static final int SCROLLBAR = new Color(60, 60, 80).getRGB();
    public static final int SCROLLBAR_HOVER = new Color(80, 80, 105).getRGB();

    // Header
    public static final int BG_HEADER = new Color(28, 28, 38).getRGB();
    public static final int BG_HEADER_ACTIVE = new Color(40, 40, 55).getRGB();
}
