package meteordevelopment.meteorclient.utils.player;

import java.util.function.Predicate;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.class_1713;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_2680;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/player/InvUtils.class */
public class InvUtils {
    private static final Action ACTION = new Action();
    public static int previousSlot = -1;

    private InvUtils() {
    }

    public static boolean testInMainHand(Predicate<class_1799> predicate) {
        return predicate.test(MeteorClient.mc.field_1724.method_6047());
    }

    public static boolean testInMainHand(class_1792... items) {
        return testInMainHand((Predicate<class_1799>) itemStack -> {
            for (class_1792 item : items) {
                if (itemStack.method_31574(item)) {
                    return true;
                }
            }
            return false;
        });
    }

    public static boolean testInOffHand(Predicate<class_1799> predicate) {
        return predicate.test(MeteorClient.mc.field_1724.method_6079());
    }

    public static boolean testInOffHand(class_1792... items) {
        return testInOffHand((Predicate<class_1799>) itemStack -> {
            for (class_1792 item : items) {
                if (itemStack.method_31574(item)) {
                    return true;
                }
            }
            return false;
        });
    }

    public static boolean testInHands(Predicate<class_1799> predicate) {
        return testInMainHand(predicate) || testInOffHand(predicate);
    }

    public static boolean testInHands(class_1792... items) {
        return testInMainHand(items) || testInOffHand(items);
    }

    public static boolean testInHotbar(Predicate<class_1799> predicate) {
        if (testInHands(predicate)) {
            return true;
        }
        for (int i = 0; i < 8; i++) {
            class_1799 stack = MeteorClient.mc.field_1724.method_31548().method_5438(i);
            if (predicate.test(stack)) {
                return true;
            }
        }
        return false;
    }

    public static boolean testInHotbar(class_1792... items) {
        return testInHotbar((Predicate<class_1799>) itemStack -> {
            for (class_1792 item : items) {
                if (itemStack.method_31574(item)) {
                    return true;
                }
            }
            return false;
        });
    }

    public static FindItemResult findEmpty() {
        return find((Predicate<class_1799>) (v0) -> {
            return v0.method_7960();
        });
    }

    public static FindItemResult findInHotbar(class_1792... items) {
        return findInHotbar((Predicate<class_1799>) itemStack -> {
            for (class_1792 item : items) {
                if (itemStack.method_7909() == item) {
                    return true;
                }
            }
            return false;
        });
    }

    public static FindItemResult findInHotbar(Predicate<class_1799> isGood) {
        if (testInOffHand(isGood)) {
            return new FindItemResult(45, MeteorClient.mc.field_1724.method_6079().method_7947());
        }
        if (testInMainHand(isGood)) {
            return new FindItemResult(MeteorClient.mc.field_1724.method_31548().field_7545, MeteorClient.mc.field_1724.method_6047().method_7947());
        }
        return find(isGood, 0, 8);
    }

    public static FindItemResult find(class_1792... items) {
        return find((Predicate<class_1799>) itemStack -> {
            for (class_1792 item : items) {
                if (itemStack.method_7909() == item) {
                    return true;
                }
            }
            return false;
        });
    }

    public static FindItemResult find(Predicate<class_1799> isGood) {
        if (MeteorClient.mc.field_1724 == null) {
            return new FindItemResult(0, 0);
        }
        return find(isGood, 0, 35);
    }

    public static FindItemResult find(Predicate<class_1799> isGood, int start, int end) {
        if (MeteorClient.mc.field_1724 == null) {
            return new FindItemResult(0, 0);
        }
        int slot = -1;
        int count = 0;
        for (int i = start; i <= end; i++) {
            class_1799 stack = MeteorClient.mc.field_1724.method_31548().method_5438(i);
            if (isGood.test(stack)) {
                if (slot == -1) {
                    slot = i;
                }
                count += stack.method_7947();
            }
        }
        return new FindItemResult(slot, count);
    }

