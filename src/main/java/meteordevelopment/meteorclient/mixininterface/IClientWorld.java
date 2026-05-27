/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixininterface;

public interface IClientWorld {
    int meteor$getSequence();
    int meteor$getAndIncrementSequence();
}

