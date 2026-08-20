package se.mickelus.mutil.gui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Where an attachment puts a child, which is the arithmetic both draw paths share.
 *
 * The screen path and the world path compute a child's position with the same two offset methods,
 * so this is what keeps an interaction hint on a block face landing where the same element would
 * land in a menu. If these drift, the overlay drifts with them and the only way to notice is to go
 * and look at a crate.
 */
class GuiElementLayoutTest {
    private static GuiElement element(int width, int height) {
        return new GuiElement(0, 0, width, height);
    }

    @Test
    void horizontalOffsetFollowsTheColumn() {
        GuiElement e = element(40, 20);

        for (GuiAttachment left : new GuiAttachment[]{GuiAttachment.topLeft, GuiAttachment.middleLeft, GuiAttachment.bottomLeft}) {
            assertEquals(0, GuiElement.getXOffset(e, left), left.name());
        }
        for (GuiAttachment center : new GuiAttachment[]{GuiAttachment.topCenter, GuiAttachment.middleCenter, GuiAttachment.bottomCenter}) {
            assertEquals(20, GuiElement.getXOffset(e, center), center.name());
        }
        for (GuiAttachment right : new GuiAttachment[]{GuiAttachment.topRight, GuiAttachment.middleRight, GuiAttachment.bottomRight}) {
            assertEquals(40, GuiElement.getXOffset(e, right), right.name());
        }
    }

    @Test
    void verticalOffsetFollowsTheRow() {
        GuiElement e = element(40, 20);

        for (GuiAttachment top : new GuiAttachment[]{GuiAttachment.topLeft, GuiAttachment.topCenter, GuiAttachment.topRight}) {
            assertEquals(0, GuiElement.getYOffset(e, top), top.name());
        }
        for (GuiAttachment middle : new GuiAttachment[]{GuiAttachment.middleLeft, GuiAttachment.middleCenter, GuiAttachment.middleRight}) {
            assertEquals(10, GuiElement.getYOffset(e, middle), middle.name());
        }
        for (GuiAttachment bottom : new GuiAttachment[]{GuiAttachment.bottomLeft, GuiAttachment.bottomCenter, GuiAttachment.bottomRight}) {
            assertEquals(20, GuiElement.getYOffset(e, bottom), bottom.name());
        }
    }

    @Test
    void oddSizesTruncateRatherThanRound() {
        // Integer division, so a 15 wide element centres at 7 and not at 7.5. Worth stating, because
        // a one pixel drift only shows up on odd sizes and reads as a texture being off.
        GuiElement odd = element(15, 9);

        assertEquals(7, GuiElement.getXOffset(odd, GuiAttachment.middleCenter));
        assertEquals(4, GuiElement.getYOffset(odd, GuiAttachment.middleCenter));
    }

    @Test
    void anchoringToTheParentRightPutsAChildAtTheParentEdge() {
        // What a container does to place a child: parent anchor offset minus the child's own point
        // offset. A tool icon anchored middleRight with its own middleLeft point sits just past the
        // parent's right edge, vertically centred on it, which is how the overlay places one.
        GuiElement parent = element(64, 64);
        GuiElement child = element(16, 16);

        int x = GuiElement.getXOffset(parent, GuiAttachment.middleRight) - GuiElement.getXOffset(child, GuiAttachment.middleLeft);
        int y = GuiElement.getYOffset(parent, GuiAttachment.middleRight) - GuiElement.getYOffset(child, GuiAttachment.middleLeft);

        assertEquals(64, x, "hard against the parent's right edge");
        assertEquals(24, y, "centred on the parent, offset by half the child");
    }

    @Test
    void flipHorizontalSwapsSidesAndLeavesCentresAlone() {
        assertEquals(GuiAttachment.topRight, GuiAttachment.topLeft.flipHorizontal());
        assertEquals(GuiAttachment.bottomLeft, GuiAttachment.bottomRight.flipHorizontal());
        assertEquals(GuiAttachment.middleRight, GuiAttachment.middleLeft.flipHorizontal());
        assertEquals(GuiAttachment.topCenter, GuiAttachment.topCenter.flipHorizontal());
        assertEquals(GuiAttachment.middleCenter, GuiAttachment.middleCenter.flipHorizontal());
    }
}
