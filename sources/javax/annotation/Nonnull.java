package javax.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.annotation.meta.TypeQualifier;
import javax.annotation.meta.TypeQualifierValidator;
import javax.annotation.meta.When;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javax/annotation/Nonnull.class */
@TypeQualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Nonnull {
    When when() default When.ALWAYS;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javax/annotation/Nonnull$Checker.class */
    public static class Checker implements TypeQualifierValidator<Nonnull> {
        @Override // javax.annotation.meta.TypeQualifierValidator
        public When forConstantValue(Nonnull qualifierArgument, Object value) {
            if (value == null) {
                return When.NEVER;
            }
            return When.ALWAYS;
        }
    }
}
