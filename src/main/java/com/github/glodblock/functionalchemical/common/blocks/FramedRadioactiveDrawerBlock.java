package com.github.glodblock.functionalchemical.common.blocks;

import com.buuz135.functionalstorage.block.FramedBlock;
import com.github.glodblock.functionalchemical.common.FCRegistryHandler;
import com.github.glodblock.functionalchemical.common.tileentities.FramedRadioactiveDrawerTile;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

public class FramedRadioactiveDrawerBlock extends RadioactiveDrawerBlock implements FramedBlock {

    public FramedRadioactiveDrawerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<?> getTileEntityFactory() {
        return (blockPos, state) -> new FramedRadioactiveDrawerTile(this, FCRegistryHandler.INSTANCE.getTileType(this), blockPos, state);
    }



}
