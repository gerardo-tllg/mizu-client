package meteordevelopment.starscript.compiler;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.starscript.compiler.Expr;
import meteordevelopment.starscript.utils.Error;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/starscript/compiler/Parser.class */
public class Parser {
    private final Lexer lexer;
    private final TokenData previous = new TokenData();
    private final TokenData current = new TokenData();
    private int expressionDepth;

    private Parser(String source) {
        this.lexer = new Lexer(source);
    }

    private Result parse_() {
        Result result = new Result();
        advance();
        while (!isAtEnd()) {
            try {
                result.exprs.add(statement());
            } catch (ParseException e) {
                result.errors.add(e.error);
                synchronize();
            }
        }
        return result;
    }

    public static Result parse(String source) {
        return new Parser(source).parse_();
    }

    private Expr statement() {
        if (match(Token.Section)) {
            if (this.previous.lexeme.isEmpty()) {
                error("Expected section index.", null);
            }
            int start = this.previous.start;
            int index = Integer.parseInt(this.previous.lexeme);
            Expr expr = new Expr.Section(start, this.previous.end, index, expression());
            if (index > 255) {
                error("Section index cannot be larger than 255.", expr);
            }
            return expr;
        }
        return expression();
    }

    private Expr expression() {
        return conditional();
    }

    private Expr conditional() {
        int start = this.previous.start;
        Expr expr = and();
        if (match(Token.QuestionMark)) {
            Expr trueExpr = statement();
            consume(Token.Colon, "Expected ':' after first part of condition.", expr);
            Expr falseExpr = statement();
            expr = new Expr.Conditional(start, this.previous.end, expr, trueExpr, falseExpr);
        }
        return expr;
    }

    private Expr and() {
        Expr exprOr = or();
        while (true) {
            Expr expr = exprOr;
            if (match(Token.And)) {
                int start = this.previous.start;
                Expr right = or();
                exprOr = new Expr.Logical(start, this.previous.end, expr, Token.And, right);
            } else {
                return expr;
            }
        }
    }

    private Expr or() {
        Expr exprEquality = equality();
        while (true) {
            Expr expr = exprEquality;
            if (match(Token.Or)) {
                int start = this.previous.start;
                Expr right = equality();
                exprEquality = new Expr.Logical(start, this.previous.end, expr, Token.Or, right);
            } else {
                return expr;
            }
        }
    }

    private Expr equality() {
        int start = this.previous.start;
        Expr exprComparison = comparison();
        while (true) {
            Expr expr = exprComparison;
            if (match(Token.EqualEqual, Token.BangEqual)) {
                Token op = this.previous.token;
                Expr right = comparison();
                exprComparison = new Expr.Binary(start, this.previous.end, expr, op, right);
            } else {
                return expr;
            }
        }
    }

    private Expr comparison() {
        int start = this.previous.start;
        Expr exprTerm = term();
        while (true) {
            Expr expr = exprTerm;
            if (match(Token.Greater, Token.GreaterEqual, Token.Less, Token.LessEqual)) {
                Token op = this.previous.token;
                Expr right = term();
                exprTerm = new Expr.Binary(start, this.previous.end, expr, op, right);
            } else {
                return expr;
            }
        }
    }

    private Expr term() {
        int start = this.previous.start;
        Expr exprFactor = factor();
        while (true) {
            Expr expr = exprFactor;
            if (match(Token.Plus, Token.Minus)) {
                Token op = this.previous.token;
                Expr right = factor();
                exprFactor = new Expr.Binary(start, this.previous.end, expr, op, right);
            } else {
                return expr;
            }
        }
    }

    private Expr factor() {
        int start = this.previous.start;
        Expr exprUnary = unary();
        while (true) {
            Expr expr = exprUnary;
            if (match(Token.Star, Token.Slash, Token.Percentage, Token.UpArrow)) {
                Token op = this.previous.token;
                Expr right = unary();
                exprUnary = new Expr.Binary(start, this.previous.end, expr, op, right);
            } else {
                return expr;
            }
        }
    }

    private Expr unary() {
        if (match(Token.Bang, Token.Minus)) {
            int start = this.previous.start;
            Token op = this.previous.token;
            Expr right = unary();
            return new Expr.Unary(start, this.previous.end, op, right);
        }
        return call();
    }

