package meteordevelopment.meteorclient.systems.modules.misc;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.events.entity.EntityRemovedEvent;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerEntity;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_124;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1657;
import net.minecraft.class_1684;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2703;
import net.minecraft.class_3417;
import net.minecraft.class_3419;
import net.minecraft.class_5250;
import net.minecraft.class_640;
import net.minecraft.class_7828;
import net.minecraft.class_8623;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/misc/Notifier.class */
public class Notifier extends Module {
    private final SettingGroup sgTotemPops;
    private final SettingGroup sgVisualRange;
    private final SettingGroup sgPearl;
    private final SettingGroup sgJoinsLeaves;
    private final Setting<Boolean> totemPops;
    private final Setting<Boolean> totemsDistanceCheck;
    private final Setting<Integer> totemsDistance;
    private final Setting<Boolean> totemsIgnoreOwn;
    private final Setting<Boolean> totemsIgnoreFriends;
    private final Setting<Boolean> totemsIgnoreOthers;
    private final Setting<Boolean> visualRange;
    private final Setting<Event> event;
    private final Setting<Set<class_1299<?>>> entities;
    private final Setting<Boolean> visualRangeIgnoreFriends;
    private final Setting<Boolean> visualRangeIgnoreFakes;
    private final Setting<Boolean> visualMakeSound;
    private final Setting<Boolean> pearl;
    private final Setting<Boolean> pearlIgnoreOwn;
    private final Setting<Boolean> pearlIgnoreFriends;
    private final Setting<JoinLeaveModes> joinsLeavesMode;
    private final Setting<Integer> notificationDelay;
    private final Setting<Boolean> simpleNotifications;
    private final Setting<Boolean> replaceNotifications;
    private final Setting<SettingColor> joinBracketColor;
    private final Setting<SettingColor> joinSymbolColor;
    private final Setting<SettingColor> joinNameColor;
    private final Setting<SettingColor> leaveBracketColor;
    private final Setting<SettingColor> leaveSymbolColor;
    private final Setting<SettingColor> leaveNameColor;
    private int timer;
    private boolean loginPacket;
    private final Object2IntMap<UUID> totemPopMap;
    private final Object2IntMap<UUID> chatIdMap;
    private final Map<Integer, class_243> pearlStartPosMap;
    private final class_8623<class_2561> messageQueue;
    private static final int JOIN_LEAVE_CHAT_ID = "notifier-joinleave".hashCode();
    private final Random random;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/misc/Notifier$Event.class */
    public enum Event {
        Spawn,
        Despawn,
        Both
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/misc/Notifier$JoinLeaveModes.class */
    public enum JoinLeaveModes {
        None,
        Joins,
        Leaves,
        Both
    }

