package meteordevelopment.meteorclient.gui;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.screens.settings.BlockDataSettingScreen;
import meteordevelopment.meteorclient.gui.screens.settings.BlockListSettingScreen;
import meteordevelopment.meteorclient.gui.screens.settings.BlockSettingScreen;
import meteordevelopment.meteorclient.gui.screens.settings.ColorSettingScreen;
import meteordevelopment.meteorclient.gui.screens.settings.EnchantmentListSettingScreen;
import meteordevelopment.meteorclient.gui.screens.settings.EntityTypeListSettingScreen;
import meteordevelopment.meteorclient.gui.screens.settings.FontFaceSettingScreen;
import meteordevelopment.meteorclient.gui.screens.settings.ItemListSettingScreen;
import meteordevelopment.meteorclient.gui.screens.settings.ItemSettingScreen;
import meteordevelopment.meteorclient.gui.screens.settings.ModuleListSettingScreen;
import meteordevelopment.meteorclient.gui.screens.settings.PacketBoolSettingScreen;
import meteordevelopment.meteorclient.gui.screens.settings.ParticleTypeListSettingScreen;
import meteordevelopment.meteorclient.gui.screens.settings.PotionSettingScreen;
import meteordevelopment.meteorclient.gui.screens.settings.ScreenHandlerSettingScreen;
import meteordevelopment.meteorclient.gui.screens.settings.SoundEventListSettingScreen;
import meteordevelopment.meteorclient.gui.screens.settings.StatusEffectAmplifierMapSettingScreen;
import meteordevelopment.meteorclient.gui.screens.settings.StatusEffectListSettingScreen;
import meteordevelopment.meteorclient.gui.screens.settings.StorageBlockListSettingScreen;
import meteordevelopment.meteorclient.gui.themes.meteor.widgets.WMeteorLabel;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.utils.CharFilter;
import meteordevelopment.meteorclient.gui.utils.IScreenFactory;
import meteordevelopment.meteorclient.gui.utils.SettingsWidgetFactory;
import meteordevelopment.meteorclient.gui.widgets.WItem;
import meteordevelopment.meteorclient.gui.widgets.WItemWithLabel;
import meteordevelopment.meteorclient.gui.widgets.WKeybind;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.WQuad;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.input.WBlockPosEdit;
import meteordevelopment.meteorclient.gui.widgets.input.WDoubleEdit;
import meteordevelopment.meteorclient.gui.widgets.input.WDropdown;
import meteordevelopment.meteorclient.gui.widgets.input.WIntEdit;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPlus;
import meteordevelopment.meteorclient.renderer.Fonts;
import meteordevelopment.meteorclient.settings.BlockDataSetting;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BlockPosSetting;
import meteordevelopment.meteorclient.settings.BlockSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorListSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnchantmentListSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.FontFaceSetting;
import meteordevelopment.meteorclient.settings.GenericSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.ItemSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.ModuleListSetting;
import meteordevelopment.meteorclient.settings.PacketListSetting;
import meteordevelopment.meteorclient.settings.ParticleTypeListSetting;
import meteordevelopment.meteorclient.settings.PotionSetting;
import meteordevelopment.meteorclient.settings.ProvidedStringSetting;
import meteordevelopment.meteorclient.settings.ScreenHandlerListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.settings.SoundEventListSetting;
import meteordevelopment.meteorclient.settings.StatusEffectAmplifierMapSetting;
import meteordevelopment.meteorclient.settings.StatusEffectListSetting;
import meteordevelopment.meteorclient.settings.StorageBlockListSetting;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.settings.Vector3dSetting;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.MyPotion;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.class_1074;
import org.apache.commons.lang3.StringUtils;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/DefaultSettingsWidgetFactory.class */
public class DefaultSettingsWidgetFactory extends SettingsWidgetFactory {
    private static final SettingColor WHITE = new SettingColor();

