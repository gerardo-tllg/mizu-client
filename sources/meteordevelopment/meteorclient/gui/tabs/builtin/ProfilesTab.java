package meteordevelopment.meteorclient.gui.tabs.builtin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.WindowTabScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.systems.profiles.Profile;
import meteordevelopment.meteorclient.systems.profiles.Profiles;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import net.minecraft.class_437;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/tabs/builtin/ProfilesTab.class */
public class ProfilesTab extends Tab {
    public ProfilesTab() {
        super("Profiles");
    }

    @Override // meteordevelopment.meteorclient.gui.tabs.Tab
    public TabScreen createScreen(GuiTheme theme) {
        return new ProfilesScreen(theme, this);
    }

    @Override // meteordevelopment.meteorclient.gui.tabs.Tab
    public boolean isScreen(class_437 screen) {
        return screen instanceof ProfilesScreen;
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/tabs/builtin/ProfilesTab$ProfilesScreen.class */
    private static class ProfilesScreen extends WindowTabScreen {
        public ProfilesScreen(GuiTheme theme, Tab tab) {
            super(theme, tab);
        }

        @Override // meteordevelopment.meteorclient.gui.WidgetScreen
        public void initWidgets() {
            WTable table = (WTable) add(this.theme.table()).expandX().minWidth(400.0d).widget();
            initTable(table);
            add(this.theme.horizontalSeparator()).expandX();
            WButton create = (WButton) add(this.theme.button("Create")).expandX().widget();
            create.action = () -> {
                MeteorClient.mc.method_1507(new EditProfileScreen(this.theme, null, this::reload));
            };
        }

        private void initTable(WTable table) {
            table.clear();
            if (Profiles.get().isEmpty()) {
                return;
            }
            for (Profile profile : Profiles.get()) {
                table.add(this.theme.label(profile.name.get())).expandCellX();
                WButton save = (WButton) table.add(this.theme.button("Save")).widget();
                Objects.requireNonNull(profile);
                save.action = profile::save;
                WButton load = (WButton) table.add(this.theme.button("Load")).widget();
                Objects.requireNonNull(profile);
                load.action = profile::load;
                WButton edit = (WButton) table.add(this.theme.button(GuiRenderer.EDIT)).widget();
                edit.action = () -> {
                    MeteorClient.mc.method_1507(new EditProfileScreen(this.theme, profile, this::reload));
                };
                WMinus remove = (WMinus) table.add(this.theme.minus()).widget();
                remove.action = () -> {
                    Profiles.get().remove(profile);
                    reload();
                };
                table.row();
            }
        }

        @Override // meteordevelopment.meteorclient.gui.WidgetScreen
        public boolean toClipboard() {
            return NbtUtils.toClipboard(Profiles.get());
        }

        @Override // meteordevelopment.meteorclient.gui.WidgetScreen
        public boolean fromClipboard() {
            return NbtUtils.fromClipboard(Profiles.get());
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/tabs/builtin/ProfilesTab$EditProfileScreen.class */
    private static class EditProfileScreen extends WindowScreen {
        private WContainer settingsContainer;
        private final Profile profile;
        private final boolean isNew;
        private final Runnable action;

        public EditProfileScreen(GuiTheme theme, Profile profile, Runnable action) {
            super(theme, profile == null ? "New Profile" : "Edit Profile");
            this.isNew = profile == null;
            this.profile = this.isNew ? new Profile() : profile;
            this.action = action;
        }

        @Override // meteordevelopment.meteorclient.gui.WidgetScreen
        public void initWidgets() {
            this.settingsContainer = (WContainer) add(this.theme.verticalList()).expandX().minWidth(400.0d).widget();
            this.settingsContainer.add(this.theme.settings(this.profile.settings)).expandX();
            add(this.theme.horizontalSeparator()).expandX();
            WButton save = (WButton) add(this.theme.button(this.isNew ? "Create" : "Save")).expandX().widget();
            save.action = () -> {
                if (this.profile.name.get().isEmpty()) {
                    return;
                }
                if (this.isNew) {
                    for (Profile p : Profiles.get()) {
                        if (this.profile.equals(p)) {
                            return;
                        }
                    }
                }
                List<String> valid = new ArrayList<>();
                for (String address : this.profile.loadOnJoin.get()) {
                    if (Utils.resolveAddress(address)) {
                        valid.add(address);
                    }
                }
                this.profile.loadOnJoin.set(valid);
                if (this.isNew) {
                    Profiles.get().add(this.profile);
                } else {
                    Profiles.get().save();
                }
                method_25419();
            };
            this.enterAction = save.action;
        }

        public void method_25393() {
            this.profile.settings.tick(this.settingsContainer, this.theme);
        }

        @Override // meteordevelopment.meteorclient.gui.WidgetScreen
        protected void onClosed() {
            if (this.action != null) {
                this.action.run();
            }
        }
    }
}
