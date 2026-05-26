package meteordevelopment.starscript.value;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import meteordevelopment.starscript.utils.SFunction;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/starscript/value/ValueMap.class */
public class ValueMap {
    private final Map<String, Supplier<Value>> values = new HashMap();

    public ValueMap set(String name, Supplier<Value> supplier) {
        this.values.put(name, supplier);
        return this;
    }

    public ValueMap set(String name, Value value) {
        set(name, () -> {
            return value;
        });
        return this;
    }

    public ValueMap set(String name, boolean bool) {
        return set(name, Value.bool(bool));
    }

    public ValueMap set(String name, double number) {
        return set(name, Value.number(number));
    }

    public ValueMap set(String name, String string) {
        return set(name, Value.string(string));
    }

    public ValueMap set(String name, SFunction function) {
        return set(name, Value.function(function));
    }

    public ValueMap set(String name, ValueMap map) {
        return set(name, Value.map(map));
    }

    public Supplier<Value> get(String name) {
        return this.values.get(name);
    }

    public Set<String> keys() {
        return this.values.keySet();
    }
}