    public DefaultSettingsWidgetFactory(GuiTheme theme) {
        super(theme);
        this.factories.put(BoolSetting.class, (table, setting) -> {
            boolW(table, (BoolSetting) setting);
        });
        this.factories.put(IntSetting.class, (table2, setting2) -> {
            intW(table2, (IntSetting) setting2);
        });
        this.factories.put(DoubleSetting.class, (table3, setting3) -> {
            doubleW(table3, (DoubleSetting) setting3);
        });
        this.factories.put(StringSetting.class, (table4, setting4) -> {
            stringW(table4, (StringSetting) setting4);
        });
        this.factories.put(EnumSetting.class, (table5, setting5) -> {
            enumW(table5, (EnumSetting) setting5);
        });
        this.factories.put(ProvidedStringSetting.class, (table6, setting6) -> {
            providedStringW(table6, (ProvidedStringSetting) setting6);
        });
        this.factories.put(GenericSetting.class, (table7, setting7) -> {
            genericW(table7, (GenericSetting) setting7);
        });
        this.factories.put(ColorSetting.class, (table8, setting8) -> {
            colorW(table8, (ColorSetting) setting8);
        });
        this.factories.put(KeybindSetting.class, (table9, setting9) -> {
            keybindW(table9, (KeybindSetting) setting9);
        });
        this.factories.put(BlockSetting.class, (table10, setting10) -> {
            blockW(table10, (BlockSetting) setting10);
        });
        this.factories.put(BlockListSetting.class, (table11, setting11) -> {
            blockListW(table11, (BlockListSetting) setting11);
        });
        this.factories.put(ItemSetting.class, (table12, setting12) -> {
            itemW(table12, (ItemSetting) setting12);
        });
        this.factories.put(ItemListSetting.class, (table13, setting13) -> {
            itemListW(table13, (ItemListSetting) setting13);
        });
        this.factories.put(EntityTypeListSetting.class, (table14, setting14) -> {
            entityTypeListW(table14, (EntityTypeListSetting) setting14);
        });
        this.factories.put(EnchantmentListSetting.class, (table15, setting15) -> {
            enchantmentListW(table15, (EnchantmentListSetting) setting15);
        });
        this.factories.put(ModuleListSetting.class, (table16, setting16) -> {
            moduleListW(table16, (ModuleListSetting) setting16);
        });
        this.factories.put(PacketListSetting.class, (table17, setting17) -> {
            packetListW(table17, (PacketListSetting) setting17);
        });
        this.factories.put(ParticleTypeListSetting.class, (table18, setting18) -> {
            particleTypeListW(table18, (ParticleTypeListSetting) setting18);
        });
        this.factories.put(SoundEventListSetting.class, (table19, setting19) -> {
            soundEventListW(table19, (SoundEventListSetting) setting19);
        });
        this.factories.put(StatusEffectAmplifierMapSetting.class, (table20, setting20) -> {
            statusEffectAmplifierMapW(table20, (StatusEffectAmplifierMapSetting) setting20);
        });
        this.factories.put(StatusEffectListSetting.class, (table21, setting21) -> {
            statusEffectListW(table21, (StatusEffectListSetting) setting21);
        });
        this.factories.put(StorageBlockListSetting.class, (table22, setting22) -> {
            storageBlockListW(table22, (StorageBlockListSetting) setting22);
        });
        this.factories.put(ScreenHandlerListSetting.class, (table23, setting23) -> {
            screenHandlerListW(table23, (ScreenHandlerListSetting) setting23);
        });
        this.factories.put(BlockDataSetting.class, (table24, setting24) -> {
            blockDataW(table24, (BlockDataSetting) setting24);
        });
        this.factories.put(PotionSetting.class, (table25, setting25) -> {
            potionW(table25, (PotionSetting) setting25);
        });
        this.factories.put(StringListSetting.class, (table26, setting26) -> {
            stringListW(table26, (StringListSetting) setting26);
        });
        this.factories.put(BlockPosSetting.class, (table27, setting27) -> {
            blockPosW(table27, (BlockPosSetting) setting27);
        });
        this.factories.put(ColorListSetting.class, (table28, setting28) -> {
            colorListW(table28, (ColorListSetting) setting28);
        });
        this.factories.put(FontFaceSetting.class, (table29, setting29) -> {
            fontW(table29, (FontFaceSetting) setting29);
        });
        this.factories.put(Vector3dSetting.class, (table30, setting30) -> {
            vector3dW(table30, (Vector3dSetting) setting30);
        });
    }

