package meteordevelopment.meteorclient.utils.notebot.instrumentdetect;

import net.minecraft.class_2428;
import net.minecraft.class_310;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/notebot/instrumentdetect/InstrumentDetectMode.class */
public enum InstrumentDetectMode {
    BlockState((noteBlock, blockPos) -> {
        return noteBlock.method_11654(class_2428.field_11325);
    }),
    BelowBlock((noteBlock2, blockPos2) -> {
        return class_310.method_1551().field_1687.method_8320(blockPos2.method_10074()).method_51364();
    });

    private final InstrumentDetectFunction instrumentDetectFunction;

    InstrumentDetectMode(InstrumentDetectFunction instrumentDetectFunction) {
        this.instrumentDetectFunction = instrumentDetectFunction;
    }

    public InstrumentDetectFunction getInstrumentDetectFunction() {
        return this.instrumentDetectFunction;
    }
}