    public Notifier() {
        super(Categories.Misc, "notifier", "Notifies you of different events.");
        this.sgTotemPops = this.settings.createGroup("Totem Pops");
        this.sgVisualRange = this.settings.createGroup("Visual Range");
        this.sgPearl = this.settings.createGroup("Pearl");
        this.sgJoinsLeaves = this.settings.createGroup("Joins/Leaves");
        this.totemPops = this.sgTotemPops.add(new BoolSetting.Builder().name("totem-pops").description("Notifies you when a player pops a totem.").defaultValue(true).build());
        SettingGroup settingGroup = this.sgTotemPops;
        BoolSetting.Builder builderDefaultValue = new BoolSetting.Builder().name("distance-check").description("Limits the distance in which the pops are recognized.").defaultValue(false);
        Setting<Boolean> setting = this.totemPops;
        Objects.requireNonNull(setting);
        this.totemsDistanceCheck = settingGroup.add(builderDefaultValue.visible(setting::get).build());
        this.totemsDistance = this.sgTotemPops.add(new IntSetting.Builder().name("player-radius").description("The radius in which to log totem pops.").defaultValue(30).sliderRange(1, 50).range(1, 100).visible(() -> {
            return this.totemPops.get().booleanValue() && this.totemsDistanceCheck.get().booleanValue();
        }).build());
        this.totemsIgnoreOwn = this.sgTotemPops.add(new BoolSetting.Builder().name("ignore-own").description("Ignores your own totem pops.").defaultValue(false).build());
        this.totemsIgnoreFriends = this.sgTotemPops.add(new BoolSetting.Builder().name("ignore-friends").description("Ignores friends totem pops.").defaultValue(false).build());
        this.totemsIgnoreOthers = this.sgTotemPops.add(new BoolSetting.Builder().name("ignore-others").description("Ignores other players totem pops.").defaultValue(false).build());
        this.visualRange = this.sgVisualRange.add(new BoolSetting.Builder().name("visual-range").description("Notifies you when an entity enters your render distance.").defaultValue(false).build());
        this.event = this.sgVisualRange.add(new EnumSetting.Builder().name("event").description("When to log the entities.").defaultValue(Event.Both).build());
        this.entities = this.sgVisualRange.add(new EntityTypeListSetting.Builder().name("entities").description("Which entities to notify about.").defaultValue(class_1299.field_6097).build());
        this.visualRangeIgnoreFriends = this.sgVisualRange.add(new BoolSetting.Builder().name("ignore-friends").description("Ignores friends.").defaultValue(true).build());
        this.visualRangeIgnoreFakes = this.sgVisualRange.add(new BoolSetting.Builder().name("ignore-fake-players").description("Ignores fake players.").defaultValue(true).build());
        this.visualMakeSound = this.sgVisualRange.add(new BoolSetting.Builder().name("sound").description("Emits a sound effect on enter / leave").defaultValue(true).build());
        this.pearl = this.sgPearl.add(new BoolSetting.Builder().name("pearl").description("Notifies you when a player is teleported using an ender pearl.").defaultValue(true).build());
        this.pearlIgnoreOwn = this.sgPearl.add(new BoolSetting.Builder().name("ignore-own").description("Ignores your own pearls.").defaultValue(false).build());
        this.pearlIgnoreFriends = this.sgPearl.add(new BoolSetting.Builder().name("ignore-friends").description("Ignores friends pearls.").defaultValue(false).build());
        this.joinsLeavesMode = this.sgJoinsLeaves.add(new EnumSetting.Builder().name("player-joins-leaves").description("How to handle player join/leave notifications.").defaultValue(JoinLeaveModes.None).build());
        this.notificationDelay = this.sgJoinsLeaves.add(new IntSetting.Builder().name("notification-delay").description("How long to wait in ticks before posting the next join/leave notification in your chat.").range(0, 1000).sliderRange(0, 100).defaultValue(0).build());
        this.simpleNotifications = this.sgJoinsLeaves.add(new BoolSetting.Builder().name("simple-notifications").description("Display join/leave notifications without a prefix, to reduce chat clutter.").defaultValue(true).build());
        this.replaceNotifications = this.sgJoinsLeaves.add(new BoolSetting.Builder().name("replace-notifications").description("Join/leave notifications replace each other instead of stacking in chat.").defaultValue(false).build());
        this.joinBracketColor = this.sgJoinsLeaves.add(new ColorSetting.Builder().name("join-bracket-color").description("Color of the brackets on join notifications.").defaultValue(new SettingColor(class_124.field_1080)).build());
        this.joinSymbolColor = this.sgJoinsLeaves.add(new ColorSetting.Builder().name("join-symbol-color").description("Color of the + symbol on join notifications.").defaultValue(new SettingColor(class_124.field_1060)).build());
        this.joinNameColor = this.sgJoinsLeaves.add(new ColorSetting.Builder().name("join-name-color").description("Color of the player name on join notifications.").defaultValue(new SettingColor(255, 255, 255, 255)).build());
        this.leaveBracketColor = this.sgJoinsLeaves.add(new ColorSetting.Builder().name("leave-bracket-color").description("Color of the brackets on leave notifications.").defaultValue(new SettingColor(class_124.field_1080)).build());
        this.leaveSymbolColor = this.sgJoinsLeaves.add(new ColorSetting.Builder().name("leave-symbol-color").description("Color of the - symbol on leave notifications.").defaultValue(new SettingColor(class_124.field_1061)).build());
        this.leaveNameColor = this.sgJoinsLeaves.add(new ColorSetting.Builder().name("leave-name-color").description("Color of the player name on leave notifications.").defaultValue(new SettingColor(255, 255, 255, 255)).build());
        this.loginPacket = true;
        this.totemPopMap = new Object2IntOpenHashMap();
        this.chatIdMap = new Object2IntOpenHashMap();
        this.pearlStartPosMap = new HashMap();
        this.messageQueue = new class_8623<>();
        this.random = new Random();
    }

