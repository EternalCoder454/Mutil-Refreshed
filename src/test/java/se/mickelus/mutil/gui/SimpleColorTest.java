package se.mickelus.mutil.gui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Channel extraction, which every tint in the mod goes through.
 *
 * Worth pinning because the two halves disagree in a way nothing would notice. getRedShort and
 * getRedFloat read the same bits by different routes, and a shift wrong in one of them tints
 * everything slightly and looks like an art decision rather than a bug.
 */
class SimpleColorTest {
    @Test
    void readsEachChannelOutOfArgb() {
        SimpleColor color = new SimpleColor(0x8811AA44);

        assertEquals(0x88, color.getAlphaShort(), "alpha");
        assertEquals(0x11, color.getRedShort(), "red");
        assertEquals(0xAA, color.getGreenShort(), "green");
        assertEquals(0x44, color.getBlueShort(), "blue");
    }

    @Test
    void floatChannelsAgreeWithShortChannels() {
        SimpleColor color = new SimpleColor(0x8811AA44);

        assertEquals(color.getAlphaShort() / 255f, color.getAlphaFloat(), "alpha");
        assertEquals(color.getRedShort() / 255f, color.getRedFloat(), "red");
        assertEquals(color.getGreenShort() / 255f, color.getGreenFloat(), "green");
        assertEquals(color.getBlueShort() / 255f, color.getBlueFloat(), "blue");
    }

    @Test
    void fullChannelsReadAsOne() {
        SimpleColor white = new SimpleColor(0xFFFFFFFF);

        assertEquals(1f, white.getRedFloat());
        assertEquals(1f, white.getGreenFloat());
        assertEquals(1f, white.getBlueFloat());
        assertEquals(1f, white.getAlphaFloat());
    }

    @Test
    void anOpaqueColourWithNoAlphaBitsReadsAsTransparent() {
        // 0xRRGGBB with no alpha byte is how colours arrive from data, and it is fully transparent
        // as an argb value. Anything drawing one has to supply the alpha itself.
        SimpleColor fromData = new SimpleColor(0xFF8800);

        assertEquals(0, fromData.getAlphaShort());
        assertEquals(0xFF, fromData.getRedShort());
        assertEquals(0x88, fromData.getGreenShort());
        assertEquals(0x00, fromData.getBlueShort());
    }

    @Test
    void theRawValueSurvivesUnchanged() {
        assertEquals(0x8811AA44, new SimpleColor(0x8811AA44).getRaw());
    }
}
