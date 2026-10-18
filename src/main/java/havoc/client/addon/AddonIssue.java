/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.addon;

public record AddonIssue(
        String addonId,
        Severity severity,
        String message,
        String detail
) {
    public enum Severity {
        INFO,
        WARNING,
        ERROR
    }
}
