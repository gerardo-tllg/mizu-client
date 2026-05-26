package meteordevelopment.meteorclient.utils.player;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import meteordevelopment.meteorclient.mixininterface.ISlot;
import meteordevelopment.meteorclient.utils.render.PeekScreen;
import net.minecraft.class_1277;
import net.minecraft.class_1661;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_3545;
import net.minecraft.class_465;
import net.minecraft.class_476;
import net.minecraft.class_481;
import net.minecraft.class_495;
import net.minecraft.class_7923;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/player/InventorySorter.class */
public class InventorySorter {
    private final class_465<?> screen;
    private final InvPart originInvPart;
    private boolean invalid;
    private List<Action> actions;
    private int timer;
    private int currentActionI;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/player/InventorySorter$InvPart.class */
    private enum InvPart {
        Hotbar,
        Player,
        Main,
        Invalid
    }

    public InventorySorter(class_465<?> screen, class_1735 originSlot) {
        this.screen = screen;
        this.originInvPart = getInvPart(originSlot);
        if (this.originInvPart == InvPart.Invalid || this.originInvPart == InvPart.Hotbar || (screen instanceof PeekScreen)) {
            this.invalid = true;
        } else {
            this.actions = new ArrayList();
            generateActions();
        }
    }

    public boolean tick(int delay) {
        if (this.invalid || this.currentActionI >= this.actions.size()) {
            return true;
        }
        if (this.timer >= delay) {
            this.timer = 0;
            Action action = this.actions.get(this.currentActionI);
            InvUtils.move().fromId(action.from).toId(action.to);
            this.currentActionI++;
            return false;
        }
        this.timer++;
        return false;
    }

    private void generateActions() {
        List<MySlot> slots = new ArrayList<>();
        for (ISlot iSlot : this.screen.method_17577().field_7761) {
            if (getInvPart(iSlot) == this.originInvPart) {
                slots.add(new MySlot(iSlot.meteor$getId(), iSlot.method_7677()));
            }
        }
        slots.sort(Comparator.comparingInt(value -> {
            return value.id;
        }));
        generateStackingActions(slots);
        generateSortingActions(slots);
    }

    private void generateStackingActions(List<MySlot> slots) {
        SlotMap slotMap = new SlotMap();
        for (MySlot slot : slots) {
            if (!slot.itemStack.method_7960() && slot.itemStack.method_7946() && slot.itemStack.method_7947() < slot.itemStack.method_7914()) {
                slotMap.get(slot.itemStack).add(slot);
            }
        }
        for (class_3545<class_1799, List<MySlot>> entry : slotMap.map) {
            List<MySlot> slotsToStack = (List) entry.method_15441();
            MySlot slotToStackTo = null;
            int i = 0;
            while (i < slotsToStack.size()) {
                MySlot slot2 = slotsToStack.get(i);
                if (slotToStackTo == null) {
                    slotToStackTo = slot2;
                } else {
                    this.actions.add(new Action(slot2.id, slotToStackTo.id));
                    if (slotToStackTo.itemStack.method_7947() + slot2.itemStack.method_7947() <= slotToStackTo.itemStack.method_7914()) {
                        slotToStackTo.itemStack = new class_1799(slotToStackTo.itemStack.method_7909(), slotToStackTo.itemStack.method_7947() + slot2.itemStack.method_7947());
                        slot2.itemStack = class_1799.field_8037;
                        if (slotToStackTo.itemStack.method_7947() >= slotToStackTo.itemStack.method_7914()) {
                            slotToStackTo = null;
                        }
                    } else {
                        int needed = slotToStackTo.itemStack.method_7914() - slotToStackTo.itemStack.method_7947();
                        slotToStackTo.itemStack = new class_1799(slotToStackTo.itemStack.method_7909(), slotToStackTo.itemStack.method_7914());
                        slot2.itemStack = new class_1799(slot2.itemStack.method_7909(), slot2.itemStack.method_7947() - needed);
                        slotToStackTo = null;
                        i--;
                    }
                }
                i++;
            }
        }
    }

