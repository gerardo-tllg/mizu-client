package meteordevelopment.meteorclient.systems.modules.render;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.screens.EditSystemScreen;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.waypoints.Waypoint;
import meteordevelopment.meteorclient.systems.waypoints.Waypoints;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_418;
import net.minecraft.class_5250;
import org.joml.Vector3d;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/WaypointsModule.class */
public class WaypointsModule extends Module {
    private static final Color GRAY = new Color(200, 200, 200);
    private static final Color TEXT = new Color(255, 255, 255);
    private final SettingGroup sgGeneral;
    private final SettingGroup sgDeathPosition;
    public final Setting<Integer> textRenderDistance;
    private final Setting<Integer> waypointFadeDistance;
    private final Setting<Integer> maxDeathPositions;
    private final Setting<Boolean> dpChat;
    private final SimpleDateFormat dateFormat;

    public WaypointsModule() {
        super(Categories.Render, "waypoints", "Allows you to create waypoints.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgDeathPosition = this.settings.createGroup("Death Position");
        this.textRenderDistance = this.sgGeneral.add(new IntSetting.Builder().name("text-render-distance").description("Maximum distance from the center of the screen at which text will be rendered.").defaultValue(100).min(0).sliderMax(200).build());
        this.waypointFadeDistance = this.sgGeneral.add(new IntSetting.Builder().name("waypoint-fade-distance").description("The distance to a waypoint at which it begins to start fading.").defaultValue(20).sliderRange(0, 100).min(0).build());
        this.maxDeathPositions = this.sgDeathPosition.add(new IntSetting.Builder().name("max-death-positions").description("The amount of death positions to save, 0 to disable").defaultValue(0).min(0).sliderMax(20).onChanged((v1) -> {
            cleanDeathWPs(v1);
        }).build());
        this.dpChat = this.sgDeathPosition.add(new BoolSetting.Builder().name("chat").description("Send a chat message with your position once you die").defaultValue(false).build());
        this.dateFormat = new SimpleDateFormat("HH:mm:ss");
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        TextRenderer text = TextRenderer.get();
        Vector3d center = new Vector3d(((double) this.mc.method_22683().method_4489()) / 2.0d, ((double) this.mc.method_22683().method_4506()) / 2.0d, 0.0d);
        int textRenderDist = this.textRenderDistance.get().intValue();
        for (Waypoint waypoint : Waypoints.get()) {
            if (waypoint.visible.get().booleanValue() && Waypoints.checkDimension(waypoint)) {
                class_2338 blockPos = waypoint.getPos();
                Vector3d pos = new Vector3d(((double) blockPos.method_10263()) + 0.5d, blockPos.method_10264(), ((double) blockPos.method_10260()) + 0.5d);
                double dist = PlayerUtils.distanceToCamera(pos.x, pos.y, pos.z);
                if (dist <= waypoint.maxVisible.get().intValue() && NametagUtils.to2D(pos, waypoint.scale.get().doubleValue() - 0.2d)) {
                    double distToCenter = pos.distance(center);
                    double a = 1.0d;
                    if (dist < this.waypointFadeDistance.get().intValue()) {
                        a = (dist - (((double) this.waypointFadeDistance.get().intValue()) / 2.0d)) / (((double) this.waypointFadeDistance.get().intValue()) / 2.0d);
                        if (a < 0.01d) {
                        }
                    }
                    NametagUtils.begin(pos);
                    waypoint.renderIcon(-16.0d, -16.0d, a, 32.0d);
                    if (distToCenter <= textRenderDist) {
                        int preTextA = TEXT.a;
                        TEXT.a *= (int) a;
                        text.begin();
                        text.render(waypoint.name.get(), (-text.getWidth(waypoint.name.get())) / 2.0d, (-16.0d) - text.getHeight(), TEXT, true);
                        String distText = String.format("%d blocks", Integer.valueOf((int) Math.round(dist)));
                        text.render(distText, (-text.getWidth(distText)) / 2.0d, 16.0d, TEXT, true);
                        text.end();
                        TEXT.a = preTextA;
                    }
                    NametagUtils.end();
                }
            }
        }
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if ((event.screen instanceof class_418) && !event.isCancelled()) {
            addDeath(this.mc.field_1724.method_19538());
        }
    }

    public void addDeath(class_243 deathPos) {
        String time = this.dateFormat.format(new Date());
        if (this.dpChat.get().booleanValue()) {
            class_5250 text = class_2561.method_43470("Died at ");
            text.method_10852(ChatUtils.formatCoords(deathPos));
            text.method_27693(String.format(" on %s.", time));
            info(text);
        }
        if (this.maxDeathPositions.get().intValue() > 0) {
            Waypoint waypoint = new Waypoint.Builder().name("Death " + time).icon("skull").pos(class_2338.method_49638(deathPos).method_10086(2)).dimension(PlayerUtils.getDimension()).build();
            Waypoints.get().add(waypoint);
        }
        cleanDeathWPs(this.maxDeathPositions.get().intValue());
    }

    private void cleanDeathWPs(int max) {
        int oldWpC = 0;
        Iterator<Waypoint> it = Waypoints.get().iterator();
        while (it.hasNext()) {
            Waypoint wp = it.next();
            if (wp.name.get().startsWith("Death ") && wp.icon.get().equals("skull")) {
                oldWpC++;
                if (oldWpC > max) {
                    it.remove();
                }
            }
        }
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public WWidget getWidget(GuiTheme theme) {
        if (!Utils.canUpdate()) {
            return theme.label("You need to be in a world.");
        }
        WTable table = theme.table();
        initTable(theme, table);
        return table;
    }

    private void initTable(GuiTheme theme, WTable table) {
        table.clear();
        for (Waypoint waypoint : Waypoints.get()) {
            boolean validDim = Waypoints.checkDimension(waypoint);
            table.add(new WIcon(waypoint));
            WLabel name = (WLabel) table.add(theme.label(waypoint.name.get())).expandCellX().widget();
            if (!validDim) {
                name.color = GRAY;
            }
            WCheckbox visible = (WCheckbox) table.add(theme.checkbox(waypoint.visible.get().booleanValue())).widget();
            visible.action = () -> {
                waypoint.visible.set(Boolean.valueOf(visible.checked));
                Waypoints.get().save();
            };
            WButton edit = (WButton) table.add(theme.button(GuiRenderer.EDIT)).widget();
            edit.action = () -> {
                this.mc.method_1507(new EditWaypointScreen(theme, waypoint, () -> {
                    initTable(theme, table);
                }));
            };
            if (validDim) {
                WButton gotoB = (WButton) table.add(theme.button("Goto")).widget();
                gotoB.action = () -> {
                    if (PathManagers.get().isPathing()) {
                        PathManagers.get().stop();
                    }
                    PathManagers.get().moveTo(waypoint.getPos());
                };
            }
            WMinus remove = (WMinus) table.add(theme.minus()).widget();
            remove.action = () -> {
                Waypoints.get().remove(waypoint);
                initTable(theme, table);
            };
            table.row();
        }
        table.add(theme.horizontalSeparator()).expandX();
        table.row();
        WButton create = (WButton) table.add(theme.button("Create")).expandX().widget();
        create.action = () -> {
            this.mc.method_1507(new EditWaypointScreen(theme, null, () -> {
                initTable(theme, table);
            }));
        };
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/WaypointsModule$EditWaypointScreen.class */
    private static class EditWaypointScreen extends EditSystemScreen<Waypoint> {
        public EditWaypointScreen(GuiTheme theme, Waypoint value, Runnable reload) {
            super(theme, value, reload);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // meteordevelopment.meteorclient.gui.screens.EditSystemScreen
        public Waypoint create() {
            return new Waypoint.Builder().pos(class_310.method_1551().field_1724.method_24515().method_10086(2)).dimension(PlayerUtils.getDimension()).build();
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // meteordevelopment.meteorclient.gui.screens.EditSystemScreen
        public boolean save() {
            if (((Waypoint) this.value).name.get().isBlank()) {
                return false;
            }
            Waypoints.get().add((Waypoint) this.value);
            return true;
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // meteordevelopment.meteorclient.gui.screens.EditSystemScreen
        public Settings getSettings() {
            return ((Waypoint) this.value).settings;
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/WaypointsModule$WIcon.class */
    private static class WIcon extends WWidget {
        private final Waypoint waypoint;

        public WIcon(Waypoint waypoint) {
            this.waypoint = waypoint;
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
        protected void onCalculateSize() {
            double s = this.theme.scale(32.0d);
            this.width = s;
            this.height = s;
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            renderer.post(() -> {
                this.waypoint.renderIcon(this.x, this.y, 1.0d, this.width);
            });
        }
    }
}
