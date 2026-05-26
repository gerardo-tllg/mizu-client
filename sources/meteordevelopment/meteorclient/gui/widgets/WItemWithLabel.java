package meteordevelopment.meteorclient.gui.widgets;

import java.util.Iterator;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.utils.misc.Names;
import net.minecraft.class_1292;
import net.minecraft.class_1293;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1844;
import net.minecraft.class_9334;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/widgets/WItemWithLabel.class */
public class WItemWithLabel extends WHorizontalList {
    private class_1799 itemStack;
    private String name;
    private WItem item;
    private WLabel label;

    public WItemWithLabel(class_1799 itemStack, String name) {
        this.itemStack = itemStack;
        this.name = name;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public void init() {
        this.item = (WItem) add(this.theme.item(this.itemStack)).widget();
        this.label = (WLabel) add(this.theme.label(this.name + getStringToAppend())).widget();
    }

    private String getStringToAppend() {
        String str = "";
        if (this.itemStack.method_7909() == class_1802.field_8574) {
            Iterator<class_1293> effects = ((class_1844) this.itemStack.method_7909().method_57347().method_58694(class_9334.field_49651)).method_57397().iterator();
            if (!effects.hasNext()) {
                return str;
            }
            String str2 = str + " ";
            class_1293 effect = effects.next();
            if (effect.method_5578() > 0) {
                str2 = str2 + "%d ".formatted(Integer.valueOf(effect.method_5578() + 1));
            }
            String str3 = str2;
            Object[] objArr = new Object[1];
            objArr[0] = class_1292.method_5577(effect, 1.0f, MeteorClient.mc.field_1687 != null ? MeteorClient.mc.field_1687.method_54719().method_54748() : 20.0f).getString();
            str = str3 + "(%s)".formatted(objArr);
        }
        return str;
    }

    public void set(class_1799 itemStack) {
        this.itemStack = itemStack;
        this.item.itemStack = itemStack;
        this.name = Names.get(itemStack);
        this.label.set(this.name + getStringToAppend());
    }

    public String getLabelText() {
        return this.label == null ? this.name : this.label.get();
    }
}
