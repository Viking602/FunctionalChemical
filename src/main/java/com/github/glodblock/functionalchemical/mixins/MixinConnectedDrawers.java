package com.github.glodblock.functionalchemical.mixins;

import com.buuz135.functionalstorage.block.tile.ItemControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.StorageControllerTile;
import com.buuz135.functionalstorage.util.ConnectedDrawers;
import com.github.glodblock.functionalchemical.common.tileentities.ChemicalDrawerTile;
import com.github.glodblock.functionalchemical.util.asm.ChemicalController;
import com.github.glodblock.functionalchemical.util.asm.ChemicalModule;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mekanism.api.chemical.ChemicalResource;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(ConnectedDrawers.class)
public abstract class MixinConnectedDrawers implements ChemicalModule {

    @Final
    @Shadow(remap = false)
    private StorageControllerTile<?> controllerTile;
    @Unique
    private List<ResourceHandler<ChemicalResource>> chemHandlers;

    @Unique
    private void init() {
        this.chemHandlers = new ArrayList<>();
    }

    @Inject(
            method = "<init>",
            at = @At("TAIL"),
            remap = false
    )
    private void onConstruct(Level level, StorageControllerTile<?> controllerTile, CallbackInfo ci) {
        this.init();
    }

    @Inject(
            method = "rebuild",
            at = @At(value = "FIELD", target = "Lcom/buuz135/functionalstorage/util/ConnectedDrawers;itemHandlers:Ljava/util/List;", opcode = org.objectweb.asm.Opcodes.PUTFIELD),
            remap = false
    )
    private void onRebuild(CallbackInfo ci) {
        this.init();
    }

    @WrapOperation(
            method = "rebuild",
            constant = @Constant(classValue = ItemControllableDrawerTile.class, ordinal = 0),
            remap = false
    )
    private boolean passChemDrawer(Object obj, Operation<Boolean> original) {
        if (obj instanceof ChemicalDrawerTile) {
            return true;
        } else {
            return original.call(obj);
        }
    }

    @Redirect(
            method = "rebuild",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;",
                    ordinal = 1,
                    remap = true
            ),
            remap = false
    )
    private BlockEntity addChemicalHandler(Level world, BlockPos pos) {
        var te = world.getBlockEntity(pos);
        if (te instanceof ChemicalDrawerTile drawer) {
            this.chemHandlers.add(drawer.getChemicalHandler());
            return null;
        } else {
            return te;
        }
    }

    @Inject(
            method = "rebuild",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/buuz135/functionalstorage/fluid/ControllerFluidHandler;invalidateSlots()V"
            ),
            remap = false
    )
    private void afterRebuild(CallbackInfo ci) {
        ChemicalController controller = (ChemicalController) this.controllerTile;
        var handler = controller.getHandler();
        if (handler != null) {
            handler.refresh();
        }
    }

    @Override
    public List<ResourceHandler<ChemicalResource>> getHandlers() {
        return this.chemHandlers;
    }

    @Override
    public int size() {
        return this.chemHandlers.size();
    }

}
