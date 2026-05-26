package meteordevelopment.starscript.compiler;

import javassist.bytecode.Opcode;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/starscript/compiler/Lexer.class */
public class Lexer {
    public Token token;
    public String lexeme;
    public int line = 1;
    public int character = -1;
    public char ch;
    private final String source;
    public int start;
    public int current;
    private int expressionDepth;

    public Lexer(String source) {
        this.source = source;
    }

    public void next() {
        this.start = this.current;
        if (isAtEnd()) {
            createToken(Token.EOF);
            return;
        }
        if (this.expressionDepth > 0) {
            skipWhitespace();
            if (isAtEnd()) {
                createToken(Token.EOF);
                return;
            }
            char c = advance();
            if (!isDigit(c) && (c != '-' || !isDigit(peek()))) {
                if (!isAlpha(c)) {
                    switch (c) {
                        case '!':
                            createToken(match('=') ? Token.BangEqual : Token.Bang);
                            return;
                        case Opcode.FLOAD_0 /* 34 */:
                        case '\'':
                            string();
                            return;
                        case '#':
                            break;
                        case '$':
                        case Opcode.DLOAD_0 /* 38 */:
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case Opcode.FSTORE /* 56 */:
                        case Opcode.DSTORE /* 57 */:
                        case Opcode.ISTORE_0 /* 59 */:
                        case '@':
                        case 'A':
                        case 'B':
                        case 'C':
                        case 'D':
                        case 'E':
                        case Opcode.FSTORE_3 /* 70 */:
                        case Opcode.DSTORE_0 /* 71 */:
                        case Opcode.DSTORE_1 /* 72 */:
                        case Opcode.DSTORE_2 /* 73 */:
                        case Opcode.DSTORE_3 /* 74 */:
                        case Opcode.ASTORE_0 /* 75 */:
                        case 'L':
                        case Opcode.ASTORE_2 /* 77 */:
                        case Opcode.ASTORE_3 /* 78 */:
                        case Opcode.IASTORE /* 79 */:
                        case Opcode.LASTORE /* 80 */:
                        case Opcode.FASTORE /* 81 */:
                        case Opcode.DASTORE /* 82 */:
                        case Opcode.AASTORE /* 83 */:
                        case Opcode.BASTORE /* 84 */:
                        case Opcode.CASTORE /* 85 */:
                        case Opcode.SASTORE /* 86 */:
                        case Opcode.POP /* 87 */:
                        case Opcode.POP2 /* 88 */:
                        case Opcode.DUP /* 89 */:
                        case Opcode.DUP_X1 /* 90 */:
                        case Opcode.DUP_X2 /* 91 */:
                        case Opcode.DUP2 /* 92 */:
                        case Opcode.DUP2_X1 /* 93 */:
                        case Opcode.SWAP /* 95 */:
                        case Opcode.IADD /* 96 */:
                        case Opcode.LADD /* 97 */:
                        case Opcode.FADD /* 98 */:
                        case 'c':
                        case 'd':
                        case Opcode.LSUB /* 101 */:
                        case Opcode.FSUB /* 102 */:
                        case Opcode.DSUB /* 103 */:
                        case Opcode.IMUL /* 104 */:
                        case Opcode.LMUL /* 105 */:
                        case Opcode.FMUL /* 106 */:
                        case Opcode.DMUL /* 107 */:
                        case Opcode.IDIV /* 108 */:
                        case Opcode.LDIV /* 109 */:
                        case Opcode.FDIV /* 110 */:
                        case Opcode.DDIV /* 111 */:
                        case Opcode.IREM /* 112 */:
                        case Opcode.LREM /* 113 */:
                        case Opcode.FREM /* 114 */:
                        case Opcode.DREM /* 115 */:
                        case Opcode.INEG /* 116 */:
                        case Opcode.LNEG /* 117 */:
                        case Opcode.FNEG /* 118 */:
                        case Opcode.DNEG /* 119 */:
                        case Opcode.ISHL /* 120 */:
                        case Opcode.LSHL /* 121 */:
                        case Opcode.ISHR /* 122 */:
                        case Opcode.IUSHR /* 124 */:
                        default:
                            unexpected();
                            return;
                        case Opcode.FLOAD_3 /* 37 */:
                            createToken(Token.Percentage);
                            return;
                        case Opcode.DLOAD_2 /* 40 */:
                            createToken(Token.LeftParen);
                            return;
                        case Opcode.DLOAD_3 /* 41 */:
                            createToken(Token.RightParen);
                            return;
                        case Opcode.ALOAD_0 /* 42 */:
                            createToken(Token.Star);
                            return;
                        case Opcode.ALOAD_1 /* 43 */:
                            createToken(Token.Plus);
                            return;
                        case Opcode.ALOAD_2 /* 44 */:
                            createToken(Token.Comma);
                            return;
                        case '-':
                            createToken(Token.Minus);
                            return;
                        case '.':
                            createToken(Token.Dot);
                            return;
                        case '/':
                            createToken(Token.Slash);
                            return;
                        case Opcode.ASTORE /* 58 */:
                            createToken(Token.Colon);
                            return;
                        case Opcode.ISTORE_1 /* 60 */:
                            createToken(match('=') ? Token.LessEqual : Token.Less);
                            return;
                        case Opcode.ISTORE_2 /* 61 */:
                            if (match('=')) {
                                createToken(Token.EqualEqual);
                                return;
                            } else {
                                unexpected();
                                return;
                            }
                        case Opcode.ISTORE_3 /* 62 */:
                            createToken(match('=') ? Token.GreaterEqual : Token.Greater);
                            return;
                        case Opcode.LSTORE_0 /* 63 */:
                            createToken(Token.QuestionMark);
                            return;
                        case Opcode.DUP2_X2 /* 94 */:
                            createToken(Token.UpArrow);
                            return;
                        case Opcode.LSHR /* 123 */:
                            this.expressionDepth++;
                            createToken(Token.LeftBrace);
                            return;
                        case Opcode.LUSHR /* 125 */:
                            this.expressionDepth--;
                            createToken(Token.RightBrace);
                            return;
                    }
                    while (isDigit(peek())) {
                        advance();
                    }
                    createToken(Token.Section, this.source.substring(this.start + 1, this.current));
                    return;
                }
                identifier();
                return;
            }
            number();
            return;
        }
        char c2 = advance();
        if (c2 == '\n') {
            this.line++;
        }
        if (c2 == '{') {
            this.expressionDepth++;
            createToken(Token.LeftBrace);
            return;
        }
        if (c2 == '#') {
            while (isDigit(peek())) {
                advance();
            }
            createToken(Token.Section, this.source.substring(this.start + 1, this.current));
            return;
        }
        while (!isAtEnd() && peek() != '{' && peek() != '#') {
            if (peek() == '\n') {
                this.line++;
            }
            advance();
        }
        createToken(Token.String);
    }

