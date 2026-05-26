package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.ProfileArgumentType;
import meteordevelopment.meteorclient.systems.profiles.Profile;
import meteordevelopment.meteorclient.systems.profiles.Profiles;
import net.minecraft.class_2172;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/commands/ProfilesCommand.class */
public class ProfilesCommand extends Command {
    public ProfilesCommand() {
        super("profiles", "Loads and saves profiles.", new String[0]);
    }

    @Override // meteordevelopment.meteorclient.commands.Command
    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.then(literal("load").then(argument("profile", ProfileArgumentType.create()).executes(context -> {
            Profile profile = ProfileArgumentType.get(context);
            if (profile != null) {
                profile.load();
                info("Loaded profile (highlight)%s(default).", profile.name.get());
                return 1;
            }
            return 1;
        })));
        builder.then(literal("save").then(argument("profile", ProfileArgumentType.create()).executes(context2 -> {
            Profile profile = ProfileArgumentType.get(context2);
            if (profile != null) {
                profile.save();
                info("Saved profile (highlight)%s(default).", profile.name.get());
                return 1;
            }
            return 1;
        })));
        builder.then(literal("delete").then(argument("profile", ProfileArgumentType.create()).executes(context3 -> {
            Profile profile = ProfileArgumentType.get(context3);
            if (profile != null) {
                Profiles.get().remove(profile);
                info("Deleted profile (highlight)%s(default).", profile.name.get());
                return 1;
            }
            return 1;
        })));
    }
}
