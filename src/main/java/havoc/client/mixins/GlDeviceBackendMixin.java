/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.mixins;

import com.mojang.blaze3d.opengl.DirectStateAccess;
import com.mojang.blaze3d.opengl.FrameBufferCache;
import com.mojang.blaze3d.opengl.GlRenderPipeline;
import com.mojang.blaze3d.opengl.VertexArrayCache;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.ShaderSource;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import havoc.client.util.logging.DebugLog;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GL11C;
import havoc.client.render.engine.profiler.ProfilerPhase;
import havoc.client.render.engine.rhi.backend.gl.GlNativeStateTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.gen.Invoker;
import havoc.client.mixininterface.IGlBackendInfo;

@Mixin(targets = "com.mojang.blaze3d.opengl.GlDevice")
public abstract class GlDeviceBackendMixin implements IGlBackendInfo {
    @Shadow
    protected static boolean USE_GL_KHR_debug;
    @Shadow
    protected static boolean USE_GL_ARB_direct_state_access;

    @Override
    @Invoker("directStateAccess")
    public abstract DirectStateAccess havoc$directStateAccess();

    @Override
    @Invoker("frameBufferCache")
    public abstract FrameBufferCache havoc$frameBufferCache();

    @Override
    @Invoker("vertexArrayCache")
    public abstract VertexArrayCache havoc$vertexArrayCache();

    @Override
    public boolean havoc$nativeDirectStateAccess() {
        return USE_GL_ARB_direct_state_access;
    }

    @Override
    public boolean havoc$khrDebug() {
        return USE_GL_KHR_debug;
    }

