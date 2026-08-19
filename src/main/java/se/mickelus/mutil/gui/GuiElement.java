package se.mickelus.mutil.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import se.mickelus.mutil.gui.animation.KeyframeAnimation;

/**
 * Base of the gui toolkit. Every element draws its children, so the methods here run once per
 * element per frame and are written as loops rather than streams for that reason.
 */
public class GuiElement {
    protected int x;
    protected int y;
    protected GuiAttachment attachmentPoint = GuiAttachment.topLeft;
    protected GuiAttachment attachmentAnchor = GuiAttachment.topLeft;

    protected int width;
    protected int height;

    protected float opacity = 1;

    protected boolean hasFocus = false;

    protected boolean isVisible = true;

    protected boolean shouldRemove = false;

    protected ArrayList<GuiElement> elements;

    protected Set<KeyframeAnimation> activeAnimations;

    public GuiElement(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        elements = new ArrayList<>();

        activeAnimations = new HashSet<>();
    }

    public void draw(final GuiGraphicsExtractor graphics, int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY,
            float opacity) {
        drawChildren(graphics, refX + x, refY + y, screenWidth, screenHeight, mouseX, mouseY, opacity * this.opacity);
    }

    /**
     * The same tree, drawn into the world instead of onto the screen.
     *
     * A GuiGraphicsExtractor writes into a GuiRenderState that the gui renderer later draws in
     * screen space, so it cannot put anything on the side of a block. World space needs a pose and
     * a buffer source, which is what a block outline renderer is handed, so this is a second draw
     * path rather than a different graphics object.
     *
     * Only the leaves that the block overlay actually uses override it, which is GuiTexture and
     * GuiString. Everything else is a container and inherits the recursion below.
     */
    public void drawWorld(final PoseStack pose, final MultiBufferSource buffers, int refX, int refY, float opacity, int light) {
        drawChildrenWorld(pose, buffers, refX + x, refY + y, opacity * this.opacity, light);
    }

    /**
     * Layout is decided the same way as on screen, by the same attachment offsets, so an element
     * sits in the same place on a block face as it would in a menu.
     */
    protected void drawChildrenWorld(final PoseStack pose, final MultiBufferSource buffers, int refX, int refY, float opacity, int light) {
        elements.removeIf(GuiElement::shouldRemove);

        for (int i = 0; i < elements.size(); i++) {
            GuiElement element = elements.get(i);
            if (!element.isVisible()) {
                continue;
            }

            element.updateAnimations();
            element.drawWorld(pose, buffers,
                    refX + getXOffset(this, element.attachmentAnchor) - getXOffset(element, element.attachmentPoint),
                    refY + getYOffset(this, element.attachmentAnchor) - getYOffset(element, element.attachmentPoint),
                    opacity, light);
        }
    }

    public void updateAnimations() {
        activeAnimations.removeIf(animation -> !animation.isActive());
        activeAnimations.forEach(KeyframeAnimation::preDraw);
    }

    protected void drawChildren(final GuiGraphicsExtractor graphics, int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY,
            float opacity) {
        elements.removeIf(GuiElement::shouldRemove);

        // Indexed rather than a stream. This is the innermost thing the toolkit does, once per
        // element per frame, and the pipeline allocated a spliterator and a capturing lambda each
        // time. Re-reading size each step also means an element added while drawing is handled
        // rather than throwing.
        for (int i = 0; i < elements.size(); i++) {
            GuiElement element = elements.get(i);
            if (!element.isVisible()) {
                continue;
            }

            element.updateAnimations();
            element.draw(
                    graphics, refX + getXOffset(this, element.attachmentAnchor) - getXOffset(element, element.attachmentPoint),
                    refY + getYOffset(this, element.attachmentAnchor) - getYOffset(element, element.attachmentPoint),
                    screenWidth, screenHeight, mouseX, mouseY, opacity);
        }
    }

    protected static int getXOffset(GuiElement element, GuiAttachment attachment) {
        return switch (attachment) {
            case topLeft, middleLeft, bottomLeft -> 0;
            case topCenter, middleCenter, bottomCenter -> element.getWidth() / 2;
            case topRight, middleRight, bottomRight -> element.getWidth();
        };
    }

    protected static int getYOffset(GuiElement element, GuiAttachment attachment) {
        return switch (attachment) {
            case topLeft, topCenter, topRight -> 0;
            case middleLeft, middleCenter, middleRight -> element.getHeight() / 2;
            case bottomLeft, bottomCenter, bottomRight -> element.getHeight();
        };
    }

