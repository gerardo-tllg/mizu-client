package meteordevelopment.meteorclient.systems;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.files.NbtJsonBridge;
import meteordevelopment.meteorclient.utils.files.StreamUtils;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.class_148;
import net.minecraft.class_2487;
import net.minecraft.class_2507;
import org.apache.commons.io.FilenameUtils;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/System.class */
public abstract class System<T> implements ISerializable<T> {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss", Locale.ROOT);
    private final String name;
    private File file;
    protected boolean isFirstInit;

    public System(String name) {
        this.name = name;
        if (name != null) {
            this.file = new File(MeteorClient.FOLDER, name + ".json");
            File legacyNbt = new File(MeteorClient.FOLDER, name + ".nbt");
            this.isFirstInit = (this.file.exists() || legacyNbt.exists()) ? false : true;
        }
    }

    public void init() {
    }

    public void save(File folder) {
        class_2487 tag;
        File jsonFile = getFile();
        if (jsonFile == null || (tag = toTag()) == null) {
            return;
        }
        if (folder != null) {
            jsonFile = new File(folder, jsonFile.getName());
        }
        File jsonFile2 = withExtension(jsonFile, "json");
        try {
            File parent = jsonFile2.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            JsonElement json = NbtJsonBridge.toJson(tag);
            File tempFile = File.createTempFile(MeteorClient.MOD_ID, ".json", parent);
            Writer w = new FileWriter(tempFile);
            try {
                GSON.toJson(json, w);
                w.close();
                try {
                    Files.move(tempFile.toPath(), jsonFile2.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (IOException e) {
                    StreamUtils.copy(tempFile, jsonFile2);
                    tempFile.delete();
                }
            } finally {
            }
        } catch (IOException e2) {
            MeteorClient.LOG.error("Failed to save '{}'.", this.name, e2);
        }
    }

    public void save() {
        save(null);
    }

    public void load(File folder) {
        File jsonFile = getFile();
        if (jsonFile == null) {
            return;
        }
        if (folder != null) {
            jsonFile = new File(folder, jsonFile.getName());
        }
        File jsonFile2 = withExtension(jsonFile, "json");
        File nbtFile = withExtension(jsonFile2, "nbt");
        try {
            if (jsonFile2.exists()) {
                loadJson(jsonFile2);
            } else if (nbtFile.exists()) {
                loadLegacyNbt(nbtFile);
                save(folder);
            }
        } catch (IOException e) {
            MeteorClient.LOG.error("Failed to load '{}'.", this.name, e);
        }
    }

    public void load() {
        load(null);
    }

    private void loadJson(File jsonFile) throws IOException {
        try {
            Reader r = new FileReader(jsonFile);
            try {
                JsonElement json = JsonParser.parseReader(r);
                class_2487 nbt = NbtJsonBridge.toNbt(json);
                if (nbt instanceof class_2487) {
                    class_2487 compound = nbt;
                    fromTag(compound);
                } else {
                    MeteorClient.LOG.warn("'{}' is not a JSON object; ignoring.", jsonFile.getName());
                }
                r.close();
            } catch (Throwable th) {
                try {
                    r.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
                throw th;
            }
        } catch (Exception e) {
            backup(jsonFile);
            MeteorClient.LOG.error("Error parsing {} as JSON. Possibly corrupted?", jsonFile.getName(), e);
        } catch (class_148 e2) {
            backup(jsonFile);
            MeteorClient.LOG.error("Error loading {}. Possibly corrupted?", this.name, e2);
        }
    }

    private void loadLegacyNbt(File nbtFile) throws IOException {
        try {
            class_2487 nbt = class_2507.method_10633(nbtFile.toPath());
            if (nbt != null) {
                fromTag(nbt);
            }
        } catch (class_148 e) {
            backup(nbtFile);
            MeteorClient.LOG.error("Error loading legacy {}. Possibly corrupted?", this.name, e);
        }
    }

    private void backup(File file) {
        try {
            String ext = FilenameUtils.getExtension(file.getName());
            if (ext.isEmpty()) {
                ext = "bak";
            }
            String backupName = FilenameUtils.removeExtension(file.getName()) + "-" + ZonedDateTime.now().format(DATE_TIME_FORMATTER) + ".backup." + ext;
            File backup = new File(file.getParentFile(), backupName);
            StreamUtils.copy(file, backup);
            MeteorClient.LOG.info("Saved settings backup to '{}'.", backup);
        } catch (Exception ex) {
            MeteorClient.LOG.warn("Failed to create backup for '{}'.", file.getName(), ex);
        }
    }

    private static File withExtension(File file, String newExt) {
        String baseName = FilenameUtils.removeExtension(file.getName());
        return new File(file.getParentFile(), baseName + "." + newExt);
    }

    public File getFile() {
        return this.file;
    }

    public String getName() {
        return this.name;
    }

    @Override // meteordevelopment.meteorclient.utils.misc.ISerializable
    public class_2487 toTag() {
        return null;
    }

    @Override // meteordevelopment.meteorclient.utils.misc.ISerializable
    /* JADX INFO: renamed from: fromTag */
    public T fromTag(class_2487 tag) {
        return null;
    }
}
