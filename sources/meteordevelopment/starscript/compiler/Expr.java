package meteordevelopment.starscript.compiler;

import java.util.List;
import java.util.function.Consumer;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/starscript/compiler/Expr.class */
public abstract class Expr {
    public final int start;
    public final int end;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/starscript/compiler/Expr$Visitor.class */
    public interface Visitor {
        void visitNull(Null r1);

        void visitString(String string);

        void visitNumber(Number number);

        void visitBool(Bool bool);

        void visitBlock(Block block);

        void visitGroup(Group group);

        void visitBinary(Binary binary);

        void visitUnary(Unary unary);

        void visitVariable(Variable variable);

        void visitGet(Get get);

        void visitCall(Call call);

        void visitLogical(Logical logical);

        void visitConditional(Conditional conditional);

        void visitSection(Section section);
    }

    public abstract void accept(Visitor visitor);

    public Expr(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public java.lang.String getSource(java.lang.String source) {
        return source.substring(this.start, this.end);
    }

    public void forEach(Consumer<Expr> consumer) {
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/starscript/compiler/Expr$Null.class */
    public static class Null extends Expr {
        public Null(int start, int end) {
            super(start, end);
        }

        @Override // meteordevelopment.starscript.compiler.Expr
        public void accept(Visitor visitor) {
            visitor.visitNull(this);
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/starscript/compiler/Expr$String.class */
    public static class String extends Expr {
        public final java.lang.String string;

        public String(int start, int end, java.lang.String string) {
            super(start, end);
            this.string = string;
        }

        @Override // meteordevelopment.starscript.compiler.Expr
        public void accept(Visitor visitor) {
            visitor.visitString(this);
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/starscript/compiler/Expr$Number.class */
    public static class Number extends Expr {
        public final double number;

        public Number(int start, int end, double number) {
            super(start, end);
            this.number = number;
        }

        @Override // meteordevelopment.starscript.compiler.Expr
        public void accept(Visitor visitor) {
            visitor.visitNumber(this);
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/starscript/compiler/Expr$Bool.class */
    public static class Bool extends Expr {
        public final boolean bool;

        public Bool(int start, int end, boolean bool) {
            super(start, end);
            this.bool = bool;
        }

        @Override // meteordevelopment.starscript.compiler.Expr
        public void accept(Visitor visitor) {
            visitor.visitBool(this);
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/starscript/compiler/Expr$Block.class */
    public static class Block extends Expr {
        public final Expr expr;

        public Block(int start, int end, Expr expr) {
            super(start, end);
            this.expr = expr;
        }

        @Override // meteordevelopment.starscript.compiler.Expr
        public void accept(Visitor visitor) {
            visitor.visitBlock(this);
        }

        @Override // meteordevelopment.starscript.compiler.Expr
        public void forEach(Consumer<Expr> consumer) {
            if (this.expr != null) {
                consumer.accept(this.expr);
            }
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/starscript/compiler/Expr$Group.class */
    public static class Group extends Expr {
        public final Expr expr;

        public Group(int start, int end, Expr expr) {
            super(start, end);
            this.expr = expr;
        }

        @Override // meteordevelopment.starscript.compiler.Expr
        public void accept(Visitor visitor) {
            visitor.visitGroup(this);
        }

        @Override // meteordevelopment.starscript.compiler.Expr
        public void forEach(Consumer<Expr> consumer) {
            consumer.accept(this.expr);
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/starscript/compiler/Expr$Binary.class */
    public static class Binary extends Expr {
        public final Expr left;
        public final Token op;
        public final Expr right;

        public Binary(int start, int end, Expr left, Token op, Expr right) {
            super(start, end);
            this.left = left;
            this.op = op;
            this.right = right;
        }

        @Override // meteordevelopment.starscript.compiler.Expr
        public void accept(Visitor visitor) {
            visitor.visitBinary(this);
        }

        @Override // meteordevelopment.starscript.compiler.Expr
        public void forEach(Consumer<Expr> consumer) {
            consumer.accept(this.left);
            consumer.accept(this.right);
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/starscript/compiler/Expr$Unary.class */
    public static class Unary extends Expr {
        public final Token op;
        public final Expr right;

        public Unary(int start, int end, Token op, Expr right) {
            super(start, end);
            this.op = op;
            this.right = right;
        }

        @Override // meteordevelopment.starscript.compiler.Expr
        public void accept(Visitor visitor) {
            visitor.visitUnary(this);
        }

        @Override // meteordevelopment.starscript.compiler.Expr
        public void forEach(Consumer<Expr> consumer) {
            consumer.accept(this.right);
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/starscript/compiler/Expr$Variable.class */
    public static class Variable extends Expr {
        public final java.lang.String name;

        public Variable(int start, int end, java.lang.String name) {
            super(start, end);
            this.name = name;
        }

        @Override // meteordevelopment.starscript.compiler.Expr
        public void accept(Visitor visitor) {
            visitor.visitVariable(this);
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/starscript/compiler/Expr$Get.class */
    public static class Get extends Expr {
        public final Expr object;
        public final java.lang.String name;

        public Get(int start, int end, Expr object, java.lang.String name) {
            super(start, end);
            this.object = object;
            this.name = name;
        }

        @Override // meteordevelopment.starscript.compiler.Expr
        public void accept(Visitor visitor) {
            visitor.visitGet(this);
        }

        @Override // meteordevelopment.starscript.compiler.Expr
        public void forEach(Consumer<Expr> consumer) {
            consumer.accept(this.object);
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/starscript/compiler/Expr$Call.class */
    public static class Call extends Expr {
        public final Expr callee;
        public final List<Expr> args;

        public Call(int start, int end, Expr callee, List<Expr> args) {
            super(start, end);
            this.callee = callee;
            this.args = args;
        }

        @Override // meteordevelopment.starscript.compiler.Expr
        public void accept(Visitor visitor) {
            visitor.visitCall(this);
        }

        @Override // meteordevelopment.starscript.compiler.Expr
        public void forEach(Consumer<Expr> consumer) {
            consumer.accept(this.callee);
            for (Expr arg : this.args) {
                consumer.accept(arg);
            }
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/starscript/compiler/Expr$Logical.class */
    public static class Logical extends Expr {
        public final Expr left;
        public final Token op;
        public final Expr right;

        public Logical(int start, int end, Expr left, Token op, Expr right) {
            super(start, end);
            this.left = left;
            this.op = op;
            this.right = right;
        }

        @Override // meteordevelopment.starscript.compiler.Expr
        public void accept(Visitor visitor) {
            visitor.visitLogical(this);
        }

        @Override // meteordevelopment.starscript.compiler.Expr
        public void forEach(Consumer<Expr> consumer) {
            consumer.accept(this.left);
            consumer.accept(this.right);
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/starscript/compiler/Expr$Conditional.class */
    public static class Conditional extends Expr {
        public final Expr condition;
        public final Expr trueExpr;
        public final Expr falseExpr;

        public Conditional(int start, int end, Expr condition, Expr trueExpr, Expr falseExpr) {
            super(start, end);
            this.condition = condition;
            this.trueExpr = trueExpr;
            this.falseExpr = falseExpr;
        }

        @Override // meteordevelopment.starscript.compiler.Expr
        public void accept(Visitor visitor) {
            visitor.visitConditional(this);
        }

        @Override // meteordevelopment.starscript.compiler.Expr
        public void forEach(Consumer<Expr> consumer) {
            consumer.accept(this.condition);
            consumer.accept(this.trueExpr);
            consumer.accept(this.falseExpr);
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/starscript/compiler/Expr$Section.class */
    public static class Section extends Expr {
        public final int index;
        public final Expr expr;

        public Section(int start, int end, int index, Expr expr) {
            super(start, end);
            this.index = index;
            this.expr = expr;
        }

        @Override // meteordevelopment.starscript.compiler.Expr
        public void accept(Visitor visitor) {
            visitor.visitSection(this);
        }

        @Override // meteordevelopment.starscript.compiler.Expr
        public void forEach(Consumer<Expr> consumer) {
            consumer.accept(this.expr);
        }
    }
}
