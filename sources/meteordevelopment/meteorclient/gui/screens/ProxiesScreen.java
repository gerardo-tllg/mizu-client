package meteordevelopment.meteorclient.gui.screens;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.systems.proxies.Proxies;
import meteordevelopment.meteorclient.systems.proxies.Proxy;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/screens/ProxiesScreen.class */
public class ProxiesScreen extends WindowScreen {
    private final List<WCheckbox> checkboxes;

    public ProxiesScreen(GuiTheme theme) {
        super(theme, "Proxies");
        this.checkboxes = new ArrayList();
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public void initWidgets() {
        WTable table = (WTable) add(this.theme.table()).expandX().minWidth(400.0d).widget();
        initTable(table);
        add(this.theme.horizontalSeparator()).expandX();
        WHorizontalList l = (WHorizontalList) add(this.theme.horizontalList()).expandX().widget();
        WButton newBtn = (WButton) l.add(this.theme.button("New")).expandX().widget();
        newBtn.action = () -> {
            MeteorClient.mc.method_1507(new EditProxyScreen(this.theme, null, this::reload));
        };
        PointerBuffer filters = BufferUtils.createPointerBuffer(1);
        ByteBuffer txtFilter = MemoryUtil.memASCII("*.txt");
        filters.put(txtFilter);
        filters.rewind();
        WButton importBtn = (WButton) l.add(this.theme.button("Import")).expandX().widget();
        importBtn.action = () -> {
            String selectedFile = TinyFileDialogs.tinyfd_openFileDialog("Import Proxies", (CharSequence) null, filters, (CharSequence) null, false);
            if (selectedFile != null) {
                File file = new File(selectedFile);
                MeteorClient.mc.method_1507(new ProxiesImportScreen(this.theme, file));
            }
        };
    }

    private void initTable(WTable table) {
        table.clear();
        if (Proxies.get().isEmpty()) {
            return;
        }
        for (Proxy proxy : Proxies.get()) {
            WCheckbox enabled = (WCheckbox) table.add(this.theme.checkbox(proxy.enabled.get().booleanValue())).widget();
            this.checkboxes.add(enabled);
            enabled.action = () -> {
                boolean checked = enabled.checked;
                Proxies.get().setEnabled(proxy, checked);
                for (WCheckbox checkbox : this.checkboxes) {
                    checkbox.checked = false;
                }
                enabled.checked = checked;
            };
            WLabel name = (WLabel) table.add(this.theme.label(proxy.name.get())).widget();
            name.color = this.theme.textColor();
            WLabel type = (WLabel) table.add(this.theme.label("(" + String.valueOf(proxy.type.get()) + ")")).widget();
            type.color = this.theme.textSecondaryColor();
            WHorizontalList ipList = (WHorizontalList) table.add(this.theme.horizontalList()).expandCellX().widget();
            ipList.spacing = 0.0d;
            ipList.add(this.theme.label(proxy.address.get()));
            ((WLabel) ipList.add(this.theme.label(":")).widget()).color = this.theme.textSecondaryColor();
            ipList.add(this.theme.label(Integer.toString(proxy.port.get().intValue())));
            WButton edit = (WButton) table.add(this.theme.button(GuiRenderer.EDIT)).widget();
            edit.action = () -> {
                MeteorClient.mc.method_1507(new EditProxyScreen(this.theme, proxy, this::reload));
            };
            WMinus remove = (WMinus) table.add(this.theme.minus()).widget();
            remove.action = () -> {
                Proxies.get().remove(proxy);
                reload();
            };
            table.row();
        }
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public boolean toClipboard() {
        return NbtUtils.toClipboard(Proxies.get());
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public boolean fromClipboard() {
        return NbtUtils.fromClipboard(Proxies.get());
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/screens/ProxiesScreen$EditProxyScreen.class */
    protected static class EditProxyScreen extends EditSystemScreen<Proxy> {
        public EditProxyScreen(GuiTheme theme, Proxy value, Runnable reload) {
            super(theme, value, reload);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // meteordevelopment.meteorclient.gui.screens.EditSystemScreen
        public Proxy create() {
            return new Proxy.Builder().build();
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // meteordevelopment.meteorclient.gui.screens.EditSystemScreen
        public boolean save() {
            return ((Proxy) this.value).resolveAddress() && (!this.isNew || Proxies.get().add((Proxy) this.value));
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // meteordevelopment.meteorclient.gui.screens.EditSystemScreen
        public Settings getSettings() {
            return ((Proxy) this.value).settings;
        }
    }
}
