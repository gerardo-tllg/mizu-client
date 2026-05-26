package meteordevelopment.meteorclient.systems.modules.misc;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Set;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.DamageUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_124;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1657;
import net.minecraft.class_2561;
import net.minecraft.class_2661;
import net.minecraft.class_2663;
import net.minecraft.class_5250;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/misc/AutoLog.class */
public class AutoLog extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgEntities;
    private final Setting<Integer> health;
    private final Setting<Boolean> smart;
    private final Setting<Integer> totemPops;
    private final Setting<Boolean> onlyTrusted;
    private final Setting<Boolean> instantDeath;
    private final Setting<Boolean> smartToggle;
    private final Setting<Boolean> toggleOff;
    private final Setting<Boolean> toggleAutoReconnect;
    private final Setting<Set<class_1299<?>>> entities;
    private final Setting<Boolean> useTotalCount;
    private final Setting<Integer> combinedEntityThreshold;
    private final Setting<Integer> individualEntityThreshold;
    private final Setting<Integer> range;
    private final Object2IntMap<class_1299<?>> entityCounts;
    private int pops;
    private final StaticListener staticListener;

    public AutoLog() {
        super(Categories.Combat, "auto-log", "Automatically disconnects you when certain requirements are met.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgEntities = this.settings.createGroup("Entities");
        this.health = this.sgGeneral.add(new IntSetting.Builder().name("health").description("Automatically disconnects when health is lower or equal to this value. Set to 0 to disable.").defaultValue(6).range(0, 19).sliderMax(19).build());
        this.smart = this.sgGeneral.add(new BoolSetting.Builder().name("predict-incoming-damage").description("Disconnects when it detects you're about to take enough damage to set you under the 'health' setting.").defaultValue(true).build());
        this.totemPops = this.sgGeneral.add(new IntSetting.Builder().name("totem-pops").description("Disconnects when you have popped this many totems. Set to 0 to disable.").defaultValue(0).min(0).build());
        this.onlyTrusted = this.sgGeneral.add(new BoolSetting.Builder().name("only-trusted").description("Disconnects when a player not on your friends list appears in render distance.").defaultValue(false).build());
        this.instantDeath = this.sgGeneral.add(new BoolSetting.Builder().name("32K").description("Disconnects when a player near you can instantly kill you.").defaultValue(false).build());
        this.smartToggle = this.sgGeneral.add(new BoolSetting.Builder().name("smart-toggle").description("Disables Auto Log after a low-health logout. WILL re-enable once you heal.").defaultValue(false).build());
        this.toggleOff = this.sgGeneral.add(new BoolSetting.Builder().name("toggle-off").description("Disables Auto Log after usage.").defaultValue(true).build());
        this.toggleAutoReconnect = this.sgGeneral.add(new BoolSetting.Builder().name("toggle-auto-reconnect").description("Whether to disable Auto Reconnect after a logout.").defaultValue(true).build());
        this.entities = this.sgEntities.add(new EntityTypeListSetting.Builder().name("entities").description("Disconnects when a specified entity is present within a specified range.").defaultValue(class_1299.field_6110).build());
        this.useTotalCount = this.sgEntities.add(new BoolSetting.Builder().name("use-total-count").description("Toggle between counting the total number of all selected entities or each entity individually.").defaultValue(true).visible(() -> {
            return !this.entities.get().isEmpty();
        }).build());
        this.combinedEntityThreshold = this.sgEntities.add(new IntSetting.Builder().name("combined-entity-threshold").description("The minimum total number of selected entities that must be near you before disconnection occurs.").defaultValue(10).min(1).sliderMax(32).visible(() -> {
            return this.useTotalCount.get().booleanValue() && !this.entities.get().isEmpty();
        }).build());
        this.individualEntityThreshold = this.sgEntities.add(new IntSetting.Builder().name("individual-entity-threshold").description("The minimum number of entities individually that must be near you before disconnection occurs.").defaultValue(2).min(1).sliderMax(16).visible(() -> {
            return (this.useTotalCount.get().booleanValue() || this.entities.get().isEmpty()) ? false : true;
        }).build());
        this.range = this.sgEntities.add(new IntSetting.Builder().name("range").description("How close an entity has to be to you before you disconnect.").defaultValue(5).min(1).sliderMax(16).visible(() -> {
            return !this.entities.get().isEmpty();
        }).build());
        this.entityCounts = new Object2IntOpenHashMap();
        this.staticListener = new StaticListener();
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        this.pops = 0;
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        class_1297 entity;
        class_2663 class_2663Var = event.packet;
        if (class_2663Var instanceof class_2663) {
            class_2663 p = class_2663Var;
            if (p.method_11470() == 35 && (entity = p.method_11469(this.mc.field_1687)) != null && entity.equals(this.mc.field_1724)) {
                this.pops++;
                if (this.totemPops.get().intValue() > 0 && this.pops >= this.totemPops.get().intValue()) {
                    disconnect("Popped " + this.pops + " totems.");
                    if (this.toggleOff.get().booleanValue()) {
                        toggle();
                    }
                }
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        float playerHealth = this.mc.field_1724.method_6032();
        if (playerHealth <= 0.0f) {
            toggle();
            return;
        }
        if (playerHealth <= this.health.get().intValue()) {
            disconnect("Health was lower than " + String.valueOf(this.health.get()) + ".");
            if (this.smartToggle.get().booleanValue()) {
                if (isActive()) {
                    toggle();
                }
                enableHealthListener();
                return;
            } else {
                if (this.toggleOff.get().booleanValue()) {
                    toggle();
                    return;
                }
                return;
            }
        }
        if (this.smart.get().booleanValue() && (playerHealth + this.mc.field_1724.method_6067()) - PlayerUtils.possibleHealthReductions() < this.health.get().intValue()) {
            disconnect("Health was going to be lower than " + String.valueOf(this.health.get()) + ".");
            if (this.toggleOff.get().booleanValue()) {
                toggle();
                return;
            }
            return;
        }
        if (!this.onlyTrusted.get().booleanValue() && !this.instantDeath.get().booleanValue() && this.entities.get().isEmpty()) {
            return;
        }
        for (class_1657 class_1657Var : this.mc.field_1687.method_18112()) {
            if (class_1657Var instanceof class_1657) {
                class_1657 player = class_1657Var;
                if (player.method_5667() == this.mc.field_1724.method_5667()) {
                    continue;
                } else {
                    if (this.onlyTrusted.get().booleanValue() && player != this.mc.field_1724 && !Friends.get().isFriend(player)) {
                        disconnect((class_2561) class_2561.method_43470("Non-trusted player '" + String.valueOf(class_124.field_1061) + player.method_5477().getString() + String.valueOf(class_124.field_1068) + "' appeared in your render distance."));
                        if (this.toggleOff.get().booleanValue()) {
                            toggle();
                            return;
                        }
                        return;
                    }
                    if (this.instantDeath.get().booleanValue() && PlayerUtils.isWithin((class_1297) class_1657Var, 8.0d) && DamageUtils.getAttackDamage(player, this.mc.field_1724) > playerHealth + this.mc.field_1724.method_6067()) {
                        disconnect("Anti-32k measures.");
                        if (this.toggleOff.get().booleanValue()) {
                            toggle();
                            return;
                        }
                        return;
                    }
                }
            }
        }
        if (!this.entities.get().isEmpty()) {
            int totalEntities = 0;
            this.entityCounts.clear();
            for (class_1297 entity : this.mc.field_1687.method_18112()) {
                if (PlayerUtils.isWithin(entity, this.range.get().intValue()) && this.entities.get().contains(entity.method_5864())) {
                    totalEntities++;
                    if (!this.useTotalCount.get().booleanValue()) {
                        this.entityCounts.put(entity.method_5864(), this.entityCounts.getOrDefault(entity.method_5864(), 0) + 1);
                    }
                }
            }
            if (this.useTotalCount.get().booleanValue() && totalEntities >= this.combinedEntityThreshold.get().intValue()) {
                disconnect("Total number of selected entities within range exceeded the limit.");
                if (this.toggleOff.get().booleanValue()) {
                    toggle();
                    return;
                }
                return;
            }
            if (!this.useTotalCount.get().booleanValue()) {
                ObjectIterator it = this.entityCounts.object2IntEntrySet().iterator();
                while (it.hasNext()) {
                    Object2IntMap.Entry<class_1299<?>> entry = (Object2IntMap.Entry) it.next();
                    if (entry.getIntValue() >= this.individualEntityThreshold.get().intValue()) {
                        disconnect("Number of " + ((class_1299) entry.getKey()).method_5897().getString() + " within range exceeded the limit.");
                        if (this.toggleOff.get().booleanValue()) {
                            toggle();
                            return;
                        }
                        return;
                    }
                }
            }
        }
    }

    private void disconnect(String reason) {
        disconnect((class_2561) class_2561.method_43470(reason));
    }

    private void disconnect(class_2561 reason) {
        class_5250 text = class_2561.method_43470("[AutoLog] ");
        text.method_10852(reason);
        AutoReconnect autoReconnect = (AutoReconnect) Modules.get().get(AutoReconnect.class);
        if (autoReconnect.isActive() && this.toggleAutoReconnect.get().booleanValue()) {
            text.method_10852(class_2561.method_43470("\n\nINFO - AutoReconnect was disabled").method_54663(-8355712));
            autoReconnect.toggle();
        }
        this.mc.field_1724.field_3944.method_52781(new class_2661(text));
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/misc/AutoLog$StaticListener.class */
    private class StaticListener {
        private StaticListener() {
        }

        @EventHandler
        private void healthListener(TickEvent.Post event) {
            if (!AutoLog.this.isActive()) {
                if (Utils.canUpdate() && !AutoLog.this.mc.field_1724.method_29504() && AutoLog.this.mc.field_1724.method_6032() > AutoLog.this.health.get().intValue()) {
                    AutoLog.this.info("Player health greater than minimum, re-enabling module.", new Object[0]);
                    AutoLog.this.toggle();
                    AutoLog.this.disableHealthListener();
                    return;
                }
                return;
            }
            AutoLog.this.disableHealthListener();
        }
    }

    private void enableHealthListener() {
        MeteorClient.EVENT_BUS.subscribe(this.staticListener);
    }

    private void disableHealthListener() {
        MeteorClient.EVENT_BUS.unsubscribe(this.staticListener);
    }
}
