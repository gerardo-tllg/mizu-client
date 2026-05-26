package javassist.compiler;

import java.util.HashMap;
import javassist.compiler.ast.Declarator;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/compiler/SymbolTable.class */
public final class SymbolTable extends HashMap<String, Declarator> {
    private static final long serialVersionUID = 1;
    private SymbolTable parent;

    public SymbolTable() {
        this(null);
    }

    public SymbolTable(SymbolTable p) {
        this.parent = p;
    }

    public SymbolTable getParent() {
        return this.parent;
    }

    public Declarator lookup(String name) {
        Declarator found = get(name);
        if (found == null && this.parent != null) {
            return this.parent.lookup(name);
        }
        return found;
    }

    public void append(String name, Declarator value) {
        put(name, value);
    }
}
