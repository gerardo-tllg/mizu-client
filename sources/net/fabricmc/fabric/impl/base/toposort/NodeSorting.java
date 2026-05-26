package net.fabricmc.fabric.impl.base.toposort;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.PriorityQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-api-base-0.4.62+73a52b4b49.jar:net/fabricmc/fabric/impl/base/toposort/NodeSorting.class */
public class NodeSorting {
    private static final Logger LOGGER = LoggerFactory.getLogger("fabric-api-base");

    @VisibleForTesting
    public static boolean ENABLE_CYCLE_WARNING = true;

    public static <N extends SortableNode<N>> boolean sort(List<N> sortedNodes, String elementDescription, Comparator<N> comparator) {
        List<N> toposort = new ArrayList<>(sortedNodes.size());
        for (N node : sortedNodes) {
            forwardVisit(node, null, toposort);
        }
        clearStatus(toposort);
        Collections.reverse(toposort);
        IdentityHashMap identityHashMap = new IdentityHashMap();
        for (N node2 : toposort) {
            if (!node2.visited) {
                List<N> sccNodes = new ArrayList<>();
                backwardVisit(node2, sccNodes);
                sccNodes.sort(comparator);
                NodeScc<N> scc = new NodeScc<>(sccNodes);
                for (N nodeInScc : sccNodes) {
                    identityHashMap.put(nodeInScc, scc);
                }
            }
        }
        clearStatus(toposort);
        for (NodeScc<N> scc2 : identityHashMap.values()) {
            for (N node3 : scc2.nodes) {
                for (N subsequentNode : node3.subsequentNodes) {
                    NodeScc<N> subsequentScc = (NodeScc) identityHashMap.get(subsequentNode);
                    if (subsequentScc != scc2) {
                        scc2.subsequentSccs.add(subsequentScc);
                        subsequentScc.inDegree++;
                    }
                }
            }
        }
        PriorityQueue<NodeScc<N>> pq = new PriorityQueue<>((Comparator<? super NodeScc<N>>) Comparator.comparing(scc3 -> {
            return (SortableNode) scc3.nodes.get(0);
        }, comparator));
        sortedNodes.clear();
        for (NodeScc<N> scc4 : identityHashMap.values()) {
            if (scc4.inDegree == 0) {
                pq.add(scc4);
                scc4.inDegree = -1;
            }
        }
        boolean noCycle = true;
        while (!pq.isEmpty()) {
            NodeScc<N> scc5 = pq.poll();
            sortedNodes.addAll(scc5.nodes);
            if (scc5.nodes.size() > 1) {
                noCycle = false;
                if (ENABLE_CYCLE_WARNING) {
                    StringBuilder builder = new StringBuilder();
                    builder.append("Found cycle while sorting ").append(elementDescription).append(":\n");
                    for (N node4 : scc5.nodes) {
                        builder.append("\t").append(node4.getDescription()).append("\n");
                    }
                    LOGGER.warn(builder.toString());
                }
            }
            for (NodeScc<N> subsequentScc2 : scc5.subsequentSccs) {
                subsequentScc2.inDegree--;
                if (subsequentScc2.inDegree == 0) {
                    pq.add(subsequentScc2);
                }
            }
        }
        return noCycle;
    }

    private static <N extends SortableNode<N>> void forwardVisit(N node, N parent, List<N> toposort) {
        if (!node.visited) {
            node.visited = true;
            for (N data : node.subsequentNodes) {
                forwardVisit(data, node, toposort);
            }
            toposort.add(node);
        }
    }

    private static <N extends SortableNode<N>> void clearStatus(List<N> nodes) {
        for (N node : nodes) {
            node.visited = false;
        }
    }

    private static <N extends SortableNode<N>> void backwardVisit(N node, List<N> sccNodes) {
        if (!node.visited) {
            node.visited = true;
            sccNodes.add(node);
            for (N data : node.previousNodes) {
                backwardVisit(data, sccNodes);
            }
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-api-base-0.4.62+73a52b4b49.jar:net/fabricmc/fabric/impl/base/toposort/NodeSorting$NodeScc.class */
    private static class NodeScc<N extends SortableNode<N>> {
        final List<N> nodes;
        final List<NodeScc<N>> subsequentSccs = new ArrayList();
        int inDegree = 0;

        private NodeScc(List<N> nodes) {
            this.nodes = nodes;
        }
    }
}