    private void generateSortingActions(List<MySlot> slots) {
        MySlot toSlot;
        int from;
        int to;
        for (int i = 0; i < slots.size(); i++) {
            MySlot bestSlot = null;
            for (int j = i; j < slots.size(); j++) {
                MySlot slot = slots.get(j);
                if (bestSlot == null) {
                    bestSlot = slot;
                } else if (isSlotBetter(bestSlot, slot)) {
                    bestSlot = slot;
                }
            }
            if (!bestSlot.itemStack.method_7960() && (from = bestSlot.id) != (to = (toSlot = slots.get(i)).id)) {
                class_1799 temp = bestSlot.itemStack;
                bestSlot.itemStack = toSlot.itemStack;
                toSlot.itemStack = temp;
                this.actions.add(new Action(from, to));
            }
        }
    }

    private boolean isSlotBetter(MySlot best, MySlot slot) {
        class_1799 bestI = best.itemStack;
        class_1799 slotI = slot.itemStack;
        if (bestI.method_7960() && !slotI.method_7960()) {
            return true;
        }
        if (!bestI.method_7960() && slotI.method_7960()) {
            return false;
        }
        int c = class_7923.field_41178.method_10221(bestI.method_7909()).method_12833(class_7923.field_41178.method_10221(slotI.method_7909()));
        if (c == 0) {
            if (slotI.method_7947() != bestI.method_7947()) {
                return slotI.method_7947() > bestI.method_7947();
            }
            if (slotI.method_7919() != bestI.method_7919()) {
                return slotI.method_7919() > bestI.method_7919();
            }
        }
        return c > 0;
    }

    private InvPart getInvPart(class_1735 slot) {
        int i = ((ISlot) slot).meteor$getIndex();
        if ((slot.field_7871 instanceof class_1661) && (!(this.screen instanceof class_481) || ((ISlot) slot).meteor$getId() > 8)) {
            if (SlotUtils.isHotbar(i)) {
                return InvPart.Hotbar;
            }
            if (SlotUtils.isMain(i)) {
                return InvPart.Player;
            }
        } else if (((this.screen instanceof class_476) || (this.screen instanceof class_495)) && (slot.field_7871 instanceof class_1277)) {
            return InvPart.Main;
        }
        return InvPart.Invalid;
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/player/InventorySorter$MySlot.class */
    private static class MySlot {
        public final int id;
        public class_1799 itemStack;

        public MySlot(int id, class_1799 itemStack) {
            this.id = id;
            this.itemStack = itemStack;
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/player/InventorySorter$SlotMap.class */
    private static class SlotMap {
        private final List<class_3545<class_1799, List<MySlot>>> map = new ArrayList();

        private SlotMap() {
        }

        public List<MySlot> get(class_1799 itemStack) {
            for (class_3545<class_1799, List<MySlot>> entry : this.map) {
                if (class_1799.method_31577(itemStack, (class_1799) entry.method_15442())) {
                    return (List) entry.method_15441();
                }
            }
            List<MySlot> list = new ArrayList<>();
            this.map.add(new class_3545<>(itemStack, list));
            return list;
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/player/InventorySorter$Action.class */
    private static final class Action extends Record {
        private final int from;
        private final int to;

        private Action(int from, int to) {
            this.from = from;
            this.to = to;
        }

        @Override // java.lang.Record
        public final String toString() {
            return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, Action.class), Action.class, "from;to", "FIELD:Lmeteordevelopment/meteorclient/utils/player/InventorySorter$Action;->from:I", "FIELD:Lmeteordevelopment/meteorclient/utils/player/InventorySorter$Action;->to:I").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final int hashCode() {
            return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, Action.class), Action.class, "from;to", "FIELD:Lmeteordevelopment/meteorclient/utils/player/InventorySorter$Action;->from:I", "FIELD:Lmeteordevelopment/meteorclient/utils/player/InventorySorter$Action;->to:I").dynamicInvoker().invoke(this) /* invoke-custom */;
        }

        @Override // java.lang.Record
        public final boolean equals(Object o) {
            return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, Action.class, Object.class), Action.class, "from;to", "FIELD:Lmeteordevelopment/meteorclient/utils/player/InventorySorter$Action;->from:I", "FIELD:Lmeteordevelopment/meteorclient/utils/player/InventorySorter$Action;->to:I").dynamicInvoker().invoke(this, o) /* invoke-custom */;
        }

        public int from() {
            return this.from;
        }

        public int to() {
            return this.to;
        }
    }
}
