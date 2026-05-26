package javax.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.annotation.meta.TypeQualifier;
import javax.annotation.meta.When;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javax/annotation/Untainted.class */
@TypeQualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Untainted {
    When when() default When.ALWAYS;
}