    @EventHandler
    private void onEntityAdded(EntityAddedEvent event) {
        if (!event.entity.method_5667().equals(this.mc.field_1724.method_5667()) && this.entities.get().contains(event.entity.method_5864()) && this.visualRange.get().booleanValue() && this.event.get() != Event.Despawn) {
            if (event.entity instanceof class_1657) {
                if ((!this.visualRangeIgnoreFriends.get().booleanValue() || !Friends.get().isFriend((class_1657) event.entity)) && (!this.visualRangeIgnoreFakes.get().booleanValue() || !(event.entity instanceof FakePlayerEntity))) {
                    ChatUtils.sendMsg(event.entity.method_5628() + 100, class_124.field_1080, "(highlight)%s(default) has entered your visual range!", event.entity.method_5477().getString());
                    if (this.visualMakeSound.get().booleanValue()) {
                        this.mc.field_1687.method_43129(this.mc.field_1724, this.mc.field_1724, class_3417.field_14627, class_3419.field_15256, 3.0f, 1.0f);
                    }
                }
            } else {
                class_5250 text = class_2561.method_43470(event.entity.method_5864().method_5897().getString()).method_27692(class_124.field_1068);
                text.method_10852(class_2561.method_43470(" has spawned at ").method_27692(class_124.field_1080));
                text.method_10852(ChatUtils.formatCoords(event.entity.method_19538()));
                text.method_10852(class_2561.method_43470(".").method_27692(class_124.field_1080));
                info(text);
            }
        }
        if (this.pearl.get().booleanValue()) {
            class_1684 class_1684Var = event.entity;
            if (class_1684Var instanceof class_1684) {
                class_1684 pearlEntity = class_1684Var;
                this.pearlStartPosMap.put(Integer.valueOf(pearlEntity.method_5628()), new class_243(pearlEntity.method_23317(), pearlEntity.method_23318(), pearlEntity.method_23321()));
            }
        }
    }