    private void string() {
        while (!isAtEnd() && peek() != '\"' && peek() != '\'') {
            if (peek() == '\n') {
                this.line++;
            }
            advance();
        }
        if (isAtEnd()) {
            createToken(Token.Error, "Unterminated expression.");
        } else {
            advance();
            createToken(Token.String, this.source.substring(this.start + 1, this.current - 1));
        }
    }

    private void number() {
        while (isDigit(peek())) {
            advance();
        }
        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) {
                advance();
            }
        }
        createToken(Token.Number);
    }

    private void identifier() {
        while (!isAtEnd() && isAlphaNumeric(peek())) {
            advance();
        }
        createToken(Token.Identifier);
        switch (this.lexeme) {
            case "null":
                this.token = Token.Null;
                break;
            case "true":
                this.token = Token.True;
                break;
            case "false":
                this.token = Token.False;
                break;
            case "and":
                this.token = Token.And;
                break;
            case "or":
                this.token = Token.Or;
                break;
        }
    }

    private void skipWhitespace() {
        while (!isAtEnd()) {
            char c = peek();
            switch (c) {
                case '\t':
                case Opcode.FCONST_2 /* 13 */:
                case ' ':
                    advance();
                    break;
                case '\n':
                    this.line++;
                    advance();
                    break;
                default:
                    this.start = this.current;
                    return;
            }
        }
    }

    private void unexpected() {
        createToken(Token.Error, "Unexpected character.");
    }

    private void createToken(Token token, String lexeme) {
        this.token = token;
        this.lexeme = lexeme;
    }

    private void createToken(Token token) {
        createToken(token, this.source.substring(this.start, this.current));
    }

    private boolean match(char expected) {
        if (isAtEnd() || this.source.charAt(this.current) != expected) {
            return false;
        }
        advance();
        return true;
    }

    private char advance() {
        this.character++;
        String str = this.source;
        int i = this.current;
        this.current = i + 1;
        char cCharAt = str.charAt(i);
        this.ch = cCharAt;
        return cCharAt;
    }

    private char peek() {
        if (isAtEnd()) {
            return (char) 0;
        }
        return this.source.charAt(this.current);
    }

    private char peekNext() {
        if (this.current + 1 >= this.source.length()) {
            return (char) 0;
        }
        return this.source.charAt(this.current + 1);
    }

    private boolean isAtEnd() {
        return this.current >= this.source.length();
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
}
