package havoc.client.mixins;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import havoc.client.render.engine.rhi.backend.vulkan.debug.VulkanCrashDiagnostics;

@Mixin(RenderPass.class)
public abstract class RenderPassWrapperDiagnosticsMixin {
    @Unique
    private static boolean havoc$isVulkanBackend() {
        try {
            if (RenderSystem.getDevice() == null || RenderSystem.getDevice().getDeviceInfo() == null) return false;
            String backendName = RenderSystem.getDevice().getDeviceInfo().backendName();
            return backendName != null && backendName.toLowerCase(java.util.Locale.ROOT).contains("vulkan");
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Unique
    private static String havoc$pipelineId(RenderPipeline pipeline) {
        if (pipeline == null) return "<null>";
        try {
            return String.valueOf(pipeline.getLocation());
        } catch (Throwable ignored) {
            return pipeline.toString();
        }
    }

    @Inject(method = "setPipeline", at = @At("HEAD"))
    private void havoc$setPipelineHead(RenderPipeline pipeline, CallbackInfo ci) {
        if (!havoc$isVulkanBackend()) return;
        VulkanCrashDiagnostics.breadcrumbQuiet("renderpass.wrapper.setpipeline.head", "pipeline", havoc$pipelineId(pipeline));
    }

    @Inject(
            method = "setPipeline",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderPassBackend;setPipeline(Lcom/mojang/blaze3d/pipeline/RenderPipeline;)V", shift = At.Shift.BEFORE)
    )
    private void havoc$backendSetPipelineBefore(RenderPipeline pipeline, CallbackInfo ci) {
        if (!havoc$isVulkanBackend()) return;
        VulkanCrashDiagnostics.breadcrumbQuiet("renderpass.wrapper.backend.setpipeline.before", "pipeline", havoc$pipelineId(pipeline));
    }

    @Inject(
            method = "setPipeline",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderPassBackend;setPipeline(Lcom/mojang/blaze3d/pipeline/RenderPipeline;)V", shift = At.Shift.AFTER)
    )
    private void havoc$backendSetPipelineAfter(RenderPipeline pipeline, CallbackInfo ci) {
        if (!havoc$isVulkanBackend()) return;
        VulkanCrashDiagnostics.breadcrumbQuiet("renderpass.wrapper.backend.setpipeline.after", "pipeline", havoc$pipelineId(pipeline));
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void havoc$closeHead(CallbackInfo ci) {
        if (!havoc$isVulkanBackend()) return;
        VulkanCrashDiagnostics.breadcrumbQuiet("renderpass.wrapper.close.head");
    }

    @Inject(method = "close", at = @At(value = "INVOKE", target = "Ljava/lang/Runnable;run()V", shift = At.Shift.BEFORE))
    private void havoc$closeOnFinishBefore(CallbackInfo ci) {
        if (!havoc$isVulkanBackend()) return;
        VulkanCrashDiagnostics.breadcrumbQuiet("renderpass.wrapper.close.onfinish.before");
    }

    @Inject(method = "close", at = @At(value = "INVOKE", target = "Ljava/lang/Runnable;run()V", shift = At.Shift.AFTER))
    private void havoc$closeOnFinishAfter(CallbackInfo ci) {
        if (!havoc$isVulkanBackend()) return;
        VulkanCrashDiagnostics.breadcrumbQuiet("renderpass.wrapper.close.onfinish.after");
    }

    @Inject(method = "close", at = @At("TAIL"))
    private void havoc$closeTail(CallbackInfo ci) {
        if (!havoc$isVulkanBackend()) return;
        VulkanCrashDiagnostics.breadcrumbQuiet("renderpass.wrapper.close.tail");
    }
}
