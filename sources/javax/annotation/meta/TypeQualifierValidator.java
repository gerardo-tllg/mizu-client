package javax.annotation.meta;

import java.lang.annotation.Annotation;
import javax.annotation.Nonnull;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javax/annotation/meta/TypeQualifierValidator.class */
public interface TypeQualifierValidator<A extends Annotation> {
    @Nonnull
    When forConstantValue(@Nonnull A a, Object obj);
}
