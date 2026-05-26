package org.reflections;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:org/reflections/Store.class */
public class Store extends HashMap<String, Map<String, Set<String>>> {
    public Store() {
    }

    public Store(Map<String, Map<String, Set<String>>> storeMap) {
        super(storeMap);
    }
}
