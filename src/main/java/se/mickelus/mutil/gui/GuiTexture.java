package se.mickelus.mutil.gui;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public class GuiTexture extends GuiElement {

    protected Identifier textureLocation;

    protected int textureWidth = 256;
    protected int textureHeight = 256;
    protected int textureX;
    protected int textureY;

    protected int color = 0xffffff;
    private boolean useDefaultBlending = true;

    public GuiTexture(int x, int y, int width, int height, Identifier textureLocation) {
        this(x, y, width, height, 0, 0, textureLocation);
    }

    public GuiTexture(int x, int y, int width, int height, int textureX, int textureY, Identifier textureLocation) {
        super(x, y, width, height);

        this.textureX = textureX;
        this.textureY = textureY;

        this.textureLocation = textureLocation;
    }

    public GuiTexture setTextureCoordinates(int x, int y) {
        textureX = x;
        textureY = y;
        return this;
    }

    public GuiTexture setColor(int color) {
        this.color = color;
        return this;
    }

    public GuiTexture setSpriteSize(int width, int height) {
        this.textureWidth = width;
        this.textureHeight = height;
        return this;
    }

    public GuiTexture setUseDefaultBlending(boolean useDefault) {
        this.useDefaultBlending = useDefault;
        return this;
    }

    @Override
    public void draw(final GuiGraphicsExtractor graphics, int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY,
            float opacity) {
        super.draw(graphics, refX, refY, screenWidth, screenHeight, mouseX, mouseY, opacity);

        drawTexture(graphics, textureLocation, refX + x, refY + y, width, height, textureX, textureY, color, getOpacity() * opacity);
    }

    protected void drawTexture(final GuiGraphicsExtractor graphics, Identifier textureLocation, int x, int y, int width, int height,
            int u, int v, int color, float opacity) {
        // RenderSystem.setShaderColor is gone with the Blaze3D pipeline rewrite. The tint is not
        // lost: blit carries an ARGB colour itself now, which is the same result without leaving
        // global state set for whatever draws next.
        int argb = (Math.round(opacity * 255) << 24) | (color & 0xFFFFFF);
        graphics.blit(RenderPipelines.GUI_TEXTURED, textureLocation, x, y, u, v, width, height,
                textureWidth, textureHeight, argb);
    }
}
