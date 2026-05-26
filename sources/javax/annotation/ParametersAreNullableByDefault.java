package javax.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.annotation.meta.TypeQualifierDefault;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javax/annotation/ParametersAreNullableByDefault.class */
@TypeQualifierDefault({ElementType.PARAMETER})
@Nullable
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface ParametersAreNullableByDefault {
}
