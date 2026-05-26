package meteordevelopment.meteorclient.gui.renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.operations.TextOperation;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.gui.renderer.packer.TexturePacker;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.renderer.Texture;
import meteordevelopment.meteorclient.utils.PostInit;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_3532;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/gui/renderer/GuiRenderer.class */
public class GuiRenderer {
    private static final Color WHITE = new Color(255, 255, 255);
    private static final TexturePacker TEXTURE_PACKER = new TexturePacker();
    private static Texture TEXTURE;
    public static GuiTexture CIRCLE;
    public static GuiTexture TRIANGLE;
    public static GuiTexture EDIT;
    public static GuiTexture RESET;
    public static GuiTexture FAVORITE_NO;
    public static GuiTexture FAVORITE_YES;
    public static GuiTexture COPY;
    public static GuiTexture PASTE;
    public GuiTheme theme;
    private final Renderer2D r = new Renderer2D(false);
    private final Renderer2D rTex = new Renderer2D(true);
    private final Pool<Scissor> scissorPool = new Pool<>(Scissor::new);
    private final Stack<Scissor> scissorStack = new Stack<>();
    private final Pool<TextOperation> textPool = new Pool<>(TextOperation::new);
    private final List<TextOperation> texts = new ArrayList();
    private final List<Runnable> postTasks = new ArrayList();
    public String tooltip;
    public String lastTooltip;
    public WWidget tooltipWidget;
    private double tooltipAnimProgress;
    private class_332 drawContext;

    public static GuiTexture addTexture(class_2960 id) {
        return TEXTURE_PACKER.add(id);
    }

    @PostInit
    public static void init() {
        CIRCLE = addTexture(MeteorClient.identifier("textures/icons/gui/circle.png"));
        TRIANGLE = addTexture(MeteorClient.identifier("textures/icons/gui/triangle.png"));
        EDIT = addTexture(MeteorClient.identifier("textures/icons/gui/edit.png"));
        RESET = addTexture(MeteorClient.identifier("textures/icons/gui/reset.png"));
        FAVORITE_NO = addTexture(MeteorClient.identifier("textures/icons/gui/favorite_no.png"));
        FAVORITE_YES = addTexture(MeteorClient.identifier("textures/icons/gui/favorite_yes.png"));
        COPY = addTexture(MeteorClient.identifier("textures/icons/gui/copy.png"));
        PASTE = addTexture(MeteorClient.identifier("textures/icons/gui/paste.png"));
        TEXTURE = TEXTURE_PACKER.pack();
    }

    public void begin(class_332 drawContext) {
        this.drawContext = drawContext;
        scissorStart(0.0d, 0.0d, Utils.getWindowWidth(), Utils.getWindowHeight());
    }

    public void end() {
        scissorEnd();
        for (Runnable task : this.postTasks) {
            task.run();
        }
        this.postTasks.clear();
    }

    public void beginRender() {
        this.r.begin();
        this.rTex.begin();
    }

    public void endRender() {
        endRender(null);
    }

    public void endRender(Scissor scissor) {
        if (scissor != null) {
            scissor.push();
        }
        this.r.end();
        this.rTex.end();
        this.r.render();
        TEXTURE.bind();
        this.rTex.render();
        this.theme.textRenderer().begin(this.theme.scale(1.0d));
        for (TextOperation text : this.texts) {
            if (!text.title) {
                text.run(this.textPool);
            }
        }
        this.theme.textRenderer().end();
        this.theme.textRenderer().begin(this.theme.scale(1.25d));
        for (TextOperation text2 : this.texts) {
            if (text2.title) {
                text2.run(this.textPool);
            }
        }
        this.theme.textRenderer().end();
        this.texts.clear();
        if (scissor != null) {
            scissor.pop();
        }
    }

    public void scissorStart(double x, double y, double width, double height) {
        if (!this.scissorStack.isEmpty()) {
            Scissor parent = this.scissorStack.peek();
            if (x < parent.x) {
                x = parent.x;
            } else if (x + width > parent.x + parent.width) {
                width -= (x + width) - ((double) (parent.x + parent.width));
            }
            if (y < parent.y) {
                y = parent.y;
            } else if (y + height > parent.y + parent.height) {
                height -= (y + height) - ((double) (parent.y + parent.height));
            }
            endRender(parent);
        }
        this.scissorStack.push(this.scissorPool.get().set(x, y, width, height));
        beginRender();
    }

