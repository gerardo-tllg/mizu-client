/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.newgui;

public class GuiColors {
    // Backgrounds — deep ocean navy
    public static final int BG_DARK         = 0xFF060d18;  // #060d18
    public static final int BG_PANEL        = 0xFF060d18;
    public static final int BG_MODULE       = 0xFF060d18;
    public static final int BG_MODULE_HOVER = 0xFF091525;
    public static final int BG_SETTINGS     = 0xFF060d18;
    public static final int BG_SETTING_HOVER= 0xFF0a1a2c;
    public static final int BG_HEADER       = 0xFF0a1e30;  // #0a1e30 category header
    public static final int BG_HEADER_ACTIVE= 0xFF0d2840;

    // Accents — water theme
    public static final int ACCENT          = 0xFF1D9E75;  // #1D9E75 teal (active)
    public static final int ACCENT_LIGHT    = 0xFF378ADD;  // #378ADD ocean blue

    // Active module row highlight — teal at 45% alpha
    public static final int ACTIVE_ROW_BG   = 0x721D9E75;

    // Text
    public static final int TEXT_PRIMARY    = 0xFFF0F0FA;  // near-white active text
    public static final int TEXT_SECONDARY  = 0xFF185FA5;  // #185FA5 inactive module text
    public static final int TEXT_DISABLED   = 0xFF0D3A5C;  // very dim
    public static final int TEXT_SETTING_VALUE = 0xFF7FB3D4;
    public static final int TEXT_STUB       = 0xFF0D3A5C;

    // Status
    public static final int ENABLED         = 0xFF1D9E75;
    public static final int DISABLED_COLOR  = 0xFF185FA5;

    // Borders — dark teal
    public static final int BORDER          = 0xFF0D3A5C;  // #0d3a5c
    public static final int BORDER_LIGHT    = 0xFF1D4A6C;

    // Scrollbar
    public static final int SCROLLBAR       = 0xFF0D3A5C;
    public static final int SCROLLBAR_HOVER = 0xFF1D4A6C;

    // Pre-blended variants for fills (ARGB)
    public static final int HEADER_FILL     = 0xDC0A1E30;  // header bg at alpha 220
    public static final int OUTLINE_COLOR   = 0xAA0D3A5C;  // border at alpha 170
    public static final int OUTLINE_HEADER  = 0xDC0D3A5C;  // border at alpha 220 (on header)
}
