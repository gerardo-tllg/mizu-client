package meteordevelopment.meteorclient.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.class_2350;
import net.minecraft.class_7485;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/arguments/DirectionArgumentType.class */
public class DirectionArgumentType extends class_7485<class_2350> {
    private static final DirectionArgumentType INSTANCE = new DirectionArgumentType();

    private DirectionArgumentType() {
        super(class_2350.field_29502, class_2350::values);
    }

    public static DirectionArgumentType create() {
        return INSTANCE;
    }
}
