package javax.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.regex.Pattern;
import javax.annotation.meta.TypeQualifier;
import javax.annotation.meta.TypeQualifierValidator;
import javax.annotation.meta.When;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javax/annotation/MatchesPattern.class */
@TypeQualifier(applicableTo = String.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface MatchesPattern {
    @RegEx
    String value();

    int flags() default 0;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javax/annotation/MatchesPattern$Checker.class */
    public static class Checker implements TypeQualifierValidator<MatchesPattern> {
        @Override // javax.annotation.meta.TypeQualifierValidator
        public When forConstantValue(MatchesPattern annotation, Object value) {
            Pattern p = Pattern.compile(annotation.value(), annotation.flags());
            if (p.matcher((String) value).matches()) {
                return When.ALWAYS;
            }
            return When.NEVER;
        }
    }
}
