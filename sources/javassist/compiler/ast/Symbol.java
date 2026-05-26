package javassist.compiler.ast;

import javassist.compiler.CompileError;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/compiler/ast/Symbol.class */
public class Symbol extends ASTree {
    private static final long serialVersionUID = 1;
    protected String identifier;

    public Symbol(String sym) {
        this.identifier = sym;
    }

    public String get() {
        return this.identifier;
    }

    @Override // javassist.compiler.ast.ASTree
    public String toString() {
        return this.identifier;
    }

    @Override // javassist.compiler.ast.ASTree
    public void accept(Visitor v) throws CompileError {
        v.atSymbol(this);
    }
}
