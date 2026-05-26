package meteordevelopment.meteorclient.utils.render.prompts;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.systems.config.Config;
import net.minecraft.class_437;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/render/prompts/Prompt.class */
public abstract class Prompt<T> {
    protected final GuiTheme theme;
    protected final class_437 parent;
    protected String title = "";
    protected final List<String> messages = new ArrayList();
    protected boolean dontShowAgainCheckboxVisible = true;
    protected String id = null;

    protected abstract void initialiseWidgets(Prompt<T>.PromptScreen promptScreen);

    protected Prompt(GuiTheme theme, class_437 parent) {
        this.theme = theme;
        this.parent = parent;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public T title(String title) {
        this.title = title;
        return this;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public T message(String message) {
        this.messages.add(message);
        return this;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public T message(String message, Object... args) {
        this.messages.add(String.format(message, args));
        return this;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public T dontShowAgainCheckboxVisible(boolean visible) {
        this.dontShowAgainCheckboxVisible = visible;
        return this;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public T id(String from) {
        this.id = from;
        return this;
    }

    public boolean show() {
        if (this.id != null && Config.get().dontShowAgainPrompts.contains(this.id)) {
            return false;
        }
        if (!RenderSystem.isOnRenderThread()) {
            MeteorClient.mc.execute(() -> {
                MeteorClient.mc.method_1507(new PromptScreen(this.theme));
            });
            return true;
        }
        MeteorClient.mc.method_1507(new PromptScreen(this.theme));
        return true;
    }

    protected void dontShowAgain(Prompt<T>.PromptScreen screen) {
        if (screen.dontShowAgainCheckbox != null && screen.dontShowAgainCheckbox.checked && this.id != null) {
            Config.get().dontShowAgainPrompts.add(this.id);
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/render/prompts/Prompt$PromptScreen.class */
    protected class PromptScreen extends WindowScreen {
        protected WCheckbox dontShowAgainCheckbox;
        protected WHorizontalList list;

        public PromptScreen(GuiTheme theme) {
            super(theme, Prompt.this.title);
            this.parent = Prompt.this.parent;
        }

        @Override // meteordevelopment.meteorclient.gui.WidgetScreen
        public void initWidgets() {
            for (String line : Prompt.this.messages) {
                add(this.theme.label(line)).expandX();
            }
            add(this.theme.horizontalSeparator()).expandX();
            if (Prompt.this.dontShowAgainCheckboxVisible) {
                WHorizontalList checkboxContainer = (WHorizontalList) add(this.theme.horizontalList()).expandX().widget();
                this.dontShowAgainCheckbox = (WCheckbox) checkboxContainer.add(this.theme.checkbox(false)).widget();
                checkboxContainer.add(this.theme.label("Don't show this again.")).expandX();
            } else {
                this.dontShowAgainCheckbox = null;
            }
            this.list = (WHorizontalList) add(this.theme.horizontalList()).expandX().widget();
            Prompt.this.initialiseWidgets(this);
        }
    }
}
