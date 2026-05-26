package org.reflections.util;

import java.lang.reflect.AnnotatedElement;
import java.util.LinkedHashSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.reflections.ReflectionUtils;
import org.reflections.Store;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:org/reflections/util/UtilQueryBuilder.class */
public interface UtilQueryBuilder<F, E> {
    QueryFunction<Store, E> get(F f);

    default QueryFunction<Store, E> of(F element) {
        return of((QueryFunction) ReflectionUtils.extendType().get((AnnotatedElement) element));
    }

    default QueryFunction<Store, E> of(F element, Predicate<? super E> predicate) {
        return of(element).filter(predicate);
    }

    default <T> QueryFunction<Store, E> of(QueryFunction<Store, T> function) {
        return store -> {
            return (LinkedHashSet) function.apply(store).stream().flatMap(t -> {
                return get(t).apply(store).stream();
            }).collect(Collectors.toCollection(LinkedHashSet::new));
        };
    }
}
