package meteordevelopment.meteorclient.systems.modules.movement;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/AntiAFK.class */
public class AntiAFK extends Module {
    private final SettingGroup sgActions;
    private final SettingGroup sgMessages;
    private final Setting<Boolean> jump;
    private final Setting<Boolean> swing;
    private final Setting<Boolean> sneak;
    private final Setting<Integer> sneakTime;
    private final Setting<Boolean> strafe;
    private final Setting<Boolean> spin;
    private final Setting<SpinMode> spinMode;
    private final Setting<Integer> spinSpeed;
    private final Setting<Integer> pitch;
    private final Setting<Boolean> sendMessages;
    private final Setting<Boolean> randomMessage;
    private final Setting<Integer> delay;
    private final Setting<List<String>> messages;
    private final Random random;
    private int messageTimer;
    private int messageI;
    private int sneakTimer;
    private int strafeTimer;
    private boolean direction;
    private float prevYaw;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/movement/AntiAFK$SpinMode.class */
    public enum SpinMode {
        Server,
        Client
    }

    public AntiAFK() {
        super(Categories.Player, "anti-afk", "Performs different actions to prevent getting kicked while AFK.");
        this.sgActions = this.settings.createGroup("Actions");
        this.sgMessages = this.settings.createGroup("Messages");
        this.jump = this.sgActions.add(new BoolSetting.Builder().name("jump").description("Jump randomly.").defaultValue(true).build());
        this.swing = this.sgActions.add(new BoolSetting.Builder().name("swing").description("Swings your hand.").defaultValue(false).build());
        this.sneak = this.sgActions.add(new BoolSetting.Builder().name("sneak").description("Sneaks and unsneaks quickly.").defaultValue(false).build());
        SettingGroup settingGroup = this.sgActions;
        IntSetting.Builder builderSliderMin = new IntSetting.Builder().name("sneak-time").description("How many ticks to stay sneaked.").defaultValue(5).min(1).sliderMin(1);
        Setting<Boolean> setting = this.sneak;
        Objects.requireNonNull(setting);
        this.sneakTime = settingGroup.add(builderSliderMin.visible(setting::get).build());
        this.strafe = this.sgActions.add(new BoolSetting.Builder().name("strafe").description("Strafe right and left.").defaultValue(false).onChanged(aBoolean -> {
            this.strafeTimer = 0;
            this.direction = false;
            if (isActive()) {
                this.mc.field_1690.field_1913.method_23481(false);
                this.mc.field_1690.field_1849.method_23481(false);
            }
        }).build());
        this.spin = this.sgActions.add(new BoolSetting.Builder().name("spin").description("Spins the player in place.").defaultValue(true).build());
        SettingGroup settingGroup2 = this.sgActions;
        EnumSetting.Builder builderDefaultValue = new EnumSetting.Builder().name("spin-mode").description("The method of rotating.").defaultValue(SpinMode.Server);
        Setting<Boolean> setting2 = this.spin;
        Objects.requireNonNull(setting2);
        this.spinMode = settingGroup2.add(builderDefaultValue.visible(setting2::get).build());
        SettingGroup settingGroup3 = this.sgActions;
        IntSetting.Builder builderDefaultValue2 = new IntSetting.Builder().name("speed").description("The speed to spin you.").defaultValue(7);
        Setting<Boolean> setting3 = this.spin;
        Objects.requireNonNull(setting3);
        this.spinSpeed = settingGroup3.add(builderDefaultValue2.visible(setting3::get).build());
        this.pitch = this.sgActions.add(new IntSetting.Builder().name("pitch").description("The pitch to send to the server.").defaultValue(0).range(-90, 90).sliderRange(-90, 90).visible(() -> {
            return this.spin.get().booleanValue() && this.spinMode.get() == SpinMode.Server;
        }).build());
        this.sendMessages = this.sgMessages.add(new BoolSetting.Builder().name("send-messages").description("Sends messages to prevent getting kicked for AFK.").defaultValue(false).build());
        SettingGroup settingGroup4 = this.sgMessages;
        BoolSetting.Builder builderDefaultValue3 = new BoolSetting.Builder().name("random").description("Selects a random message from your message list.").defaultValue(false);
        Setting<Boolean> setting4 = this.sendMessages;
        Objects.requireNonNull(setting4);
        this.randomMessage = settingGroup4.add(builderDefaultValue3.visible(setting4::get).build());
        SettingGroup settingGroup5 = this.sgMessages;
        IntSetting.Builder builderSliderMax = new IntSetting.Builder().name("delay").description("The delay between specified messages in seconds.").defaultValue(15).min(0).sliderMax(30);
        Setting<Boolean> setting5 = this.sendMessages;
        Objects.requireNonNull(setting5);
        this.delay = settingGroup5.add(builderSliderMax.visible(setting5::get).build());
        SettingGroup settingGroup6 = this.sgMessages;
        StringListSetting.Builder builderDefaultValue4 = new StringListSetting.Builder().name("messages").description("The messages to choose from.").defaultValue("Meteor on top!", "Meteor on crack!");
        Setting<Boolean> setting6 = this.sendMessages;
        Objects.requireNonNull(setting6);
        this.messages = settingGroup6.add(builderDefaultValue4.visible(setting6::get).build());
        this.random = new Random();
        this.messageTimer = 0;
        this.messageI = 0;
        this.sneakTimer = 0;
        this.strafeTimer = 0;
        this.direction = false;
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        if (this.sendMessages.get().booleanValue() && this.messages.get().isEmpty()) {
            warning("Message list is empty, disabling messages...", new Object[0]);
            this.sendMessages.set(false);
        }
        this.prevYaw = this.mc.field_1724.method_36454();
        this.messageTimer = this.delay.get().intValue() * 20;
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        if (this.strafe.get().booleanValue()) {
            this.mc.field_1690.field_1913.method_23481(false);
            this.mc.field_1690.field_1849.method_23481(false);
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (Utils.canUpdate()) {
            if (this.jump.get().booleanValue()) {
                if (this.mc.field_1690.field_1903.method_1434()) {
                    this.mc.field_1690.field_1903.method_23481(false);
                } else if (this.random.nextInt(99) == 0) {
                    this.mc.field_1690.field_1903.method_23481(true);
                }
            }
            if (this.swing.get().booleanValue() && this.random.nextInt(99) == 0) {
                this.mc.field_1724.method_6104(this.mc.field_1724.method_6058());
            }
            if (this.sneak.get().booleanValue()) {
                int i = this.sneakTimer;
                this.sneakTimer = i + 1;
                if (i >= this.sneakTime.get().intValue()) {
                    this.mc.field_1690.field_1832.method_23481(false);
                    if (this.random.nextInt(99) == 0) {
                        this.sneakTimer = 0;
                    }
                } else {
                    this.mc.field_1690.field_1832.method_23481(true);
                }
            }
            if (this.strafe.get().booleanValue()) {
                int i2 = this.strafeTimer;
                this.strafeTimer = i2 - 1;
                if (i2 <= 0) {
                    this.mc.field_1690.field_1913.method_23481(!this.direction);
                    this.mc.field_1690.field_1849.method_23481(this.direction);
                    this.direction = !this.direction;
                    this.strafeTimer = 20;
                }
            }
            if (this.spin.get().booleanValue()) {
                this.prevYaw += this.spinSpeed.get().intValue();
                switch (this.spinMode.get()) {
                    case Server:
                        Rotations.rotate(this.prevYaw, this.pitch.get().intValue(), -15);
                        break;
                    case Client:
                        this.mc.field_1724.method_36456(this.prevYaw);
                        break;
                }
            }
            if (!this.sendMessages.get().booleanValue() || this.messages.get().isEmpty()) {
                return;
            }
            int i3 = this.messageTimer;
            this.messageTimer = i3 - 1;
            if (i3 <= 0) {
                if (this.randomMessage.get().booleanValue()) {
                    this.messageI = this.random.nextInt(this.messages.get().size());
                } else {
                    int i4 = this.messageI + 1;
                    this.messageI = i4;
                    if (i4 >= this.messages.get().size()) {
                        this.messageI = 0;
                    }
                }
                ChatUtils.sendPlayerMsg(this.messages.get().get(this.messageI));
                this.messageTimer = this.delay.get().intValue() * 20;
            }
        }
    }
}
