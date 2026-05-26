package javassist.compiler.ast;

import javassist.CtField;
import javassist.compiler.CompileError;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/compiler/ast/Member.class */
public class Member extends Symbol {
    private static final long serialVersionUID = 1;
    private CtField field;

    public Member(String name) {
        super(name);
        this.field = null;
    }

    public void setField(CtField f) {
        this.field = f;
    }

    public CtField getField() {
        return this.field;
    }

    @Override // javassist.compiler.ast.Symbol, javassist.compiler.ast.ASTree
    public void accept(Visitor v) throws CompileError {
        v.atMember(this);
    }
}
