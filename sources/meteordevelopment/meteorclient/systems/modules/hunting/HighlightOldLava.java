package meteordevelopment.meteorclient.systems.modules.hunting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2661;
import net.minecraft.class_2680;
import net.minecraft.class_2818;
import net.minecraft.class_2902;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/hunting/HighlightOldLava.class */
public class HighlightOldLava extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Integer> searchAbove;
    private final Setting<Integer> lavaHeight;
    private final Setting<Integer> renderDistance;
    private final Setting<Boolean> disconnectOnFound;
    public final Setting<Mode> logMode;
    public final Setting<String> webhookLink;
    public final Setting<Boolean> ping;
    public final Setting<String> discordId;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private HashSet<class_2338> oldLava;
    private HashSet<class_243> loadedChunks;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/hunting/HighlightOldLava$Mode.class */
    public enum Mode {
        Highlight,
        LogWebhook,
        Both
    }

    public HighlightOldLava() {
        super(Categories.Hunting, "highlight-old-lava", "Highlights lava that has already flowed down");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.searchAbove = this.sgGeneral.add(new IntSetting.Builder().name("search-above").description("Y value to look above").defaultValue(0).sliderRange(0, Opcode.ISHL).build());
        this.lavaHeight = this.sgGeneral.add(new IntSetting.Builder().name("lava-height").description("The height of the lava to count as already loaded").defaultValue(5).sliderRange(1, 30).build());
        this.renderDistance = this.sgGeneral.add(new IntSetting.Builder().name("render-distance").description("How far away to render the blocks.").defaultValue(128).sliderRange(0, 512).build());
        this.disconnectOnFound = this.sgGeneral.add(new BoolSetting.Builder().name("disconnect-on-found").description("Will auto disconnect you if old lava is found. (Good for afking)").defaultValue(false).build());
        this.logMode = this.sgGeneral.add(new EnumSetting.Builder().name("log-mode").description("How results are shown.").defaultValue(Mode.Highlight).build());
        this.webhookLink = this.sgGeneral.add(new StringSetting.Builder().name("webhook-link").description("A discord webhook link. Looks like this: https://discord.com/api/webhooks/webhookUserId/webHookTokenOrSomething").defaultValue("").visible(() -> {
            return this.logMode.get() == Mode.LogWebhook || this.logMode.get() == Mode.Both;
        }).build());
        this.ping = this.sgGeneral.add(new BoolSetting.Builder().name("ping-on-lava-found").description("Pings you when lava that matches your search is found.").defaultValue(false).visible(() -> {
            return this.logMode.get() == Mode.LogWebhook || this.logMode.get() == Mode.Both;
        }).build());
        this.discordId = this.sgGeneral.add(new StringSetting.Builder().name("discord-ID").description("Your discord ID").defaultValue("").visible(() -> {
            return this.ping.get().booleanValue() && (this.logMode.get() == Mode.LogWebhook || this.logMode.get() == Mode.Both);
        }).build());
        this.shapeMode = this.sgGeneral.add(new EnumSetting.Builder().name("box-render-mode").description("How the shape for the bounding box is rendered.").defaultValue(ShapeMode.Both).build());
        this.sideColor = this.sgGeneral.add(new ColorSetting.Builder().name("side-color").description("The side color of the bounding box.").defaultValue(new SettingColor(16, Opcode.FMUL, Opcode.D2F, 100)).build());
        this.lineColor = this.sgGeneral.add(new ColorSetting.Builder().name("line-color").description("The line color of the bounding box.").defaultValue(new SettingColor(16, Opcode.FMUL, Opcode.D2F, 255)).build());
        this.oldLava = new HashSet<>();
        this.loadedChunks = new HashSet<>();
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public WWidget getWidget(GuiTheme theme) {
        WVerticalList list = theme.verticalList();
        WButton clearLog = (WButton) list.add(theme.button("Clear saved data.")).widget();
        clearLog.action = () -> {
            File lavaPosFile = new File(new File(new File(MeteorClient.FOLDER, "HighlightOldLava"), Utils.getFileWorldName()), "lavaPos.json");
            File loadedChunksFile = new File(new File(new File(MeteorClient.FOLDER, "HighlightOldLava"), Utils.getFileWorldName()), "loadedChunks.json");
            lavaPosFile.delete();
            loadedChunksFile.delete();
        };
        return list;
    }

    /* JADX WARN: Type inference failed for: r3v2, types: [meteordevelopment.meteorclient.systems.modules.hunting.HighlightOldLava$1] */
    /* JADX WARN: Type inference failed for: r3v6, types: [meteordevelopment.meteorclient.systems.modules.hunting.HighlightOldLava$2] */
    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        this.oldLava = new HashSet<>();
        this.loadedChunks = new HashSet<>();
        try {
            File lavaPosFile = new File(new File(new File(MeteorClient.FOLDER, "HighlightOldLava"), Utils.getFileWorldName()), "lavaPos.json");
            FileReader reader = new FileReader(lavaPosFile);
            this.oldLava = (HashSet) GSON.fromJson(reader, new TypeToken<HashSet<class_2338>>(this) { // from class: meteordevelopment.meteorclient.systems.modules.hunting.HighlightOldLava.1
            }.getType());
            reader.close();
            File loadedChunksFile = new File(new File(new File(MeteorClient.FOLDER, "HighlightOldLava"), Utils.getFileWorldName()), "loadedChunks.json");
            FileReader reader2 = new FileReader(loadedChunksFile);
            this.loadedChunks = (HashSet) GSON.fromJson(reader2, new TypeToken<HashSet<class_243>>(this) { // from class: meteordevelopment.meteorclient.systems.modules.hunting.HighlightOldLava.2
            }.getType());
            reader2.close();
        } catch (Exception e) {
        }
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        try {
            File lavaPosFile = new File(new File(new File(MeteorClient.FOLDER, "HighlightOldLava"), Utils.getFileWorldName()), "lavaPos.json");
            lavaPosFile.getParentFile().mkdirs();
            Writer writer = new FileWriter(lavaPosFile);
            GSON.toJson(this.oldLava, writer);
            writer.close();
            File loadedChunksFile = new File(new File(new File(MeteorClient.FOLDER, "HighlightOldLava"), Utils.getFileWorldName()), "loadedChunks.json");
            loadedChunksFile.getParentFile().mkdirs();
            Writer writer2 = new FileWriter(loadedChunksFile);
            GSON.toJson(this.loadedChunks, writer2);
            writer2.close();
        } catch (IOException | NullPointerException e) {
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (this.mc.field_1724 == null) {
            return;
        }
        if (this.logMode.get() == Mode.Highlight || this.logMode.get() == Mode.Both) {
            for (class_2338 blockPos : this.oldLava) {
                if (Math.sqrt(this.mc.field_1724.method_5707(blockPos.method_46558())) <= this.renderDistance.get().intValue()) {
                    RenderUtils.renderTickingBlock(blockPos.method_10062(), this.sideColor.get(), this.lineColor.get(), this.shapeMode.get(), 0, 8, true, false);
                }
            }
        }
    }

    @EventHandler
    private void onChunkData(ChunkDataEvent event) {
        if (this.mc.field_1724 == null) {
            return;
        }
        class_2818 class_2818VarChunk = event.chunk();
        class_243 chunkPos = class_2818VarChunk.method_12004().method_8323().method_46558();
        if (this.loadedChunks.contains(chunkPos)) {
            return;
        }
        this.loadedChunks.add(chunkPos);
        for (int x = class_2818VarChunk.method_12004().method_8326(); x <= class_2818VarChunk.method_12004().method_8327(); x++) {
            for (int z = class_2818VarChunk.method_12004().method_8328(); z <= class_2818VarChunk.method_12004().method_8329(); z++) {
                int height = class_2818VarChunk.method_12032(class_2902.class_2903.field_13202).method_12603(x - class_2818VarChunk.method_12004().method_8326(), z - class_2818VarChunk.method_12004().method_8328());
                for (int y = height; y > this.searchAbove.get().intValue(); y--) {
                    class_2338 blockPos = new class_2338(x, y, z);
                    class_2680 blockState = class_2818VarChunk.method_8320(blockPos);
                    if (blockState.method_26204() == class_2246.field_23874 || blockState.method_26204() == class_2246.field_10266) {
                        return;
                    }
                    if (blockState.method_26204() == class_2246.field_10164) {
                        boolean heightFound = true;
                        int i = 1;
                        while (true) {
                            if (i >= this.lavaHeight.get().intValue()) {
                                break;
                            }
                            if (class_2818VarChunk.method_8320(blockPos.method_10069(0, i, 0)).method_26204() == class_2246.field_10164) {
                                i++;
                            } else {
                                heightFound = false;
                                break;
                            }
                        }
                        if (heightFound) {
                            if ((this.logMode.get() == Mode.LogWebhook || this.logMode.get() == Mode.Both) && !this.webhookLink.get().isEmpty()) {
                                new Thread(() -> {
                                    HuntingUtils.sendWebhook(this.webhookLink.get(), "Old Chunk Found", "At: " + blockPos.method_10263() + " " + blockPos.method_10260(), this.ping.get().booleanValue() ? this.discordId.get() : null, this.mc.field_1724.method_7334().getName());
                                }).start();
                            }
                            if (this.disconnectOnFound.get().booleanValue()) {
                                this.mc.field_1724.field_3944.method_52781(new class_2661(class_2561.method_43470("[HighlightOldLava] Old lava was found.")));
                            }
                            this.oldLava.add(blockPos);
                            return;
                        }
                    }
                }
            }
        }
    }
}
