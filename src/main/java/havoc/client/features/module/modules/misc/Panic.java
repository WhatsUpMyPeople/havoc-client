/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.features.module.modules.misc;

import havoc.client.config.values.BindMode;
import havoc.client.features.command.CommandOutput;
import havoc.client.features.gui.clickgui.settings.FunctionBindSetting;
import havoc.client.features.module.Module;
import havoc.client.features.module.ModuleCategory;
import havoc.client.features.module.ModuleInfo;
import havoc.client.features.module.ModuleSubcategory;
import havoc.client.features.module.RuntimeControlModule;
import havoc.client.runtime.ClientRuntime;
import havoc.client.runtime.ClientRuntimeState;
import havoc.client.runtime.RuntimeDiagnostics;

@ModuleInfo(
        id = "panic",
        displayName = "Panic", aliases = {"selfdestruct"},
        category = ModuleCategory.MISC, subcategory = ModuleSubcategory.UTILITY,
        enabledByDefault = true,
        description = "module.panic.description")
public final class Panic extends Module implements RuntimeControlModule {
    private final FunctionBindSetting disableRestoreBind =
            action("disable_restore", "Z+X+C", BindMode.PRESS);
    private final FunctionBindSetting jarReplacementBind =
            action("prepare_jar_replacement", "LEFT_CTRL+LEFT_SHIFT+J", BindMode.PRESS);
    private final FunctionBindSetting diagnosticsBind =
            action("runtime_diagnostics", "LEFT_CTRL+LEFT_SHIFT+D", BindMode.PRESS);

    {
        setDefaultBind("NONE");
    }

    @Override
    public void onRuntimeControlTick() {
        if (disableRestoreBind.isPressed()) {
            toggleSoftPanic("panic module disable/restore bind");
        }
        if (diagnosticsBind.isPressed()) {
            sendDiagnostics();
        }
        if (jarReplacementBind.isPressed()) {
            prepareJarReplacement("panic module jar replacement bind");
        }
    }

    private void toggleSoftPanic(String reason) {
        ClientRuntimeState state = ClientRuntime.state();
        if (state == ClientRuntimeState.JAR_REPLACEMENT_PANIC || state == ClientRuntimeState.DEAD) {
            CommandOutput.send("Client is fully disabled for safe JAR replacement. Restart Minecraft to use the new JAR.");
            return;
        }
        boolean changed = ClientRuntime.toggleSoftPanic(reason);
        if (!changed) {
            CommandOutput.send("Runtime state: " + ClientRuntime.state());
            return;
        }
        if (ClientRuntime.state() == ClientRuntimeState.SOFT_PANIC) {
            CommandOutput.send("Panic Mode: ON");
        } else {
            CommandOutput.send("Panic Mode: OFF");
        }
    }

    private void prepareJarReplacement(String reason) {
        if (ClientRuntime.prepareJarReplacement(reason)) {
            CommandOutput.send("Client is fully disabled for safe JAR replacement. Restart Minecraft to use the new JAR.");
        }
    }

    private void sendDiagnostics() {
        RuntimeDiagnostics diagnostics = ClientRuntime.diagnostics();
        for (String line : diagnostics.lines()) {
            CommandOutput.send(line);
        }
    }
}
