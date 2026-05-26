package meteordevelopment.starscript;

import java.util.function.Supplier;
import javassist.bytecode.Opcode;
import meteordevelopment.starscript.compiler.Expr;
import meteordevelopment.starscript.compiler.Parser;
import meteordevelopment.starscript.utils.CompletionCallback;
import meteordevelopment.starscript.utils.Error;
import meteordevelopment.starscript.utils.SFunction;
import meteordevelopment.starscript.utils.Stack;
import meteordevelopment.starscript.utils.StarscriptError;
import meteordevelopment.starscript.value.Value;
import meteordevelopment.starscript.value.ValueMap;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/starscript/Starscript.class */
public class Starscript {
    private final ValueMap globals = new ValueMap();
    private final Stack<Value> stack = new Stack<>();

    public Section run(Script script, StringBuilder sb) {
        Section section;
        this.stack.clear();
        sb.setLength(0);
        int ip = 0;
        Section firstSection = null;
        Section section2 = null;
        int index = 0;
        while (true) {
            int i = ip;
            ip++;
            switch (AnonymousClass1.$SwitchMap$meteordevelopment$starscript$Instruction[Instruction.valueOf(script.code[i]).ordinal()]) {
                case 1:
                    ip++;
                    push(script.constants.get(script.code[ip]));
                    break;
                case 2:
                    push(Value.null_());
                    break;
                case 3:
                    push(Value.bool(true));
                    break;
                case 4:
                    push(Value.bool(false));
                    break;
                case 5:
                    Value b = pop();
                    Value a = pop();
                    if (a.isNumber() && b.isNumber()) {
                        push(Value.number(a.getNumber() + b.getNumber()));
                    } else if (!a.isString()) {
                        error("Can only add 2 numbers or 1 string and other value.", new Object[0]);
                    } else {
                        push(Value.string(a.getString() + b.toString()));
                    }
                    break;
                case 6:
                    Value b2 = pop();
                    Value a2 = pop();
                    if (a2.isNumber() && b2.isNumber()) {
                        push(Value.number(a2.getNumber() - b2.getNumber()));
                    } else {
                        error("Can only subtract 2 numbers.", new Object[0]);
                    }
                    break;
                case 7:
                    Value b3 = pop();
                    Value a3 = pop();
                    if (a3.isNumber() && b3.isNumber()) {
                        push(Value.number(a3.getNumber() * b3.getNumber()));
                    } else {
                        error("Can only multiply 2 numbers.", new Object[0]);
                    }
                    break;
                case 8:
                    Value b4 = pop();
                    Value a4 = pop();
                    if (a4.isNumber() && b4.isNumber()) {
                        push(Value.number(a4.getNumber() / b4.getNumber()));
                    } else {
                        error("Can only divide 2 numbers.", new Object[0]);
                    }
                    break;
                case 9:
                    Value b5 = pop();
                    Value a5 = pop();
                    if (a5.isNumber() && b5.isNumber()) {
                        push(Value.number(a5.getNumber() % b5.getNumber()));
                    } else {
                        error("Can only modulo 2 numbers.", new Object[0]);
                    }
                    break;
                case 10:
                    Value b6 = pop();
                    Value a6 = pop();
                    if (a6.isNumber() && b6.isNumber()) {
                        push(Value.number(Math.pow(a6.getNumber(), b6.getNumber())));
                    } else {
                        error("Can only power 2 numbers.", new Object[0]);
                    }
                    break;
                case 11:
                    ip++;
                    Value b7 = script.constants.get(script.code[ip]);
                    Value a7 = pop();
                    if (a7.isNumber() && b7.isNumber()) {
                        push(Value.number(a7.getNumber() + b7.getNumber()));
                    } else if (!a7.isString()) {
                        error("Can only add 2 numbers or 1 string and other value.", new Object[0]);
                    } else {
                        push(Value.string(a7.getString() + b7.toString()));
                    }
                    break;
                case 12:
                    pop();
                    break;
                case Opcode.FCONST_2 /* 13 */:
                    push(Value.bool(!pop().isTruthy()));
                    break;
                case Opcode.DCONST_0 /* 14 */:
                    Value a8 = pop();
                    if (!a8.isNumber()) {
                        error("This operation requires a number.", new Object[0]);
                    } else {
                        push(Value.number(-a8.getNumber()));
                    }
                    break;
                case 15:
                    push(Value.bool(pop().equals(pop())));
                    break;
                case 16:
                    push(Value.bool(!pop().equals(pop())));
                    break;
                case 17:
                    Value b8 = pop();
                    Value a9 = pop();
                    if (a9.isNumber() && b8.isNumber()) {
                        push(Value.bool(a9.getNumber() > b8.getNumber()));
                    } else {
                        error("This operation requires 2 number.", new Object[0]);
                    }
                    break;
                case 18:
                    Value b9 = pop();
                    Value a10 = pop();
                    if (a10.isNumber() && b9.isNumber()) {
                        push(Value.bool(a10.getNumber() >= b9.getNumber()));
                    } else {
                        error("This operation requires 2 number.", new Object[0]);
                    }
                    break;
                case 19:
                    Value b10 = pop();
                    Value a11 = pop();
                    if (a11.isNumber() && b10.isNumber()) {
                        push(Value.bool(a11.getNumber() < b10.getNumber()));
                    } else {
                        error("This operation requires 2 number.", new Object[0]);
                    }
                    break;
                case 20:
                    Value b11 = pop();
                    Value a12 = pop();
                    if (a12.isNumber() && b11.isNumber()) {
                        push(Value.bool(a12.getNumber() <= b11.getNumber()));
                    } else {
                        error("This operation requires 2 number.", new Object[0]);
                    }
                    break;
                case Opcode.ILOAD /* 21 */:
                    ip++;
                    String name = script.constants.get(script.code[ip]).getString();
                    Supplier<Value> s = this.globals.get(name);
                    push(s != null ? s.get() : Value.null_());
                    break;
                case Opcode.LLOAD /* 22 */:
                    ip++;
                    String name2 = script.constants.get(script.code[ip]).getString();
                    Value v = pop();
                    if (!v.isMap()) {
                        push(Value.null_());
                    } else {
                        Supplier<Value> s2 = v.getMap().get(name2);
                        push(s2 != null ? s2.get() : Value.null_());
                    }
                    break;
                case Opcode.FLOAD /* 23 */:
                    ip++;
                    byte b12 = script.code[ip];
                    Value a13 = peek(b12);
                    if (!a13.isFunction()) {
                        error("Tried to call a %s, can only call functions.", a13.type);
                    } else {
                        Value r = a13.getFunction().run(this, b12);
                        pop();
                        push(r);
                    }
                    break;
                case Opcode.DLOAD /* 24 */:
                    int ip2 = ip + 1;
                    int jump = ((script.code[ip] << 8) & 255) | (script.code[ip2] & 255);
                    ip = ip2 + 1 + jump;
                    break;
                case Opcode.ALOAD /* 25 */:
                    int ip3 = ip + 1;
                    int i2 = (script.code[ip] << 8) & 255;
                    ip = ip3 + 1;
                    int jump2 = i2 | (script.code[ip3] & 255);
                    if (peek().isTruthy()) {
                        ip += jump2;
                    }
                    break;
                case Opcode.ILOAD_0 /* 26 */:
                    int ip4 = ip + 1;
                    int i3 = (script.code[ip] << 8) & 255;
                    ip = ip4 + 1;
                    int jump3 = i3 | (script.code[ip4] & 255);
                    if (!peek().isTruthy()) {
                        ip += jump3;
                    }
                    break;
                case Opcode.ILOAD_1 /* 27 */:
                    if (firstSection == null) {
                        firstSection = new Section(index, sb.toString());
                        section = firstSection;
                    } else {
                        section2.next = new Section(index, sb.toString());
                        section = section2.next;
                    }
                    section2 = section;
                    sb.setLength(0);
                    ip++;
                    index = script.code[ip];
                    break;
                case Opcode.ILOAD_2 /* 28 */:
                    sb.append(pop().toString());
                    break;
                case Opcode.ILOAD_3 /* 29 */:
                    ip++;
                    sb.append(script.constants.get(script.code[ip]).toString());
                    break;
                case Opcode.LLOAD_0 /* 30 */:
                    ip++;
                    Supplier<Value> s3 = this.globals.get(script.constants.get(script.code[ip]).getString());
                    sb.append((s3 == null ? Value.null_() : s3.get()).toString());
                    break;
                case Opcode.LLOAD_1 /* 31 */:
                    ip++;
                    String name3 = script.constants.get(script.code[ip]).getString();
                    Value v2 = pop();
                    if (!v2.isMap()) {
                        sb.append(Value.null_());
                    } else {
                        Supplier<Value> s4 = v2.getMap().get(name3);
                        sb.append((s4 != null ? s4.get() : Value.null_()).toString());
                    }
                    break;
                case 32:
                    ip++;
                    byte b13 = script.code[ip];
                    Value a14 = peek(b13);
                    if (!a14.isFunction()) {
                        error("Tried to call a %s, can only call functions.", a14.type);
                    } else {
                        Value r2 = a14.getFunction().run(this, b13);
                        pop();
                        sb.append(r2.toString());
                    }
                    break;
                case 33:
                    int ip5 = ip + 1;
                    String name4 = script.constants.get(script.code[ip]).getString();
                    Supplier<Value> s5 = this.globals.get(name4);
                    Value v3 = s5 != null ? s5.get() : Value.null_();
                    ip = ip5 + 1;
                    String name5 = script.constants.get(script.code[ip5]).getString();
                    if (!v3.isMap()) {
                        push(Value.null_());
                    } else {
                        Supplier<Value> s6 = v3.getMap().get(name5);
                        push(s6 != null ? s6.get() : Value.null_());
                    }
                    break;
                case Opcode.FLOAD_0 /* 34 */:
                    int ip6 = ip + 1;
                    String name6 = script.constants.get(script.code[ip]).getString();
                    Supplier<Value> s7 = this.globals.get(name6);
                    Value v4 = s7 != null ? s7.get() : Value.null_();
                    ip = ip6 + 1;
                    String name7 = script.constants.get(script.code[ip6]).getString();
                    if (!v4.isMap()) {
                        push(Value.null_());
                    } else {
                        Supplier<Value> s8 = v4.getMap().get(name7);
                        sb.append((s8 != null ? s8.get() : Value.null_()).toString());
                    }
                    break;
                case 35:
                    if (firstSection != null) {
                        section2.next = new Section(index, sb.toString());
                        return firstSection;
                    }
                    return new Section(index, sb.toString());
                default:
                    throw new UnsupportedOperationException("Unknown instruction '" + Instruction.valueOf(script.code[ip]) + "'");
            }
        }
    }

