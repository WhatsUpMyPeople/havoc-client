/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.config.values;

public class BooleanValue extends ConfigValue<Boolean> {

    public BooleanValue(String name, boolean def) {
        super(name, def);
    }

    @Override
    public Object toJson() {
        return value;
    }

    @Override
    public void fromJson(Object json) {
        if (json instanceof Boolean b)
            value = b;
    }
}
