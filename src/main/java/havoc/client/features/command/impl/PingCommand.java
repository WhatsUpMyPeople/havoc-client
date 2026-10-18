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
import havoc.client.util.player.NetworkStatsUtil;

@CommandInfo(
        id = "ping",
        aliases = "latency",
        usage = "@ping",
        descriptionKey = "command.ping.description"
)
public final class PingCommand implements ClientCommand {
    @Override
    public boolean execute(CommandContext ctx) {
        int ping = NetworkStatsUtil.getPing(ctx.mc());
        if (ping < 0) CommandOutput.warning("Ping is unavailable.");
        else CommandOutput.send("Ping: " + ping + " ms");
        return true;
    }
}
