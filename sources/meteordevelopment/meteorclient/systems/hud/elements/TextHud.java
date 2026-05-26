package meteordevelopment.meteorclient.systems.hud.elements;

import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.utils.StarscriptTextBoxRenderer;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.starscript.Script;
import meteordevelopment.starscript.Section;
import meteordevelopment.starscript.compiler.Compiler;
import meteordevelopment.starscript.compiler.Parser;
import meteordevelopment.starscript.utils.Error;
import meteordevelopment.starscript.utils.StarscriptError;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/elements/TextHud.class */
public class TextHud extends HudElement {
    private static final Color WHITE = new Color();
    private final SettingGroup sgGeneral;
    private final SettingGroup sgShown;
    private final SettingGroup sgScale;
    private final SettingGroup sgBackground;
    private double originalWidth;
    private double originalHeight;
    private boolean needsCompile;
    private boolean recalculateSize;
    private int timer;
    public final Setting<String> text;
    public final Setting<Integer> updateDelay;
    public final Setting<Boolean> shadow;
    public final Setting<Integer> border;
    public final Setting<Shown> shown;
    public final Setting<String> condition;
    public final Setting<Boolean> customScale;
    public final Setting<Double> scale;
    public final Setting<Boolean> background;
    public final Setting<SettingColor> backgroundColor;
    private Script script;
    private Script conditionScript;
    private Section section;
    private boolean firstTick;
    private boolean empty;
    private boolean visible;

    public TextHud(HudElementInfo<TextHud> info) {
        super(info);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgShown = this.settings.createGroup("Shown");
        this.sgScale = this.settings.createGroup("Scale");
        this.sgBackground = this.settings.createGroup("Background");
        this.text = this.sgGeneral.add(new StringSetting.Builder().name("text").description("Text to display with Starscript.").defaultValue(MeteorClient.NAME).onChanged(s -> {
            recompile();
        }).wide().renderer(StarscriptTextBoxRenderer.class).build());
        this.updateDelay = this.sgGeneral.add(new IntSetting.Builder().name("update-delay").description("Update delay in ticks").defaultValue(4).onChanged(integer -> {
            if (this.timer > integer.intValue()) {
                this.timer = integer.intValue();
            }
        }).min(0).build());
        this.shadow = this.sgGeneral.add(new BoolSetting.Builder().name("shadow").description("Renders shadow behind text.").defaultValue(true).onChanged(aBoolean -> {
            this.recalculateSize = true;
        }).build());
        this.border = this.sgGeneral.add(new IntSetting.Builder().name("border").description("How much space to add around the text.").defaultValue(0).onChanged(integer2 -> {
            super.setSize(this.originalWidth + ((double) (integer2.intValue() * 2)), this.originalHeight + ((double) (integer2.intValue() * 2)));
        }).build());
        this.shown = this.sgShown.add(new EnumSetting.Builder().name("shown").description("When this text element is shown.").defaultValue(Shown.Always).onChanged(s2 -> {
            recompile();
        }).build());
        this.condition = this.sgShown.add(new StringSetting.Builder().name("condition").description("Condition to check when shown is not Always.").visible(() -> {
            return this.shown.get() != Shown.Always;
        }).onChanged(s3 -> {
            recompile();
        }).renderer(StarscriptTextBoxRenderer.class).build());
        this.customScale = this.sgScale.add(new BoolSetting.Builder().name("custom-scale").description("Applies custom text scale rather than the global one.").defaultValue(false).onChanged(integer3 -> {
            this.recalculateSize = true;
        }).build());
        SettingGroup settingGroup = this.sgScale;
        DoubleSetting.Builder builderDescription = new DoubleSetting.Builder().name("scale").description("Custom scale.");
        Setting<Boolean> setting = this.customScale;
        Objects.requireNonNull(setting);
        this.scale = settingGroup.add(builderDescription.visible(setting::get).defaultValue(1.0d).onChanged(integer4 -> {
            this.recalculateSize = true;
        }).min(0.5d).sliderRange(0.5d, 3.0d).build());
        this.background = this.sgBackground.add(new BoolSetting.Builder().name("background").description("Displays background.").defaultValue(false).build());
        SettingGroup settingGroup2 = this.sgBackground;
        ColorSetting.Builder builderDescription2 = new ColorSetting.Builder().name("background-color").description("Color used for the background.");
        Setting<Boolean> setting2 = this.background;
        Objects.requireNonNull(setting2);
        this.backgroundColor = settingGroup2.add(builderDescription2.visible(setting2::get).defaultValue(new SettingColor(25, 25, 25, 50)).build());
        this.firstTick = true;
        this.empty = false;
        this.needsCompile = true;
    }

