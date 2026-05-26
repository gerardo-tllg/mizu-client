package meteordevelopment.meteorclient.commands;

import com.google.gson.Gson;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.stardust.ApiHandler;
import meteordevelopment.meteorclient.utils.stardust.LogUtil;
import net.minecraft.class_2172;
import net.minecraft.class_2561;
import net.minecraft.class_746;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/Stats2b2t.class */
public class Stats2b2t extends Command {
    private final String API_ENDPOINT = "/stats/player?playerName=";

    public Stats2b2t() {
        super("stats2b2t", "Fetch stats for a 2b2t player from api.2b2t.vc.", "stats");
        this.API_ENDPOINT = "/stats/player?playerName=";
    }

    @Override // meteordevelopment.meteorclient.commands.Command
    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.then(argument("player", StringArgumentType.word()).executes(ctx -> {
            MeteorExecutor.execute(() -> {
                String formattedPlaytime;
                String formattedPlaytimeInMonth;
                class_746 player = mc.field_1724;
                if (player == null) {
                    return;
                }
                String playerString = (String) ctx.getArgument("player", String.class);
                String requestString = "https://api.2b2t.vc/stats/player?playerName=" + playerString.trim();
                String response = new ApiHandler().fetchResponse(requestString);
                if (response == null) {
                    return;
                }
                if (response.equals("204 Undocumented")) {
                    player.method_7353(class_2561.method_30163("§8[§7MasterClient§8] §4Player not found§7..."), false);
                    return;
                }
                try {
                    Gson gson = new Gson();
                    PlayerStats stats = (PlayerStats) gson.fromJson(response, PlayerStats.class);
                    Instant firstInstant = Instant.parse(stats.firstSeen);
                    Instant lastInstant = Instant.parse(stats.lastSeen);
                    ZonedDateTime firstZonedTime = firstInstant.atZone(ZoneId.systemDefault());
                    ZonedDateTime lastZonedTime = lastInstant.atZone(ZoneId.systemDefault());
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US);
                    String formattedFirstSeen = firstZonedTime.format(fmt);
                    String formattedLastSeen = lastZonedTime.format(fmt);
                    long playtimeSeconds = stats.playtimeSeconds;
                    long playtimeSecondsInMonth = stats.playtimeSecondsMonth;
                    if (playtimeSeconds <= 0) {
                        formattedPlaytime = "none";
                    } else {
                        long days = TimeUnit.SECONDS.toDays(playtimeSeconds);
                        long hours = TimeUnit.SECONDS.toHours(playtimeSeconds);
                        long minutes = TimeUnit.SECONDS.toMinutes(playtimeSeconds);
                        if (days >= 60) {
                            formattedPlaytime = (days / 30) + " months";
                        } else if (days >= 30) {
                            formattedPlaytime = "1 month";
                        } else if (days >= 14) {
                            formattedPlaytime = (days / 7) + " weeks";
                        } else if (days >= 7) {
                            formattedPlaytime = "1 week";
                        } else if (days >= 2) {
                            formattedPlaytime = days + " days";
                        } else if (days > 0) {
                            formattedPlaytime = "1 day";
                        } else if (hours >= 2) {
                            formattedPlaytime = hours + " hours";
                        } else if (hours > 0) {
                            formattedPlaytime = "1 hour";
                        } else if (minutes >= 2) {
                            formattedPlaytime = minutes + " minutes";
                        } else if (minutes > 0) {
                            formattedPlaytime = "1 minute";
                        } else if (playtimeSeconds > 1) {
                            formattedPlaytime = playtimeSeconds + " seconds";
                        } else {
                            formattedPlaytime = "1 second";
                        }
                    }
                    if (playtimeSecondsInMonth <= 0) {
                        formattedPlaytimeInMonth = "none";
                    } else {
                        long daysInMonth = TimeUnit.SECONDS.toDays(playtimeSecondsInMonth);
                        long hoursInMonth = TimeUnit.SECONDS.toHours(playtimeSecondsInMonth);
                        long minutesInMonth = TimeUnit.SECONDS.toMinutes(playtimeSecondsInMonth);
                        if (daysInMonth >= 28) {
                            formattedPlaytimeInMonth = "1 month";
                        } else if (daysInMonth >= 14) {
                            formattedPlaytimeInMonth = (daysInMonth / 7) + " weeks";
                        } else if (daysInMonth >= 7) {
                            formattedPlaytimeInMonth = "1 week";
                        } else if (daysInMonth >= 2) {
                            formattedPlaytimeInMonth = daysInMonth + " days";
                        } else if (daysInMonth > 0) {
                            formattedPlaytimeInMonth = "1 day";
                        } else if (hoursInMonth >= 2) {
                            formattedPlaytimeInMonth = hoursInMonth + " hours";
                        } else if (hoursInMonth > 0) {
                            formattedPlaytimeInMonth = "1 hour";
                        } else if (minutesInMonth >= 2) {
                            formattedPlaytimeInMonth = minutesInMonth + " minutes";
                        } else if (minutesInMonth > 0) {
                            formattedPlaytimeInMonth = "1 minute";
                        } else if (playtimeSecondsInMonth > 1) {
                            formattedPlaytimeInMonth = playtimeSecondsInMonth + " seconds";
                        } else {
                            formattedPlaytimeInMonth = "1 second";
                        }
                    }
                    String kdRatioString = String.valueOf(stats.killCount / stats.deathCount);
                    player.method_7353(class_2561.method_30163("§8[§7MasterClient§8] §7Stats for " + "MasterClient" + playerString + "§7:\n    §7Joins: " + "MasterClient" + stats.joinCount + "\n    §7Leaves: " + "MasterClient" + stats.leaveCount + "\n    §7K/D Ratio: " + "MasterClient" + kdRatioString + "\n    §7Chats: " + "MasterClient" + stats.chatsCount + "\n    §7Prio: " + "MasterClient" + stats.prio + "\n    §7First Seen: " + "MasterClient" + formattedFirstSeen + "\n    §7Last Seen: " + "MasterClient" + formattedLastSeen + "\n    §7Playtime: " + "MasterClient" + formattedPlaytime + "\n    §7Playtime in last month: " + "MasterClient" + formattedPlaytimeInMonth), false);
                } catch (Exception err) {
                    error("§7Failed to deserialize response from the server§4..!", new Object[0]);
                    LogUtil.error("Failed to deserialize Json: " + String.valueOf(err), getName());
                }
            });
            return 1;
        }));
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/Stats2b2t$PlayerStats.class */
    private static final class PlayerStats extends Record {
        private final int joinCount;
        private final int leaveCount;
        private final int deathCount;
        private final int killCount;
        private final String firstSeen;
        private final String lastSeen;
        private final long playtimeSeconds;
        private final long playtimeSecondsMonth;
        private final int chatsCount;
        private final boolean prio;

        private PlayerStats(int joinCount, int leaveCount, int deathCount, int killCount, String firstSeen, String lastSeen, long playtimeSeconds, long playtimeSecondsMonth, int chatsCount, boolean prio) {
            this.joinCount = joinCount;
            this.leaveCount = leaveCount;
            this.deathCount = deathCount;
            this.killCount = killCount;
            this.firstSeen = firstSeen;
            this.lastSeen = lastSeen;
            this.playtimeSeconds = playtimeSeconds;
            this.playtimeSecondsMonth = playtimeSecondsMonth;
            this.chatsCount = chatsCount;
            this.prio = prio;
        }

        @Override // java.lang.Record
        public final String toString() {
            return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, PlayerStats.class), PlayerStats.class, "joinCount;leaveCount;deathCount;killCount;firstSeen;lastSeen;playtimeSeconds;playtimeSecondsMonth;chatsCount;prio", "FIELD:Lmeteordevelopment/meteorclient/commands/Stats2b2t$PlayerStats;->joinCount:I", "FIELD:Lmeteordevelopment/meteorclient/commands/Stats2b2t$PlayerStats;->leaveCount:I", "FIELD:Lmeteordevelopment/meteorclient/commands/Stats2b2t$PlayerStats;->deathCount:I", "FIELD:Lmeteordevelopment/meteorclient/commands/Stats2b2t$PlayerStats;->killCount:I", "FIELD:Lmeteordevelopment/meteorclient/commands/Stats2b2t$PlayerStats;->firstSeen:Ljava/lang/String;", "FIELD:Lmeteordevelopment/meteorclient/commands/Stats2b2t$PlayerStats;->lastSeen:Ljava/lang/String;", "FIELD:Lmeteordevelopment/meteorclient/commands/Stats2b2t$PlayerStats;->playtimeSeconds:J", "FIELD:Lmeteordevelopment/meteorclient/commands/Stats2b2t$PlayerStats;->playtimeSecondsMonth:J", "FIELD:Lmeteordevelopment/meteorclient/commands/Stats2b2t$PlayerStats;->chatsCount:I", "FIELD:Lmeteordevelopment/meteorclient/commands/Stats2b2t$PlayerStats;->prio:Z").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, PlayerStats.class), PlayerStats.class, "joinCount;leaveCount;deathCount;killCount;firstSeen;lastSeen;playtimeSeconds;playtimeSecondsMonth;chatsCount;prio", "FIELD:Lmeteordevelopment/meteorclient/commands/Stats2b2t$PlayerStats;->joinCount:I", "FIELD:Lmeteordevelopment/meteorclient/commands/Stats2b2t$PlayerStats;->leaveCount:I", "FIELD:Lmeteordevelopment/meteorclient/commands/Stats2b2t$PlayerStats;->deathCount:I", "FIELD:Lmeteordevelopment/meteorclient/commands/Stats2b2t$PlayerStats;->killCount:I", "FIELD:Lmeteordevelopment/meteorclient/commands/Stats2b2t$PlayerStats;->firstSeen:Ljava/lang/String;", "FIELD:Lmeteordevelopment/meteorclient/commands/Stats2b2t$PlayerStats;->lastSeen:Ljava/lang/String;", "FIELD:Lmeteordevelopment/meteorclient/commands/Stats2b2t$PlayerStats;->playtimeSeconds:J", "FIELD:Lmeteordevelopment/meteorclient/commands/Stats2b2t$PlayerStats;->playtimeSecondsMonth:J", "FIELD:Lmeteordevelopment/meteorclient/commands/Stats2b2t$PlayerStats;->chatsCount:I", "FIELD:Lmeteordevelopment/meteorclient/commands/Stats2b2t$PlayerStats;->prio:Z").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final boolean equals(Object o) {
            return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, PlayerStats.class, Object.class), PlayerStats.class, "joinCount;leaveCount;deathCount;killCount;firstSeen;lastSeen;playtimeSeconds;playtimeSecondsMonth;chatsCount;prio", "FIELD:Lmeteordevelopment/meteorclient/commands/Stats2b2t$PlayerStats;->joinCount:I", "FIELD:Lmeteordevelopment/meteorclient/commands/Stats2b2t$PlayerStats;->leaveCount:I", "FIELD:Lmeteordevelopment/meteorclient/commands/Stats2b2t$PlayerStats;->deathCount:I", "FIELD:Lmeteordevelopment/meteorclient/commands/Stats2b2t$PlayerStats;->killCount:I", "FIELD:Lmeteordevelopment/meteorclient/commands/Stats2b2t$PlayerStats;->firstSeen:Ljava/lang/String;", "FIELD:Lmeteordevelopment/meteorclient/commands/Stats2b2t$PlayerStats;->lastSeen:Ljava/lang/String;", "FIELD:Lmeteordevelopment/meteorclient/commands/Stats2b2t$PlayerStats;->playtimeSeconds:J", "FIELD:Lmeteordevelopment/meteorclient/commands/Stats2b2t$PlayerStats;->playtimeSecondsMonth:J", "FIELD:Lmeteordevelopment/meteorclient/commands/Stats2b2t$PlayerStats;->chatsCount:I", "FIELD:Lmeteordevelopment/meteorclient/commands/Stats2b2t$PlayerStats;->prio:Z").dynamicInvoker().invoke(this, o) /* invoke-custom */;
        }

        public int joinCount() {
            return this.joinCount;
        }

        public int leaveCount() {
            return this.leaveCount;
        }

        public int deathCount() {
            return this.deathCount;
        }

        public int killCount() {
            return this.killCount;
        }

        public String firstSeen() {
            return this.firstSeen;
        }

        public String lastSeen() {
            return this.lastSeen;
        }

        public long playtimeSeconds() {
            return this.playtimeSeconds;
        }

        public long playtimeSecondsMonth() {
            return this.playtimeSecondsMonth;
        }

        public int chatsCount() {
            return this.chatsCount;
        }

        public boolean prio() {
            return this.prio;
        }
    }
}
