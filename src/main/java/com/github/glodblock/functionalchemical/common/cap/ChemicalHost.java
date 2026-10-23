package com.github.glodblock.functionalchemical.common.cap;

import mekanism.api.chemical.ChemicalResource;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.minecraft.core.Direction;

public interface ChemicalHost {

    ResourceHandler<ChemicalResource> getChemicalHandler();

    default ResourceHandler<ChemicalResource> getChemicalHandler(Direction side) {
        return getChemicalHandler();
    }

}
