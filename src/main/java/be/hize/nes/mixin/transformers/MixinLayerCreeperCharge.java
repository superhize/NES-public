package be.hize.nes.mixin.transformers;

import be.hize.nes.mixin.hooks.LayerCreeperChargeHookKt;
import net.minecraft.client.renderer.entity.layers.LayerCreeperCharge;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LayerCreeperCharge.class)
public abstract class MixinLayerCreeperCharge implements LayerRenderer<EntityCreeper> {

    @ModifyArg(method = "doRenderLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderCreeper;bindTexture(Lnet/minecraft/util/ResourceLocation;)V"))
    private ResourceLocation modifyChargedCreeperLayer(ResourceLocation res) {
        return LayerCreeperChargeHookKt.modifyChargedCreeperLayer(res);
    }
}