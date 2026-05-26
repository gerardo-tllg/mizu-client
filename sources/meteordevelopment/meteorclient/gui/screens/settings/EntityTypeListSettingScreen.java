package meteordevelopment.meteorclient.gui.screens.settings;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Names;
import net.minecraft.class_1299;
import net.minecraft.class_1311;
import net.minecraft.class_3545;
import net.minecraft.class_7923;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/screens/settings/EntityTypeListSettingScreen.class */
public class EntityTypeListSettingScreen extends WindowScreen {
    private final EntityTypeListSetting setting;
    private WVerticalList list;
    private final WTextBox filter;
    private String filterText;
    private WSection animals;
    private WSection waterAnimals;
    private WSection monsters;
    private WSection ambient;
    private WSection misc;
    private WTable animalsT;
    private WTable waterAnimalsT;
    private WTable monstersT;
    private WTable ambientT;
    private WTable miscT;
    int hasAnimal;
    int hasWaterAnimal;
    int hasMonster;
    int hasAmbient;
    int hasMisc;

    public EntityTypeListSettingScreen(GuiTheme theme, EntityTypeListSetting setting) {
        super(theme, "Select entities");
        this.filterText = "";
        this.hasAnimal = 0;
        this.hasWaterAnimal = 0;
        this.hasMonster = 0;
        this.hasAmbient = 0;
        this.hasMisc = 0;
        this.setting = setting;
        this.filter = (WTextBox) super.add(theme.textBox("")).minWidth(400.0d).expandX().widget();
        this.filter.setFocused(true);
        this.filter.action = () -> {
            this.filterText = this.filter.get().trim();
            this.list.clear();
            initWidgets();
        };
        this.list = (WVerticalList) super.add(theme.verticalList()).expandX().widget();
    }

