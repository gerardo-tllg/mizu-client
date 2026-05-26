package javassist.compiler;

import javassist.bytecode.Bytecode;
import javassist.compiler.ast.ASTList;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/compiler/ProceedHandler.class */
public interface ProceedHandler {
    void doit(JvstCodeGen jvstCodeGen, Bytecode bytecode, ASTList aSTList) throws CompileError;

    void setReturnType(JvstTypeChecker jvstTypeChecker, ASTList aSTList) throws CompileError;
}
