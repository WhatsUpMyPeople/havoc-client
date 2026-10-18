/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.ShaderSource;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vulkan.VulkanConst;
import com.mojang.blaze3d.vulkan.VulkanDevice;
import com.mojang.blaze3d.vulkan.VulkanInstance;
import com.mojang.blaze3d.vulkan.VulkanPhysicalDevice;
import com.mojang.blaze3d.vulkan.VulkanRenderPipeline;
import com.mojang.blaze3d.vulkan.checkpoints.CheckpointExtension;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkImageFormatProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceDepthStencilResolvePropertiesKHR;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPhysicalDeviceLimits;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import havoc.client.render.engine.rhi.backend.vulkan.util.VulkanRenderStateBridge;
import havoc.client.mixininterface.IVulkanBackendInfo;
import havoc.client.util.logging.DebugLog;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static org.lwjgl.vulkan.VK12.*;

@Mixin(VulkanDevice.class)
public abstract class VulkanDeviceMixin implements IVulkanBackendInfo {
    @Shadow
    private ShaderSource defaultShaderSource;

    @Unique
    private final Map<VulkanRenderStateBridge.PipelineVariantKey, VulkanRenderPipeline> havoc$pipelineVariants = new HashMap<>();
    @Unique
    private final Map<RenderPipeline, RuntimeException> havoc$pipelineCompileFailures = new IdentityHashMap<>();
    @Unique private VkDevice havoc$vkDevice;
    @Unique private long havoc$vma;
    @Unique private VulkanPhysicalDevice havoc$physicalDevice;
    @Unique private long havoc$minStorageBufferOffsetAlignment = 1L;
    @Unique private long havoc$maxStorageBufferRange;

    @Override
    public VkDevice havoc$vkDevice() {
        return havoc$vkDevice;
    }

    @Override
    public long havoc$vma() {
        return havoc$vma;
    }

    @Override
    public VulkanPhysicalDevice havoc$physicalDevice() {
        return havoc$physicalDevice;
    }

    @Override
    public long havoc$minStorageBufferOffsetAlignment() {
        return havoc$minStorageBufferOffsetAlignment;
    }

    @Override
    public long havoc$maxStorageBufferRange() {
        return havoc$maxStorageBufferRange;
    }


