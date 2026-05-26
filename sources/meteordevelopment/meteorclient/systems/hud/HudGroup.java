package meteordevelopment.meteorclient.systems.hud;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/hud/HudGroup.class */
public final class HudGroup extends Record {
    private final String title;

    public HudGroup(String title) {
        this.title = title;
    }

    @Override // java.lang.Record
    public final String toString() {
        return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, HudGroup.class), HudGroup.class, "title", "FIELD:Lmeteordevelopment/meteorclient/systems/hud/HudGroup;->title:Ljava/lang/String;").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final int hashCode() {
        return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, HudGroup.class), HudGroup.class, "title", "FIELD:Lmeteordevelopment/meteorclient/systems/hud/HudGroup;->title:Ljava/lang/String;").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final boolean equals(Object o) {
        return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, HudGroup.class, Object.class), HudGroup.class, "title", "FIELD:Lmeteordevelopment/meteorclient/systems/hud/HudGroup;->title:Ljava/lang/String;").dynamicInvoker().invoke(this, o) /* invoke-custom */;
    }

    public String title() {
        return this.title;
    }
}
