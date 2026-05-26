package meteordevelopment.meteorclient.systems.modules.render;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.network.Http;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1309;
import net.minecraft.class_1321;
import net.minecraft.class_1657;
import org.joml.Vector3d;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/EntityOwner.class */
public class EntityOwner extends Module {
    private static final Color BACKGROUND = new Color(0, 0, 0, 75);
    private static final Color TEXT = new Color(255, 255, 255);
    private final SettingGroup sgGeneral;
    private final Setting<Double> scale;
    private final Vector3d pos;
    private final Map<UUID, String> uuidToName;

    public EntityOwner() {
        super(Categories.Render, "entity-owner", "Displays the name of the player who owns the entity you're looking at.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.scale = this.sgGeneral.add(new DoubleSetting.Builder().name("scale").description("The scale of the text.").defaultValue(1.0d).min(0.0d).build());
        this.pos = new Vector3d();
        this.uuidToName = new HashMap();
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        this.uuidToName.clear();
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        for (class_1321 class_1321Var : this.mc.field_1687.method_18112()) {
            if (class_1321Var instanceof class_1321) {
                class_1321 tameable = class_1321Var;
                class_1309 owner = tameable.method_35057();
                UUID ownerUuid = owner != null ? owner.method_5667() : null;
                if (ownerUuid != null) {
                    Utils.set(this.pos, class_1321Var, event.tickDelta);
                    this.pos.add(0.0d, ((double) class_1321Var.method_18381(class_1321Var.method_18376())) + 0.75d, 0.0d);
                    if (NametagUtils.to2D(this.pos, this.scale.get().doubleValue())) {
                        renderNametag(getOwnerName(ownerUuid));
                    }
                }
            }
        }
    }

    private void renderNametag(String name) {
        TextRenderer text = TextRenderer.get();
        NametagUtils.begin(this.pos);
        text.beginBig();
        double w = text.getWidth(name);
        double x = (-w) / 2.0d;
        double y = -text.getHeight();
        Renderer2D.COLOR.begin();
        Renderer2D.COLOR.quad(x - 1.0d, y - 1.0d, w + 2.0d, text.getHeight() + 2.0d, BACKGROUND);
        Renderer2D.COLOR.render();
        text.render(name, x, y, TEXT);
        text.end();
        NametagUtils.end();
    }

    private String getOwnerName(UUID uuid) {
        class_1657 player = this.mc.field_1687.method_18470(uuid);
        if (player != null) {
            return player.method_5477().getString();
        }
        String name = this.uuidToName.get(uuid);
        if (name != null) {
            return name;
        }
        MeteorExecutor.execute(() -> {
            if (isActive()) {
                ProfileResponse res = (ProfileResponse) Http.get("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString().replace("-", "")).sendJson(ProfileResponse.class);
                if (isActive()) {
                    if (res != null) {
                        this.uuidToName.put(uuid, res.name);
                    } else {
                        this.uuidToName.put(uuid, "Failed to get name");
                    }
                }
            }
        });
        this.uuidToName.put(uuid, "Retrieving");
        return "Retrieving";
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/EntityOwner$ProfileResponse.class */
    private static class ProfileResponse {
        public String name;

        private ProfileResponse() {
        }
    }
}
