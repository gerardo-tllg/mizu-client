package javassist.compiler.ast;

import javassist.compiler.CompileError;
import javassist.compiler.MemberResolver;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/compiler/ast/CallExpr.class */
public class CallExpr extends Expr {
    private static final long serialVersionUID = 1;
    private MemberResolver.Method method;

    private CallExpr(ASTree _head, ASTList _tail) {
        super(67, _head, _tail);
        this.method = null;
    }

    public void setMethod(MemberResolver.Method m) {
        this.method = m;
    }

    public MemberResolver.Method getMethod() {
        return this.method;
    }

    public static CallExpr makeCall(ASTree target, ASTree args) {
        return new CallExpr(target, new ASTList(args));
    }

    @Override // javassist.compiler.ast.Expr, javassist.compiler.ast.ASTList, javassist.compiler.ast.ASTree
    public void accept(Visitor v) throws CompileError {
        v.atCallExpr(this);
    }
}