    private void recompile() {
        this.firstTick = true;
        this.needsCompile = true;
    }

    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    public void setSize(double width, double height) {
        this.originalWidth = width;
        this.originalHeight = height;
        super.setSize(width + ((double) (this.border.get().intValue() * 2)), height + ((double) (this.border.get().intValue() * 2)));
    }

    private void calculateSize(HudRenderer renderer) {
        double width = 0.0d;
        if (this.section != null) {
            String str = this.section.toString();
            if (!str.isBlank()) {
                width = renderer.textWidth(str, this.shadow.get().booleanValue(), getScale());
            }
        }
        if (width != 0.0d) {
            setSize(width, renderer.textHeight(this.shadow.get().booleanValue(), getScale()));
            this.empty = false;
        } else {
            setSize(100.0d, renderer.textHeight(this.shadow.get().booleanValue(), getScale()));
            this.empty = true;
        }
    }

    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    public void tick(HudRenderer renderer) {
        if (this.recalculateSize) {
            calculateSize(renderer);
            this.recalculateSize = false;
        }
        if (this.timer <= 0) {
            runTick(renderer);
            this.timer = this.updateDelay.get().intValue();
        } else {
            this.timer--;
        }
    }

    private void runTick(HudRenderer renderer) {
        if (this.needsCompile) {
            Parser.Result result = Parser.parse(this.text.get());
            if (result.hasErrors()) {
                this.script = null;
                this.section = new Section(0, ((Error) result.errors.getFirst()).toString());
                calculateSize(renderer);
            } else {
                this.script = Compiler.compile(result);
            }
            if (this.shown.get() != Shown.Always) {
                this.conditionScript = Compiler.compile(Parser.parse(this.condition.get()));
            }
            this.needsCompile = false;
        }
        try {
            if (this.script != null) {
                this.section = MeteorStarscript.ss.run(this.script);
                calculateSize(renderer);
            }
        } catch (StarscriptError error) {
            this.section = new Section(0, error.getMessage());
            calculateSize(renderer);
        }
        if (this.shown.get() != Shown.Always && this.conditionScript != null) {
            String text = MeteorStarscript.run(this.conditionScript);
            if (text == null) {
                this.visible = false;
            } else {
                this.visible = this.shown.get() == Shown.WhenTrue ? text.equalsIgnoreCase("true") : text.equalsIgnoreCase("false");
            }
        }
        this.firstTick = false;
    }

    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    public void render(HudRenderer renderer) {
        if (this.firstTick) {
            runTick(renderer);
        }
        boolean visible = this.shown.get() == Shown.Always || this.visible;
        if ((this.empty || !visible) && isInEditor()) {
            renderer.line(this.x, this.y, this.x + getWidth(), this.y + getHeight(), Color.GRAY);
            renderer.line(this.x, this.y + getHeight(), this.x + getWidth(), this.y, Color.GRAY);
        }
        if (this.section == null || !visible) {
            return;
        }
        double x = this.x + this.border.get().intValue();
        Section section = this.section;
        while (true) {
            Section s = section;
            if (s == null) {
                break;
            }
            x = renderer.text(s.text, x, this.y + this.border.get().intValue(), getSectionColor(s.index), this.shadow.get().booleanValue(), getScale());
            section = s.next;
        }
        if (this.background.get().booleanValue()) {
            renderer.quad(this.x, this.y, getWidth(), getHeight(), this.backgroundColor.get());
        }
    }

    @Override // meteordevelopment.meteorclient.systems.hud.HudElement
    public void onFontChanged() {
        this.recalculateSize = true;
    }

    private double getScale() {
        if (this.customScale.get().booleanValue()) {
            return this.scale.get().doubleValue();
        }
        return -1.0d;
    }

    public static Color getSectionColor(int i) {
        List<SettingColor> colors = Hud.get().textColors.get();
        return (i < 0 || i >= colors.size()) ? WHITE : colors.get(i);
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/elements/TextHud$Shown.class */
    public enum Shown {
        Always,
        WhenTrue,
        WhenFalse;

        /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
        @Override // java.lang.Enum
        public String toString() throws MatchException {
            switch (this) {
                case Always:
                    return "Always";
                case WhenTrue:
                    return "When True";
                case WhenFalse:
                    return "When False";
                default:
                    throw new MatchException((String) null, (Throwable) null);
            }
        }
    }
}
