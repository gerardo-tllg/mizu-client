package meteordevelopment.meteorclient.gui.widgets.input;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.InteractBlockEvent;
import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_2338;
import net.minecraft.class_239;
import net.minecraft.class_437;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/widgets/input/WBlockPosEdit.class */
public class WBlockPosEdit extends WHorizontalList {
    public Runnable action;
    public Runnable actionOnRelease;
    private WTextBox textBoxX;
    private WTextBox textBoxY;
    private WTextBox textBoxZ;
    private class_437 previousScreen;
    private class_2338 value;
    private class_2338 lastValue;
    private boolean clicking;

    public WBlockPosEdit(class_2338 value) {
        this.value = value;
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public void init() {
        addTextBox();
        if (Utils.canUpdate()) {
            WButton click = (WButton) add(this.theme.button("Click")).expandX().widget();
            click.action = () -> {
                this.clicking = true;
                MeteorClient.EVENT_BUS.subscribe(this);
                this.previousScreen = MeteorClient.mc.field_1755;
                MeteorClient.mc.method_1507((class_437) null);
            };
            WButton here = (WButton) add(this.theme.button("Set Here")).expandX().widget();
            here.action = () -> {
                this.lastValue = this.value;
                set(new class_2338(MeteorClient.mc.field_1724.method_24515()));
                newValueCheck();
                clear();
                init();
            };
        }
    }

    @EventHandler
    private void onStartBreakingBlock(StartBreakingBlockEvent event) {
        if (this.clicking) {
            this.clicking = false;
            event.cancel();
            MeteorClient.EVENT_BUS.unsubscribe(this);
            MeteorClient.mc.method_1507(this.previousScreen);
        }
    }

    @EventHandler
    private void onInteractBlock(InteractBlockEvent event) {
        if (!this.clicking || event.result.method_17783() == class_239.class_240.field_1333) {
            return;
        }
        this.lastValue = this.value;
        set(event.result.method_17777());
        newValueCheck();
        clear();
        init();
        this.clicking = false;
        event.cancel();
        MeteorClient.EVENT_BUS.unsubscribe(this);
        MeteorClient.mc.method_1507(this.previousScreen);
    }

    private boolean filter(String text, char c) {
        boolean good;
        boolean validate = true;
        if (c == '-' && text.isEmpty()) {
            good = true;
            validate = false;
        } else {
            good = Character.isDigit(c);
        }
        if (good && validate) {
            try {
                Integer.parseInt(text + c);
            } catch (NumberFormatException e) {
                good = false;
            }
        }
        return good;
    }

    public class_2338 get() {
        return this.value;
    }

    public void set(class_2338 value) {
        this.value = value;
    }

    private void addTextBox() {
        this.textBoxX = (WTextBox) add(this.theme.textBox(Integer.toString(this.value.method_10263()), this::filter)).minWidth(75.0d).widget();
        this.textBoxY = (WTextBox) add(this.theme.textBox(Integer.toString(this.value.method_10264()), this::filter)).minWidth(75.0d).widget();
        this.textBoxZ = (WTextBox) add(this.theme.textBox(Integer.toString(this.value.method_10260()), this::filter)).minWidth(75.0d).widget();
        this.textBoxX.actionOnUnfocused = () -> {
            this.lastValue = this.value;
            if (this.textBoxX.get().isEmpty()) {
                set(new class_2338(0, 0, 0));
            } else {
                try {
                    set(new class_2338(Integer.parseInt(this.textBoxX.get()), this.value.method_10264(), this.value.method_10260()));
                } catch (NumberFormatException e) {
                }
            }
            newValueCheck();
        };
        this.textBoxY.actionOnUnfocused = () -> {
            this.lastValue = this.value;
            if (this.textBoxY.get().isEmpty()) {
                set(new class_2338(0, 0, 0));
            } else {
                try {
                    set(new class_2338(this.value.method_10263(), Integer.parseInt(this.textBoxY.get()), this.value.method_10260()));
                } catch (NumberFormatException e) {
                }
            }
            newValueCheck();
        };
        this.textBoxZ.actionOnUnfocused = () -> {
            this.lastValue = this.value;
            if (this.textBoxZ.get().isEmpty()) {
                set(new class_2338(0, 0, 0));
            } else {
                try {
                    set(new class_2338(this.value.method_10263(), this.value.method_10264(), Integer.parseInt(this.textBoxZ.get())));
                } catch (NumberFormatException e) {
                }
            }
            newValueCheck();
        };
    }

    private void newValueCheck() {
        if (this.value != this.lastValue) {
            if (this.action != null) {
                this.action.run();
            }
            if (this.actionOnRelease != null) {
                this.actionOnRelease.run();
            }
        }
    }
}
