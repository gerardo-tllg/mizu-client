package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.class_10093;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1806;
import net.minecraft.class_2172;
import net.minecraft.class_22;
import net.minecraft.class_2561;
import net.minecraft.class_9209;
import net.minecraft.class_9334;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/commands/SaveMapCommand.class */
public class SaveMapCommand extends Command {
    private static final SimpleCommandExceptionType MAP_NOT_FOUND = new SimpleCommandExceptionType(class_2561.method_43470("You must be holding a filled map."));
    private static final SimpleCommandExceptionType OOPS = new SimpleCommandExceptionType(class_2561.method_43470("Something went wrong."));
    private final PointerBuffer filters;

    public SaveMapCommand() {
        super("save-map", "Saves a map to an image.", "sm");
        this.filters = BufferUtils.createPointerBuffer(1);
        ByteBuffer pngFilter = MemoryUtil.memASCII("*.png");
        this.filters.put(pngFilter);
        this.filters.rewind();
    }

    @Override // meteordevelopment.meteorclient.commands.Command
    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.executes(context -> {
            saveMap(128);
            return 1;
        }).then(argument("scale", IntegerArgumentType.integer(1)).executes(context2 -> {
            saveMap(IntegerArgumentType.getInteger(context2, "scale"));
            return 1;
        }));
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: com.mojang.brigadier.exceptions.CommandSyntaxException */
    private void saveMap(int scale) throws CommandSyntaxException {
        class_1799 map = getMap();
        class_22 state = getMapState();
        if (map == null || state == null) {
            throw MAP_NOT_FOUND.create();
        }
        File path = getPath();
        if (path == null) {
            throw OOPS.create();
        }
        class_10093.class_331 texture = mc.field_1773.method_35772().method_61965().invokeGetMapTexture((class_9209) map.method_58694(class_9334.field_49646), state);
        if (texture.field_2048.method_4525() == null) {
            throw OOPS.create();
        }
        try {
            if (scale == 128) {
                texture.field_2048.method_4525().method_4325(path);
            } else {
                int[] data = texture.field_2048.method_4525().method_4322();
                BufferedImage image = new BufferedImage(128, 128, 2);
                image.setRGB(0, 0, image.getWidth(), image.getHeight(), data, 0, 128);
                BufferedImage scaledImage = new BufferedImage(scale, scale, 2);
                scaledImage.createGraphics().drawImage(image, 0, 0, scale, scale, (ImageObserver) null);
                ImageIO.write(scaledImage, "png", path);
            }
        } catch (IOException e) {
            error("Error writing map texture", new Object[0]);
            MeteorClient.LOG.error(e.toString());
        }
    }

    @Nullable
    private class_22 getMapState() {
        class_1799 map = getMap();
        if (map == null) {
            return null;
        }
        return class_1806.method_7997((class_9209) map.method_58694(class_9334.field_49646), mc.field_1687);
    }

    @Nullable
    private File getPath() {
        String path = TinyFileDialogs.tinyfd_saveFileDialog("Save image", (CharSequence) null, this.filters, (CharSequence) null);
        if (path == null) {
            return null;
        }
        if (!path.endsWith(".png")) {
            path = path + ".png";
        }
        return new File(path);
    }

    @Nullable
    private class_1799 getMap() {
        class_1799 itemStack = mc.field_1724.method_6047();
        if (itemStack.method_7909() == class_1802.field_8204) {
            return itemStack;
        }
        class_1799 itemStack2 = mc.field_1724.method_6079();
        if (itemStack2.method_7909() == class_1802.field_8204) {
            return itemStack2;
        }
        return null;
    }
}