    @Override // meteordevelopment.meteorclient.gui.WindowScreen, meteordevelopment.meteorclient.gui.WidgetScreen
    public <W extends WWidget> Cell<W> add(W widget) {
        return this.list.add(widget);
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public void initWidgets() {
        this.hasMisc = 0;
        this.hasAmbient = 0;
        this.hasMonster = 0;
        this.hasWaterAnimal = 0;
        this.hasAnimal = 0;
        for (class_1299<?> entityType : this.setting.get()) {
            if (this.setting.filter == null || this.setting.filter.test(entityType)) {
                switch (AnonymousClass1.$SwitchMap$net$minecraft$entity$SpawnGroup[entityType.method_5891().ordinal()]) {
                    case 1:
                        this.hasAnimal++;
                        break;
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                        this.hasWaterAnimal++;
                        break;
                    case 6:
                        this.hasMonster++;
                        break;
                    case 7:
                        this.hasAmbient++;
                        break;
                    case 8:
                        this.hasMisc++;
                        break;
                }
            }
        }
        boolean first = this.animals == null;
        List<class_1299<?>> animalsE = new ArrayList<>();
        WCheckbox animalsC = this.theme.checkbox(this.hasAnimal > 0);
        this.animals = this.theme.section("Animals", this.animals != null && this.animals.isExpanded(), animalsC);
        animalsC.action = () -> {
            tableChecked(animalsE, animalsC.checked);
        };
        Cell<WSection> animalsCell = add(this.animals).expandX();
        this.animalsT = (WTable) this.animals.add(this.theme.table()).expandX().widget();
        List<class_1299<?>> waterAnimalsE = new ArrayList<>();
        WCheckbox waterAnimalsC = this.theme.checkbox(this.hasWaterAnimal > 0);
        this.waterAnimals = this.theme.section("Water Animals", this.waterAnimals != null && this.waterAnimals.isExpanded(), waterAnimalsC);
        waterAnimalsC.action = () -> {
            tableChecked(waterAnimalsE, waterAnimalsC.checked);
        };
        Cell<WSection> waterAnimalsCell = add(this.waterAnimals).expandX();
        this.waterAnimalsT = (WTable) this.waterAnimals.add(this.theme.table()).expandX().widget();
        List<class_1299<?>> monstersE = new ArrayList<>();
        WCheckbox monstersC = this.theme.checkbox(this.hasMonster > 0);
        this.monsters = this.theme.section("Monsters", this.monsters != null && this.monsters.isExpanded(), monstersC);
        monstersC.action = () -> {
            tableChecked(monstersE, monstersC.checked);
        };
        Cell<WSection> monstersCell = add(this.monsters).expandX();
        this.monstersT = (WTable) this.monsters.add(this.theme.table()).expandX().widget();
        List<class_1299<?>> ambientE = new ArrayList<>();
        WCheckbox ambientC = this.theme.checkbox(this.hasAmbient > 0);
        this.ambient = this.theme.section("Ambient", this.ambient != null && this.ambient.isExpanded(), ambientC);
        ambientC.action = () -> {
            tableChecked(ambientE, ambientC.checked);
        };
        Cell<WSection> ambientCell = add(this.ambient).expandX();
        this.ambientT = (WTable) this.ambient.add(this.theme.table()).expandX().widget();
        List<class_1299<?>> miscE = new ArrayList<>();
        WCheckbox miscC = this.theme.checkbox(this.hasMisc > 0);
        this.misc = this.theme.section("Misc", this.misc != null && this.misc.isExpanded(), miscC);
        miscC.action = () -> {
            tableChecked(miscE, miscC.checked);
        };
        Cell<WSection> miscCell = add(this.misc).expandX();
        this.miscT = (WTable) this.misc.add(this.theme.table()).expandX().widget();
        Consumer<class_1299<?>> entityTypeForEach = entityType2 -> {
            if (this.setting.filter == null || this.setting.filter.test(entityType2)) {
                switch (AnonymousClass1.$SwitchMap$net$minecraft$entity$SpawnGroup[entityType2.method_5891().ordinal()]) {
                    case 1:
                        animalsE.add(entityType2);
                        addEntityType(this.animalsT, animalsC, entityType2);
                        break;
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                        waterAnimalsE.add(entityType2);
                        addEntityType(this.waterAnimalsT, waterAnimalsC, entityType2);
                        break;
                    case 6:
                        monstersE.add(entityType2);
                        addEntityType(this.monstersT, monstersC, entityType2);
                        break;
                    case 7:
                        ambientE.add(entityType2);
                        addEntityType(this.ambientT, ambientC, entityType2);
                        break;
                    case 8:
                        miscE.add(entityType2);
                        addEntityType(this.miscT, miscC, entityType2);
                        break;
                }
            }
        };
        if (this.filterText.isEmpty()) {
            class_7923.field_41177.forEach(entityTypeForEach);
        } else {
            List<class_3545<class_1299<?>, Integer>> entities = new ArrayList<>();
            class_7923.field_41177.forEach(entity -> {
                int words = Utils.searchInWords(Names.get((class_1299<?>) entity), this.filterText);
                int diff = Utils.searchLevenshteinDefault(Names.get((class_1299<?>) entity), this.filterText, false);
                if (words > 0 || diff < Names.get((class_1299<?>) entity).length() / 2) {
                    entities.add(new class_3545(entity, Integer.valueOf(-diff)));
                }
            });
            entities.sort(Comparator.comparingInt(value -> {
                return -((Integer) value.method_15441()).intValue();
            }));
            for (class_3545<class_1299<?>, Integer> pair : entities) {
                entityTypeForEach.accept((class_1299) pair.method_15442());
            }
        }
        if (this.animalsT.cells.isEmpty()) {
            this.list.cells.remove(animalsCell);
        }
        if (this.waterAnimalsT.cells.isEmpty()) {
            this.list.cells.remove(waterAnimalsCell);
        }
        if (this.monstersT.cells.isEmpty()) {
            this.list.cells.remove(monstersCell);
        }
        if (this.ambientT.cells.isEmpty()) {
            this.list.cells.remove(ambientCell);
        }
        if (this.miscT.cells.isEmpty()) {
            this.list.cells.remove(miscCell);
        }
        if (first) {
            int totalCount = ((((this.hasWaterAnimal + this.waterAnimals.cells.size()) + this.monsters.cells.size()) + this.ambient.cells.size()) + this.misc.cells.size()) / 2;
            if (totalCount <= 20) {
                if (!this.animalsT.cells.isEmpty()) {
                    this.animals.setExpanded(true);
                }
                if (!this.waterAnimalsT.cells.isEmpty()) {
                    this.waterAnimals.setExpanded(true);
                }
                if (!this.monstersT.cells.isEmpty()) {
                    this.monsters.setExpanded(true);
                }
                if (!this.ambientT.cells.isEmpty()) {
                    this.ambient.setExpanded(true);
                }
                if (!this.miscT.cells.isEmpty()) {
                    this.misc.setExpanded(true);
                    return;
                }
                return;
            }
            if (!this.animalsT.cells.isEmpty()) {
                this.animals.setExpanded(false);
            }
            if (!this.waterAnimalsT.cells.isEmpty()) {
                this.waterAnimals.setExpanded(false);
            }
            if (!this.monstersT.cells.isEmpty()) {
                this.monsters.setExpanded(false);
            }
            if (!this.ambientT.cells.isEmpty()) {
                this.ambient.setExpanded(false);
            }
            if (!this.miscT.cells.isEmpty()) {
                this.misc.setExpanded(false);
            }
        }
    }

    /* JADX INFO: renamed from: meteordevelopment.meteorclient.gui.screens.settings.EntityTypeListSettingScreen$1, reason: invalid class name */
    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/screens/settings/EntityTypeListSettingScreen$1.class */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$net$minecraft$entity$SpawnGroup = new int[class_1311.values().length];

        static {
            try {
                $SwitchMap$net$minecraft$entity$SpawnGroup[class_1311.field_6294.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$net$minecraft$entity$SpawnGroup[class_1311.field_24460.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$net$minecraft$entity$SpawnGroup[class_1311.field_6300.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$net$minecraft$entity$SpawnGroup[class_1311.field_30092.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$net$minecraft$entity$SpawnGroup[class_1311.field_34447.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$net$minecraft$entity$SpawnGroup[class_1311.field_6302.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$net$minecraft$entity$SpawnGroup[class_1311.field_6303.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$net$minecraft$entity$SpawnGroup[class_1311.field_17715.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
        }
    }

    private void tableChecked(List<class_1299<?>> entityTypes, boolean checked) {
        boolean changed = false;
        for (class_1299<?> entityType : entityTypes) {
            if (checked) {
                this.setting.get().add(entityType);
                changed = true;
            } else if (this.setting.get().remove(entityType)) {
                changed = true;
            }
        }
        if (changed) {
            this.list.clear();
            initWidgets();
            this.setting.onChanged();
        }
    }

    private void addEntityType(WTable table, WCheckbox tableCheckbox, class_1299<?> entityType) {
        table.add(this.theme.label(Names.get(entityType)));
        WCheckbox a = (WCheckbox) table.add(this.theme.checkbox(this.setting.get().contains(entityType))).expandCellX().right().widget();
        a.action = () -> {
            if (a.checked) {
                this.setting.get().add(entityType);
                switch (AnonymousClass1.$SwitchMap$net$minecraft$entity$SpawnGroup[entityType.method_5891().ordinal()]) {
                    case 1:
                        if (this.hasAnimal == 0) {
                            tableCheckbox.checked = true;
                        }
                        this.hasAnimal++;
                        break;
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                        if (this.hasWaterAnimal == 0) {
                            tableCheckbox.checked = true;
                        }
                        this.hasWaterAnimal++;
                        break;
                    case 6:
                        if (this.hasMonster == 0) {
                            tableCheckbox.checked = true;
                        }
                        this.hasMonster++;
                        break;
                    case 7:
                        if (this.hasAmbient == 0) {
                            tableCheckbox.checked = true;
                        }
                        this.hasAmbient++;
                        break;
                    case 8:
                        if (this.hasMisc == 0) {
                            tableCheckbox.checked = true;
                        }
                        this.hasMisc++;
                        break;
                }
            } else if (this.setting.get().remove(entityType)) {
                switch (AnonymousClass1.$SwitchMap$net$minecraft$entity$SpawnGroup[entityType.method_5891().ordinal()]) {
                    case 1:
                        this.hasAnimal--;
                        if (this.hasAnimal == 0) {
                            tableCheckbox.checked = false;
                        }
                        break;
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                        this.hasWaterAnimal--;
                        if (this.hasWaterAnimal == 0) {
                            tableCheckbox.checked = false;
                        }
                        break;
                    case 6:
                        this.hasMonster--;
                        if (this.hasMonster == 0) {
                            tableCheckbox.checked = false;
                        }
                        break;
                    case 7:
                        this.hasAmbient--;
                        if (this.hasAmbient == 0) {
                            tableCheckbox.checked = false;
                        }
                        break;
                    case 8:
                        this.hasMisc--;
                        if (this.hasMisc == 0) {
                            tableCheckbox.checked = false;
                        }
                        break;
                }
            }
            this.setting.onChanged();
        };
        table.row();
    }
}
