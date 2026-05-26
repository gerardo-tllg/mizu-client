package meteordevelopment.meteorclient.utils.render.prompts;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import net.minecraft.class_437;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/render/prompts/OkPrompt.class */
public class OkPrompt extends Prompt<OkPrompt> {
    private Runnable onOk;

    private OkPrompt(GuiTheme theme, class_437 parent) {
        super(theme, parent);
        this.onOk = () -> {
        };
    }

    public static OkPrompt create() {
        return new OkPrompt(GuiThemes.get(), MeteorClient.mc.field_1755);
    }

    public static OkPrompt create(GuiTheme theme, class_437 parent) {
        return new OkPrompt(theme, parent);
    }

    public OkPrompt onOk(Runnable action) {
        this.onOk = action;
        return this;
    }

    @Override // meteordevelopment.meteorclient.utils.render.prompts.Prompt
    protected void initialiseWidgets(Prompt<OkPrompt>.PromptScreen screen) {
        WButton okButton = (WButton) screen.list.add(this.theme.button("Ok")).expandX().widget();
        okButton.action = () -> {
            dontShowAgain(screen);
            this.onOk.run();
            screen.method_25419();
        };
    }
}