    public boolean onMouseClick(int x, int y, int button) {
        for (int i = elements.size() - 1; i >= 0; i--) {
            if (elements.get(i).isVisible()) {
                if (elements.get(i).onMouseClick(x, y, button)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void onMouseRelease(int x, int y, int button) {
        for (int i = 0; i < elements.size(); i++) {
            elements.get(i).onMouseRelease(x, y, button);
        }
    }

    public boolean onMouseScroll(double mouseX, double mouseY, double distance) {
        for (int i = elements.size() - 1; i >= 0; i--) {
            if (elements.get(i).isVisible()) {
                if (elements.get(i).onMouseScroll(mouseX, mouseY, distance)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
        for (int i = elements.size() - 1; i >= 0; i--) {
            if (elements.get(i).isVisible()) {
                if (elements.get(i).onKeyPress(keyCode, scanCode, modifiers)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean onKeyRelease(int keyCode, int scanCode, int modifiers) {
        for (int i = elements.size() - 1; i >= 0; i--) {
            if (elements.get(i).isVisible()) {
                if (elements.get(i).onKeyRelease(keyCode, scanCode, modifiers)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean onCharType(char character, int modifiers) {
        for (int i = elements.size() - 1; i >= 0; i--) {
            if (elements.get(i).isVisible()) {
                if (elements.get(i).onCharType(character, modifiers)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void updateFocusState(int refX, int refY, int mouseX, int mouseY) {
        // Runs every frame alongside the draw, so the same reasoning applies.
        for (int i = 0; i < elements.size(); i++) {
            GuiElement element = elements.get(i);
            if (!element.isVisible()) {
                continue;
            }

            element.updateFocusState(
                    refX + x + getXOffset(this, element.attachmentAnchor) - getXOffset(element, element.attachmentPoint),
                    refY + y + getYOffset(this, element.attachmentAnchor) - getYOffset(element, element.attachmentPoint),
                    mouseX, mouseY);
        }

        boolean gainFocus = mouseX >= getX() + refX
                && mouseX < getX() + refX + getWidth()
                && mouseY >= getY() + refY
                && mouseY < getY() + refY + getHeight();

        if (gainFocus != hasFocus) {
            hasFocus = gainFocus;
            if (hasFocus) {
                onFocus();
            }
            else {
                onBlur();
            }
        }
    }

    protected void onFocus() {

    }

    protected void onBlur() {

    }

    public boolean hasFocus() {
        return hasFocus;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    /**
     * Set which point, relative this element, that it should be positioned on.
     */
    public GuiElement setAttachmentPoint(GuiAttachment attachment) {
        attachmentPoint = attachment;

        return this;
    }

    /**
     * Set which point, relative the parent, that this element should be positioned on.
     */
    public GuiElement setAttachmentAnchor(GuiAttachment attachment) {
        attachmentAnchor = attachment;

        return this;
    }

    public GuiElement setAttachment(GuiAttachment attachment) {
        attachmentPoint = attachment;
        attachmentAnchor = attachment;

        return this;
    }

    public GuiAttachment getAttachmentPoint() {
        return attachmentPoint;
    }

    public GuiAttachment getAttachmentAnchor() {
        return attachmentAnchor;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setVisible(boolean visible) {
        if (isVisible != visible) {
            if (visible) {
                onShow();
            }
            else {
                if (!onHide()) {
                    return;
                }
            }
            isVisible = visible;
        }
    }

    public boolean isVisible() {
        return isVisible;
    }

    protected void onShow() {
    }

    /**
     * Can be overridden to do something when the element is hidden. Returning false indicates that the handler will
     * take care of setting isVisible to false.
     */
    protected boolean onHide() {
        this.hasFocus = false;
        return true;
    }

    public GuiElement setOpacity(float opacity) {
        this.opacity = opacity;
        return this;
    }

    public float getOpacity() {
        return opacity;
    }

    public void addAnimation(KeyframeAnimation animation) {
        activeAnimations.add(animation);
    }

    public void removeAnimation(KeyframeAnimation animation) {
        activeAnimations.remove(animation);
    }

    public void remove() {
        shouldRemove = true;
    }

    public boolean shouldRemove() {
        return shouldRemove;
    }

    public void addChild(GuiElement child) {
        this.elements.add(child);
    }

    public void clearChildren() {
        this.elements.clear();
    }

    public int getNumChildren() {
        return elements.size();
    }

    public GuiElement getChild(int index) {
        if (index >= 0 && index < elements.size()) {
            return elements.get(index);
        }
        return null;
    }

    public List<GuiElement> getChildren() {
        return Collections.unmodifiableList(elements);
    }

    /**
     * Return child elements which has the given type
     */
    public <T> List<T> getChildren(Class<T> type) {
        return elements.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .collect(Collectors.toList());
    }

    /**
     * The first tooltip any child offers, or null. Asked every frame while a screen is open.
     */
    public List<Component> getTooltipLines() {
        if (!isVisible()) {
            return null;
        }

        for (int i = 0; i < elements.size(); i++) {
            List<Component> lines = elements.get(i).getTooltipLines();
            if (lines != null) {
                return lines;
            }
        }

        return null;
    }

    protected static void drawRect(final GuiGraphicsExtractor graphics, int left, int top, int right, int bottom, int color, float opacity) {
        graphics.fill(left, top, right, bottom, colorWithOpacity(color, opacity));
    }

    protected static int colorWithOpacity(int color, float opacity) {
        return colorWithOpacity(color, Math.round(opacity * 255));
    }

    protected static int colorWithOpacity(int color, int opacity) {
        // replace alpha bits with passed opacity value, multiples opacity with current alpha bits if they are present
        return color & 0xffffff | (opacity * (color >> 24 == 0 ? 255 : color >> 24 & 255) / 255 << 24);
    }
}
