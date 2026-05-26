package meteordevelopment.meteorclient.systems.hud;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.systems.hud.screens.HudEditorScreen;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.other.Snapper;
import net.minecraft.class_2487;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/HudElement.class */
public abstract class HudElement implements Snapper.Element, ISerializable<HudElement> {
    public final HudElementInfo<?> info;
    public int x;
    public int y;
    public final Settings settings = new Settings();
    public final HudBox box = new HudBox(this);
    public boolean autoAnchors = true;
    private boolean active = true;

    public HudElement(HudElementInfo<?> info) {
        this.info = info;
    }

    public boolean isActive() {
        return this.active;
    }

    public void toggle() {
        this.active = !this.active;
    }

    public void setSize(double width, double height) {
        this.box.setSize(width, height);
    }

    @Override // meteordevelopment.meteorclient.utils.other.Snapper.Element
    public void setPos(int x, int y) {
        if (this.autoAnchors) {
            this.box.setPos(x, y);
            this.box.xAnchor = XAnchor.Left;
            this.box.yAnchor = YAnchor.Top;
            this.box.updateAnchors();
        } else {
            this.box.setPos(this.box.x + (x - this.x), this.box.y + (y - this.y));
        }
        updatePos();
    }

    @Override // meteordevelopment.meteorclient.utils.other.Snapper.Element
    public void move(int deltaX, int deltaY) {
        this.box.move(deltaX, deltaY);
        updatePos();
    }

    public void updatePos() {
        this.x = this.box.getRenderX();
        this.y = this.box.getRenderY();
    }

    protected double alignX(double width, Alignment alignment) {
        return this.box.alignX(getWidth(), width, alignment);
    }

    @Override // meteordevelopment.meteorclient.utils.other.Snapper.Element
    public int getX() {
        return this.x;
    }

    @Override // meteordevelopment.meteorclient.utils.other.Snapper.Element
    public int getY() {
        return this.y;
    }

    @Override // meteordevelopment.meteorclient.utils.other.Snapper.Element
    public int getWidth() {
        return this.box.width;
    }

    @Override // meteordevelopment.meteorclient.utils.other.Snapper.Element
    public int getHeight() {
        return this.box.height;
    }

    protected boolean isInEditor() {
        return !Utils.canUpdate() || HudEditorScreen.isOpen();
    }

    public void remove() {
        Hud.get().remove(this);
    }

    public void tick(HudRenderer renderer) {
    }

    public void render(HudRenderer renderer) {
    }

    public void onFontChanged() {
    }

    public WWidget getWidget(GuiTheme theme) {
        return null;
    }

    @Override // meteordevelopment.meteorclient.utils.misc.ISerializable
    public class_2487 toTag() {
        class_2487 tag = new class_2487();
        tag.method_10582("name", this.info.name);
        tag.method_10556("active", this.active);
        tag.method_10566("settings", this.settings.toTag());
        tag.method_10566("box", this.box.toTag());
        tag.method_10556("autoAnchors", this.autoAnchors);
        return tag;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // meteordevelopment.meteorclient.utils.misc.ISerializable
    /* JADX INFO: renamed from: fromTag */
    public HudElement fromTag2(class_2487 tag) {
        this.settings.reset();
        this.active = ((Boolean) tag.method_10577("active").orElse(false)).booleanValue();
        this.settings.fromTag2((class_2487) tag.method_10562("settings").orElse(new class_2487()));
        this.box.fromTag2((class_2487) tag.method_10562("box").orElse(new class_2487()));
        this.autoAnchors = ((Boolean) tag.method_10577("autoAnchors").orElse(false)).booleanValue();
        this.x = this.box.getRenderX();
        this.y = this.box.getRenderY();
        return this;
    }
}
