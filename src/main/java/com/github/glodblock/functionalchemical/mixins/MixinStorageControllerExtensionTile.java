package com.github.glodblock.functionalchemical.mixins;

import com.buuz135.functionalstorage.block.tile.StorageControllerExtensionTile;
import com.buuz135.functionalstorage.block.tile.StorageControllerTile;
import com.github.glodblock.functionalchemical.common.cap.ChemicalHost;
import mekanism.api.chemical.ChemicalResource;
import net.neoforged.neoforge.transfer.ResourceHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(StorageControllerExtensionTile.class)
public abstract class MixinStorageControllerExtensionTile implements ChemicalHost {

    @Shadow(remap = false)
    protected abstract Optional<StorageControllerTile<?>> getControllerInstance();

    @Override
    public ResourceHandler<ChemicalResource> getChemicalHandler() {
        return this.getControllerInstance().map(c -> ((ChemicalHost) c).getChemicalHandler()).orElse(null);
    }

}
