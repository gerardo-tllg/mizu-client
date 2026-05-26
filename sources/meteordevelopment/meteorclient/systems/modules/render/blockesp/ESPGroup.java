package meteordevelopment.meteorclient.systems.modules.render.blockesp;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.UnorderedArrayList;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import net.minecraft.class_2248;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/blockesp/ESPGroup.class */
public class ESPGroup {
    private static final BlockESP blockEsp = (BlockESP) Modules.get().get(BlockESP.class);
    private final class_2248 block;
    public final UnorderedArrayList<ESPBlock> blocks = new UnorderedArrayList<>();
    private double sumX;
    private double sumY;
    private double sumZ;

    public ESPGroup(class_2248 block) {
        this.block = block;
    }

    public void add(ESPBlock block, boolean removeFromOld, boolean splitGroup) {
        this.blocks.add(block);
        this.sumX += (double) block.x;
        this.sumY += (double) block.y;
        this.sumZ += (double) block.z;
        if (block.group != null && removeFromOld) {
            block.group.remove(block, splitGroup);
        }
        block.group = this;
    }

    public void add(ESPBlock block) {
        add(block, true, true);
    }

    public void remove(ESPBlock block, boolean splitGroup) {
        this.blocks.remove(block);
        this.sumX -= (double) block.x;
        this.sumY -= (double) block.y;
        this.sumZ -= (double) block.z;
        if (!this.blocks.isEmpty()) {
            if (splitGroup) {
                trySplit(block);
                return;
            }
            return;
        }
        blockEsp.removeGroup(block.group);
    }

    public void remove(ESPBlock block) {
        remove(block, true);
    }

    private void trySplit(ESPBlock block) {
        ESPBlock neighbour;
        ESPBlock neighbour2;
        ObjectOpenHashSet<ESPBlock> objectOpenHashSet = new ObjectOpenHashSet(6);
        for (int side : ESPBlock.SIDES) {
            if ((block.neighbours & side) == side && (neighbour2 = block.getSideBlock(side)) != null) {
                objectOpenHashSet.add(neighbour2);
            }
        }
        if (objectOpenHashSet.size() <= 1) {
            return;
        }
        ObjectOpenHashSet<ESPBlock> objectOpenHashSet2 = new ObjectOpenHashSet(this.blocks);
        Queue<ESPBlock> blocksToCheck = new ArrayDeque<>();
        blocksToCheck.offer((ESPBlock) this.blocks.getFirst());
        objectOpenHashSet2.remove(this.blocks.getFirst());
        objectOpenHashSet.remove(this.blocks.getFirst());
        loop1: while (!blocksToCheck.isEmpty()) {
            ESPBlock b = blocksToCheck.poll();
            for (int side2 : ESPBlock.SIDES) {
                if ((b.neighbours & side2) == side2 && (neighbour = b.getSideBlock(side2)) != null && objectOpenHashSet2.contains(neighbour)) {
                    blocksToCheck.offer(neighbour);
                    objectOpenHashSet2.remove(neighbour);
                    objectOpenHashSet.remove(neighbour);
                    if (objectOpenHashSet.isEmpty()) {
                        break loop1;
                    }
                }
            }
        }
        if (!objectOpenHashSet.isEmpty()) {
            ESPGroup group = blockEsp.newGroup(this.block);
            group.blocks.ensureCapacity(objectOpenHashSet2.size());
            UnorderedArrayList<ESPBlock> unorderedArrayList = this.blocks;
            Objects.requireNonNull(objectOpenHashSet2);
            unorderedArrayList.removeIf((v1) -> {
                return r1.contains(v1);
            });
            for (ESPBlock b2 : objectOpenHashSet2) {
                group.add(b2, false, false);
                this.sumX -= (double) b2.x;
                this.sumY -= (double) b2.y;
                this.sumZ -= (double) b2.z;
            }
            if (objectOpenHashSet.size() > 1) {
                block.neighbours = 0;
                for (ESPBlock b3 : objectOpenHashSet) {
                    int x = b3.x - block.x;
                    if (x == 1) {
                        block.neighbours |= 8;
                    } else if (x == -1) {
                        block.neighbours |= 128;
                    }
                    int y = b3.y - block.y;
                    if (y == 1) {
                        block.neighbours |= 512;
                    } else if (y == -1) {
                        block.neighbours |= 16384;
                    }
                    int z = b3.z - block.z;
                    if (z == 1) {
                        block.neighbours |= 2;
                    } else if (z == -1) {
                        block.neighbours |= 32;
                    }
                }
                group.trySplit(block);
            }
        }
    }

    public void merge(ESPGroup group) {
        this.blocks.ensureCapacity(this.blocks.size() + group.blocks.size());
        for (ESPBlock block : group.blocks) {
            add(block, false, false);
        }
        blockEsp.removeGroup(group);
    }

    public void render(Render3DEvent event) {
        ESPBlockData blockData = blockEsp.getBlockData(this.block);
        if (blockData.tracer) {
            event.renderer.line(RenderUtils.center.field_1352, RenderUtils.center.field_1351, RenderUtils.center.field_1350, (this.sumX / ((double) this.blocks.size())) + 0.5d, (this.sumY / ((double) this.blocks.size())) + 0.5d, (this.sumZ / ((double) this.blocks.size())) + 0.5d, blockData.tracerColor);
        }
    }
}