    /* JADX INFO: renamed from: meteordevelopment.starscript.Starscript$1, reason: invalid class name */
    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/starscript/Starscript$1.class */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$meteordevelopment$starscript$Instruction = new int[Instruction.values().length];

        static {
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.Constant.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.Null.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.True.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.False.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.Add.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.Subtract.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.Multiply.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.Divide.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.Modulo.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.Power.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.AddConstant.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.Pop.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.Not.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.Negate.ordinal()] = 14;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.Equals.ordinal()] = 15;
            } catch (NoSuchFieldError e15) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.NotEquals.ordinal()] = 16;
            } catch (NoSuchFieldError e16) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.Greater.ordinal()] = 17;
            } catch (NoSuchFieldError e17) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.GreaterEqual.ordinal()] = 18;
            } catch (NoSuchFieldError e18) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.Less.ordinal()] = 19;
            } catch (NoSuchFieldError e19) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.LessEqual.ordinal()] = 20;
            } catch (NoSuchFieldError e20) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.Variable.ordinal()] = 21;
            } catch (NoSuchFieldError e21) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.Get.ordinal()] = 22;
            } catch (NoSuchFieldError e22) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.Call.ordinal()] = 23;
            } catch (NoSuchFieldError e23) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.Jump.ordinal()] = 24;
            } catch (NoSuchFieldError e24) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.JumpIfTrue.ordinal()] = 25;
            } catch (NoSuchFieldError e25) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.JumpIfFalse.ordinal()] = 26;
            } catch (NoSuchFieldError e26) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.Section.ordinal()] = 27;
            } catch (NoSuchFieldError e27) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.Append.ordinal()] = 28;
            } catch (NoSuchFieldError e28) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.ConstantAppend.ordinal()] = 29;
            } catch (NoSuchFieldError e29) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.VariableAppend.ordinal()] = 30;
            } catch (NoSuchFieldError e30) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.GetAppend.ordinal()] = 31;
            } catch (NoSuchFieldError e31) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.CallAppend.ordinal()] = 32;
            } catch (NoSuchFieldError e32) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.VariableGet.ordinal()] = 33;
            } catch (NoSuchFieldError e33) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.VariableGetAppend.ordinal()] = 34;
            } catch (NoSuchFieldError e34) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.End.ordinal()] = 35;
            } catch (NoSuchFieldError e35) {
            }
        }
    }

    public Section run(Script script) {
        return run(script, new StringBuilder());
    }

    public void push(Value value) {
        this.stack.push(value);
    }

    public Value pop() {
        return this.stack.pop();
    }

    public Value peek() {
        return this.stack.peek();
    }

    public Value peek(int offset) {
        return this.stack.peek(offset);
    }

    public boolean popBool(String errorMsg) {
        Value a = pop();
        if (!a.isBool()) {
            error(errorMsg, new Object[0]);
        }
        return a.getBool();
    }

    public double popNumber(String errorMsg) {
        Value a = pop();
        if (!a.isNumber()) {
            error(errorMsg, new Object[0]);
        }
        return a.getNumber();
    }

    public String popString(String errorMsg) {
        Value a = pop();
        if (!a.isString()) {
            error(errorMsg, new Object[0]);
        }
        return a.getString();
    }

    public void error(String format, Object... args) {
        throw new StarscriptError(String.format(format, args));
    }

    public ValueMap set(String name, Supplier<Value> supplier) {
        return this.globals.set(name, supplier);
    }

    public ValueMap set(String name, Value value) {
        return this.globals.set(name, value);
    }

    public ValueMap set(String name, boolean bool) {
        return this.globals.set(name, bool);
    }

    public ValueMap set(String name, double number) {
        return this.globals.set(name, number);
    }

    public ValueMap set(String name, String string) {
        return this.globals.set(name, string);
    }

    public ValueMap set(String name, SFunction function) {
        return this.globals.set(name, function);
    }

    public ValueMap set(String name, ValueMap map) {
        return this.globals.set(name, map);
    }

    public ValueMap getGlobals() {
        return this.globals;
    }

    public void getCompletions(String source, int position, CompletionCallback callback) {
        Parser.Result result = Parser.parse(source);
        for (Expr expr : result.exprs) {
            completionsExpr(source, position, expr, callback);
        }
        for (Error error : result.errors) {
            if (error.expr != null) {
                completionsExpr(source, position, error.expr, callback);
            }
        }
    }

    private void completionsExpr(String source, int position, Expr expr, CompletionCallback callback) {
        if (position >= expr.start) {
            if (position <= expr.end || position == source.length()) {
                if (expr instanceof Expr.Variable) {
                    Expr.Variable var = (Expr.Variable) expr;
                    String start = source.substring(var.start, position);
                    for (String key : this.globals.keys()) {
                        if (!key.startsWith("_") && key.startsWith(start)) {
                            callback.onCompletion(key, this.globals.get(key).get().isFunction());
                        }
                    }
                    return;
                }
                if (expr instanceof Expr.Get) {
                    Expr.Get get = (Expr.Get) expr;
                    if (position >= get.end - get.name.length()) {
                        Value value = resolveExpr(get.object);
                        if (value != null && value.isMap()) {
                            String start2 = source.substring(get.object.end + 1, position);
                            for (String key2 : value.getMap().keys()) {
                                if (!key2.startsWith("_") && key2.startsWith(start2)) {
                                    callback.onCompletion(key2, value.getMap().get(key2).get().isFunction());
                                }
                            }
                            return;
                        }
                        return;
                    }
                    expr.forEach(child -> {
                        completionsExpr(source, position, child, callback);
                    });
                    return;
                }
                if (expr instanceof Expr.Block) {
                    if (((Expr.Block) expr).expr == null) {
                        for (String key3 : this.globals.keys()) {
                            if (!key3.startsWith("_")) {
                                callback.onCompletion(key3, this.globals.get(key3).get().isFunction());
                            }
                        }
                        return;
                    }
                    expr.forEach(child2 -> {
                        completionsExpr(source, position, child2, callback);
                    });
                    return;
                }
                expr.forEach(child3 -> {
                    completionsExpr(source, position, child3, callback);
                });
            }
        }
    }

    private Value resolveExpr(Expr expr) {
        Value value;
        Supplier<Value> supplier;
        if (expr instanceof Expr.Variable) {
            Supplier<Value> supplier2 = this.globals.get(((Expr.Variable) expr).name);
            if (supplier2 != null) {
                return supplier2.get();
            }
            return null;
        }
        if (!(expr instanceof Expr.Get) || (value = resolveExpr(((Expr.Get) expr).object)) == null || !value.isMap() || (supplier = value.getMap().get(((Expr.Get) expr).name)) == null) {
            return null;
        }
        return supplier.get();
    }
}
