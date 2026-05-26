package net.fabricmc.fabric.impl.base.toposort;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.impl.base.toposort.SortableNode;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-api-base-0.4.62+73a52b4b49.jar:net/fabricmc/fabric/impl/base/toposort/SortableNode.class */
public abstract class SortableNode<N extends SortableNode<N>> {
    final List<N> subsequentNodes = new ArrayList();
    final List<N> previousNodes = new ArrayList();
    boolean visited = false;

    protected abstract String getDescription();

    public static <N extends SortableNode<N>> void link(N first, N second) {
        if (first == second) {
            throw new IllegalArgumentException("Cannot link a node to itself!");
        }
        first.subsequentNodes.add(second);
        second.previousNodes.add(first);
    }
}
