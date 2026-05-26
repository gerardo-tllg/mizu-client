package meteordevelopment.meteorclient.gui.screens;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import net.minecraft.class_1802;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/screens/ModulesScreen.class */
public class ModulesScreen extends TabScreen {
    private WCategoryController controller;

    public ModulesScreen(GuiTheme theme) {
        super(theme, (Tab) Tabs.get().getFirst());
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public void initWidgets() {
        this.controller = (WCategoryController) add(new WCategoryController()).widget();
        WVerticalList help = (WVerticalList) add(this.theme.verticalList()).pad(4.0d).bottom().widget();
        help.add(this.theme.label("Left click - Toggle module"));
        help.add(this.theme.label("Right click - Open module settings"));
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    protected void method_25426() {
        super.method_25426();
        this.controller.refresh();
    }

    protected WWindow createCategory(WContainer c, Category category, List<Module> moduleList) {
        WWindow w = this.theme.window(category.name);
        w.id = category.name;
        w.padding = 0.0d;
        w.spacing = 0.0d;
        if (this.theme.categoryIcons()) {
            w.beforeHeaderInit = wContainer -> {
                wContainer.add(this.theme.item(category.icon)).pad(2.0d);
            };
        }
        c.add(w);
        w.view.scrollOnlyWhenMouseOver = true;
        w.view.hasScrollBar = false;
        w.view.spacing = 0.0d;
        for (Module module : moduleList) {
            w.add(this.theme.module(module)).expandX();
        }
        return w;
    }

    protected void createSearchW(WContainer w, String text) {
        if (!text.isEmpty()) {
            Set<Module> modules = Modules.get().searchTitles(text);
            if (!modules.isEmpty()) {
                WSection section = (WSection) w.add(this.theme.section("Modules")).expandX().widget();
                section.spacing = 0.0d;
                int count = 0;
                for (Module module : modules) {
                    if (count >= Config.get().moduleSearchCount.get().intValue() || count >= modules.size()) {
                        break;
                    }
                    section.add(this.theme.module(module)).expandX();
                    count++;
                }
            }
            Set<Module> modules2 = Modules.get().searchSettingTitles(text);
            if (!modules2.isEmpty()) {
                WSection section2 = (WSection) w.add(this.theme.section("Settings")).expandX().widget();
                section2.spacing = 0.0d;
                int count2 = 0;
                for (Module module2 : modules2) {
                    if (count2 < Config.get().moduleSearchCount.get().intValue() && count2 < modules2.size()) {
                        section2.add(this.theme.module(module2)).expandX();
                        count2++;
                    } else {
                        return;
                    }
                }
            }
        }
    }

    protected WWindow createSearch(WContainer c) {
        WWindow w = this.theme.window("Search");
        w.id = "search";
        if (this.theme.categoryIcons()) {
            w.beforeHeaderInit = wContainer -> {
                wContainer.add(this.theme.item(class_1802.field_8251.method_7854())).pad(2.0d);
            };
        }
        c.add(w);
        w.view.scrollOnlyWhenMouseOver = true;
        w.view.hasScrollBar = false;
        w.view.maxHeight -= 20.0d;
        WVerticalList l = this.theme.verticalList();
        WTextBox text = (WTextBox) w.add(this.theme.textBox("")).minWidth(140.0d).expandX().widget();
        text.setFocused(true);
        text.action = () -> {
            l.clear();
            createSearchW(l, text.get());
        };
        w.add(l).expandX();
        createSearchW(l, text.get());
        return w;
    }

    protected Cell<WWindow> createFavorites(WContainer c) {
        boolean hasFavorites = Modules.get().getAll().stream().anyMatch(module -> {
            return module.favorite;
        });
        if (!hasFavorites) {
            return null;
        }
        WWindow w = this.theme.window("Favorites");
        w.id = "favorites";
        w.padding = 0.0d;
        w.spacing = 0.0d;
        if (this.theme.categoryIcons()) {
            w.beforeHeaderInit = wContainer -> {
                wContainer.add(this.theme.item(class_1802.field_8137.method_7854())).pad(2.0d);
            };
        }
        Cell<WWindow> cell = c.add(w);
        w.view.scrollOnlyWhenMouseOver = true;
        w.view.hasScrollBar = false;
        w.view.spacing = 0.0d;
        createFavoritesW(w);
        return cell;
    }

    protected boolean createFavoritesW(WWindow w) {
        List<Module> modules = new ArrayList<>();
        for (Module module : Modules.get().getAll()) {
            if (module.favorite) {
                modules.add(module);
            }
        }
        modules.sort((o1, o2) -> {
            return String.CASE_INSENSITIVE_ORDER.compare(o1.name, o2.name);
        });
        Iterator<Module> it = modules.iterator();
        while (it.hasNext()) {
            w.add(this.theme.module(it.next())).expandX();
        }
        return !modules.isEmpty();
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public boolean toClipboard() {
        return NbtUtils.toClipboard(Modules.get());
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public boolean fromClipboard() {
        return NbtUtils.fromClipboard(Modules.get());
    }

    @Override // meteordevelopment.meteorclient.gui.WidgetScreen
    public void reload() {
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/screens/ModulesScreen$WCategoryController.class */
    protected class WCategoryController extends WContainer {
        public final List<WWindow> windows = new ArrayList();
        private Cell<WWindow> favorites;

        protected WCategoryController() {
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
        public void init() {
            List<Module> moduleList = new ArrayList<>();
            for (Category category : Modules.loopCategories()) {
                for (Module module : Modules.get().getGroup(category)) {
                    if (!Config.get().hiddenModules.get().contains(module)) {
                        moduleList.add(module);
                    }
                }
                if (!moduleList.isEmpty()) {
                    this.windows.add(ModulesScreen.this.createCategory(this, category, moduleList));
                    moduleList.clear();
                }
            }
            this.windows.add(ModulesScreen.this.createSearch(this));
            refresh();
        }

        protected void refresh() {
            if (this.favorites == null) {
                this.favorites = ModulesScreen.this.createFavorites(this);
                if (this.favorites != null) {
                    this.windows.add((WWindow) this.favorites.widget());
                    return;
                }
                return;
            }
            ((WWindow) this.favorites.widget()).clear();
            if (!ModulesScreen.this.createFavoritesW((WWindow) this.favorites.widget())) {
                remove(this.favorites);
                this.windows.remove(this.favorites.widget());
                this.favorites = null;
            }
        }

        @Override // meteordevelopment.meteorclient.gui.widgets.containers.WContainer, meteordevelopment.meteorclient.gui.widgets.WWidget
        protected void onCalculateWidgetPositions() {
            double pad = this.theme.scale(4.0d);
            double h = this.theme.scale(40.0d);
            double x = this.x + pad;
            double y = this.y;
            for (Cell<?> cell : this.cells) {
                double windowWidth = Utils.getWindowWidth();
                double windowHeight = Utils.getWindowHeight();
                if (x + cell.width > windowWidth) {
                    x += pad;
                    y += h;
                }
                if (x > windowWidth) {
                    x = (windowWidth / 2.0d) - (cell.width / 2.0d);
                    if (x < 0.0d) {
                        x = 0.0d;
                    }
                }
                if (y > windowHeight) {
                    y = (windowHeight / 2.0d) - (cell.height / 2.0d);
                    if (y < 0.0d) {
                        y = 0.0d;
                    }
                }
                cell.x = x;
                cell.y = y;
                cell.width = cell.widget().width;
                cell.height = cell.widget().height;
                cell.alignWidget();
                x += cell.width + pad;
            }
        }
    }
}