    @Invoker("compilePipeline")
    protected abstract VulkanRenderPipeline havoc$compilePipelineVariant(RenderPipeline pipeline, ShaderSource source);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void havoc$captureMsaaCapabilities(ShaderSource shaderSource,
                                                   VulkanInstance instance,
                                                   VulkanPhysicalDevice physicalDevice,
                                                   Set<String> enabledExtensions,
                                                   VkDevice device,
                                                   long vma,
                                                   CheckpointExtension checkpointExtension,
                                                   CallbackInfo ci) {
        havoc$vkDevice = device;
        havoc$vma = vma;
        havoc$physicalDevice = physicalDevice;
        if (physicalDevice == null) return;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Copy the limits we need while querying the VkPhysicalDevice directly. The LWJGL
            // structs exposed by Mojang are native-backed views and must not become long-lived
            // configuration state for Havoc.
            VkPhysicalDeviceProperties deviceProperties = VkPhysicalDeviceProperties.calloc(stack);
            vkGetPhysicalDeviceProperties(physicalDevice.vkPhysicalDevice(), deviceProperties);
            VkPhysicalDeviceLimits limits = deviceProperties.limits();
            havoc$minStorageBufferOffsetAlignment = Math.max(1L, limits.minStorageBufferOffsetAlignment());
            havoc$maxStorageBufferRange = Integer.toUnsignedLong(limits.maxStorageBufferRange());
            int framebufferSamples = limits.framebufferColorSampleCounts()
                    & limits.framebufferDepthSampleCounts()
                    & limits.framebufferStencilSampleCounts();

            VkPhysicalDeviceFeatures features = VkPhysicalDeviceFeatures.calloc(stack);
            vkGetPhysicalDeviceFeatures(physicalDevice.vkPhysicalDevice(), features);
            VulkanRenderStateBridge.configureAdvancedShaderCapabilities(
                    physicalDevice.computeQueueFamilyAndIndex() != null,
                    features.tessellationShader(),
                    features.geometryShader(),
                    havoc$maxStorageBufferRange > 0L
            );
            int textureUsage = GpuTexture.USAGE_COPY_DST
                    | GpuTexture.USAGE_COPY_SRC
                    | GpuTexture.USAGE_TEXTURE_BINDING
                    | GpuTexture.USAGE_RENDER_ATTACHMENT;
            framebufferSamples &= havoc$imageSampleCounts(
                    stack,
                    physicalDevice,
                    GpuFormat.RGBA8_UNORM,
                    VulkanConst.textureUsageToVk(textureUsage, GpuFormat.RGBA8_UNORM)
            );
            framebufferSamples &= havoc$imageSampleCounts(
                    stack,
                    physicalDevice,
                    GpuFormat.D32_FLOAT,
                    VulkanConst.textureUsageToVk(textureUsage, GpuFormat.D32_FLOAT)
            );
            framebufferSamples &= havoc$imageSampleCounts(
                    stack,
                    physicalDevice,
                    GpuFormat.S8_UINT,
                    VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT
            );

            VkPhysicalDeviceDepthStencilResolvePropertiesKHR resolve =
                    VkPhysicalDeviceDepthStencilResolvePropertiesKHR.calloc(stack).sType$Default();
            VkPhysicalDeviceProperties2 properties2 = VkPhysicalDeviceProperties2.calloc(stack)
                    .sType$Default()
                    .pNext(resolve);
            vkGetPhysicalDeviceProperties2(physicalDevice.vkPhysicalDevice(), properties2);
            VulkanRenderStateBridge.configureMsaaCapabilities(
                    framebufferSamples,
                    resolve.supportedDepthResolveModes(),
                    resolve.independentResolveNone()
            );
        } catch (Throwable t) {
            VulkanRenderStateBridge.configureMsaaCapabilities(1, 0, false);
            DebugLog.warnOnChange(
                    "msaa.vulkan.capabilities.failed",
                    t.getClass().getSimpleName() + "|" + t.getMessage(),
                    "[MSAA/Vulkan] capability query failed; MSAA remains disabled: %s: %s",
                    t.getClass().getSimpleName(),
                    t.getMessage()
            );
        }
    }

    @Unique
    private static int havoc$imageSampleCounts(MemoryStack stack,
                                                   VulkanPhysicalDevice physicalDevice,
                                                   GpuFormat format,
                                                   int usage) {
        VkImageFormatProperties properties = VkImageFormatProperties.calloc(stack);
        int result = vkGetPhysicalDeviceImageFormatProperties(
                physicalDevice.vkPhysicalDevice(),
                VulkanConst.toVk(format),
                VK_IMAGE_TYPE_2D,
                VK_IMAGE_TILING_OPTIMAL,
                usage,
                0,
                properties
        );
        return result == VK_SUCCESS ? properties.sampleCounts() : VK_SAMPLE_COUNT_1_BIT;
    }

    @Inject(method = "getOrCompilePipeline", at = @At("HEAD"), cancellable = true)
    private void havoc$getOrCompilePipelineVariant(RenderPipeline pipeline,
                                                       CallbackInfoReturnable<VulkanRenderPipeline> cir) {
        RuntimeException previousFailure = havoc$pipelineCompileFailures.get(pipeline);
        if (previousFailure != null) throw previousFailure;
        if (!VulkanRenderStateBridge.needsPipelineVariant(pipeline)) return;

        VulkanRenderStateBridge.PipelineVariantKey key = VulkanRenderStateBridge.pipelineVariantKey(pipeline);
        VulkanRenderPipeline variant = havoc$pipelineVariants.get(key);
        if (variant != null) {
            cir.setReturnValue(variant);
            return;
        }

        try {
            variant = havoc$compilePipelineVariant(pipeline, defaultShaderSource);
            if (variant == null || !variant.isValid()) {
                throw new IllegalStateException("Vulkan returned an invalid pipeline variant");
            }
            havoc$pipelineVariants.put(key, variant);
            DebugLog.stencilOnChange(
                    "vulkan.pipeline.variant.compile",
                    key.toString(),
                    "[Vulkan/RHI] compiled pipeline variant %s",
                    key
            );
            cir.setReturnValue(variant);
        } catch (Throwable t) {
            if (key.usesMsaa()) {
                VulkanRenderStateBridge.disableMsaaAfterFailure("pipeline variant compile " + key, t);
            }
            if (key.usesStencil()) {
                VulkanRenderStateBridge.disableStencilAfterFailure("pipeline variant compile " + key, t);
            }
            DebugLog.error("[Vulkan/RHI] pipeline variant compile failed for " + key, t);
            // The current dynamic-rendering scope is already active. Falling through to vanilla's
            // single-sample/no-stencil pipeline would bind an incompatible VkPipeline and can lose
            // the device, so abort on the CPU and only disable the feature for future passes.
            throw t instanceof RuntimeException runtime ? runtime
                    : new IllegalStateException("Failed to compile Vulkan pipeline variant " + key, t);
        }
    }

    @WrapOperation(
            method = "getOrCompilePipeline",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;computeIfAbsent(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;"
            )
    )
    @SuppressWarnings({"rawtypes", "unchecked"})
    private Object havoc$rememberPipelineCompileFailure(Map cache,
                                                             Object key,
                                                             Function factory,
                                                             Operation<Object> original) {
        try {
            return original.call(cache, key, factory);
        } catch (RuntimeException failure) {
            if (key instanceof RenderPipeline pipeline) {
                havoc$pipelineCompileFailures.put(pipeline, failure);
            }
            VulkanRenderStateBridge.endPipelineCompile();
            throw failure;
        }
    }

    @WrapOperation(
            method = "compilePipeline",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vulkan/VulkanRenderPipeline;compile(Lcom/mojang/blaze3d/vulkan/VulkanDevice;Lcom/mojang/blaze3d/vulkan/VulkanBindGroupLayout;Lcom/mojang/blaze3d/pipeline/RenderPipeline;JJ)Lcom/mojang/blaze3d/vulkan/VulkanRenderPipeline;"
            )
    )
    private VulkanRenderPipeline havoc$releaseModulesAfterNativePipelineFailure(
            VulkanDevice device,
            com.mojang.blaze3d.vulkan.VulkanBindGroupLayout layout,
            RenderPipeline pipeline,
            long vertexModule,
            long fragmentModule,
            Operation<VulkanRenderPipeline> original) {
        try {
            return original.call(device, layout, pipeline, vertexModule, fragmentModule);
        } catch (RuntimeException failure) {
            if (failure.getMessage() != null && failure.getMessage().contains("Can't compile pipeline")) {
                if (vertexModule != 0L) vkDestroyShaderModule(device.vkDevice(), vertexModule, null);
                if (fragmentModule != 0L) vkDestroyShaderModule(device.vkDevice(), fragmentModule, null);
                if (layout != null && layout.handle() != 0L) {
                    vkDestroyDescriptorSetLayout(device.vkDevice(), layout.handle(), null);
                }
            }
            VulkanRenderStateBridge.endPipelineCompile();
            throw failure;
        }
    }

    @Inject(method = "clearPipelineCache", at = @At("RETURN"))
    private void havoc$clearPipelineVariants(CallbackInfo ci) {
        if (!havoc$pipelineVariants.isEmpty()) {
            for (VulkanRenderPipeline pipeline : havoc$pipelineVariants.values()) {
                try {
                    pipeline.destroy();
                } catch (Throwable t) {
                    DebugLog.warnOnChange(
                            "vulkan.pipeline.variant.destroy.failed",
                            t.getClass().getSimpleName() + "|" + t.getMessage(),
                            "[Vulkan/RHI] failed to destroy pipeline variant: %s: %s",
                            t.getClass().getSimpleName(),
                            t.getMessage()
                    );
                }
            }
            havoc$pipelineVariants.clear();
        }
        havoc$pipelineCompileFailures.clear();
    }
}
