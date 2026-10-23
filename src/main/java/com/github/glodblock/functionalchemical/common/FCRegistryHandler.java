package com.github.glodblock.functionalchemical.common;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.github.glodblock.functionalchemical.FunctionalChemical;
import com.github.glodblock.functionalchemical.common.blocks.ChemicalDrawerBlock;
import com.github.glodblock.functionalchemical.common.cap.ChemicalHost;
import com.github.glodblock.functionalchemical.common.items.DrawerBlockItem;
import com.glodblock.github.glodium.registry.RegistryHandler;
import com.hrznstudio.titanium.block.BasicTileBlock;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.IdentityHashMap;

public class FCRegistryHandler  {

    public static final FCRegistryHandler INSTANCE = new FCRegistryHandler();
    private static final IdentityHashMap<Block, BlockEntityType<? extends BlockEntity>> TILE_TYPE = new IdentityHashMap<>();

    private final java.util.Map<String, java.util.function.Supplier<? extends Block>> blocks = new java.util.LinkedHashMap<>();
    private final java.util.Map<String, java.util.function.Supplier<? extends Item>> items = new java.util.LinkedHashMap<>();
    private final java.util.Map<String, java.util.function.Supplier<BlockEntityType<?>>> tiles = new java.util.LinkedHashMap<>();

    public void item(String name, java.util.function.Supplier<? extends Item> item) { items.put(name, item); }

    @SubscribeEvent
    public void register(net.neoforged.neoforge.registries.RegisterEvent event) {
        event.register(net.minecraft.core.registries.Registries.BLOCK, helper -> blocks.forEach((name, value) -> helper.register(FunctionalChemical.id(name), value.get())));
        event.register(net.minecraft.core.registries.Registries.ITEM, helper -> items.forEach((name, value) -> helper.register(FunctionalChemical.id(name), value.get())));
        event.register(net.minecraft.core.registries.Registries.BLOCK_ENTITY_TYPE, helper -> tiles.forEach((name, value) -> helper.register(FunctionalChemical.id(name), value.get())));
        event.register(net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE, helper -> helper.register(FunctionalChemical.id("chemical_storage_modifier"), FCSingletons.CHEM_STORAGE_MODIFIER));
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    protected void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        for (var pair : TILE_TYPE.entrySet()) {
            var block = pair.getKey();
            if (block instanceof ChemicalDrawerBlock) {
                event.registerBlockEntity(Capabilities.CHEMICAL.block(), (BlockEntityType<? extends ChemicalHost>) pair.getValue(), ChemicalHost::getChemicalHandler);
            }
        }
        event.registerBlockEntity(Capabilities.CHEMICAL.block(), (BlockEntityType<? extends ChemicalHost>) FunctionalStorage.DRAWER_CONTROLLER.type().get(), ChemicalHost::getChemicalHandler);
        event.registerBlockEntity(Capabilities.CHEMICAL.block(), (BlockEntityType<? extends ChemicalHost>) FunctionalStorage.FRAMED_DRAWER_CONTROLLER.type().get(), ChemicalHost::getChemicalHandler);
        event.registerBlockEntity(Capabilities.CHEMICAL.block(), (BlockEntityType<? extends ChemicalHost>) FunctionalStorage.CONTROLLER_EXTENSION.type().get(), ChemicalHost::getChemicalHandler);
        event.registerBlockEntity(Capabilities.CHEMICAL.block(), (BlockEntityType<? extends ChemicalHost>) FunctionalStorage.FRAMED_CONTROLLER_EXTENSION.type().get(), ChemicalHost::getChemicalHandler);
    }

    public void block(String name, java.util.function.Supplier<? extends ChemicalDrawerBlock> factory) {
        final ChemicalDrawerBlock[] block = new ChemicalDrawerBlock[1];
        this.blocks.put(name, () -> {
            block[0] = factory.get();
            return block[0];
        });
        this.items.put(name, () -> new DrawerBlockItem(block[0], new Item.Properties()
                .setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, FunctionalChemical.id(name)))
                .useBlockDescriptionPrefix()));
        this.tiles.put(name, () -> TILE_TYPE.computeIfAbsent(block[0], key -> new BlockEntityType<>(block[0].getTileEntityFactory(), block[0])));
    }

    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityType<T> getTileType(Block host) {
        return (BlockEntityType<T>) TILE_TYPE.get(host);
    }

}
