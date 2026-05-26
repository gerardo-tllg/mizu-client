package javassist.compiler;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/compiler/SyntaxError.class */
public class SyntaxError extends CompileError {
    private static final long serialVersionUID = 1;

    public SyntaxError(Lex lexer) {
        super("syntax error near \"" + lexer.getTextAround() + "\"", lexer);
    }
}
