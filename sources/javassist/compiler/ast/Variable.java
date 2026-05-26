package javassist.compiler.ast;

import javassist.compiler.CompileError;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/compiler/ast/Variable.class */
public class Variable extends Symbol {
    private static final long serialVersionUID = 1;
    protected Declarator declarator;

    public Variable(String sym, Declarator d) {
        super(sym);
        this.declarator = d;
    }

    public Declarator getDeclarator() {
        return this.declarator;
    }

    @Override // javassist.compiler.ast.Symbol, javassist.compiler.ast.ASTree
    public String toString() {
        return this.identifier + ":" + this.declarator.getType();
    }

    @Override // javassist.compiler.ast.Symbol, javassist.compiler.ast.ASTree
    public void accept(Visitor v) throws CompileError {
        v.atVariable(this);
    }
}
