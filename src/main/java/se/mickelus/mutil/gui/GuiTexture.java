package se.mickelus.mutil.gui;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Matrix4f;

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

    /**
     * The same sprite, as a quad in the world rather than a blit onto the screen.
     *
     * entityTranslucent is the render type because these are transparent cutouts drawn on the face
     * of a block, and it takes the texture directly rather than through an atlas. The pose arrives
     * already rotated onto the face and scaled so that one gui pixel is one unit, so the corners
     * are just the element's own bounds.
     */
    @Override
    public void drawWorld(final PoseStack pose, final MultiBufferSource buffers, int refX, int refY, float opacity, int light) {
        super.drawWorld(pose, buffers, refX, refY, opacity, light);

        float alpha = getOpacity() * opacity;
        if (alpha <= 0) {
            return;
        }

        int x1 = refX + x;
        int y1 = refY + y;
        int x2 = x1 + width;
        int y2 = y1 + height;

        float u1 = (float) textureX / textureWidth;
        float v1 = (float) textureY / textureHeight;
        float u2 = (float) (textureX + width) / textureWidth;
        float v2 = (float) (textureY + height) / textureHeight;

        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int a = Math.round(alpha * 255);

        VertexConsumer consumer = buffers.getBuffer(RenderTypes.entityTranslucent(textureLocation));
        Matrix4f matrix = pose.last().pose();

        consumer.addVertex(matrix, x1, y2, 0).setColor(r, g, b, a).setUv(u1, v2)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(matrix, x2, y2, 0).setColor(r, g, b, a).setUv(u2, v2)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(matrix, x2, y1, 0).setColor(r, g, b, a).setUv(u2, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(matrix, x1, y1, 0).setColor(r, g, b, a).setUv(u1, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 0, 1);
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
