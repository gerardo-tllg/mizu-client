package meteordevelopment.meteorclient.gui.screens.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.BlockDataSetting;
import meteordevelopment.meteorclient.settings.IBlockData;
import meteordevelopment.meteorclient.utils.misc.IChangeable;
import meteordevelopment.meteorclient.utils.misc.ICopyable;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.misc.Names;
import net.minecraft.class_2248;
import net.minecraft.class_7923;
import org.apache.commons.lang3.StringUtils;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/screens/settings/BlockDataSettingScreen.class */
public class BlockDataSettingScreen extends WindowScreen {
    private static final List<class_2248> BLOCKS = new ArrayList(100);
    private final BlockDataSetting<?> setting;
    private WTable table;
    private String filterText;

    public BlockDataSettingScreen(GuiTheme theme, BlockDataSetting<?> setting) {
        super(theme, "Configure Blocks");
        this.filterText = "";
        this.setting = setting;
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public void initWidgets() {
        WTextBox filter = (WTextBox) add(this.theme.textBox("")).minWidth(400.0d).expandX().widget();
        filter.setFocused(true);
        filter.action = () -> {
            this.filterText = filter.get().trim();
            this.table.clear();
            initTable();
        };
        this.table = (WTable) add(this.theme.table()).expandX().widget();
        initTable();
    }

    public <T extends ICopyable<T> & ISerializable<T> & IChangeable & IBlockData<T>> void initTable() {
        for (class_2248 block : class_7923.field_41175) {
            ICopyable iCopyable = (ICopyable) ((Map) this.setting.get()).get(block);
            if (iCopyable == null || !((IChangeable) iCopyable).isChanged()) {
                BLOCKS.add(block);
            } else {
                BLOCKS.addFirst(block);
            }
        }
        for (class_2248 block2 : BLOCKS) {
            String name = Names.get(block2);
            if (StringUtils.containsIgnoreCase(name, this.filterText)) {
                ICopyable iCopyable2 = (ICopyable) ((Map) this.setting.get()).get(block2);
                this.table.add(this.theme.itemWithLabel(block2.method_8389().method_7854(), Names.get(block2))).expandCellX();
                this.table.add(this.theme.label((iCopyable2 == null || !((IChangeable) iCopyable2).isChanged()) ? " " : "*"));
                WButton edit = (WButton) this.table.add(this.theme.button(GuiRenderer.EDIT)).widget();
                edit.action = () -> {
                    ICopyable iCopyableCopy = iCopyable2;
                    if (iCopyableCopy == null) {
                        iCopyableCopy = ((ICopyable) this.setting.defaultData.get()).copy();
                    }
                    MeteorClient.mc.method_1507(((IBlockData) iCopyableCopy).createScreen(this.theme, block2, this.setting));
                };
                WButton reset = (WButton) this.table.add(this.theme.button(GuiRenderer.RESET)).widget();
                reset.action = () -> {
                    ((Map) this.setting.get()).remove(block2);
                    this.setting.onChanged();
                    if (iCopyable2 != null && ((IChangeable) iCopyable2).isChanged()) {
                        this.table.clear();
                        initTable();
                    }
                };
                this.table.row();
            }
        }
        BLOCKS.clear();
    }
}
