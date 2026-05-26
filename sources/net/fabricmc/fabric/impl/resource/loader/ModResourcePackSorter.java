package net.fabricmc.fabric.impl.resource.loader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.fabric.api.resource.ModResourcePack;
import net.fabricmc.fabric.impl.base.toposort.NodeSorting;
import net.fabricmc.fabric.impl.base.toposort.SortableNode;
import net.fabricmc.fabric.impl.resource.loader.ModResourcePackUtil;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/impl/resource/loader/ModResourcePackSorter.class */
public class ModResourcePackSorter {
    private final Object lock = new Object();
    private final Map<String, LoadPhaseData> phases = new LinkedHashMap();
    private final List<LoadPhaseData> sortedPhases = new ArrayList();
    private ModResourcePack[] packs = new ModResourcePack[0];

    ModResourcePackSorter() {
    }

    public List<ModResourcePack> getPacks() {
        return Collections.unmodifiableList(Arrays.asList(this.packs));
    }

    public void addPack(ModResourcePack pack) {
        Objects.requireNonNull(pack, "Can't register a null pack");
        String modId = pack.method_14409();
        Objects.requireNonNull(modId, "Can't register a pack without a mod id");
        synchronized (this.lock) {
            getOrCreatePhase(modId, true).addPack(pack);
            rebuildPackList(this.packs.length + 1);
        }
    }

    private LoadPhaseData getOrCreatePhase(String id, boolean sortIfCreate) {
        LoadPhaseData phase = this.phases.get(id);
        if (phase == null) {
            phase = new LoadPhaseData(id);
            this.phases.put(id, phase);
            this.sortedPhases.add(phase);
            if (sortIfCreate) {
                NodeSorting.sort(this.sortedPhases, "mod resource packs", Comparator.comparing(data -> {
                    return data.modId;
                }));
            }
        }
        return phase;
    }

    private void rebuildPackList(int newLength) {
        if (this.sortedPhases.size() == 1) {
            this.packs = ((LoadPhaseData) this.sortedPhases.getFirst()).packs;
            return;
        }
        ModResourcePack[] newHandlers = new ModResourcePack[newLength];
        int newHandlersIndex = 0;
        for (LoadPhaseData existingPhase : this.sortedPhases) {
            int length = existingPhase.packs.length;
            System.arraycopy(existingPhase.packs, 0, newHandlers, newHandlersIndex, length);
            newHandlersIndex += length;
        }
        this.packs = newHandlers;
    }

    public void addLoadOrdering(String firstPhase, String secondPhase, ModResourcePackUtil.Order order) {
        Objects.requireNonNull(firstPhase, "Tried to add an ordering for a null phase.");
        Objects.requireNonNull(secondPhase, "Tried to add an ordering for a null phase.");
        if (firstPhase.equals(secondPhase)) {
            throw new IllegalArgumentException("Tried to add a phase that depends on itself.");
        }
        synchronized (this.lock) {
            LoadPhaseData first = getOrCreatePhase(firstPhase, false);
            LoadPhaseData second = getOrCreatePhase(secondPhase, false);
            switch (order) {
                case BEFORE:
                    LoadPhaseData.link(first, second);
                    break;
                case AFTER:
                    LoadPhaseData.link(second, first);
                    break;
            }
            NodeSorting.sort(this.sortedPhases, "event phases", Comparator.comparing(data -> {
                return data.modId;
            }));
            rebuildPackList(this.packs.length);
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:META-INF/jars/fabric-resource-loader-v0-3.1.6+02ca679649.jar:net/fabricmc/fabric/impl/resource/loader/ModResourcePackSorter$LoadPhaseData.class */
    public static class LoadPhaseData extends SortableNode<LoadPhaseData> {
        final String modId;
        ModResourcePack[] packs = new ModResourcePack[0];

        LoadPhaseData(String modId) {
            this.modId = modId;
        }

        void addPack(ModResourcePack pack) {
            int oldLength = this.packs.length;
            this.packs = (ModResourcePack[]) Arrays.copyOf(this.packs, oldLength + 1);
            this.packs[oldLength] = pack;
        }

        @Override // net.fabricmc.fabric.impl.base.toposort.SortableNode
        protected String getDescription() {
            return this.modId;
        }
    }
}
