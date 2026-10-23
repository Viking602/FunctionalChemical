package com.github.glodblock.functionalchemical.datagen;

import com.buuz135.functionalstorage.util.StorageTags;
import com.github.glodblock.functionalchemical.common.FCSingletons;
import com.hrznstudio.titanium.block.BasicBlock;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FCRecipeProvider extends RecipeProvider {

    private final Lazy<List<Block>> blocksToProcess;

    public FCRecipeProvider(HolderLookup.Provider lookup, RecipeOutput output, Lazy<List<Block>> blocksToProcess) {
        super(lookup, output);
        this.blocksToProcess = blocksToProcess;
    }

    public static class Runner extends RecipeProvider.Runner {
        private final Lazy<List<Block>> blocks;
        public Runner(DataGenerator gen, Lazy<List<Block>> blocks, CompletableFuture<HolderLookup.Provider> lookup) {
            super(gen.getPackOutput(), lookup); this.blocks = blocks;
        }
        @Override protected RecipeProvider createRecipeProvider(HolderLookup.Provider lookup, RecipeOutput output) { return new FCRecipeProvider(lookup, output, blocks); }
        @Override public String getName() { return "Functional Chemical recipes"; }
    }
    @Override
    protected void buildRecipes() {
        for (Block block : blocksToProcess.get()) {
            var drawer = (com.github.glodblock.functionalchemical.common.blocks.ChemicalDrawerBlock) block;
            boolean radioactive = drawer instanceof com.github.glodblock.functionalchemical.common.blocks.RadioactiveDrawerBlock;
            boolean framed = drawer instanceof com.buuz135.functionalstorage.block.FramedBlock;
            int slots = drawer.getType().getSlots();
            var material = framed ? net.neoforged.neoforge.common.Tags.Items.NUGGETS_IRON : net.minecraft.tags.ItemTags.PLANKS;
            var tank = radioactive ? mekanism.common.registries.MekanismBlocks.RADIOACTIVE_WASTE_BARREL : mekanism.common.registries.MekanismBlocks.BASIC_CHEMICAL_TANK;
            var builder = net.minecraft.data.recipes.ShapedRecipeBuilder.shaped(items, net.minecraft.data.recipes.RecipeCategory.MISC, block, slots);
            if (slots == 1) builder.pattern("PPP").pattern("PCP").pattern("PPP");
            else if (slots == 2) builder.pattern("PCP").pattern("PPP").pattern("PCP");
            else builder.pattern("CPC").pattern("PPP").pattern("CPC");
            builder.define('P', material).define('C', tank).unlockedBy("has_tank", has(tank)).save(output);
        }
        net.minecraft.data.recipes.ShapedRecipeBuilder.shaped(items, net.minecraft.data.recipes.RecipeCategory.MISC, FCSingletons.DECAY_UPGRADE)
            .pattern("EEE").pattern("BDB").pattern("EEE").define('E', MekanismItems.HDPE_SHEET)
            .define('B', MekanismTags.Items.INGOTS_BRONZE).define('D', StorageTags.DRAWER)
            .unlockedBy("has_hdpe", has(MekanismItems.HDPE_SHEET)).save(output);
        net.minecraft.data.recipes.ShapedRecipeBuilder.shaped(items, net.minecraft.data.recipes.RecipeCategory.MISC, FCSingletons.ADV_DECAY_UPGRADE)
            .pattern("DDD").pattern("DXD").pattern("DDD").define('D', FCSingletons.DECAY_UPGRADE)
            .define('X', MekanismTags.Items.STORAGE_BLOCKS_REFINED_GLOWSTONE)
            .unlockedBy("has_decay_upgrade", has(FCSingletons.DECAY_UPGRADE)).save(output);
    }
}