    public static FindItemResult findFastestToolHotbar(class_2680 state) {
        float bestScore = 1.0f;
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            class_1799 stack = MeteorClient.mc.field_1724.method_31548().method_5438(i);
            if (stack.method_7951(state)) {
                float score = stack.method_7924(state);
                if (score > bestScore) {
                    bestScore = score;
                    slot = i;
                }
            }
        }
        return new FindItemResult(slot, 1);
    }

    public static FindItemResult findFastestTool(class_2680 state) {
        float bestScore = 1.0f;
        int slot = -1;
        for (int i = 0; i < 36; i++) {
            class_1799 stack = MeteorClient.mc.field_1724.method_31548().method_5438(i);
            if (stack.method_7951(state)) {
                float score = stack.method_7924(state);
                if (score > bestScore) {
                    bestScore = score;
                    slot = i;
                }
            }
        }
        return new FindItemResult(slot, 1);
    }

    public static boolean swap(int slot, boolean swapBack) {
        if (slot == 45) {
            return true;
        }
        if (slot < 0 || slot > 8) {
            return false;
        }
        if (swapBack && previousSlot == -1) {
            previousSlot = MeteorClient.mc.field_1724.method_31548().field_7545;
        } else if (!swapBack) {
            previousSlot = -1;
        }
        MeteorClient.mc.field_1724.method_31548().field_7545 = slot;
        MeteorClient.mc.field_1761.meteor$syncSelected();
        return true;
    }

    public static boolean swapBack() {
        if (previousSlot == -1) {
            return false;
        }
        boolean return_ = swap(previousSlot, false);
        previousSlot = -1;
        return return_;
    }

    public static Action move() {
        ACTION.type = class_1713.field_7790;
        ACTION.two = true;
        return ACTION;
    }

    public static Action click() {
        ACTION.type = class_1713.field_7790;
        return ACTION;
    }

    public static Action quickSwap() {
        ACTION.type = class_1713.field_7791;
        return ACTION;
    }

    public static Action shiftClick() {
        ACTION.type = class_1713.field_7794;
        return ACTION;
    }

    public static Action drop() {
        ACTION.type = class_1713.field_7795;
        ACTION.data = 1;
        return ACTION;
    }

    public static void dropHand() {
        if (!MeteorClient.mc.field_1724.field_7512.method_34255().method_7960()) {
            MeteorClient.mc.field_1761.method_2906(MeteorClient.mc.field_1724.field_7512.field_7763, -999, 0, class_1713.field_7790, MeteorClient.mc.field_1724);
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/player/InvUtils$Action.class */
    public static class Action {
        private class_1713 type = null;
        private boolean two = false;
        private int from = -1;
        private int to = -1;
        private int data = 0;
        private boolean isRecursive = false;

        private Action() {
        }

        public Action fromId(int id) {
            this.from = id;
            return this;
        }

        public Action from(int index) {
            return fromId(SlotUtils.indexToId(index));
        }

        public Action fromHotbar(int i) {
            return from(0 + i);
        }

        public Action fromOffhand() {
            return from(45);
        }

        public Action fromMain(int i) {
            return from(9 + i);
        }

        public Action fromArmor(int i) {
            return from(36 + (3 - i));
        }

        public void toId(int id) {
            this.to = id;
            run();
        }

        public void to(int index) {
            toId(SlotUtils.indexToId(index));
        }

        public void toHotbar(int i) {
            to(0 + i);
        }

        public void toOffhand() {
            to(45);
        }

        public void toMain(int i) {
            to(9 + i);
        }

        public void toArmor(int i) {
            to(36 + (3 - i));
        }

        public void slotId(int id) {
            this.to = id;
            this.from = id;
            run();
        }

        public void slot(int index) {
            slotId(SlotUtils.indexToId(index));
        }

        public void slotHotbar(int i) {
            slot(0 + i);
        }

        public void slotOffhand() {
            slot(45);
        }

        public void slotMain(int i) {
            slot(9 + i);
        }

        public void slotArmor(int i) {
            slot(36 + (3 - i));
        }

        private void run() {
            boolean hadEmptyCursor = MeteorClient.mc.field_1724.field_7512.method_34255().method_7960();
            if (this.type == class_1713.field_7791) {
                MeteorClient.mc.field_1761.method_2906(MeteorClient.mc.field_1724.field_7498.field_7763, this.to, this.from, this.type, MeteorClient.mc.field_1724);
                return;
            }
            if (this.type != null && this.from != -1 && this.to != -1) {
                click(this.from);
                if (this.two) {
                    click(this.to);
                }
            }
            class_1713 preType = this.type;
            boolean preTwo = this.two;
            int preFrom = this.from;
            int preTo = this.to;
            this.type = null;
            this.two = false;
            this.from = -1;
            this.to = -1;
            this.data = 0;
            if (!this.isRecursive && hadEmptyCursor && preType == class_1713.field_7790 && preTwo && preFrom != -1 && preTo != -1 && !MeteorClient.mc.field_1724.field_7512.method_34255().method_7960()) {
                this.isRecursive = true;
                InvUtils.click().slotId(preFrom);
                this.isRecursive = false;
            }
        }

        private void click(int id) {
            MeteorClient.mc.field_1761.method_2906(MeteorClient.mc.field_1724.field_7512.field_7763, id, this.data, this.type, MeteorClient.mc.field_1724);
        }
    }
}
