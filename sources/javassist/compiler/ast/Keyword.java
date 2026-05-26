package javassist.compiler.ast;

import javassist.compiler.CompileError;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/compiler/ast/Keyword.class */
public class Keyword extends ASTree {
    private static final long serialVersionUID = 1;
    protected int tokenId;

    public Keyword(int token) {
        this.tokenId = token;
    }

    public int get() {
        return this.tokenId;
    }

    @Override // javassist.compiler.ast.ASTree
    public String toString() {
        return "id:" + this.tokenId;
    }

    @Override // javassist.compiler.ast.ASTree
    public void accept(Visitor v) throws CompileError {
        v.atKeyword(this);
    }
}
