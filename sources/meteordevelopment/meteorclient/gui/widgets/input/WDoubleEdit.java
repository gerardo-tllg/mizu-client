package meteordevelopment.meteorclient.gui.widgets.input;

import java.util.Locale;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/widgets/input/WDoubleEdit.class */
public class WDoubleEdit extends WHorizontalList {
    private double value;
    private final double min;
    private final double max;
    private final double sliderMin;
    private final double sliderMax;
    public int decimalPlaces;
    public boolean noSlider;
    public boolean small = false;
    public Runnable action;
    public Runnable actionOnRelease;
    private WTextBox textBox;
    private WSlider slider;

    public WDoubleEdit(double value, double min, double max, double sliderMin, double sliderMax, int decimalPlaces, boolean noSlider) {
        this.noSlider = false;
        this.value = value;
        this.min = min;
        this.max = max;
        this.decimalPlaces = decimalPlaces;
        this.sliderMin = sliderMin;
        this.sliderMax = sliderMax;
        if (noSlider || (sliderMin == 0.0d && sliderMax == 0.0d)) {
            this.noSlider = true;
        }
    }

    @Override // meteordevelopment.meteorclient.gui.widgets.WWidget
    public void init() {
        this.textBox = (WTextBox) add(this.theme.textBox(valueString(), this::filter)).minWidth(75.0d).widget();
        if (this.noSlider) {
            ((WButton) add(this.theme.button("+")).widget()).action = () -> {
                setButton(get() + 1.0d);
            };
            ((WButton) add(this.theme.button("-")).widget()).action = () -> {
                setButton(get() - 1.0d);
            };
        } else {
            this.slider = (WSlider) add(this.theme.slider(this.value, this.sliderMin, this.sliderMax)).minWidth(this.small ? 125.0d - this.spacing : 200.0d).centerY().expandX().widget();
        }
        this.textBox.actionOnUnfocused = () -> {
            double lastValue = this.value;
            if (this.textBox.get().isEmpty() || this.textBox.get().equals("-") || this.textBox.get().equals(".") || this.textBox.get().equals("-.")) {
                this.value = 0.0d;
            } else {
                try {
                    this.value = Double.parseDouble(this.textBox.get());
                } catch (NumberFormatException e) {
                }
            }
            double preValidationValue = this.value;
            if (this.value < this.min) {
                this.value = this.min;
            } else if (this.value > this.max) {
                this.value = this.max;
            }
            if (this.value != preValidationValue) {
                this.textBox.set(valueString());
            }
            if (this.slider != null) {
                this.slider.set(this.value);
            }
            if (this.value != lastValue) {
                if (this.action != null) {
                    this.action.run();
                }
                if (this.actionOnRelease != null) {
                    this.actionOnRelease.run();
                }
            }
        };
        if (this.slider != null) {
            this.slider.action = () -> {
                double lastValue = this.value;
                this.value = this.slider.get();
                this.textBox.set(valueString());
                if (this.action == null || this.value == lastValue) {
                    return;
                }
                this.action.run();
            };
            this.slider.actionOnRelease = () -> {
                if (this.actionOnRelease != null) {
                    this.actionOnRelease.run();
                }
            };
        }
    }

    private boolean filter(String text, char c) {
        boolean good;
        boolean validate = true;
        if (c == '-' && !text.contains("-") && this.textBox.cursor == 0) {
            good = true;
            validate = false;
        } else if (c == '.' && !text.contains(".")) {
            good = true;
            if (text.isEmpty()) {
                validate = false;
            }
        } else {
            good = Character.isDigit(c);
        }
        if (good && validate) {
            try {
                Double.parseDouble(text + c);
            } catch (NumberFormatException e) {
                good = false;
            }
        }
        return good;
    }

    private void setButton(double v) {
        if (this.value == v) {
            return;
        }
        if (v < this.min) {
            this.value = this.min;
        } else {
            this.value = Math.min(v, this.max);
        }
        if (this.value == v) {
            this.textBox.set(valueString());
            if (this.slider != null) {
                this.slider.set(this.value);
            }
            if (this.action != null) {
                this.action.run();
            }
            if (this.actionOnRelease != null) {
                this.actionOnRelease.run();
            }
        }
    }

    public double get() {
        return this.value;
    }

    public void set(double value) {
        this.value = value;
        this.textBox.set(valueString());
        if (this.slider != null) {
            this.slider.set(value);
        }
    }

    private String valueString() {
        return String.format(Locale.US, "%." + this.decimalPlaces + "f", Double.valueOf(this.value));
    }
}
