package javax.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.annotation.meta.TypeQualifier;
import javax.annotation.meta.TypeQualifierValidator;
import javax.annotation.meta.When;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javax/annotation/Nonnegative.class */
@TypeQualifier(applicableTo = Number.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Nonnegative {
    When when() default When.ALWAYS;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javax/annotation/Nonnegative$Checker.class */
    public static class Checker implements TypeQualifierValidator<Nonnegative> {
        @Override // javax.annotation.meta.TypeQualifierValidator
        public When forConstantValue(Nonnegative annotation, Object v) {
            boolean isNegative;
            if (!(v instanceof Number)) {
                return When.NEVER;
            }
            Number value = (Number) v;
            if (value instanceof Long) {
                isNegative = value.longValue() < 0;
            } else if (value instanceof Double) {
                isNegative = value.doubleValue() < 0.0d;
            } else if (value instanceof Float) {
                isNegative = value.floatValue() < 0.0f;
            } else {
                isNegative = value.intValue() < 0;
            }
            if (isNegative) {
                return When.NEVER;
            }
            return When.ALWAYS;
        }
    }
}
