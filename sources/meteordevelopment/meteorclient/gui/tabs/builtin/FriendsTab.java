package meteordevelopment.meteorclient.gui.tabs.builtin;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.WindowTabScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPlus;
import meteordevelopment.meteorclient.systems.friends.Friend;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import net.minecraft.class_437;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/tabs/builtin/FriendsTab.class */
public class FriendsTab extends Tab {
    public FriendsTab() {
        super("Friends");
    }

    @Override // meteordevelopment.meteorclient.gui.tabs.Tab
    public TabScreen createScreen(GuiTheme theme) {
        return new FriendsScreen(theme, this);
    }

    @Override // meteordevelopment.meteorclient.gui.tabs.Tab
    public boolean isScreen(class_437 screen) {
        return screen instanceof FriendsScreen;
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/tabs/builtin/FriendsTab$FriendsScreen.class */
    private static class FriendsScreen extends WindowTabScreen {
        public FriendsScreen(GuiTheme theme, Tab tab) {
            super(theme, tab);
        }

        @Override // meteordevelopment.meteorclient.gui.WidgetScreen
        public void initWidgets() {
            WTable table = (WTable) add(this.theme.table()).expandX().minWidth(400.0d).widget();
            initTable(table);
            add(this.theme.horizontalSeparator()).expandX();
            WHorizontalList list = (WHorizontalList) add(this.theme.horizontalList()).expandX().widget();
            WTextBox nameW = (WTextBox) list.add(this.theme.textBox("", (text, c) -> {
                return c != ' ';
            })).expandX().widget();
            nameW.setFocused(true);
            WPlus add = (WPlus) list.add(this.theme.plus()).widget();
            add.action = () -> {
                String name = nameW.get().trim();
                Friend friend = new Friend(name);
                if (Friends.get().add(friend)) {
                    nameW.set("");
                    reload();
                    MeteorExecutor.execute(() -> {
                        friend.updateInfo();
                        reload();
                    });
                }
            };
            this.enterAction = add.action;
        }

        private void initTable(WTable table) {
            table.clear();
            if (Friends.get().isEmpty()) {
                return;
            }
            Friends.get().forEach(friend -> {
                MeteorExecutor.execute(() -> {
                    if (friend.headTextureNeedsUpdate()) {
                        friend.updateInfo();
                        reload();
                    }
                });
            });
            for (Friend friend2 : Friends.get()) {
                table.add(this.theme.texture(32.0d, 32.0d, friend2.getHead().needsRotate() ? 90.0d : 0.0d, friend2.getHead()));
                table.add(this.theme.label(friend2.getName()));
                WMinus remove = (WMinus) table.add(this.theme.minus()).expandCellX().right().widget();
                remove.action = () -> {
                    Friends.get().remove(friend2);
                    reload();
                };
                table.row();
            }
        }

        @Override // meteordevelopment.meteorclient.gui.WidgetScreen
        public boolean toClipboard() {
            return NbtUtils.toClipboard(Friends.get());
        }

        @Override // meteordevelopment.meteorclient.gui.WidgetScreen
        public boolean fromClipboard() {
            return NbtUtils.fromClipboard(Friends.get());
        }
    }
}
