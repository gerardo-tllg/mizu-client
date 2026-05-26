package meteordevelopment.meteorclient.utils.files;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.class_2481;
import net.minecraft.class_2487;
import net.minecraft.class_2489;
import net.minecraft.class_2494;
import net.minecraft.class_2497;
import net.minecraft.class_2499;
import net.minecraft.class_2503;
import net.minecraft.class_2516;
import net.minecraft.class_2519;
import net.minecraft.class_2520;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/files/NbtJsonBridge.class */
public final class NbtJsonBridge {
    private NbtJsonBridge() {
    }

    public static JsonElement toJson(class_2520 nbt) {
        if (nbt == null) {
            return JsonNull.INSTANCE;
        }
        if (nbt instanceof class_2487) {
            class_2487 c = (class_2487) nbt;
            JsonObject obj = new JsonObject();
            for (String key : c.method_10541()) {
                obj.add(key, toJson(c.method_10580(key)));
            }
            return obj;
        }
        if (nbt instanceof class_2499) {
            class_2499 list = (class_2499) nbt;
            JsonArray arr = new JsonArray();
            for (int i = 0; i < list.size(); i++) {
                arr.add(toJson((class_2520) list.get(i)));
            }
            return arr;
        }
        if (nbt instanceof class_2519) {
            return new JsonPrimitive((String) nbt.method_68658().orElse(""));
        }
        if (nbt instanceof class_2481) {
            class_2481 b = (class_2481) nbt;
            return new JsonPrimitive(Byte.valueOf(b.method_10698()));
        }
        if (nbt instanceof class_2516) {
            class_2516 s = (class_2516) nbt;
            return new JsonPrimitive(Short.valueOf(s.method_10696()));
        }
        if (nbt instanceof class_2497) {
            class_2497 i2 = (class_2497) nbt;
            return new JsonPrimitive(Integer.valueOf(i2.method_10701()));
        }
        if (nbt instanceof class_2503) {
            class_2503 l = (class_2503) nbt;
            return new JsonPrimitive(Long.valueOf(l.method_10699()));
        }
        if (nbt instanceof class_2494) {
            class_2494 f = (class_2494) nbt;
            return new JsonPrimitive(Float.valueOf(f.method_10700()));
        }
        if (!(nbt instanceof class_2489)) {
            return new JsonPrimitive((String) nbt.method_68658().orElse(""));
        }
        class_2489 d = (class_2489) nbt;
        return new JsonPrimitive(Double.valueOf(d.method_10697()));
    }

    public static class_2520 toNbt(JsonElement json) {
        if (json == null || json.isJsonNull()) {
            return null;
        }
        if (json.isJsonObject()) {
            class_2487 c = new class_2487();
            for (Map.Entry<String, JsonElement> e : json.getAsJsonObject().entrySet()) {
                class_2520 v = toNbt(e.getValue());
                if (v != null) {
                    c.method_10566(e.getKey(), v);
                }
            }
            return c;
        }
        if (json.isJsonArray()) {
            class_2499 list = new class_2499();
            Iterator it = json.getAsJsonArray().iterator();
            while (it.hasNext()) {
                class_2520 v2 = toNbt((JsonElement) it.next());
                if (v2 != null) {
                    list.add(v2);
                }
            }
            return list;
        }
        if (json.isJsonPrimitive()) {
            JsonPrimitive p = json.getAsJsonPrimitive();
            if (p.isBoolean()) {
                return class_2481.method_23233((byte) (p.getAsBoolean() ? 1 : 0));
            }
            if (p.isString()) {
                return class_2519.method_23256(p.getAsString());
            }
            if (p.isNumber()) {
                Number n = p.getAsNumber();
                double d = n.doubleValue();
                if (!Double.isInfinite(d) && !Double.isNaN(d) && d == Math.floor(d)) {
                    long as_long = n.longValue();
                    return (as_long < -2147483648L || as_long > 2147483647L) ? class_2503.method_23251(as_long) : class_2497.method_23247((int) as_long);
                }
                return class_2489.method_23241(d);
            }
            return null;
        }
        return null;
    }
}