    @Override // meteordevelopment.meteorclient.gui.utils.SettingsWidgetFactory
    public WWidget create(GuiTheme theme, Settings settings, String filter) {
        WVerticalList list = theme.verticalList();
        List<RemoveInfo> removeInfoList = new ArrayList<>();
        for (SettingGroup group : settings.groups) {
            group(list, group, filter, removeInfoList);
        }
        list.calculateSize();
        list.minWidth = list.width;
        for (RemoveInfo removeInfo : removeInfoList) {
            removeInfo.remove(list);
        }
        return list;
    }

    protected double settingTitleTopMargin() {
        return 6.0d;
    }

    private void group(WVerticalList list, SettingGroup group, String filter, List<RemoveInfo> removeInfoList) {
        WSection section = (WSection) list.add(this.theme.section(group.name, group.sectionExpanded)).expandX().widget();
        section.action = () -> {
            group.sectionExpanded = section.isExpanded();
        };
        WTable table = (WTable) section.add(this.theme.table()).expandX().widget();
        RemoveInfo removeInfo = null;
        for (Setting<?> setting : group) {
            if (StringUtils.containsIgnoreCase(setting.title, filter)) {
                boolean visible = setting.isVisible();
                setting.lastWasVisible = visible;
                if (!visible) {
                    if (removeInfo == null) {
                        removeInfo = new RemoveInfo(section, table);
                    }
                    removeInfo.markRowForRemoval();
                }
                ((WLabel) table.add(this.theme.label(setting.title)).top().marginTop(settingTitleTopMargin()).widget()).tooltip = setting.description;
                SettingsWidgetFactory.Factory factory = getFactory(setting.getClass());
                if (factory != null) {
                    factory.create(table, setting);
                }
                table.row();
            }
        }
        if (removeInfo != null) {
            removeInfoList.add(removeInfo);
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/DefaultSettingsWidgetFactory$RemoveInfo.class */
    private static class RemoveInfo {
        private final WSection section;
        private final WTable table;
        private final IntList rowIds = new IntArrayList();

        public RemoveInfo(WSection section, WTable table) {
            this.section = section;
            this.table = table;
        }

        public void markRowForRemoval() {
            this.rowIds.add(this.table.rowI());
        }

        public void remove(WVerticalList list) {
            for (int i = 0; i < this.rowIds.size(); i++) {
                this.table.removeRow(this.rowIds.getInt(i) - i);
            }
            if (this.table.cells.isEmpty()) {
                list.cells.removeIf(cell -> {
                    return cell.widget() == this.section;
                });
            }
        }
    }

    private void boolW(WTable table, BoolSetting setting) {
        WCheckbox checkbox = (WCheckbox) table.add(this.theme.checkbox(setting.get().booleanValue())).expandCellX().widget();
        checkbox.action = () -> {
            setting.set(Boolean.valueOf(checkbox.checked));
        };
        reset(table, setting, () -> {
            checkbox.checked = setting.get().booleanValue();
        });
    }

    private void intW(WTable table, IntSetting setting) {
        WIntEdit edit = (WIntEdit) table.add(this.theme.intEdit(setting.get().intValue(), setting.min, setting.max, setting.sliderMin, setting.sliderMax, setting.noSlider)).expandX().widget();
        edit.action = () -> {
            if (!setting.set(Integer.valueOf(edit.get()))) {
                edit.set(setting.get().intValue());
            }
        };
        reset(table, setting, () -> {
            edit.set(setting.get().intValue());
        });
    }

    private void doubleW(WTable table, DoubleSetting setting) {
        WDoubleEdit edit = this.theme.doubleEdit(setting.get().doubleValue(), setting.min, setting.max, setting.sliderMin, setting.sliderMax, setting.decimalPlaces, setting.noSlider);
        table.add(edit).expandX();
        Runnable action = () -> {
            if (!setting.set(Double.valueOf(edit.get()))) {
                edit.set(setting.get().doubleValue());
            }
        };
        if (setting.onSliderRelease) {
            edit.actionOnRelease = action;
        } else {
            edit.action = action;
        }
        reset(table, setting, () -> {
            edit.set(setting.get().doubleValue());
        });
    }

    private void stringW(WTable table, StringSetting setting) {
        CharFilter filter = setting.filter == null ? (text, c) -> {
            return true;
        } : setting.filter;
        Cell<WTextBox> cell = table.add(this.theme.textBox(setting.get(), filter, setting.renderer));
        if (setting.wide) {
            cell.minWidth(((double) Utils.getWindowWidth()) - (((double) Utils.getWindowWidth()) / 4.0d));
        }
        WTextBox textBox = (WTextBox) cell.expandX().widget();
        textBox.action = () -> {
            setting.set(textBox.get());
        };
        reset(table, setting, () -> {
            textBox.set(setting.get());
        });
    }

    private void stringListW(WTable table, StringListSetting setting) {
        WTable wtable = (WTable) table.add(this.theme.table()).expandX().widget();
        StringListSetting.fillTable(this.theme, wtable, setting);
    }

    private <T extends Enum<?>> void enumW(WTable table, EnumSetting<T> setting) {
        WDropdown<T> dropdown = (WDropdown) table.add(this.theme.dropdown(setting.get())).expandCellX().widget();
        dropdown.action = () -> {
            setting.set((Enum) dropdown.get());
        };
        reset(table, setting, () -> {
            dropdown.set((Enum) setting.get());
        });
    }

    private void providedStringW(WTable table, ProvidedStringSetting setting) {
        WDropdown<String> dropdown = (WDropdown) table.add(this.theme.dropdown(setting.supplier.get(), setting.get())).expandCellX().widget();
        dropdown.action = () -> {
            setting.set((String) dropdown.get());
        };
        reset(table, setting, () -> {
            dropdown.set(setting.get());
        });
    }

    private void genericW(WTable table, GenericSetting<?> setting) {
        WButton edit = (WButton) table.add(this.theme.button(GuiRenderer.EDIT)).widget();
        edit.action = () -> {
            MeteorClient.mc.method_1507(((IScreenFactory) setting.get()).createScreen(this.theme));
        };
        reset(table, setting, null);
    }

    private void colorW(WTable table, ColorSetting setting) {
        WHorizontalList list = (WHorizontalList) table.add(this.theme.horizontalList()).expandX().widget();
        WQuad quad = (WQuad) list.add(this.theme.quad(setting.get())).widget();
        WButton edit = (WButton) list.add(this.theme.button(GuiRenderer.EDIT)).widget();
        edit.action = () -> {
            MeteorClient.mc.method_1507(new ColorSettingScreen(this.theme, setting));
        };
        reset(table, setting, () -> {
            quad.color = setting.get();
        });
    }

    private void keybindW(WTable table, KeybindSetting setting) {
        WHorizontalList list = (WHorizontalList) table.add(this.theme.horizontalList()).expandX().widget();
        WKeybind keybind = (WKeybind) list.add(this.theme.keybind(setting.get(), setting.getDefaultValue())).expandX().widget();
        Objects.requireNonNull(setting);
        keybind.action = setting::onChanged;
        setting.widget = keybind;
        WButton reset = (WButton) list.add(this.theme.button(GuiRenderer.RESET)).expandCellX().right().widget();
        Objects.requireNonNull(keybind);
        reset.action = keybind::resetBind;
    }

    private void blockW(WTable table, BlockSetting setting) {
        WHorizontalList list = (WHorizontalList) table.add(this.theme.horizontalList()).expandX().widget();
        WItem item = (WItem) list.add(this.theme.item(setting.get().method_8389().method_7854())).widget();
        WButton select = (WButton) list.add(this.theme.button("Select")).widget();
        select.action = () -> {
            BlockSettingScreen screen = new BlockSettingScreen(this.theme, setting);
            screen.onClosed(() -> {
                item.set(setting.get().method_8389().method_7854());
            });
            MeteorClient.mc.method_1507(screen);
        };
        reset(table, setting, () -> {
            item.set(setting.get().method_8389().method_7854());
        });
    }

    private void blockPosW(WTable table, BlockPosSetting setting) {
        WBlockPosEdit edit = (WBlockPosEdit) table.add(this.theme.blockPosEdit(setting.get())).expandX().widget();
        edit.actionOnRelease = () -> {
            if (!setting.set(edit.get())) {
                edit.set(setting.get());
            }
        };
        reset(table, setting, () -> {
            edit.set(setting.get());
        });
    }

    private void blockListW(WTable table, BlockListSetting setting) {
        selectW(table, setting, () -> {
            MeteorClient.mc.method_1507(new BlockListSettingScreen(this.theme, setting));
        });
    }

    private void itemW(WTable table, ItemSetting setting) {
        WHorizontalList list = (WHorizontalList) table.add(this.theme.horizontalList()).expandX().widget();
        WItem item = (WItem) list.add(this.theme.item(setting.get().method_8389().method_7854())).widget();
        WButton select = (WButton) list.add(this.theme.button("Select")).widget();
        select.action = () -> {
            ItemSettingScreen screen = new ItemSettingScreen(this.theme, setting);
            screen.onClosed(() -> {
                item.set(setting.get().method_7854());
            });
            MeteorClient.mc.method_1507(screen);
        };
        reset(table, setting, () -> {
            item.set(setting.get().method_7854());
        });
    }

    private void itemListW(WTable table, ItemListSetting setting) {
        selectW(table, setting, () -> {
            MeteorClient.mc.method_1507(new ItemListSettingScreen(this.theme, setting));
        });
    }

    private void entityTypeListW(WTable table, EntityTypeListSetting setting) {
        selectW(table, setting, () -> {
            MeteorClient.mc.method_1507(new EntityTypeListSettingScreen(this.theme, setting));
        });
    }

    private void enchantmentListW(WTable table, EnchantmentListSetting setting) {
        selectW(table, setting, () -> {
            MeteorClient.mc.method_1507(new EnchantmentListSettingScreen(this.theme, setting));
        });
    }

    private void moduleListW(WTable table, ModuleListSetting setting) {
        selectW(table, setting, () -> {
            MeteorClient.mc.method_1507(new ModuleListSettingScreen(this.theme, setting));
        });
    }

    private void packetListW(WTable table, PacketListSetting setting) {
        selectW(table, setting, () -> {
            MeteorClient.mc.method_1507(new PacketBoolSettingScreen(this.theme, setting));
        });
    }

    private void particleTypeListW(WTable table, ParticleTypeListSetting setting) {
        selectW(table, setting, () -> {
            MeteorClient.mc.method_1507(new ParticleTypeListSettingScreen(this.theme, setting));
        });
    }

    private void soundEventListW(WTable table, SoundEventListSetting setting) {
        selectW(table, setting, () -> {
            MeteorClient.mc.method_1507(new SoundEventListSettingScreen(this.theme, setting));
        });
    }

    private void statusEffectAmplifierMapW(WTable table, StatusEffectAmplifierMapSetting setting) {
        selectW(table, setting, () -> {
            MeteorClient.mc.method_1507(new StatusEffectAmplifierMapSettingScreen(this.theme, setting));
        });
    }

    private void statusEffectListW(WTable table, StatusEffectListSetting setting) {
        selectW(table, setting, () -> {
            MeteorClient.mc.method_1507(new StatusEffectListSettingScreen(this.theme, setting));
        });
    }

    private void storageBlockListW(WTable table, StorageBlockListSetting setting) {
        selectW(table, setting, () -> {
            MeteorClient.mc.method_1507(new StorageBlockListSettingScreen(this.theme, setting));
        });
    }

    private void screenHandlerListW(WTable table, ScreenHandlerListSetting setting) {
        selectW(table, setting, () -> {
            MeteorClient.mc.method_1507(new ScreenHandlerSettingScreen(this.theme, setting));
        });
    }

    private void blockDataW(WTable table, BlockDataSetting<?> setting) {
        WButton button = (WButton) table.add(this.theme.button(GuiRenderer.EDIT)).expandCellX().widget();
        button.action = () -> {
            MeteorClient.mc.method_1507(new BlockDataSettingScreen(this.theme, setting));
        };
        reset(table, setting, null);
    }

    /* JADX WARN: Multi-variable type inference failed */
    private void potionW(WTable table, PotionSetting setting) {
        WHorizontalList list = (WHorizontalList) table.add(this.theme.horizontalList()).expandX().widget();
        WItemWithLabel item = (WItemWithLabel) list.add(this.theme.itemWithLabel(((MyPotion) setting.get()).potion, class_1074.method_4662(((MyPotion) setting.get()).potion.method_7909().method_7876(), new Object[0]))).widget();
        WButton button = (WButton) list.add(this.theme.button("Select")).expandCellX().widget();
        button.action = () -> {
            WidgetScreen screen = new PotionSettingScreen(this.theme, setting);
            screen.onClosed(() -> {
                item.set(((MyPotion) setting.get()).potion);
            });
            MeteorClient.mc.method_1507(screen);
        };
        reset(list, setting, () -> {
            item.set(((MyPotion) setting.get()).potion);
        });
    }

    private void fontW(WTable table, FontFaceSetting setting) {
        WHorizontalList list = (WHorizontalList) table.add(this.theme.horizontalList()).expandX().widget();
        WLabel label = (WLabel) list.add(this.theme.label(setting.get().info.family())).widget();
        WButton button = (WButton) list.add(this.theme.button("Select")).expandCellX().widget();
        button.action = () -> {
            WidgetScreen screen = new FontFaceSettingScreen(this.theme, setting);
            screen.onClosed(() -> {
                label.set(setting.get().info.family());
            });
            MeteorClient.mc.method_1507(screen);
        };
        reset(list, setting, () -> {
            label.set(Fonts.DEFAULT_FONT.info.family());
        });
    }

    private void colorListW(WTable table, ColorListSetting setting) {
        WTable tab = (WTable) table.add(this.theme.table()).expandX().widget();
        WTable t = (WTable) tab.add(this.theme.table()).expandX().widget();
        tab.row();
        colorListWFill(t, setting);
        WPlus add = (WPlus) tab.add(this.theme.plus()).expandCellX().widget();
        add.action = () -> {
            setting.get().add(new SettingColor());
            setting.onChanged();
            t.clear();
            colorListWFill(t, setting);
        };
        reset(tab, setting, () -> {
            t.clear();
            colorListWFill(t, setting);
        });
    }

    private void colorListWFill(WTable t, ColorListSetting setting) {
        int i = 0;
        for (SettingColor color : setting.get()) {
            int _i = i;
            t.add(this.theme.label(i + ":"));
            t.add(this.theme.quad(color)).widget();
            WButton edit = (WButton) t.add(this.theme.button(GuiRenderer.EDIT)).widget();
            edit.action = () -> {
                SettingColor defaultValue = WHITE;
                if (_i < setting.getDefaultValue().size()) {
                    defaultValue = setting.getDefaultValue().get(_i);
                }
                ColorSetting set = new ColorSetting(setting.name, setting.description, defaultValue, settingColor -> {
                    setting.get().get(_i).set((Color) settingColor);
                    setting.onChanged();
                }, null, null);
                set.set(setting.get().get(_i));
                MeteorClient.mc.method_1507(new ColorSettingScreen(this.theme, set));
            };
            WMinus remove = (WMinus) t.add(this.theme.minus()).expandCellX().right().widget();
            remove.action = () -> {
                setting.get().remove(_i);
                setting.onChanged();
                t.clear();
                colorListWFill(t, setting);
            };
            t.row();
            i++;
        }
    }

    private void vector3dW(WTable table, Vector3dSetting setting) {
        WTable internal = (WTable) table.add(this.theme.table()).expandX().widget();
        WDoubleEdit x = addVectorComponent(internal, "X", setting.get().x, val -> {
            setting.get().x = val.doubleValue();
        }, setting);
        WDoubleEdit y = addVectorComponent(internal, "Y", setting.get().y, val2 -> {
            setting.get().y = val2.doubleValue();
        }, setting);
        WDoubleEdit z = addVectorComponent(internal, "Z", setting.get().z, val3 -> {
            setting.get().z = val3.doubleValue();
        }, setting);
        reset(table, setting, () -> {
            x.set(setting.get().x);
            y.set(setting.get().y);
            z.set(setting.get().z);
        });
    }

    private WDoubleEdit addVectorComponent(WTable table, String label, double value, Consumer<Double> update, Vector3dSetting setting) {
        table.add(this.theme.label(label + ": "));
        WDoubleEdit component = (WDoubleEdit) table.add(this.theme.doubleEdit(value, setting.min, setting.max, setting.sliderMin, setting.sliderMax, setting.decimalPlaces, setting.noSlider)).expandX().widget();
        if (setting.onSliderRelease) {
            component.actionOnRelease = () -> {
                update.accept(Double.valueOf(component.get()));
            };
        } else {
            component.action = () -> {
                update.accept(Double.valueOf(component.get()));
            };
        }
        table.row();
        return component;
    }

    private void selectW(WContainer c, Setting<?> setting, Runnable action) {
        boolean addCount = WSelectedCountLabel.getSize(setting) != -1;
        WContainer c2 = c;
        if (addCount) {
            c2 = (WContainer) c.add(this.theme.horizontalList()).expandCellX().widget();
            ((WHorizontalList) c2).spacing *= 2.0d;
        }
        WButton button = (WButton) c2.add(this.theme.button("Select")).expandCellX().widget();
        button.action = action;
        if (addCount) {
            c2.add(new WSelectedCountLabel(setting).color(this.theme.textSecondaryColor()));
        }
        reset(c, setting, null);
    }

    private void reset(WContainer c, Setting<?> setting, Runnable action) {
        WButton reset = (WButton) c.add(this.theme.button(GuiRenderer.RESET)).widget();
        reset.action = () -> {
            setting.reset();
            if (action != null) {
                action.run();
            }
        };
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/DefaultSettingsWidgetFactory$WSelectedCountLabel.class */
    private static class WSelectedCountLabel extends WMeteorLabel {
        private final Setting<?> setting;
        private int lastSize;

        public WSelectedCountLabel(Setting<?> setting) {
            super("", false);
            this.lastSize = -1;
            this.setting = setting;
        }

        @Override // meteordevelopment.meteorclient.gui.themes.meteor.widgets.WMeteorLabel, meteordevelopment.meteorclient.gui.widgets.WWidget
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            int size = getSize(this.setting);
            if (size != this.lastSize) {
                set("(" + size + " selected)");
                this.lastSize = size;
            }
            super.onRender(renderer, mouseX, mouseY, delta);
        }

        public static int getSize(Setting<?> setting) {
            Object obj = setting.get();
            if (obj instanceof Collection) {
                Collection<?> collection = (Collection) obj;
                return collection.size();
            }
            Object obj2 = setting.get();
            if (!(obj2 instanceof Map)) {
                return -1;
            }
            Map<?, ?> map = (Map) obj2;
            return map.size();
        }
    }
}
