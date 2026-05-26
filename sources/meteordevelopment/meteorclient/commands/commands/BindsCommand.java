package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.List;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.class_124;
import net.minecraft.class_2172;
import net.minecraft.class_2561;
import net.minecraft.class_2568;
import net.minecraft.class_5250;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/commands/BindsCommand.class */
public class BindsCommand extends Command {
    public BindsCommand() {
        super("binds", "List of all bound modules.", new String[0]);
    }

    @Override // meteordevelopment.meteorclient.commands.Command
    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.executes(context -> {
            List<Module> modules = Modules.get().getAll().stream().filter(module -> {
                return module.keybind.isSet();
            }).toList();
            ChatUtils.info("--- Bound Modules ((highlight)%d(default)) ---", Integer.valueOf(modules.size()));
            for (Module module2 : modules) {
                class_2568.class_10613 class_10613Var = new class_2568.class_10613(getTooltip(module2));
                class_5250 text = class_2561.method_43470(module2.title).method_27692(class_124.field_1068);
                text.method_10862(text.method_10866().method_10949(class_10613Var));
                class_5250 sep = class_2561.method_43470(" - ");
                sep.method_10862(sep.method_10866().method_10949(class_10613Var));
                text.method_10852(sep.method_27692(class_124.field_1080));
                class_5250 key = class_2561.method_43470(module2.keybind.toString());
                key.method_10862(key.method_10866().method_10949(class_10613Var));
                text.method_10852(key.method_27692(class_124.field_1080));
                ChatUtils.sendMsg(text);
            }
            return 1;
        });
    }

    private class_5250 getTooltip(Module module) {
        class_5250 tooltip = class_2561.method_43470(Utils.nameToTitle(module.title)).method_27695(new class_124[]{class_124.field_1078, class_124.field_1067}).method_27693("\n\n");
        tooltip.method_10852(class_2561.method_43470(module.description).method_27692(class_124.field_1068));
        return tooltip;
    }
}
