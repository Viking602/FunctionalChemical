package com.github.glodblock.functionalchemical.common.blocks;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.FramedBlock;
import com.github.glodblock.functionalchemical.common.FCRegistryHandler;
import com.github.glodblock.functionalchemical.common.tileentities.FramedChemicalDrawerTile;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

public class FramedChemicalDrawerBlock extends ChemicalDrawerBlock implements FramedBlock {

    public FramedChemicalDrawerBlock(FunctionalStorage.DrawerType type, Properties properties) {
        super(type, properties);
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<?> getTileEntityFactory() {
        return (blockPos, state) -> new FramedChemicalDrawerTile(this, FCRegistryHandler.INSTANCE.getTileType(this), blockPos, state, this.getType());
    }



}
