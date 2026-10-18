/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.renderer.ui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import havoc.client.render.engine.command.UiCommand;
import havoc.client.render.engine.command.UiStatsSnapshot;
import havoc.client.render.engine.core.HavocRenderSystem;
import havoc.client.render.engine.renderer.Renderer2D;
import havoc.client.render.engine.text.GlyphFont;
import havoc.client.render.engine.text.backend.TextPlacementMode;
import havoc.client.render.engine.renderer.ui.draw.UiRect;
import havoc.client.render.engine.renderer.ui.draw.UiBackdropRequest;
import havoc.client.render.engine.renderer.ui.blend.UiBackdropBlendSpec;
import havoc.client.render.engine.uniform.MeshBuilder;

/** Routes UI work through the rendering subsystem. */
public final class UiRenderDispatcher {
    private static final UiRendererSubsystem SUBSYSTEM = new UiRendererSubsystem();

    private UiRenderDispatcher() {
    }

    public static UiStatsSnapshot statsSnapshot() {
        return SUBSYSTEM.statsSnapshot();
    }

    public static void beginLayer() {
        SUBSYSTEM.beginFrame(HavocRenderSystem.ensureFrameContext());
    }

    public static void record(UiCommand command) {
        SUBSYSTEM.record(command);
    }

    public static boolean hasPendingCommands() {
        return SUBSYSTEM.hasPendingCommands();
    }

    public static void flushLayer() {
        if (UiDeferredScheduler.shouldDefer()) return;
        SUBSYSTEM.flush(HavocRenderSystem.ensureFrameContext(), HavocRenderSystem.rhi());
    }

    static void submitOrderedBatcher(OrderedUiBatcher batcher, boolean finish) {
        SUBSYSTEM.submitOrdered(batcher, finish);
    }

    public static void submitImmediate(String label, int orderedBatchCount, UiBatchPlan.Work work) {
        SUBSYSTEM.submitImmediate(label, orderedBatchCount, work);
    }

    public static boolean enqueueTextMesh(
            String label,
            GlyphFont font,
            MeshBuilder sourceMesh,
            RenderPipeline pipeline,
            TextPlacementMode placement) {
        OrderedUiBatcher batcher = Renderer2D.UI_BATCHER;
        if (!batcher.isActive() || batcher.isFlushing()) return false;
        if (font == null || sourceMesh == null || pipeline == null) return false;
        if (sourceMesh.isBuilding()) sourceMesh.end();
        if (sourceMesh.getIndicesCount() <= 0) return true;
        TextBatch batch = batcher.getOrCreateTextBatch(
                label,
                font,
                pipeline,
                placement != null ? placement : TextPlacementMode.UI
        );
        if (batch == null) return false;
        batch.append(sourceMesh);
        return true;
    }

    public static boolean enqueueLiquidGlassTextMesh(
            String label,
            GlyphFont font,
            MeshBuilder sourceMesh,
            RenderPipeline pipeline,
            TextPlacementMode placement,
            UiRect bounds) {
        OrderedUiBatcher batcher = Renderer2D.UI_BATCHER;
        if (!batcher.isActive() || batcher.isFlushing()) return false;
        if (font == null || sourceMesh == null || pipeline == null || bounds == null) return false;
        if (sourceMesh.isBuilding()) sourceMesh.end();
        if (sourceMesh.getIndicesCount() <= 0) return true;
        TextBatch batch = batcher.getOrCreateLiquidGlassTextBatch(
                label,
                font,
                pipeline,
                placement != null ? placement : TextPlacementMode.UI,
                bounds
        );
        if (batch == null) return false;
        batch.append(sourceMesh);
        return true;
    }

    public static boolean enqueueLiquidGlassTextMesh(
            String label,
            GlyphFont font,
            MeshBuilder sourceMesh,
            RenderPipeline pipeline,
            TextPlacementMode placement,
            UiRect bounds,
            UiBackdropBlendSpec backdropBlend,
            UiBackdropRequest backdropRequest) {
        OrderedUiBatcher batcher = Renderer2D.UI_BATCHER;
        if (!batcher.isActive() || batcher.isFlushing()) return false;
        if (font == null || sourceMesh == null || pipeline == null || bounds == null) return false;
        if (sourceMesh.isBuilding()) sourceMesh.end();
        if (sourceMesh.getIndicesCount() <= 0) return true;
        TextBatch batch = batcher.getOrCreateLiquidGlassTextBatch(
                label,
                font,
                pipeline,
                placement != null ? placement : TextPlacementMode.UI,
                bounds,
                backdropBlend,
                backdropRequest
        );
        if (batch == null) return false;
        batch.append(sourceMesh);
        return true;
    }

    public static boolean beginAutoBatch() {
        if (Renderer2D.UI_BATCHER.isActive()) return false;
        Renderer2D.UI_BATCHER.begin();
        return true;
    }

    public static void endAutoBatch(boolean auto) {
        if (!auto) return;
        Renderer2D.BATCH_STATS.noteFlushReason(Renderer2D.FlushReason.AUTO_BATCH);
        Renderer2D.UI_BATCHER.flush(true);
        flushLayer();
    }

    public static void flushBatch(Renderer2D.FlushReason reason) {
        OrderedUiBatcher batcher = Renderer2D.UI_BATCHER;
        boolean pending = batcher.hasPendingWork() || SUBSYSTEM.hasPendingCommands();
        if (pending) Renderer2D.BATCH_STATS.noteFlushReason(reason);
        if (batcher.isActive()) batcher.flush(false);
        flushLayer();
    }

    public static boolean isFlushingBatch() {
        return Renderer2D.UI_BATCHER.isFlushing();
    }
}
