package javassist.compiler.ast;

import javassist.compiler.CompileError;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/compiler/ast/CondExpr.class */
public class CondExpr extends ASTList {
    private static final long serialVersionUID = 1;

    public CondExpr(ASTree cond, ASTree thenp, ASTree elsep) {
        super(cond, new ASTList(thenp, new ASTList(elsep)));
    }

    public ASTree condExpr() {
        return head();
    }

    public void setCond(ASTree t) {
        setHead(t);
    }

    public ASTree thenExpr() {
        return tail().head();
    }

    public void setThen(ASTree t) {
        tail().setHead(t);
    }

    public ASTree elseExpr() {
        return tail().tail().head();
    }

    public void setElse(ASTree t) {
        tail().tail().setHead(t);
    }

    @Override // javassist.compiler.ast.ASTree
    public String getTag() {
        return "?:";
    }

    @Override // javassist.compiler.ast.ASTList, javassist.compiler.ast.ASTree
    public void accept(Visitor v) throws CompileError {
        v.atCondExpr(this);
    }
}
