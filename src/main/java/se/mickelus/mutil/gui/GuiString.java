package se.mickelus.mutil.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import se.mickelus.mutil.gui.animation.KeyframeAnimation;

public class GuiString extends GuiElement {

    protected String string;

    protected Font fontRenderer;

    protected int color = 0xffffffff;
    protected boolean drawShadow = true;

    protected boolean fixedWidth = false;

    public GuiString(int x, int y, String string) {
        super(x, y, 0, 9);

        fontRenderer = Minecraft.getInstance().font;

        this.string = string;
        width = fontRenderer.width(string);
    }

    public GuiString(int x, int y, int width, String string) {
        super(x, y, width, 9);

        fixedWidth = true;

        fontRenderer = Minecraft.getInstance().font;

        this.string = fontRenderer.plainSubstrByWidth(string, width);
    }

    public GuiString(int x, int y, String string, GuiAttachment attachment) {
        this(x, y, string);

        attachmentPoint = attachment;
    }

    public GuiString(int x, int y, String string, int color) {
        this(x, y, string);

        this.color = color;
    }

    public GuiString(int x, int y, String string, int color, GuiAttachment attachment) {
        this(x, y, string, attachment);

        this.color = color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setString(String string) {
        if (string != null && !string.equals(this.string)) {
            if (fixedWidth) {
                this.string = fontRenderer.plainSubstrByWidth(string, width);
            } else {
                this.string = string;
                width = fontRenderer.width(string);
            }
        }
    }

    public GuiString setShadow(boolean shadow) {
        drawShadow = shadow;
        return this;
    }

    @Override
    public void draw(final GuiGraphicsExtractor graphics, int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        activeAnimations.removeIf(keyframeAnimation -> !keyframeAnimation.isActive());
        activeAnimations.forEach(KeyframeAnimation::preDraw);
        drawString(graphics, string, refX + x, refY + y, color, opacity * getOpacity(), drawShadow);
    }

    /**
     * Font.drawInBatch is the world space counterpart of the screen text call. It takes a matrix
     * and a buffer source rather than writing into the gui render state, which is exactly the
     * difference that stopped this drawing on a block face.
     */
    @Override
    public void drawWorld(final PoseStack pose, final MultiBufferSource buffers, int refX, int refY, float opacity, int light) {
        activeAnimations.removeIf(keyframeAnimation -> !keyframeAnimation.isActive());
        activeAnimations.forEach(KeyframeAnimation::preDraw);

        int argb = colorWithOpacity(color, opacity * getOpacity());
        // The vanilla font flips a nearly transparent colour back to opaque, same guard as on screen.
        if ((argb & -67108864) == 0) {
            return;
        }

        fontRenderer.drawInBatch(string, refX + x, refY + y, argb, drawShadow, pose.last().pose(), buffers,
                Font.DisplayMode.NORMAL, 0, light);
    }

    protected void drawString(final GuiGraphicsExtractor graphics, String text, int x, int y, int color, float opacity, boolean drawShadow) {
        color = colorWithOpacity(color, opacity);

        // if the vanilla fontrender considers the color to be almost transparent (0xfc) it flips the opacity back to 1
        if ((color & -67108864) != 0) {
            graphics.text(fontRenderer, text, x, y, color, drawShadow);
        }
    }
}
