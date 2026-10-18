/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.shader;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class HavocShaderSourcesTest {
    @Test
    void analyticClipVariantIsAReversibleSingleAxisKey() {
        Identifier base = Identifier.fromNamespaceAndPath("havoc", "shaders/ui_shape_batch.frag");
        Identifier variant = HavocShaderSources.analyticClipVariantId(base);

        assertEquals("shaders/ui_shape_batch__analytic_clip.frag", variant.getPath());
        assertTrue(HavocShaderSources.isAnalyticClipVariant(variant));
        assertEquals(base, HavocShaderSources.analyticClipBaseId(variant));
    }

    @Test
    void defineIsInjectedAfterRequiredVersionDirective() {
        String source = "#version 330 core\nvoid main() {}\n";
        String result = HavocShaderSources.injectDefineAfterVersion(source, "#define TEST_VARIANT 1\n");

        assertTrue(result.startsWith("#version 330 core\n#define TEST_VARIANT 1\n"));
        assertEquals(1, occurrences(result, "#version"));
        assertEquals(1, occurrences(result, "TEST_VARIANT"));
    }

    @Test
    void underlayAndAnalyticClipAxesComposeAndRemainReversible() {
        Identifier base = Identifier.fromNamespaceAndPath("havoc", "shaders/ui_liquid_glass_batch.frag");
        Identifier combined = HavocShaderSources.uiUnderlayVariantId(
                HavocShaderSources.analyticClipVariantId(base));

        assertEquals("shaders/ui_liquid_glass_batch__analytic_clip__ui_underlay.frag", combined.getPath());
        assertTrue(HavocShaderSources.isAnalyticClipVariant(combined));
        assertTrue(HavocShaderSources.isUiUnderlayVariant(combined));
        assertEquals(base, HavocShaderSources.uiUnderlayBaseId(
                HavocShaderSources.analyticClipBaseId(combined)));
    }

    private static int occurrences(String value, String needle) {
        int count = 0;
        int offset = 0;
        while ((offset = value.indexOf(needle, offset)) >= 0) {
            count++;
            offset += needle.length();
        }
        return count;
    }
}
