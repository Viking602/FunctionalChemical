package com.github.glodblock.functionalchemical.util.asm;

import mekanism.api.chemical.ChemicalResource;
import net.neoforged.neoforge.transfer.ResourceHandler;

import java.util.List;

public interface ChemicalModule {

    default List<ResourceHandler<ChemicalResource>> getHandlers() {
        return List.of();
    }

    default int size() {
        return 0;
    }

}
