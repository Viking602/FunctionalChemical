package com.github.glodblock.functionalchemical.mixins;

import com.github.glodblock.functionalchemical.common.cap.ChemicalHost;
import mekanism.common.integration.lookingat.ChemicalElement;
import mekanism.common.integration.lookingat.LookingAtHelper;
import mekanism.common.integration.lookingat.LookingAtUtils;
import mekanism.common.lib.multiblock.MultiblockData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LookingAtUtils.class)
public abstract class MixinLookingAtUtils {

    @Inject(
            method = "addInfo(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BlockEntity;Lmekanism/common/lib/multiblock/MultiblockData;Lmekanism/common/integration/lookingat/LookingAtHelper;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void filterEmptyDrawer(Level level, BlockPos pos, BlockState state, BlockEntity tile, MultiblockData structure, LookingAtHelper info, CallbackInfo ci) {
        if (tile instanceof ChemicalHost host) {
            var handler = host.getChemicalHandler();
            if (handler != null) {
                int maxInfo = 4;
                for (int i = 0; i < handler.size(); i++) {
                    if (maxInfo <= 0) {
                        break;
                    }
                    if (!handler.getResource(i).isEmpty()) {
                        maxInfo --;
                        info.addChemicalElement(new ChemicalElement(new mekanism.api.resource.LargeResourceStack<>(handler.getResource(i), handler.getAmountAsLong(i)), handler.getCapacityAsLong(i, handler.getResource(i))));
                    }
                }
            }
            ci.cancel();
        }

    }

}
