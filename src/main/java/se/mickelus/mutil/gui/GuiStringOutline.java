package se.mickelus.mutil.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import se.mickelus.mutil.gui.animation.KeyframeAnimation;

public class GuiStringOutline extends GuiString {
    private String cleanString;

    public GuiStringOutline(int x, int y, String string) {
        super(x, y, string);
        drawShadow = false;

        cleanString = ChatFormatting.stripFormatting(this.string);
    }

    public GuiStringOutline(int x, int y, int width, String string) {
        super(x, y, width, string);
        drawShadow = false;

        cleanString = ChatFormatting.stripFormatting(this.string);
    }

    public GuiStringOutline(int x, int y, String string, GuiAttachment attachment) {
        super(x, y, string, attachment);
        drawShadow = false;

        cleanString = ChatFormatting.stripFormatting(this.string);
    }

    public GuiStringOutline(int x, int y, String string, int color) {
        super(x, y, string, color);
        drawShadow = false;

        cleanString = ChatFormatting.stripFormatting(this.string);
    }

    public GuiStringOutline(int x, int y, String string, int color, GuiAttachment attachment) {
        super(x, y, string, color, attachment);
        drawShadow = false;

        cleanString = ChatFormatting.stripFormatting(this.string);
    }

    @Override
    public void setString(String string) {
        super.setString(string);

        cleanString = ChatFormatting.stripFormatting(this.string);
    }

    /**
     * The outline in one call rather than nine.
     *
     * On screen this draws the text eight times in black and once in colour. In the world the font
     * does the same thing itself, and doing it by hand would mean nine batches of geometry on a
     * block face for one number.
     */
    @Override
    public void drawWorld(final PoseStack pose, final MultiBufferSource buffers, int refX, int refY, float opacity, int light) {
        activeAnimations.removeIf(keyframeAnimation -> !keyframeAnimation.isActive());
        activeAnimations.forEach(KeyframeAnimation::preDraw);

        int argb = colorWithOpacity(color, opacity * getOpacity());
        if ((argb & -67108864) == 0) {
            return;
        }

        fontRenderer.drawInBatch8xOutline(FormattedCharSequence.forward(cleanString, Style.EMPTY),
                refX + x, refY + y, argb, 0, pose.last().pose(), buffers, light);
    }

    @Override
    protected void drawString(final GuiGraphicsExtractor graphics, String text, int x, int y, int color, float opacity, boolean drawShadow) {

        graphics.pose().pushMatrix();
        super.drawString(graphics, cleanString, x - 1, y - 1, 0, opacity, false);
        super.drawString(graphics, cleanString, x, y - 1, 0, opacity, false);
        super.drawString(graphics, cleanString, x + 1, y - 1, 0, opacity, false);

        super.drawString(graphics, cleanString, x - 1, y + 1, 0, opacity, false);
        super.drawString(graphics, cleanString, x, y + 1, 0, opacity, false);
        super.drawString(graphics, cleanString, x + 1, y + 1, 0, opacity, false);

        super.drawString(graphics, cleanString, x + 1, y, 0, opacity, false);
        super.drawString(graphics, cleanString, x - 1, y, 0, opacity, false);

        // magic offset to avoid z-fighting for in-world rendering
        graphics.pose().translate(0, 0);
        super.drawString(graphics, text, x, y, color, opacity, false);
        graphics.pose().popMatrix();
    }
}
