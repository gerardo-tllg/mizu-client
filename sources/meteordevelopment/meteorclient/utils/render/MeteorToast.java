package meteordevelopment.meteorclient.utils.render;

import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.class_1109;
import net.minecraft.class_1113;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1921;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_2960;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_368;
import net.minecraft.class_374;
import net.minecraft.class_5251;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/render/MeteorToast.class */
public class MeteorToast implements class_368 {
    public static final int TITLE_COLOR = Color.fromRGBA(Opcode.I2B, 61, 226, 255);
    public static final int TEXT_COLOR = Color.fromRGBA(220, 220, 220, 255);
    private static final class_2960 TEXTURE = class_2960.method_60654("toast/advancement");
    private class_1799 icon;
    private class_2561 title;
    private class_2561 text;
    private boolean playedSound;
    private long start;
    private long duration;
    private boolean justUpdated = true;
    private class_368.class_369 visibility = class_368.class_369.field_2209;

    public MeteorToast(@Nullable class_1792 item, @NotNull String title, @Nullable String text, long duration) {
        this.icon = item != null ? item.method_7854() : null;
        this.title = class_2561.method_43470(title).method_10862(class_2583.field_24360.method_27703(class_5251.method_27717(TITLE_COLOR)));
        this.text = text != null ? class_2561.method_43470(text).method_10862(class_2583.field_24360.method_27703(class_5251.method_27717(TEXT_COLOR))) : null;
        this.duration = duration;
    }

    public MeteorToast(@Nullable class_1792 item, @NotNull String title, @Nullable String text) {
        this.icon = item != null ? item.method_7854() : null;
        this.title = class_2561.method_43470(title).method_10862(class_2583.field_24360.method_27703(class_5251.method_27717(TITLE_COLOR)));
        this.text = text != null ? class_2561.method_43470(text).method_10862(class_2583.field_24360.method_27703(class_5251.method_27717(TEXT_COLOR))) : null;
        this.duration = 6000L;
    }

    public class_368.class_369 method_61988() {
        return this.visibility;
    }

    public void method_61989(class_374 manager, long time) {
        if (this.justUpdated) {
            this.start = time;
            this.justUpdated = false;
        }
        this.visibility = time - this.start >= this.duration ? class_368.class_369.field_2209 : class_368.class_369.field_2210;
        if (!this.playedSound) {
            MeteorClient.mc.method_1483().method_4873(getSound());
            this.playedSound = true;
        }
    }

    public void method_1986(class_332 context, class_327 textRenderer, long startTime) {
        context.method_52706(class_1921::method_62277, TEXTURE, 0, 0, method_29049(), method_29050());
        int x = this.icon != null ? 28 : 12;
        int titleY = 12;
        if (this.text != null) {
            context.method_51439(MeteorClient.mc.field_1772, this.title, x, 18, TITLE_COLOR, false);
            titleY = 7;
        }
        context.method_51439(MeteorClient.mc.field_1772, this.title, x, titleY, TITLE_COLOR, false);
        if (this.icon != null) {
            context.method_51427(this.icon, 8, 8);
        }
    }

    public void setIcon(@Nullable class_1792 item) {
        this.icon = item != null ? item.method_7854() : null;
        this.justUpdated = true;
    }

    public void setTitle(@NotNull String title) {
        this.title = class_2561.method_43470(title).method_10862(class_2583.field_24360.method_27703(class_5251.method_27717(TITLE_COLOR)));
        this.justUpdated = true;
    }

    public void setText(@Nullable String text) {
        this.text = text != null ? class_2561.method_43470(text).method_10862(class_2583.field_24360.method_27703(class_5251.method_27717(TEXT_COLOR))) : null;
        this.justUpdated = true;
    }

    public void setDuration(long duration) {
        this.duration = duration;
        this.justUpdated = true;
    }

    public class_1113 getSound() {
        return class_1109.method_4757((class_3414) class_3417.field_14725.comp_349(), 1.2f, 1.0f);
    }
}
