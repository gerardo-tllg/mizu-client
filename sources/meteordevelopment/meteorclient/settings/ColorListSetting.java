package meteordevelopment.meteorclient.settings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.class_2487;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/ColorListSetting.class */
public class ColorListSetting extends Setting<List<SettingColor>> {
    public ColorListSetting(String name, String description, List<SettingColor> defaultValue, Consumer<List<SettingColor>> onChanged, Consumer<Setting<List<SettingColor>>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public List<SettingColor> parseImpl(String str) {
        List<SettingColor> colors = new ArrayList<>();
        try {
            String[] colorsStr = str.replaceAll("\\s+", "").split(";");
            for (String colorStr : colorsStr) {
                String[] strs = colorStr.split(",");
                colors.add(new SettingColor(Integer.parseInt(strs[0]), Integer.parseInt(strs[1]), Integer.parseInt(strs[2]), Integer.parseInt(strs[3])));
            }
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
        }
        return colors;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public boolean isValueValid(List<SettingColor> value) {
        return true;
    }

    /* JADX WARN: Type inference failed for: r1v0, types: [T, java.util.ArrayList] */
    @Override // meteordevelopment.meteorclient.settings.Setting
    protected void resetImpl() {
        this.value = new ArrayList(((List) this.defaultValue).size());
        for (SettingColor settingColor : (List) this.defaultValue) {
            ((List) this.value).add(new SettingColor(settingColor));
        }
    }

    @Override // meteordevelopment.meteorclient.settings.Setting
    protected class_2487 save(class_2487 tag) {
        tag.method_10566("value", NbtUtils.listToTag(get()));
        return tag;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Can't rename method to resolve collision */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // meteordevelopment.meteorclient.settings.Setting
    public List<SettingColor> load(class_2487 tag) {
        get().clear();
        Iterator it = tag.method_68569("value").iterator();
        while (it.hasNext()) {
            get().add(new SettingColor().fromTag2((class_2487) it.next()));
        }
        return get();
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/settings/ColorListSetting$Builder.class */
    public static class Builder extends Setting.SettingBuilder<Builder, List<SettingColor>, ColorListSetting> {
        public Builder() {
            super(new ArrayList());
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // meteordevelopment.meteorclient.settings.Setting.SettingBuilder
        public ColorListSetting build() {
            return new ColorListSetting(this.name, this.description, (List) this.defaultValue, this.onChanged, this.onModuleActivated, this.visible);
        }
    }
}
