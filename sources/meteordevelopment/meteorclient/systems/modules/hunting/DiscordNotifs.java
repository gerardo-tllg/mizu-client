package meteordevelopment.meteorclient.systems.modules.hunting;

import com.mojang.authlib.GameProfile;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1657;
import net.minecraft.class_2561;
import net.minecraft.class_5251;
import net.minecraft.class_5903;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/hunting/DiscordNotifs.class */
public class DiscordNotifs extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<String> webhookURL;
    private final Setting<Integer> delay;
    private final Setting<Boolean> queueMessages;
    private final Setting<Boolean> timestamp;
    private final Setting<Boolean> logAll;
    private final Setting<Boolean> connections;
    private final Setting<Boolean> playerRange;
    private final Setting<Boolean> queue;
    private final Setting<Boolean> whisper;
    private final Setting<Boolean> chat;
    private final Setting<Boolean> commands;
    private final Setting<Boolean> deathMessages;
    private long delayTimer;
    private int lastQueuePos;
    private final Queue<String> messageQueue;
    private final Set<GameProfile> playersInRange;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/hunting/DiscordNotifs$MessageType.class */
    public enum MessageType {
        NORMAL,
        DEATH,
        QUEUE,
        DISCONNECT,
        PLAYER_RANGE
    }

    public DiscordNotifs() {
        super(Categories.Hunting, "discord-notifs", "Sends notifications to a discord webhook.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.webhookURL = this.sgGeneral.add(new StringSetting.Builder().name("webhook-link").description("The discord webhook to use, looks like this: https://discord.com/api/webhooks/webhookUserId/webHookTokenOrSomething").defaultValue("").build());
        this.delay = this.sgGeneral.add(new IntSetting.Builder().name("message-delay").description("The delay between messages in milliseconds.").defaultValue(2000).build());
        this.queueMessages = this.sgGeneral.add(new BoolSetting.Builder().name("queue-messages").description("Will queue messages if they are sent too quickly. This could result in a long delay between messages being logged if the queue gets too big.").defaultValue(false).build());
        this.timestamp = this.sgGeneral.add(new BoolSetting.Builder().name("timestamp").description("If the message should have a timestamp.").defaultValue(false).build());
        this.logAll = this.sgGeneral.add(new BoolSetting.Builder().name("all-messages").description("Logs all messages.").defaultValue(false).build());
        this.connections = this.sgGeneral.add(new BoolSetting.Builder().name("disconnect").description("If a message should be logged when leaving.").defaultValue(false).visible(() -> {
            return !this.logAll.get().booleanValue();
        }).build());
        this.playerRange = this.sgGeneral.add(new BoolSetting.Builder().name("player-range").description("If a message should be logged when players enter/exit your render distance.").defaultValue(false).visible(() -> {
            return !this.logAll.get().booleanValue();
        }).build());
        this.queue = this.sgGeneral.add(new BoolSetting.Builder().name("2b2t-queue").description("If your position in queue should be logged.").defaultValue(false).visible(() -> {
            return !this.logAll.get().booleanValue();
        }).build());
        this.whisper = this.sgGeneral.add(new BoolSetting.Builder().name("whisper").description("If whispers should be logged.").defaultValue(false).visible(() -> {
            return !this.logAll.get().booleanValue();
        }).build());
        this.chat = this.sgGeneral.add(new BoolSetting.Builder().name("chat-messages").description("Logs chat messages").defaultValue(false).visible(() -> {
            return !this.logAll.get().booleanValue();
        }).build());
        this.commands = this.sgGeneral.add(new BoolSetting.Builder().name("commands-client-info").description("Logs commands and most messages from clients.").defaultValue(false).visible(() -> {
            return !this.logAll.get().booleanValue();
        }).build());
        this.deathMessages = this.sgGeneral.add(new BoolSetting.Builder().name("death-messages").description("Logs death messages.").defaultValue(false).visible(() -> {
            return !this.logAll.get().booleanValue();
        }).build());
        this.delayTimer = 0L;
        this.messageQueue = new LinkedList();
        this.playersInRange = ConcurrentHashMap.newKeySet();
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        this.messageQueue.clear();
        this.playersInRange.clear();
        this.delayTimer = 0L;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        if (this.delayTimer > 0) {
            this.delayTimer--;
        } else if (this.queueMessages.get().booleanValue() && !this.messageQueue.isEmpty()) {
            sendWebhookMessage(this.messageQueue.poll());
        }
        Set<UUID> uuidsCurrentlyInRange = new HashSet<>();
        if (this.playerRange.get().booleanValue()) {
            for (class_1657 class_1657Var : this.mc.field_1687.method_18112()) {
                if (!class_1657Var.method_5667().equals(this.mc.field_1724.method_5667()) && (class_1657Var instanceof class_1657)) {
                    class_1657 playerEntity = class_1657Var;
                    uuidsCurrentlyInRange.add(playerEntity.method_5667());
                    if (!this.playersInRange.contains(playerEntity.method_7334())) {
                        this.playersInRange.add(playerEntity.method_7334());
                        handleMessage(playerEntity.method_7334().getName() + " has entered visual range!", MessageType.PLAYER_RANGE);
                    }
                }
            }
        }
        for (GameProfile profile : this.playersInRange) {
            if (!uuidsCurrentlyInRange.contains(profile.getId())) {
                this.playersInRange.remove(profile);
                handleMessage(profile.getName() + " has left visual range!", MessageType.PLAYER_RANGE);
            }
        }
    }

    @EventHandler(priority = 999)
    private void onReceivePacket(PacketEvent.Receive event) {
        int queuePos;
        if (event.packet instanceof class_5903) {
            class_5903 packet = event.packet;
            String message = packet.comp_2280().getString();
            int queueIndex = message.indexOf("Position in queue: ");
            if (queueIndex != -1 && (queuePos = Integer.parseInt(message.substring(queueIndex + 19))) != this.lastQueuePos) {
                handleMessage(message, MessageType.QUEUE);
                this.lastQueuePos = queuePos;
            }
        }
    }

    @EventHandler(priority = 999)
    private void onMessageReceive(ReceiveMessageEvent event) {
        class_2561 message = event.getMessage();
        for (class_2561 sibling : message.method_10855()) {
            class_5251 color = sibling.method_10866().method_10973();
            if (color != null && color.method_27716() == 43690) {
                handleMessage(message.getString(), MessageType.DEATH);
                return;
            }
        }
        handleMessage(message.getString(), MessageType.NORMAL);
    }

    public void handleMessage(String message, MessageType messageType) {
        if (this.webhookURL.get().isBlank()) {
            return;
        }
        if (this.logAll.get().booleanValue()) {
            sendWebhookMessage(message);
            return;
        }
        if (this.connections.get().booleanValue() && messageType.equals(MessageType.DISCONNECT)) {
            sendWebhookMessage(message);
            return;
        }
        if (this.playerRange.get().booleanValue() && messageType.equals(MessageType.PLAYER_RANGE)) {
            sendWebhookMessage(message);
            return;
        }
        if (this.queue.get().booleanValue() && messageType.equals(MessageType.QUEUE)) {
            sendWebhookMessage(message);
            return;
        }
        if ((this.whisper.get().booleanValue() && !message.startsWith("<") && message.contains("whispers: ")) || message.startsWith("to ")) {
            sendWebhookMessage(message);
            return;
        }
        if (this.chat.get().booleanValue() && message.startsWith("<")) {
            sendWebhookMessage(message);
            return;
        }
        if (this.deathMessages.get().booleanValue() && messageType.equals(MessageType.DEATH)) {
            sendWebhookMessage(message);
        } else if (this.commands.get().booleanValue() && !message.startsWith("<") && !message.startsWith("to ") && messageType.equals(MessageType.NORMAL)) {
            sendWebhookMessage(message);
        }
    }

    @EventHandler
    private void onDisconnect(GameLeftEvent event) {
        handleMessage("Disconnected", MessageType.DISCONNECT);
    }

    private void sendWebhookMessage(String message) {
        if (this.delayTimer > 0) {
            if (this.queueMessages.get().booleanValue()) {
                this.messageQueue.offer(message);
                return;
            }
            return;
        }
        this.delayTimer = (this.delay.get().intValue() / 1000) * 20;
        if (this.timestamp.get().booleanValue()) {
            LocalTime now = LocalTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            String timestamp = now.format(formatter);
            message = "[" + timestamp + "] " + message;
        }
        String json = "{\n\"embeds\": [{\"description\": \"" + message + "\"}]}";
        new Thread(() -> {
            HuntingUtils.sendWebhook(this.webhookURL.get(), json, null);
        }).start();
    }
}