    public void scissorEnd() {
        Scissor scissor = this.scissorStack.pop();
        endRender(scissor);
        scissor.push();
        for (Runnable task : scissor.postTasks) {
            task.run();
        }
        scissor.pop();
        if (!this.scissorStack.isEmpty()) {
            beginRender();
        }
        this.scissorPool.free(scissor);
    }

    public boolean renderTooltip(class_332 drawContext, double mouseX, double mouseY, double delta) {
        this.tooltipAnimProgress += ((double) (this.tooltip != null ? 1 : -1)) * delta * 14.0d;
        this.tooltipAnimProgress = class_3532.method_15350(this.tooltipAnimProgress, 0.0d, 1.0d);
        boolean toReturn = false;
        if (this.tooltipAnimProgress > 0.0d) {
            if (this.tooltip != null && !this.tooltip.equals(this.lastTooltip)) {
                this.tooltipWidget = this.theme.tooltip(this.tooltip);
                this.tooltipWidget.init();
            }
            this.tooltipWidget.move((-this.tooltipWidget.x) + mouseX + 12.0d, (-this.tooltipWidget.y) + mouseY + 12.0d);
            setAlpha(this.tooltipAnimProgress);
            begin(drawContext);
            this.tooltipWidget.render(this, mouseX, mouseY, delta);
            end();
            setAlpha(1.0d);
            this.lastTooltip = this.tooltip;
            toReturn = true;
        }
        this.tooltip = null;
        return toReturn;
    }

    public void setAlpha(double a) {
        this.r.setAlpha(a);
        this.rTex.setAlpha(a);
        this.theme.textRenderer().setAlpha(a);
    }

    public void tooltip(String text) {
        this.tooltip = text;
    }

    public void quad(double x, double y, double width, double height, Color cTopLeft, Color cTopRight, Color cBottomRight, Color cBottomLeft) {
        this.r.quad(x, y, width, height, cTopLeft, cTopRight, cBottomRight, cBottomLeft);
    }

    public void quad(double x, double y, double width, double height, Color colorLeft, Color colorRight) {
        quad(x, y, width, height, colorLeft, colorRight, colorRight, colorLeft);
    }

    public void quad(double x, double y, double width, double height, Color color) {
        quad(x, y, width, height, color, color);
    }

    public void quad(WWidget widget, Color color) {
        quad(widget.x, widget.y, widget.width, widget.height, color);
    }

    public void quad(double x, double y, double width, double height, GuiTexture texture, Color color) {
        this.rTex.texQuad(x, y, width, height, texture.get(width, height), color);
    }

    public void rotatedQuad(double x, double y, double width, double height, double rotation, GuiTexture texture, Color color) {
        this.rTex.texQuad(x, y, width, height, rotation, texture.get(width, height), color);
    }

    public void triangle(double x1, double y1, double x2, double y2, double x3, double y3, Color color) {
        this.r.triangle(x1, y1, x2, y2, x3, y3, color);
    }

    public void text(String text, double x, double y, Color color, boolean title) {
        this.texts.add(((TextOperation) getOp(this.textPool, x, y, color)).set(text, this.theme.textRenderer(), title));
    }

    public void texture(double x, double y, double width, double height, double rotation, Texture texture) {
        post(() -> {
            this.rTex.begin();
            this.rTex.texQuad(x, y, width, height, rotation, 0.0d, 0.0d, 1.0d, 1.0d, WHITE);
            this.rTex.end();
            texture.bind();
            this.rTex.render();
        });
    }

    public void post(Runnable task) {
        this.scissorStack.peek().postTasks.add(task);
    }

    public void item(class_1799 itemStack, int x, int y, float scale, boolean overlay) {
        RenderUtils.drawItem(this.drawContext, itemStack, x, y, scale, overlay);
    }

    public void absolutePost(Runnable task) {
        this.postTasks.add(task);
    }

    private <T extends GuiRenderOperation<T>> T getOp(Pool<T> pool, double x, double y, Color color) {
        T op = pool.get();
        op.set(x, y, color);
        return op;
    }
}