    @EventHandler
    private void onEntityRemoved(EntityRemovedEvent event) {
        if (!event.entity.method_5667().equals(this.mc.field_1724.method_5667()) && this.entities.get().contains(event.entity.method_5864()) && this.visualRange.get().booleanValue() && this.event.get() != Event.Spawn) {
            if (event.entity instanceof class_1657) {
                if ((!this.visualRangeIgnoreFriends.get().booleanValue() || !Friends.get().isFriend((class_1657) event.entity)) && (!this.visualRangeIgnoreFakes.get().booleanValue() || !(event.entity instanceof FakePlayerEntity))) {
                    ChatUtils.sendMsg(event.entity.method_5628() + 100, class_124.field_1080, "(highlight)%s(default) has left your visual range!", event.entity.method_5477().getString());
                    if (this.visualMakeSound.get().booleanValue()) {
                        this.mc.field_1687.method_43129(this.mc.field_1724, this.mc.field_1724, class_3417.field_14627, class_3419.field_15256, 3.0f, 1.0f);
                    }
                }
            } else {
                class_5250 text = class_2561.method_43470(event.entity.method_5864().method_5897().getString()).method_27692(class_124.field_1068);
                text.method_10852(class_2561.method_43470(" has despawned at ").method_27692(class_124.field_1080));
                text.method_10852(ChatUtils.formatCoords(event.entity.method_19538()));
                text.method_10852(class_2561.method_43470(".").method_27692(class_124.field_1080));
                info(text);
            }
        }
        if (this.pearl.get().booleanValue()) {
            class_1684 class_1684Var = event.entity;
            int i = class_1684Var.method_5628();
            if (this.pearlStartPosMap.containsKey(Integer.valueOf(i))) {
                class_1684 pearl = class_1684Var;
                if (pearl.method_24921() != null) {
                    class_1657 class_1657VarMethod_24921 = pearl.method_24921();
                    if (class_1657VarMethod_24921 instanceof class_1657) {
                        class_1657 p = class_1657VarMethod_24921;
                        double d = this.pearlStartPosMap.get(Integer.valueOf(i)).method_1022(class_1684Var.method_19538());
                        if ((!Friends.get().isFriend(p) || !this.pearlIgnoreFriends.get().booleanValue()) && (!p.equals(this.mc.field_1724) || !this.pearlIgnoreOwn.get().booleanValue())) {
                            info("(highlight)%s's(default) pearl landed at %d, %d, %d (highlight)(%.1fm away, travelled %.1fm)(default).", pearl.method_24921().method_5477().getString(), Integer.valueOf(pearl.method_24515().method_10263()), Integer.valueOf(pearl.method_24515().method_10264()), Integer.valueOf(pearl.method_24515().method_10260()), Float.valueOf(pearl.method_5739(this.mc.field_1724)), Double.valueOf(d));
                        }
                    }
                }
                this.pearlStartPosMap.remove(Integer.valueOf(i));
            }
        }
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        this.totemPopMap.clear();
        this.chatIdMap.clear();
        this.pearlStartPosMap.clear();
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        this.timer = 0;
        this.messageQueue.clear();
    }

    @EventHandler
    private void onGameJoin(GameJoinedEvent event) {
        this.timer = 0;
        this.totemPopMap.clear();
        this.chatIdMap.clear();
        this.messageQueue.clear();
        this.pearlStartPosMap.clear();
    }

    @EventHandler
    private void onGameLeave(GameLeftEvent event) {
        this.loginPacket = true;
    }

