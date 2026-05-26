package meteordevelopment.starscript.utils;

import meteordevelopment.starscript.compiler.Expr;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/starscript/utils/Error.class */
public class Error {
    public final int line;
    public final int character;
    public final char ch;
    public final String message;
    public Expr expr;

    public Error(int line, int character, char ch, String message, Expr expr) {
        this.line = line;
        this.character = character;
        this.ch = ch;
        this.message = message;
        this.expr = expr;
    }

    public String toString() {
        return String.format("[line %d, character %d] at '%s': %s", Integer.valueOf(this.line), Integer.valueOf(this.character), Character.valueOf(this.ch), this.message);
    }
}
