package meteordevelopment.starscript.value;

import java.util.function.Supplier;
import meteordevelopment.starscript.utils.SFunction;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/starscript/value/Value.class */
public class Value {
    private static final Value NULL = new Value(ValueType.Null);
    private static final Value TRUE = new Boolean(true);
    private static final Value FALSE = new Boolean(false);
    public final ValueType type;

    private Value(ValueType type) {
        this.type = type;
    }

    public static Value null_() {
        return NULL;
    }

    public static Value bool(boolean bool) {
        return bool ? TRUE : FALSE;
    }

    public static Value number(double number) {
        return new Number(number);
    }

    public static Value string(String string) {
        return new VString(string);
    }

    public static Value function(SFunction function) {
        return new Function(function);
    }

    public static Value map(ValueMap fields) {
        return new Map(fields);
    }

    public boolean isNull() {
        return this.type == ValueType.Null;
    }

    public boolean isBool() {
        return this.type == ValueType.Boolean;
    }

    public boolean isNumber() {
        return this.type == ValueType.Number;
    }

    public boolean isString() {
        return this.type == ValueType.String;
    }

    public boolean isFunction() {
        return this.type == ValueType.Function;
    }

    public boolean isMap() {
        return this.type == ValueType.Map;
    }

    public boolean getBool() {
        return ((Boolean) this).bool;
    }

    public double getNumber() {
        return ((Number) this).number;
    }

    public String getString() {
        return ((VString) this).string;
    }

    public SFunction getFunction() {
        return ((Function) this).function;
    }

    public ValueMap getMap() {
        return ((Map) this).fields;
    }

    public boolean isTruthy() {
        switch (this.type) {
            case Null:
            default:
                return false;
            case Boolean:
                return getBool();
            case Number:
            case String:
            case Function:
            case Map:
                return true;
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Value value = (Value) o;
        if (this.type != value.type) {
            return false;
        }
        switch (this.type) {
            case Null:
                return true;
            case Boolean:
                return getBool() == value.getBool();
            case Number:
                return getNumber() == value.getNumber();
            case String:
                return getString().equals(value.getString());
            case Function:
                return getFunction() == value.getFunction();
            case Map:
                return getMap() == value.getMap();
            default:
                return false;
        }
    }

    public int hashCode() {
        int result = super.hashCode();
        switch (this.type) {
            case Boolean:
                result = (31 * result) + (getBool() ? 1 : 0);
                break;
            case Number:
                long temp = Double.doubleToLongBits(getNumber());
                result = (31 * result) + ((int) (temp ^ (temp >>> 32)));
                break;
            case String:
                String string = getString();
                result = (31 * result) + string.hashCode();
                break;
            case Function:
                result = (31 * result) + getFunction().hashCode();
                break;
            case Map:
                result = (31 * result) + getMap().hashCode();
                break;
        }
        return result;
    }

    public String toString() {
        switch (this.type) {
            case Null:
                return "null";
            case Boolean:
                return getBool() ? "true" : "false";
            case Number:
                double n = getNumber();
                return n % 1.0d == 0.0d ? Integer.toString((int) n) : Double.toString(n);
            case String:
                return getString();
            case Function:
                return "<function>";
            case Map:
                Supplier<Value> s = getMap().get("_toString");
                return s == null ? "<map>" : s.get().toString();
            default:
                return "";
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/starscript/value/Value$Boolean.class */
    private static class Boolean extends Value {
        private final boolean bool;

        private Boolean(boolean bool) {
            super(ValueType.Boolean);
            this.bool = bool;
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/starscript/value/Value$Number.class */
    private static class Number extends Value {
        private final double number;

        private Number(double number) {
            super(ValueType.Number);
            this.number = number;
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/starscript/value/Value$VString.class */
    private static class VString extends Value {
        private final String string;

        private VString(String string) {
            super(ValueType.String);
            this.string = string;
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/starscript/value/Value$Function.class */
    private static class Function extends Value {
        private final SFunction function;

        public Function(SFunction function) {
            super(ValueType.Function);
            this.function = function;
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/starscript/value/Value$Map.class */
    private static class Map extends Value {
        private final ValueMap fields;

        public Map(ValueMap fields) {
            super(ValueType.Map);
            this.fields = fields;
        }
    }
}
