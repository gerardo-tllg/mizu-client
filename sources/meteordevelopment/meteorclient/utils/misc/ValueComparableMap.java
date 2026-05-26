package meteordevelopment.meteorclient.utils.misc;

import java.lang.Comparable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/misc/ValueComparableMap.class */
public class ValueComparableMap<K extends Comparable<K>, V> extends TreeMap<K, V> {
    private final transient Map<K, V> valueMap;

    public ValueComparableMap(Comparator<? super V> partialValueComparator) {
        this(partialValueComparator, new HashMap());
    }

    private ValueComparableMap(Comparator<? super V> partialValueComparator, HashMap<K, V> valueMap) {
        super((k1, k2) -> {
            int cmp = partialValueComparator.compare(valueMap.get(k1), valueMap.get(k2));
            return cmp != 0 ? cmp : k1.compareTo(k2);
        });
        this.valueMap = valueMap;
    }

    @Override // java.util.TreeMap, java.util.AbstractMap, java.util.Map
    public V put(K k, V v) {
        if (this.valueMap.containsKey(k)) {
            remove(k);
        }
        this.valueMap.put(k, v);
        return (V) super.put(k, (Object) v);
    }

    @Override // java.util.TreeMap, java.util.AbstractMap, java.util.Map
    public boolean containsKey(Object key) {
        return this.valueMap.containsKey(key);
    }

    @Override // java.util.Map
    public V getOrDefault(Object key, V defaultValue) {
        return containsKey(key) ? get(key) : defaultValue;
    }
}
