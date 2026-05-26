package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.mixininterface.IEntityRenderState;
import net.minecraft.class_10017;
import net.minecraft.class_1297;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/EntityRenderStateMixin.class */
@Mixin({class_10017.class})
public abstract class EntityRenderStateMixin implements IEntityRenderState {

    @Unique
    private class_1297 entity;

    @Override // meteordevelopment.meteorclient.mixininterface.IEntityRenderState
    public class_1297 meteor$getEntity() {
        return this.entity;
    }

    @Override // meteordevelopment.meteorclient.mixininterface.IEntityRenderState
    public void meteor$setEntity(class_1297 entity) {
        this.entity = entity;
    }
}