    /* JADX WARN: Code restructure failed: missing block: B:11:0x0061, code lost:
    
        if (r9.loginPacket == false) goto L14;
     */
    /* JADX WARN: Code restructure failed: missing block: B:12:0x0064, code lost:
    
        r9.loginPacket = false;
     */
    /* JADX WARN: Code restructure failed: missing block: B:13:0x0069, code lost:
    
        return;
     */
    /* JADX WARN: Code restructure failed: missing block: B:15:0x0075, code lost:
    
        if (r0.method_46327().contains(net.minecraft.class_2703.class_5893.field_29136) == false) goto L67;
     */
    /* JADX WARN: Code restructure failed: missing block: B:16:0x0078, code lost:
    
        createJoinNotifications(r0);
     */
    /* JADX WARN: Code restructure failed: missing block: B:22:0x00b2, code lost:
    
        createLeaveNotification(r0);
     */
    /* JADX WARN: Code restructure failed: missing block: B:67:0x0200, code lost:
    
        return;
     */
    /* JADX WARN: Code restructure failed: missing block: B:84:?, code lost:
    
        return;
     */
    /* JADX WARN: Code restructure failed: missing block: B:85:?, code lost:
    
        return;
     */
    @meteordevelopment.orbit.EventHandler
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private void onReceivePacket(meteordevelopment.meteorclient.events.packets.PacketEvent.Receive r10) {
        /*
            Method dump skipped, instruction units count: 513
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: meteordevelopment.meteorclient.systems.modules.misc.Notifier.onReceivePacket(meteordevelopment.meteorclient.events.packets.PacketEvent$Receive):void");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.joinsLeavesMode.get() != JoinLeaveModes.None) {
            this.timer++;
            while (this.timer >= this.notificationDelay.get().intValue() && !this.messageQueue.isEmpty()) {
                this.timer = 0;
                class_2561 msg = (class_2561) this.messageQueue.removeFirst();
                if (this.replaceNotifications.get().booleanValue()) {
                    this.mc.field_1705.method_1743().meteor$add(msg, JOIN_LEAVE_CHAT_ID);
                } else if (this.simpleNotifications.get().booleanValue()) {
                    this.mc.field_1724.method_7353(msg, false);
                } else {
                    ChatUtils.sendMsg(msg);
                }
            }
        }
        if (this.totemPops.get().booleanValue()) {
            synchronized (this.totemPopMap) {
                for (class_1657 player : this.mc.field_1687.method_18456()) {
                    if (this.totemPopMap.containsKey(player.method_5667())) {
                        if (player.field_6213 > 0 || player.method_6032() <= 0.0f) {
                            int pops = this.totemPopMap.removeInt(player.method_5667());
                            int chatId = getChatId(player);
                            class_124 class_124Var = class_124.field_1080;
                            Object[] objArr = new Object[3];
                            objArr[0] = player.method_5477().getString();
                            objArr[1] = Integer.valueOf(pops);
                            objArr[2] = pops == 1 ? "totem" : "totems";
                            ChatUtils.sendMsg(chatId, class_124Var, "(highlight)%s (default)died after popping (highlight)%d (default)%s.", objArr);
                            this.chatIdMap.removeInt(player.method_5667());
                        }
                    }
                }
            }
        }
    }

    private int getChatId(class_1297 entity) {
        return this.chatIdMap.computeIfAbsent(entity.method_5667(), value -> {
            return this.random.nextInt();
        });
    }

    private class_5250 buildNotification(String name, boolean isJoin) {
        SettingColor bracketColor = isJoin ? this.joinBracketColor.get() : this.leaveBracketColor.get();
        SettingColor symbolColor = isJoin ? this.joinSymbolColor.get() : this.leaveSymbolColor.get();
        SettingColor nameColor = isJoin ? this.joinNameColor.get() : this.leaveNameColor.get();
        String symbol = isJoin ? "+" : "-";
        class_5250 msg = class_2561.method_43470("[").method_10862(bracketColor.toStyle());
        msg.method_10852(class_2561.method_43470(symbol).method_10862(symbolColor.toStyle()));
        msg.method_10852(class_2561.method_43470("] ").method_10862(bracketColor.toStyle()));
        msg.method_10852(class_2561.method_43470(name).method_10862(nameColor.toStyle()));
        return msg;
    }

    private void createJoinNotifications(class_2703 packet) {
        for (class_2703.class_2705 entry : packet.method_46330()) {
            if (entry.comp_1107() != null) {
                String name = entry.comp_1107().getName();
                if (this.simpleNotifications.get().booleanValue() || this.replaceNotifications.get().booleanValue()) {
                    this.messageQueue.addLast(buildNotification(name, true));
                } else {
                    this.messageQueue.addLast(class_2561.method_43470(name).method_27692(class_124.field_1068).method_10852(class_2561.method_43470(" joined.").method_27692(class_124.field_1080)));
                }
            }
        }
    }

    private void createLeaveNotification(class_7828 packet) {
        if (this.mc.method_1562() == null) {
            return;
        }
        for (UUID id : packet.comp_1105()) {
            class_640 toRemove = this.mc.method_1562().method_2871(id);
            if (toRemove != null) {
                String name = toRemove.method_2966().getName();
                if (this.simpleNotifications.get().booleanValue() || this.replaceNotifications.get().booleanValue()) {
                    this.messageQueue.addLast(buildNotification(name, false));
                } else {
                    this.messageQueue.addLast(class_2561.method_43470(name).method_27692(class_124.field_1068).method_10852(class_2561.method_43470(" left.").method_27692(class_124.field_1080)));
                }
            }
        }
    }
}
