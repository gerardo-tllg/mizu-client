package javassist.compiler;

import java.util.HashMap;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/compiler/KeywordTable.class */
public final class KeywordTable extends HashMap<String, Integer> {
    private static final long serialVersionUID = 1;

    public int lookup(String name) {
        if (containsKey(name)) {
            return get(name).intValue();
        }
        return -1;
    }

    public void append(String name, int t) {
        put(name, Integer.valueOf(t));
    }
}
