/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixininterface;

public interface ISimpleOption {
    // In 1.21.5, set() was renamed to setValue() in SimpleOption
    void setValue(Object value);
}