    private Expr call() {
        Expr expr = primary();
        int start = this.previous.start;
        while (true) {
            if (match(Token.LeftParen)) {
                expr = finishCall(expr);
            } else if (match(Token.Dot)) {
                if (!check(Token.Identifier)) {
                    expr = new Expr.Get(start, this.current.end, expr, "");
                }
                TokenData name = consume(Token.Identifier, "Expected field name after '.'.", expr);
                expr = new Expr.Get(start, this.previous.end, expr, name.lexeme);
            } else {
                return expr;
            }
        }
    }

    private Expr finishCall(Expr callee) {
        List<Expr> args = new ArrayList<>(2);
        if (!check(Token.RightParen)) {
            do {
                args.add(expression());
            } while (match(Token.Comma));
        }
        Expr expr = new Expr.Call(callee.start, this.previous.end, callee, args);
        consume(Token.RightParen, "Expected ')' after function arguments.", expr);
        return expr;
    }

    private Expr primary() {
        if (match(Token.Null)) {
            return new Expr.Null(this.previous.start, this.previous.end);
        }
        if (match(Token.String)) {
            return new Expr.String(this.previous.start, this.previous.end, this.previous.lexeme);
        }
        if (match(Token.True, Token.False)) {
            return new Expr.Bool(this.previous.start, this.previous.end, this.previous.lexeme.equals("true"));
        }
        if (match(Token.Number)) {
            return new Expr.Number(this.previous.start, this.previous.end, Double.parseDouble(this.previous.lexeme));
        }
        if (match(Token.Identifier)) {
            return new Expr.Variable(this.previous.start, this.previous.end, this.previous.lexeme);
        }
        if (match(Token.LeftParen)) {
            Expr expr = new Expr.Group(this.previous.start, this.previous.end, statement());
            consume(Token.RightParen, "Expected ')' after expression.", expr);
            return expr;
        }
        if (this.expressionDepth == 0 && match(Token.LeftBrace)) {
            int start = this.previous.start;
            this.expressionDepth++;
            try {
                Expr expr2 = new Expr.Block(start, this.previous.end, statement());
                consume(Token.RightBrace, "Expected '}' after expression.", expr2);
                this.expressionDepth--;
                return expr2;
            } catch (ParseException e) {
                if (e.error.expr == null) {
                    e.error.expr = new Expr.Block(start, this.previous.end, null);
                }
                throw e;
            }
        }
        error("Expected expression.", null);
        return null;
    }

    private void synchronize() {
        while (!isAtEnd()) {
            if (match(Token.LeftBrace)) {
                this.expressionDepth++;
            } else if (match(Token.RightBrace)) {
                this.expressionDepth--;
                if (this.expressionDepth == 0) {
                    return;
                }
            } else {
                advance();
            }
        }
    }

    private void error(String message, Expr expr) {
        throw new ParseException(new Error(this.current.line, this.current.character, this.current.ch, message, expr));
    }

    private TokenData consume(Token token, String message, Expr expr) {
        if (check(token)) {
            return advance();
        }
        error(message, expr);
        return null;
    }

    private boolean match(Token... tokens) {
        for (Token token : tokens) {
            if (check(token)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(Token token) {
        return !isAtEnd() && this.current.token == token;
    }

    private TokenData advance() {
        this.previous.set(this.current);
        this.lexer.next();
        this.current.set(this.lexer.token, this.lexer.lexeme, this.lexer.start, this.lexer.current, this.lexer.line, this.lexer.character, this.lexer.ch);
        return this.previous;
    }

    private boolean isAtEnd() {
        return this.current.token == Token.EOF;
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/starscript/compiler/Parser$TokenData.class */
    private static class TokenData {
        public Token token;
        public String lexeme;
        public int start;
        public int end;
        public int line;
        public int character;
        public char ch;

        private TokenData() {
        }

        public void set(Token token, String lexeme, int start, int end, int line, int character, char ch) {
            this.token = token;
            this.lexeme = lexeme;
            this.start = start;
            this.end = end;
            this.line = line;
            this.character = character;
            this.ch = ch;
        }

        public void set(TokenData data) {
            set(data.token, data.lexeme, data.start, data.end, data.line, data.character, data.ch);
        }

        public String toString() {
            return String.format("%s '%s'", this.token, this.lexeme);
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/starscript/compiler/Parser$ParseException.class */
    private static class ParseException extends RuntimeException {
        public final Error error;

        public ParseException(Error error) {
            this.error = error;
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/starscript/compiler/Parser$Result.class */
    public static class Result {
        public final List<Expr> exprs = new ArrayList();
        public final List<Error> errors = new ArrayList();

        public boolean hasErrors() {
            return this.errors.size() > 0;
        }
    }
}
