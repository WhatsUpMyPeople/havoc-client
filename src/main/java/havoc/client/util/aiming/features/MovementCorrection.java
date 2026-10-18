/*
 * This file is part of the Havoc Client distribution.
 * Havoc modifications copyright (c) 2026 pivosos2007.
 *
 * Portions of this file are based on LiquidBounce
 * (https://github.com/CCBlueX/LiquidBounce).
 * Copyright (c) 2015-2026 CCBlueX.
 *
 * LiquidBounce portions are licensed under GPLv3-or-later.
 * Havoc modifications are licensed under GPLv3.
 * See THIRD_PARTY_NOTICES.md for details.
 */

package havoc.client.util.aiming.features;

import havoc.client.config.values.EnumValue;

/**
 * Corrects movement when aiming away from client-side view direction.
 * <p>
 * Ported from LiquidBounce (CCBlueX).
 */
public enum MovementCorrection implements EnumValue.IdProvider {
    OFF("Off"),
    STRICT("Strict"),
    SILENT("Silent");

    private final String id;

    MovementCorrection(String id) {
        this.id = id;
    }

    @Override
    public String id() {
        return id;
    }
}