    @WrapOperation(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/opengl/GL;createCapabilities()Lorg/lwjgl/opengl/GLCapabilities;"
            )
    )
    private GLCapabilities havoc$captureMojangGlCapabilities(Operation<GLCapabilities> original) {
        GLCapabilities caps = original.call();
        GlNativeStateTracker.resetBlaze3dProgram();
        havoc$computeShaders = caps.OpenGL43 || caps.GL_ARB_compute_shader;
        havoc$tessellationShaders = caps.OpenGL40 || caps.GL_ARB_tessellation_shader;
        havoc$geometryShaders = caps.OpenGL32 || caps.GL_ARB_geometry_shader4;
        havoc$shaderStorageBuffers = caps.OpenGL43 || caps.GL_ARB_shader_storage_buffer_object;
        havoc$imageLoadStore = caps.OpenGL42 || caps.GL_ARB_shader_image_load_store;
        havoc$multiBind = caps.OpenGL44 || caps.GL_ARB_multi_bind;
        havoc$copyImage = caps.OpenGL43 || caps.GL_ARB_copy_image;
        havoc$attachmentInvalidation = caps.OpenGL43 || caps.GL_ARB_invalidate_subdata;
        havoc$vendor = havoc$glString(GL11C.GL_VENDOR);
        havoc$renderer = havoc$glString(GL11C.GL_RENDERER);
        return caps;
    }

    @Override
    public boolean havoc$computeShaders() { return havoc$computeShaders; }

    @Override
    public boolean havoc$tessellationShaders() { return havoc$tessellationShaders; }

    @Override
    public boolean havoc$geometryShaders() { return havoc$geometryShaders; }

    @Override
    public boolean havoc$shaderStorageBuffers() { return havoc$shaderStorageBuffers; }

    @Override
    public boolean havoc$imageLoadStore() { return havoc$imageLoadStore; }

    @Override
    public boolean havoc$multiBind() { return havoc$multiBind; }

    @Override
    public boolean havoc$copyImage() { return havoc$copyImage; }

    @Override
    public boolean havoc$attachmentInvalidation() { return havoc$attachmentInvalidation; }
    @Override
    public String havoc$vendor() { return havoc$vendor; }

    @Override
    public String havoc$renderer() { return havoc$renderer; }

    @Unique
    private static String havoc$glString(int name) {
        try {
            String value = GL11C.glGetString(name);
            return value == null ? "" : value;
        } catch (Throwable ignored) {
            return "";
        }
    }

    @Unique private boolean havoc$computeShaders;
    @Unique private boolean havoc$tessellationShaders;
    @Unique private boolean havoc$geometryShaders;
    @Unique private boolean havoc$shaderStorageBuffers;
    @Unique private boolean havoc$imageLoadStore;
    @Unique private boolean havoc$multiBind;
    @Unique private boolean havoc$copyImage;
    @Unique private boolean havoc$attachmentInvalidation;
    @Unique private String havoc$vendor = "";
    @Unique private String havoc$renderer = "";

    @Unique
    private ProfilerPhase.Scope havoc$pipelineCompileScope;


    @WrapOperation(
            method = {"compileShader", "compileProgram"},
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"
            )
    )
    private void havoc$wrapHavocCompileError(Logger logger,
                                                       String format,
                                                       Object first,
                                                       Object second,
                                                       Operation<Void> original) {
        if (havoc$containsHavocId(first) || havoc$containsHavocId(second)) {
            DebugLog.error("[RenderPipeline] " + havoc$formatSlf4j(format, first, second));
            return;
        }

        original.call(logger, format, first, second);
    }

    @WrapOperation(
            method = "compileShader",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/slf4j/Logger;error(Ljava/lang/String;[Ljava/lang/Object;)V"
            )
    )
    private void havoc$wrapHavocShaderCompileError(Logger logger,
                                                             String format,
                                                             Object[] args,
                                                             Operation<Void> original) {
        if (havoc$containsHavocId(args)) {
            DebugLog.error("[RenderPipeline] " + havoc$formatSlf4j(format, args));
            return;
        }

        original.call(logger, format, args);
    }

    @Unique
    private static boolean havoc$containsHavocId(Object value) {
        if (value instanceof Identifier id) {
            return "havoc".equals(id.getNamespace());
        }
        if (value instanceof Object[] values) {
            for (Object element : values) {
                if (havoc$containsHavocId(element)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Unique
    private static String havoc$formatSlf4j(String format, Object... args) {
        String template = String.valueOf(format);
        if (args == null || args.length == 0) {
            return template;
        }

        StringBuilder out = new StringBuilder(template.length() + args.length * 16);
        int cursor = 0;
        int argIndex = 0;
        while (argIndex < args.length) {
            int placeholder = template.indexOf("{}", cursor);
            if (placeholder < 0) {
                break;
            }
            out.append(template, cursor, placeholder);
            out.append(String.valueOf(args[argIndex++]));
            cursor = placeholder + 2;
        }
        out.append(template, cursor, template.length());
        while (argIndex < args.length) {
            out.append(' ').append(String.valueOf(args[argIndex++]));
        }
        return out.toString();
    }

    @Inject(method = "compilePipeline", at = @At("HEAD"))
    private void havoc$profilePipelineCompileHead(RenderPipeline pipeline, ShaderSource shaderSource, CallbackInfoReturnable<GlRenderPipeline> cir) {
        ProfilerPhase.Scope staleScope = havoc$pipelineCompileScope;
        havoc$pipelineCompileScope = null;
        if (staleScope != null) staleScope.close();

        if (ProfilerPhase.isActive()) {
            havoc$pipelineCompileScope = ProfilerPhase.scope("gl:compile_pipeline");
        }
    }

    @Inject(method = "compilePipeline", at = @At("RETURN"))
    private void havoc$profilePipelineCompileReturn(RenderPipeline pipeline, ShaderSource shaderSource, CallbackInfoReturnable<GlRenderPipeline> cir) {
        ProfilerPhase.Scope scope = havoc$pipelineCompileScope;
        havoc$pipelineCompileScope = null;
        if (scope != null) scope.close();
    }

}
