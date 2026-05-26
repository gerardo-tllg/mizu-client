package net.fabricmc.fabric.api.util;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-api-base-0.4.62+73a52b4b49.jar:net/fabricmc/fabric/api/util/TriState.class */
public enum TriState {
    FALSE,
    DEFAULT,
    TRUE;

    public static TriState of(boolean bool) {
        return bool ? TRUE : FALSE;
    }

    public static TriState of(@Nullable Boolean bool) {
        return bool == null ? DEFAULT : of(bool.booleanValue());
    }

    public boolean get() {
        return this == TRUE;
    }

    @Nullable
    public Boolean getBoxed() {
        if (this == DEFAULT) {
            return null;
        }
        return Boolean.valueOf(get());
    }

    public boolean orElse(boolean value) {
        return this == DEFAULT ? value : get();
    }

    public boolean orElseGet(BooleanSupplier supplier) {
        return this == DEFAULT ? supplier.getAsBoolean() : get();
    }

    public <T> Optional<T> map(BooleanFunction<? extends T> mapper) {
        Objects.requireNonNull(mapper, "Mapper function cannot be null");
        if (this == DEFAULT) {
            return Optional.empty();
        }
        return Optional.ofNullable(mapper.apply(get()));
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: X extends java.lang.Throwable */
    public <X extends Throwable> boolean orElseThrow(Supplier<X> exceptionSupplier) throws Throwable {
        if (this != DEFAULT) {
            return get();
        }
        throw exceptionSupplier.get();
    }
}
