package meteordevelopment.meteorclient.systems.modules.misc;

import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_419;
import org.apache.commons.lang3.RandomStringUtils;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/misc/Spam.class */
public class Spam extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<List<String>> messages;
    private final Setting<Integer> delay;
    private final Setting<Boolean> disableOnLeave;
    private final Setting<Boolean> disableOnDisconnect;
    private final Setting<Boolean> random;
    private final Setting<Boolean> autoSplitMessages;
    private final Setting<Integer> splitLength;
    private final Setting<Integer> autoSplitDelay;
    private final Setting<Boolean> bypass;
    private final Setting<Boolean> uppercase;
    private final Setting<Integer> length;
    private int messageI;
    private int timer;
    private int splitNum;
    private String text;

    public Spam() {
        super(Categories.Misc, "spam", "Spams specified messages in chat.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.messages = this.sgGeneral.add(new StringListSetting.Builder().name("messages").description("Messages to use for spam.").defaultValue(List.of("Meteor on Crack!")).build());
        this.delay = this.sgGeneral.add(new IntSetting.Builder().name("delay").description("The delay between specified messages in ticks.").defaultValue(20).min(0).sliderMax(200).build());
        this.disableOnLeave = this.sgGeneral.add(new BoolSetting.Builder().name("disable-on-leave").description("Disables spam when you leave a server.").defaultValue(true).build());
        this.disableOnDisconnect = this.sgGeneral.add(new BoolSetting.Builder().name("disable-on-disconnect").description("Disables spam when you are disconnected from a server.").defaultValue(true).build());
        this.random = this.sgGeneral.add(new BoolSetting.Builder().name("randomise").description("Selects a random message from your spam message list.").defaultValue(false).build());
        this.autoSplitMessages = this.sgGeneral.add(new BoolSetting.Builder().name("auto-split-messages").description("Automatically split up large messages after a certain length").defaultValue(false).build());
        SettingGroup settingGroup = this.sgGeneral;
        IntSetting.Builder builderDescription = new IntSetting.Builder().name("split-length").description("The length after which to split messages in chat");
        Setting<Boolean> setting = this.autoSplitMessages;
        Objects.requireNonNull(setting);
        this.splitLength = settingGroup.add(builderDescription.visible(setting::get).defaultValue(256).min(1).sliderMax(256).build());
        SettingGroup settingGroup2 = this.sgGeneral;
        IntSetting.Builder builderDescription2 = new IntSetting.Builder().name("split-delay").description("The delay between split messages in ticks.");
        Setting<Boolean> setting2 = this.autoSplitMessages;
        Objects.requireNonNull(setting2);
        this.autoSplitDelay = settingGroup2.add(builderDescription2.visible(setting2::get).defaultValue(20).min(0).sliderMax(200).build());
        this.bypass = this.sgGeneral.add(new BoolSetting.Builder().name("bypass").description("Add random text at the end of the message to try to bypass anti spams.").defaultValue(false).build());
        SettingGroup settingGroup3 = this.sgGeneral;
        BoolSetting.Builder builderDescription3 = new BoolSetting.Builder().name("include-uppercase-characters").description("Whether the bypass text should include uppercase characters.");
        Setting<Boolean> setting3 = this.bypass;
        Objects.requireNonNull(setting3);
        this.uppercase = settingGroup3.add(builderDescription3.visible(setting3::get).defaultValue(true).build());
        SettingGroup settingGroup4 = this.sgGeneral;
        IntSetting.Builder builderDescription4 = new IntSetting.Builder().name("length").description("Number of characters used to bypass anti spam.");
        Setting<Boolean> setting4 = this.bypass;
        Objects.requireNonNull(setting4);
        this.length = settingGroup4.add(builderDescription4.visible(setting4::get).defaultValue(16).sliderRange(1, 256).build());
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        this.timer = this.delay.get().intValue();
        this.messageI = 0;
        this.splitNum = 0;
    }

    @EventHandler
    private void onScreenOpen(OpenScreenEvent event) {
        if (this.disableOnDisconnect.get().booleanValue() && (event.screen instanceof class_419)) {
            toggle();
        }
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (this.disableOnLeave.get().booleanValue()) {
            toggle();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        int i;
        if (this.messages.get().isEmpty()) {
            return;
        }
        if (this.timer <= 0) {
            if (this.text == null) {
                if (this.random.get().booleanValue()) {
                    i = Utils.random(0, this.messages.get().size());
                } else {
                    if (this.messageI >= this.messages.get().size()) {
                        this.messageI = 0;
                    }
                    int i2 = this.messageI;
                    this.messageI = i2 + 1;
                    i = i2;
                }
                this.text = this.messages.get().get(i);
                if (this.bypass.get().booleanValue()) {
                    String bypass = RandomStringUtils.insecure().nextAlphabetic(this.length.get().intValue());
                    if (!this.uppercase.get().booleanValue()) {
                        bypass = bypass.toLowerCase();
                    }
                    this.text += " " + bypass;
                }
            }
            if (this.autoSplitMessages.get().booleanValue() && this.text.length() > this.splitLength.get().intValue()) {
                double length = this.text.length();
                int splits = (int) Math.ceil(length / ((double) this.splitLength.get().intValue()));
                int start = this.splitNum * this.splitLength.get().intValue();
                int end = Math.min(start + this.splitLength.get().intValue(), this.text.length());
                ChatUtils.sendPlayerMsg(this.text.substring(start, end));
                int i3 = this.splitNum + 1;
                this.splitNum = i3;
                this.splitNum = i3 % splits;
                this.timer = this.autoSplitDelay.get().intValue();
                if (this.splitNum == 0) {
                    this.timer = this.delay.get().intValue();
                    this.text = null;
                    return;
                }
                return;
            }
            if (this.text.length() > 256) {
                this.text = this.text.substring(0, 256);
            }
            ChatUtils.sendPlayerMsg(this.text);
            this.timer = this.delay.get().intValue();
            this.text = null;
            return;
        }
        this.timer--;
    }
}
