# Havoc Client — ProGuard config
#
# First-pass obfuscation. Runs on the Loom-remapped jar (Minecraft refs are
# already intermediary at this point). Renames internal classes/methods/fields
# but keeps every reflection surface Fabric Loader, Mixin, Lombok, and the
# addon API need alive.
#
# Policy: no optimization (reordering mixin hosts is dangerous), no preverify,
# full name obfuscation on non-kept symbols only.

-dontoptimize
-dontpreverify
-dontnote
-dontwarn **
-ignorewarnings
-verbose

# Preserve metadata the loader + mixin + stack-trace tooling needs
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,SourceFile,LineNumberTable,Deprecated,Exceptions,MethodParameters,LocalVariableTable,LocalVariableTypeTable
-renamesourcefileattribute SourceFile

# -------------------------------------------------------------------------
# Fabric entrypoint — fabric.mod.json names this class by FQN
# -------------------------------------------------------------------------
-keep class havoc.client.Havoc { *; }

# Fabric Loader entrypoint interfaces (anything that implements them)
-keep class * implements net.fabricmc.api.ClientModInitializer { *; }
-keep class * implements net.fabricmc.api.ModInitializer { *; }
-keep class * implements net.fabricmc.api.DedicatedServerModInitializer { *; }

# Duplex Courier sub-entrypoint
-keep class havoc.duplexcourier.** { *; }

# -------------------------------------------------------------------------
# Mixin — class names + plugin + accessors are resolved by string
# -------------------------------------------------------------------------
-keep class havoc.client.mixins.** { *; }
-keep class * implements org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin { *; }
-keep @org.spongepowered.asm.mixin.Mixin class * { *; }
-keepclassmembers class * {
    @org.spongepowered.asm.mixin.Shadow *;
    @org.spongepowered.asm.mixin.Final *;
    @org.spongepowered.asm.mixin.gen.Accessor *;
    @org.spongepowered.asm.mixin.gen.Invoker *;
    @org.spongepowered.asm.mixin.injection.* *;
}

# -------------------------------------------------------------------------
# Public addon API — third-party addons link against these FQNs
# -------------------------------------------------------------------------
-keep public class havoc.client.api.** { public protected *; }
-keepclassmembers class havoc.client.api.** { public protected *; }

# -------------------------------------------------------------------------
# Annotation types + classes/members annotated with them (reflection surface)
# -------------------------------------------------------------------------
-keep @interface havoc.client.runtime.annotation.** { *; }
-keep class havoc.client.runtime.annotation.** { *; }
-keep class havoc.client.events.** { *; }

-keep @havoc.client.runtime.annotation.ClientBound class * { *; }
-keep @havoc.client.runtime.annotation.ClientBoundLevel class * { *; }
-keep @havoc.client.runtime.annotation.RuntimeAssertion class * { *; }
-keep @havoc.client.runtime.annotation.UsedImplicitly class * { *; }
-keepclassmembers class * {
    @havoc.client.events.EventHandler *;
    @havoc.client.runtime.annotation.** *;
}

# -------------------------------------------------------------------------
# Module discovery — annotation processor generates an index referencing
# every @Module class by FQN. Keep every subclass of Module/Command discoverable.
# -------------------------------------------------------------------------
-keep class havoc.client.features.module.Module { *; }
-keep class * extends havoc.client.features.module.Module { *; }
-keep class havoc.client.features.command.** { *; }
-keep class havoc.processor.** { *; }

# Config profile system — may reflect on fields by name for .cbcfg load/save
-keep class havoc.client.config.** { *; }

# -------------------------------------------------------------------------
# Service providers (META-INF/services/*)
# -------------------------------------------------------------------------
-keep class * implements javax.annotation.processing.Processor { *; }

# -------------------------------------------------------------------------
# Keep Lombok-generated accessors by convention (they are already baked by javac
# but keeping public methods on data holders is safer)
# -------------------------------------------------------------------------
-keepclassmembers class havoc.** {
    public <init>(...);
}

# -------------------------------------------------------------------------
# Minecraft, Fabric, and third-party libs — we don't own these, don't touch
# -------------------------------------------------------------------------
-dontwarn org.spongepowered.**
-dontwarn net.fabricmc.**
-dontwarn net.minecraft.**
-dontwarn com.mojang.**
-dontwarn org.lwjgl.**
-dontwarn org.joml.**
-dontwarn io.netty.**
-dontwarn com.google.**
-dontwarn org.slf4j.**
-dontwarn sun.misc.**
-dontwarn javax.**
-dontwarn java.**
-dontwarn **$$Lambda$**

# Non-havoc classes get kept wholesale — ProGuard is run on the fat remapJar
# which embeds Javet native loader and other bundled libs that we don't own.
-keep class !havoc.**,** { *; }
-keep class !havoc.**,**$* { *; }
