package net.fabricmc.fabric.api.resource;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/api/resource/ResourcePackActivationType.class */
public enum ResourcePackActivationType {
    NORMAL,
    DEFAULT_ENABLED,
    ALWAYS_ENABLED;

    public boolean isEnabledByDefault() {
        return this == DEFAULT_ENABLED || this == ALWAYS_ENABLED;
    }
}
