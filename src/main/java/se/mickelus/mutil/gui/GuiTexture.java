package se.mickelus.mutil.gui;

import com.mojang.blaze3d.systems.RenderSystem;
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
        RenderSystem.setShaderColor((color >> 16 & 255) / 255f, (color >> 8 & 255) / 255f, (color & 255) / 255f, opacity);
        graphics.blit(textureLocation, x, y, u, v, width, height, textureWidth, textureHeight);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }
}
