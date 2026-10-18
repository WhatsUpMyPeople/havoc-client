/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.effects;

import java.util.List;

public interface TransientWorldEffectSystem {
    void beginTick(long tickSequence);

    boolean spawn(TransientEffectDescriptor descriptor);

    boolean cancel(long effectId);

    void update(long nowMs);

    List<TransientEffectDescriptor> snapshot();

    int activeCount();

    EffectBudget budget();

    void setBudget(EffectBudget budget);

    void clear();
}
