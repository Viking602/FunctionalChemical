package com.github.glodblock.functionalchemical.mixins;

import com.buuz135.functionalstorage.block.tile.StorageControllerTile;
import com.buuz135.functionalstorage.util.ConnectedDrawers;
import com.github.glodblock.functionalchemical.common.cap.ChemicalHost;
import com.github.glodblock.functionalchemical.common.inventory.ControllerChemicalHandler;
import com.github.glodblock.functionalchemical.util.asm.ChemicalController;
import com.github.glodblock.functionalchemical.util.asm.ChemicalModule;
import com.hrznstudio.titanium.block.BasicTileBlock;
import mekanism.api.chemical.ChemicalResource;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(StorageControllerTile.class)
public abstract class MixinStorageControllerTile implements ChemicalController, ChemicalHost {

    @Shadow(remap = false)
    protected ConnectedDrawers connectedDrawers;
    @Unique
    private ControllerChemicalHandler chemHandler;

    @Inject(
            method = "<init>",
            at = @At("TAIL"),
            remap = false
    )
    private void init(BasicTileBlock<?> base, BlockEntityType<?> entityType, BlockPos pos, BlockState state, CallbackInfo ci) {
        this.chemHandler = new ControllerChemicalHandler(this);
    }

    @Redirect(
            method = "serverTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lcom/buuz135/functionalstorage/block/tile/StorageControllerTile;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/buuz135/functionalstorage/util/ConnectedDrawers;getExtensions()I"
            ),
            remap = false
    )
    private int compareSize(ConnectedDrawers instance) {
        return instance.getExtensions() + this.getChemicalModule().size();
    }

    @Override
    public ControllerChemicalHandler getHandler() {
        return this.chemHandler;
    }

    @Unique
    public ChemicalModule getChemicalModule() {
        return (ChemicalModule) this.connectedDrawers;
    }

    @Override
    public ResourceHandler<ChemicalResource> getChemicalHandler() {
        return this.chemHandler;
    }

}
