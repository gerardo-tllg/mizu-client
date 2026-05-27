/*
 * This file is part of the Meteor Client distribution
 * (https://github.com/MeteorDevelopment/meteor-client). Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.render;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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
// import meteordevelopment.meteorclient.utils.render.WireframeEntityRenderer; // Disabled - needs rewrite
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.Dimension;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;

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
    private final Map<UUID, PlayerEntity> playerCache;
    private final Map<UUID, Integer> ticksOnPlayerList;
    private Dimension lastDimension;

    public LogoutSpots() {
        super(Categories.Render, "logout-spots", "Displays a box where another player has logged out at.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");
        this.notifyOnRejoin = this.sgGeneral.add(new BoolSetting.Builder()
            .name("notify-on-rejoin")
            .description("Notifies you when a player rejoins.")
            .defaultValue(true)
            .build());
        this.notifyOnRejoinShowCoords = this.sgGeneral.add(new BoolSetting.Builder()
            .name("notify-on-show-coords")
            .description("Shows the coords of the player when they rejoin.")
            .defaultValue(true)
            .visible(() -> this.notifyOnRejoin.get())
            .build());
        this.notifyOnRejoinLimitDistance = this.sgGeneral.add(new BoolSetting.Builder()
            .name("notify-on-rejoin-limit-distance")
            .description("Whether or not to limit distances for rejoin coord notifications.")
            .defaultValue(true)
            .visible(() -> this.notifyOnRejoin.get() && this.notifyOnRejoinShowCoords.get())
            .build());
        this.notifyOnRejoinDistance = this.sgGeneral.add(new DoubleSetting.Builder()
            .name("notify-on-rejoin-distance")
            .description("The limit to show coords on rejoin.")
            .defaultValue(5000.0)
            .min(0.0)
            .visible(() -> this.notifyOnRejoin.get() && this.notifyOnRejoinShowCoords.get() && this.notifyOnRejoinLimitDistance.get())
            .build());
        this.shapeMode = this.sgRender.add(new EnumSetting.Builder<ShapeMode>()
            .name("shape-mode")
            .description("How the shapes are rendered.")
            .defaultValue(ShapeMode.Both)
            .build());
        this.sideColor = this.sgRender.add(new ColorSetting.Builder()
            .name("side-color")
            .description("The side color.")
            .defaultValue(new SettingColor(255, 0, 255, 55))
            .build());
        this.lineColor = this.sgRender.add(new ColorSetting.Builder()
            .name("line-color")
            .description("The line color.")
            .defaultValue(new SettingColor(255, 0, 255))
            .build());
        this.nameColor = this.sgRender.add(new ColorSetting.Builder()
            .name("name-color")
            .description("The name color.")
            .defaultValue(new SettingColor(255, 255, 255))
            .build());
        this.timeColor = this.sgRender.add(new ColorSetting.Builder()
            .name("time-color")
            .description("The time color.")
            .defaultValue(new SettingColor(255, 255, 255))
            .build());
        this.totemPopsColor = this.sgRender.add(new ColorSetting.Builder()
            .name("totem-pop-color")
            .description("The color of the totem pops.")
            .defaultValue(new SettingColor(225, 120, 20))
            .build());
        this.textBackgroundColor = this.sgRender.add(new ColorSetting.Builder()
            .name("text-background-color")
            .description("The text background color.")
            .defaultValue(new SettingColor(0, 0, 0, 75))
            .build());
        this.nametageScale = this.sgRender.add(new DoubleSetting.Builder()
            .name("text-scale")
            .description("The scale for text.")
            .defaultValue(1.0)
            .min(0.1)
            .sliderMax(2.0)
            .build());
        this.loggedPlayers = new ConcurrentHashMap<>();
        this.playerCache = new ConcurrentHashMap<>();
        this.ticksOnPlayerList = new ConcurrentHashMap<>();
        this.lineColor.onChanged();
    }

    @Override
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
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player != null && !player.equals(mc.player)) {
                this.playerCache.put(player.getGameProfile().getId(), player);
            }
        }

        this.loggedPlayers.entrySet().removeIf(entry -> {
            if (mc.getNetworkHandler().getPlayerListEntry(entry.getKey()) != null) {
                int n = 0;
                if (this.ticksOnPlayerList.containsKey(entry.getKey())) {
                    n = this.ticksOnPlayerList.get(entry.getKey());
                }

                this.ticksOnPlayerList.put(entry.getKey(), n + 1);
                if (n > 1) {
                    return true;
                }
            }

            return false;
        });
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinLeaveEvent.Join event) {
        if (event.entry.getProfile() == null || event.entry.getProfile().getId() == null) return;

        if (this.loggedPlayers.containsKey(event.entry.getProfile().getId())) {
            GhostPlayer ghost = this.loggedPlayers.remove(event.entry.getProfile().getId());
            if (this.notifyOnRejoin.get()) {
                boolean showCoords = this.notifyOnRejoinShowCoords.get();
                if (this.notifyOnRejoinLimitDistance.get() && this.notifyOnRejoinDistance.get() < ghost.pos.distanceTo(Vec3d.ZERO)) {
                    showCoords = false;
                }

                if (showCoords) {
                    this.info("(highlight)%s(default) rejoined at %d, %d, %d (highlight)(%.1fm away)(default).", ghost.name, (int) Math.floor(ghost.pos.x), (int) Math.floor(ghost.pos.y), (int) Math.floor(ghost.pos.z), mc.player.getPos().distanceTo(ghost.pos));
                } else {
                    this.info("(highlight)%s(default) rejoined", ghost.name);
                }

                mc.world.playSoundFromEntity(mc.player, mc.player, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.AMBIENT, 3.0F, 1.0F);
            }
        }
    }

    @EventHandler
    private void onPlayerLeave(PlayerJoinLeaveEvent.Leave event) {
        if (event.entry.getProfile() == null) return;

        UUID leaveId = event.entry.getProfile().getId();
        if (!this.loggedPlayers.containsKey(leaveId)) {
            if (this.playerCache.containsKey(leaveId)) {
                PlayerEntity player = this.playerCache.get(leaveId);
                if (player == null) {
                    this.warning("player with id " + leaveId.toString() + " was null for some reason :(, couldn't save logout spot");
                } else if (!(player instanceof FakePlayerEntity)) {
                    GhostPlayer ghost = new GhostPlayer(player);
                    this.loggedPlayers.put(event.entry.getProfile().getId(), ghost);
                }
            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        this.loggedPlayers.values().forEach(player -> player.render3D(event));
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        this.loggedPlayers.values().forEach(player -> player.render2D(event));
    }

    @Override
    public String getInfoString() {
        return Integer.toString(this.loggedPlayers.size());
    }

    public Map<UUID, GhostPlayer> getLoggedPlayers() {
        return this.loggedPlayers;
    }

    public class GhostPlayer {
        public final UUID uuid;
        public long logoutTime;
        public String name;
        public Box hitbox;
        public PlayerEntity playerEntity;
        // private List<WireframeEntityRenderer.RenderablePart> parts; // Disabled - needs rewrite
        public Vec3d pos;

        public GhostPlayer(PlayerEntity player) {
            this.playerEntity = player;
            this.uuid = player.getUuid();
            this.name = player.getName().getString();
            this.hitbox = player.getBoundingBox();
            this.pos = player.getPos();
            this.logoutTime = System.currentTimeMillis();
        }

        public void render3D(Render3DEvent event) {
            // WireframeEntityRenderer disabled - needs rewrite for 1.21.5
            /*if (this.parts == null && this.playerEntity != null) {
                this.parts = WireframeEntityRenderer.cloneEntityForRendering(event, this.playerEntity, this.pos);
            }

            if (this.parts != null) {
                WireframeEntityRenderer.render(event, this.pos, this.parts, 1.0, (Color) LogoutSpots.this.sideColor.get(), (Color) LogoutSpots.this.lineColor.get(), (ShapeMode) LogoutSpots.this.shapeMode.get());
            }*/
        }

        public void render2D(Render2DEvent event) {
            if (!PlayerUtils.isWithinCamera(this.pos.x, this.pos.y, this.pos.z, (double) (mc.options.getViewDistance().getValue() * 32))) return;

            TextRenderer text = TextRenderer.get();
            double scale = LogoutSpots.this.nametageScale.get();
            Vector3d nametagPos = new Vector3d(
                (this.hitbox.minX + this.hitbox.maxX) / 2.0,
                this.hitbox.maxY + 0.5,
                (this.hitbox.minZ + this.hitbox.maxZ) / 2.0
            );
            if (NametagUtils.to2D(nametagPos, scale)) {
                NametagUtils.begin(nametagPos);
                String timeText = " " + this.getTimeText();
                String totemPopsText = " " + -MeteorClient.INFO.getPops(this.uuid);
                double i = text.getWidth(this.name) / 2.0 + text.getWidth(timeText) / 2.0 + text.getWidth(totemPopsText) / 2.0;
                Renderer2D.COLOR.begin();
                Renderer2D.COLOR.quad(-i, 0.0, i * 2.0, text.getHeight(), (Color) LogoutSpots.this.textBackgroundColor.get());
                Renderer2D.COLOR.render();
                text.beginBig();
                double hX = text.render(this.name, -i, 0.0, (Color) LogoutSpots.this.nameColor.get());
                hX = text.render(timeText, hX, 0.0, (Color) LogoutSpots.this.timeColor.get());
                text.render(totemPopsText, hX, 0.0, (Color) LogoutSpots.this.totemPopsColor.get());
                text.end();
                NametagUtils.end();
            }
        }

        private String getTimeText() {
            double timeSinceLogout = (double) (System.currentTimeMillis() - this.logoutTime) / 1000.0;
            int totalSeconds = (int) timeSinceLogout;
            int hours = totalSeconds / 3600;
            int minutes = totalSeconds % 3600 / 60;
            int seconds = totalSeconds % 60;
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
    }
}
