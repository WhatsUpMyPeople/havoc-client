/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.features.command.impl;

import havoc.client.features.command.ClientCommand;
import havoc.client.features.command.CommandContext;
import havoc.client.features.command.CommandInfo;
import havoc.client.features.command.CommandOutput;
import havoc.client.runtime.ClientRuntime;
import havoc.client.runtime.ClientRuntimeState;

import java.nio.file.Path;
import java.util.List;

@CommandInfo(
        id = "runtime",
        aliases = {"panic", "jarreplace"},
        usage = "@runtime [status|panic|resume|jar|source]",
        descriptionKey = "command.runtime.description"
)
public final class RuntimeCommand implements ClientCommand {
    @Override
    public boolean execute(CommandContext ctx) {
        String action = ctx.arg(0);
        if (action == null || action.equalsIgnoreCase("status")) {
            sendStatus();
            return true;
        }
        if (action.equalsIgnoreCase("panic")) {
            ClientRuntime.toggleSoftPanic("runtime command");
            CommandOutput.send("Runtime state: " + ClientRuntime.state());
            return true;
        }
        if (action.equalsIgnoreCase("resume")) {
            if (ClientRuntime.state() == ClientRuntimeState.SOFT_PANIC) {
                ClientRuntime.resumeSoftPanic("runtime command");
            }
            CommandOutput.send("Runtime state: " + ClientRuntime.state());
            return true;
        }
        if (action.equalsIgnoreCase("jar") || action.equalsIgnoreCase("replace")) {
            ClientRuntime.prepareJarReplacement("runtime command");
            CommandOutput.send("Client is fully disabled for safe JAR replacement. Restart Minecraft to use the new JAR.");
            return true;
        }
        if (action.equalsIgnoreCase("source")) {
            Path jar = ClientRuntime.loaderBridge().currentModJarPath();
            CommandOutput.send("Loader: standardFabric=" + ClientRuntime.loaderBridge().isStandardFabric()
                    + ", safeJarReplacement=" + ClientRuntime.loaderBridge().supportsSafeJarReplacement());
            CommandOutput.send("Current mod source: " + (jar == null ? "<unknown>" : jar.toAbsolutePath()));
            CommandOutput.send("Havoc class source: "
                    + ClientRuntime.loaderBridge().describeClassSource("havoc.client.Havoc"));
            return true;
        }
        CommandOutput.send("Usage: " + metadata().usage());
        return true;
    }

    @Override
    public List<String> suggest(CommandContext ctx, int argIndex, String token) {
        if (argIndex != 1) return List.of();
        String lower = token == null ? "" : token.toLowerCase();
        return List.of("status", "panic", "resume", "jar", "source").stream()
                .filter(value -> lower.isEmpty() || value.startsWith(lower))
                .toList();
    }

    private void sendStatus() {
        for (String line : ClientRuntime.diagnostics().lines()) {
            CommandOutput.send(line);
        }
    }
}
