package meteordevelopment.meteorclient.systems.modules.render;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.PlayerJoinLeaveEvent;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerEntity;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.Dimension;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.class_1657;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3417;
import net.minecraft.class_3419;
import org.joml.Vector3d;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/LogoutSpots.class */
public class LogoutSpots extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;
    private final Setting<Boolean> notifyOnRejoin;
    private final Setting<Boolean> notifyOnRejoinShowCoords;
    private final Setting<Boolean> notifyOnRejoinLimitDistance;
    private final Setting<Double> notifyOnRejoinDistance;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final Setting<SettingColor> nameColor;
    private final Setting<SettingColor> timeColor;
    private final Setting<SettingColor> totemPopsColor;
    private final Setting<SettingColor> textBackgroundColor;
    private final Setting<Double> nametageScale;
    private final Map<UUID, GhostPlayer> loggedPlayers;
    private final Map<UUID, class_1657> playerCache;
    private final Map<UUID, Integer> ticksOnPlayerList;
    private Dimension lastDimension;

    public LogoutSpots() {
        super(Categories.Render, "logout-spots", "Displays a box where another player has logged out at.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");
        this.notifyOnRejoin = this.sgGeneral.add(new BoolSetting.Builder().name("notify-on-rejoin").description("Notifies you when a player rejoins.").defaultValue(true).build());
        this.notifyOnRejoinShowCoords = this.sgGeneral.add(new BoolSetting.Builder().name("notify-on-show-coords").description("Shows the coords of the player when they rejoin.").defaultValue(true).visible(() -> {
            return this.notifyOnRejoin.get().booleanValue();
        }).build());
        this.notifyOnRejoinLimitDistance = this.sgGeneral.add(new BoolSetting.Builder().name("notify-on-rejoin-limit-distance").description("Whether or not to limit distances for rejoin coord notifications.").defaultValue(true).visible(() -> {
            return this.notifyOnRejoin.get().booleanValue() && this.notifyOnRejoinShowCoords.get().booleanValue();
        }).build());
        this.notifyOnRejoinDistance = this.sgGeneral.add(new DoubleSetting.Builder().name("notify-on-rejoin-distance").description("The limit to show coords on rejoin.").defaultValue(5000.0d).min(0.0d).visible(() -> {
            return this.notifyOnRejoin.get().booleanValue() && this.notifyOnRejoinShowCoords.get().booleanValue() && this.notifyOnRejoinLimitDistance.get().booleanValue();
        }).build());
        this.shapeMode = this.sgRender.add(new EnumSetting.Builder().name("shape-mode").description("How the shapes are rendered.").defaultValue(ShapeMode.Both).build());
        this.sideColor = this.sgRender.add(new ColorSetting.Builder().name("side-color").description("The side color.").defaultValue(new SettingColor(255, 0, 255, 55)).build());
        this.lineColor = this.sgRender.add(new ColorSetting.Builder().name("line-color").description("The line color.").defaultValue(new SettingColor(255, 0, 255)).build());
        this.nameColor = this.sgRender.add(new ColorSetting.Builder().name("name-color").description("The name color.").defaultValue(new SettingColor(255, 255, 255)).build());
        this.timeColor = this.sgRender.add(new ColorSetting.Builder().name("time-color").description("The time color.").defaultValue(new SettingColor(255, 255, 255)).build());
        this.totemPopsColor = this.sgRender.add(new ColorSetting.Builder().name("totem-pop-color").description("The color of the totem pops.").defaultValue(new SettingColor(225, Opcode.ISHL, 20)).build());
        this.textBackgroundColor = this.sgRender.add(new ColorSetting.Builder().name("text-background-color").description("The text background color.").defaultValue(new SettingColor(0, 0, 0, 75)).build());
        this.nametageScale = this.sgRender.add(new DoubleSetting.Builder().name("text-scale").description("The scale for text.").defaultValue(1.0d).min(0.1d).sliderMax(2.0d).build());
        this.loggedPlayers = new ConcurrentHashMap();
        this.playerCache = new ConcurrentHashMap();
        this.ticksOnPlayerList = new ConcurrentHashMap();
        this.lineColor.onChanged();
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        this.lastDimension = PlayerUtils.getDimension();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onTick(TickEvent.Post event) {
        Dimension dimension = PlayerUtils.getDimension();
        if (dimension != this.lastDimension) {
            this.loggedPlayers.clear();
        }
        this.lastDimension = dimension;
        for (class_1657 player : this.mc.field_1687.method_18456()) {
            if (player != null && !player.equals(this.mc.field_1724)) {
                this.playerCache.put(player.method_7334().getId(), player);
            }
        }
        this.loggedPlayers.entrySet().removeIf(entry -> {
            if (this.mc.method_1562().method_2871((UUID) entry.getKey()) != null) {
                int n = 0;
                if (this.ticksOnPlayerList.containsKey(entry.getKey())) {
                    n = this.ticksOnPlayerList.get(entry.getKey()).intValue();
                }
                this.ticksOnPlayerList.put((UUID) entry.getKey(), Integer.valueOf(n + 1));
                if (n > 1) {
                    return true;
                }
                return false;
            }
            return false;
        });
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinLeaveEvent.Join event) {
        if (event.entry.method_2966() != null && event.entry.method_2966().getId() != null && this.loggedPlayers.containsKey(event.entry.method_2966().getId())) {
            GhostPlayer ghost = this.loggedPlayers.remove(event.entry.method_2966().getId());
            if (this.notifyOnRejoin.get().booleanValue()) {
                boolean showCoords = this.notifyOnRejoinShowCoords.get().booleanValue();
                if (this.notifyOnRejoinLimitDistance.get().booleanValue() && this.notifyOnRejoinDistance.get().doubleValue() < ghost.pos.method_1022(class_243.field_1353)) {
                    showCoords = false;
                }
                if (showCoords) {
                    info("(highlight)%s(default) rejoined at %d, %d, %d (highlight)(%.1fm away)(default).", ghost.name, Integer.valueOf((int) Math.floor(ghost.pos.field_1352)), Integer.valueOf((int) Math.floor(ghost.pos.field_1351)), Integer.valueOf((int) Math.floor(ghost.pos.field_1350)), Double.valueOf(this.mc.field_1724.method_19538().method_1022(ghost.pos)));
                } else {
                    info("(highlight)%s(default) rejoined", ghost.name);
                }
                this.mc.field_1687.method_43129(this.mc.field_1724, this.mc.field_1724, class_3417.field_14627, class_3419.field_15256, 3.0f, 1.0f);
            }
        }
    }

    @EventHandler
    private void onPlayerLeave(PlayerJoinLeaveEvent.Leave event) {
        if (event.entry.method_2966() == null) {
            return;
        }
        UUID leaveId = event.entry.method_2966().getId();
        if (!this.loggedPlayers.containsKey(leaveId) && this.playerCache.containsKey(leaveId)) {
            class_1657 player = this.playerCache.get(leaveId);
            if (player == null) {
                warning("player with id " + leaveId.toString() + " was null for some reason :(, couldn't save logout spot", new Object[0]);
            } else if (!(player instanceof FakePlayerEntity)) {
                GhostPlayer ghost = new GhostPlayer(player);
                this.loggedPlayers.put(event.entry.method_2966().getId(), ghost);
            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        this.loggedPlayers.values().forEach(player -> {
            player.render3D(event);
        });
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        this.loggedPlayers.values().forEach(player -> {
            player.render2D(event);
        });
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public String getInfoString() {
        return Integer.toString(this.loggedPlayers.size());
    }

    public Map<UUID, GhostPlayer> getLoggedPlayers() {
        return this.loggedPlayers;
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/LogoutSpots$GhostPlayer.class */
    public class GhostPlayer {
        public final UUID uuid;
        public long logoutTime = System.currentTimeMillis();
        public String name;
        public class_238 hitbox;
        public class_1657 playerEntity;
        public class_243 pos;

        public GhostPlayer(class_1657 player) {
            this.playerEntity = player;
            this.uuid = player.method_5667();
            this.name = player.method_5477().getString();
            this.hitbox = player.method_5829();
            this.pos = player.method_19538();
        }

        public void render3D(Render3DEvent event) {
        }

        public void render2D(Render2DEvent event) {
            if (PlayerUtils.isWithinCamera(this.pos.field_1352, this.pos.field_1351, this.pos.field_1350, ((Integer) LogoutSpots.this.mc.field_1690.method_42503().method_41753()).intValue() * 32)) {
                TextRenderer text = TextRenderer.get();
                double scale = LogoutSpots.this.nametageScale.get().doubleValue();
                Vector3d nametagPos = new Vector3d((this.hitbox.field_1323 + this.hitbox.field_1320) / 2.0d, this.hitbox.field_1325 + 0.5d, (this.hitbox.field_1321 + this.hitbox.field_1324) / 2.0d);
                if (NametagUtils.to2D(nametagPos, scale)) {
                    NametagUtils.begin(nametagPos);
                    String timeText = " " + getTimeText();
                    String totemPopsText = " " + (-MeteorClient.INFO.getPops(this.uuid));
                    double i = (text.getWidth(this.name) / 2.0d) + (text.getWidth(timeText) / 2.0d) + (text.getWidth(totemPopsText) / 2.0d);
                    Renderer2D.COLOR.begin();
                    Renderer2D.COLOR.quad(-i, 0.0d, i * 2.0d, text.getHeight(), LogoutSpots.this.textBackgroundColor.get());
                    Renderer2D.COLOR.render();
                    text.beginBig();
                    double hX = text.render(this.name, -i, 0.0d, LogoutSpots.this.nameColor.get());
                    text.render(totemPopsText, text.render(timeText, hX, 0.0d, LogoutSpots.this.timeColor.get()), 0.0d, LogoutSpots.this.totemPopsColor.get());
                    text.end();
                    NametagUtils.end();
                }
            }
        }

        private String getTimeText() {
            double timeSinceLogout = (System.currentTimeMillis() - this.logoutTime) / 1000.0d;
            int totalSeconds = (int) timeSinceLogout;
            int hours = totalSeconds / 3600;
            int minutes = (totalSeconds % 3600) / 60;
            int seconds = totalSeconds % 60;
            return String.format("%02d:%02d:%02d", Integer.valueOf(hours), Integer.valueOf(minutes), Integer.valueOf(seconds));
        }
    }
}
