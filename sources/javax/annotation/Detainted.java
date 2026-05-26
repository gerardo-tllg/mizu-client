package javax.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.annotation.meta.TypeQualifierNickname;
import javax.annotation.meta.When;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javax/annotation/Detainted.class */
@TypeQualifierNickname
@Documented
@Untainted(when = When.ALWAYS)
@Retention(RetentionPolicy.RUNTIME)
public @interface Detainted {
}
