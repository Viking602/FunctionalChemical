package com.github.glodblock.functionalchemical.datagen;

import com.github.glodblock.functionalchemical.FunctionalChemical;
import com.hrznstudio.titanium.datagenerator.loot.TitaniumLootTableProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Optional;

@EventBusSubscriber(modid = FunctionalChemical.MODID)
public class FCDataGen {

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent.Client dataEvent) {
        final Lazy<List<Block>> blocksToProcess = Lazy.of(
                () -> BuiltInRegistries.BLOCK.stream().filter(
                        (basicBlock) -> Optional.of(BuiltInRegistries.BLOCK.getKey(basicBlock))
                                .map(Identifier::getNamespace)
                                .filter(FunctionalChemical.MODID::equalsIgnoreCase)
                                .isPresent()).toList());
        var gen = dataEvent.getGenerator();
        gen.addProvider(true, new FCBlockTagProvider(gen, dataEvent.getLookupProvider()));
        gen.addProvider(true, new FCRecipeProvider.Runner(gen, blocksToProcess, dataEvent.getLookupProvider()));
        gen.addProvider(true, new TitaniumLootTableProvider(gen, blocksToProcess, dataEvent.getLookupProvider()));
    }

}
