package com.github.glodblock.functionalchemical.client;

import com.github.glodblock.functionalchemical.client.render.item.ChemDrawerItemRender;
import com.github.glodblock.functionalchemical.client.render.tesr.ChemDrawerTileTESR;
import com.github.glodblock.functionalchemical.common.FCSingletons;
import com.github.glodblock.functionalchemical.common.FCRegistryHandler;


import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

public class ClientRegistryHandler {

    public static final ClientRegistryHandler INSTANCE = new ClientRegistryHandler();

    @SubscribeEvent
    public void registerModels(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(FCRegistryHandler.INSTANCE.<com.github.glodblock.functionalchemical.common.tileentities.ChemicalDrawerTile>getTileType(FCSingletons.CHEM_DRAWER_1), ChemDrawerTileTESR::new);
        event.registerBlockEntityRenderer(FCRegistryHandler.INSTANCE.<com.github.glodblock.functionalchemical.common.tileentities.ChemicalDrawerTile>getTileType(FCSingletons.CHEM_DRAWER_2), ChemDrawerTileTESR::new);
        event.registerBlockEntityRenderer(FCRegistryHandler.INSTANCE.<com.github.glodblock.functionalchemical.common.tileentities.ChemicalDrawerTile>getTileType(FCSingletons.CHEM_DRAWER_4), ChemDrawerTileTESR::new);
        event.registerBlockEntityRenderer(FCRegistryHandler.INSTANCE.<com.github.glodblock.functionalchemical.common.tileentities.ChemicalDrawerTile>getTileType(FCSingletons.RADIOACTIVE_DRAWER), ChemDrawerTileTESR::new);
        event.registerBlockEntityRenderer(FCRegistryHandler.INSTANCE.<com.github.glodblock.functionalchemical.common.tileentities.ChemicalDrawerTile>getTileType(FCSingletons.FRAMED_CHEM_DRAWER_1), ChemDrawerTileTESR::new);
        event.registerBlockEntityRenderer(FCRegistryHandler.INSTANCE.<com.github.glodblock.functionalchemical.common.tileentities.ChemicalDrawerTile>getTileType(FCSingletons.FRAMED_CHEM_DRAWER_2), ChemDrawerTileTESR::new);
        event.registerBlockEntityRenderer(FCRegistryHandler.INSTANCE.<com.github.glodblock.functionalchemical.common.tileentities.ChemicalDrawerTile>getTileType(FCSingletons.FRAMED_CHEM_DRAWER_4), ChemDrawerTileTESR::new);
        event.registerBlockEntityRenderer(FCRegistryHandler.INSTANCE.<com.github.glodblock.functionalchemical.common.tileentities.ChemicalDrawerTile>getTileType(FCSingletons.FRAMED_RADIOACTIVE_DRAWER), ChemDrawerTileTESR::new);
    }

    @SubscribeEvent
    public void wrapFramedModels(ModelEvent.ModifyBakingResult event) {
        event.getBakingResult().blockStateModels().replaceAll((state, model) ->
            state.getBlock() instanceof com.github.glodblock.functionalchemical.common.blocks.ChemicalDrawerBlock
            && state.getBlock() instanceof com.buuz135.functionalstorage.block.FramedBlock
            ? new com.buuz135.functionalstorage.client.loader.FramedModel(model) : model);
    }
    @SubscribeEvent
    public void framedColors(net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(java.util.List.of(new com.buuz135.functionalstorage.client.FramedColors()), FCSingletons.FRAMED_CHEM_DRAWER_1,
            FCSingletons.FRAMED_CHEM_DRAWER_2, FCSingletons.FRAMED_CHEM_DRAWER_4, FCSingletons.FRAMED_RADIOACTIVE_DRAWER);
    }
    @SubscribeEvent
    public void registerSpecial(net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent event) {
        event.register(com.github.glodblock.functionalchemical.FunctionalChemical.id("chemical_drawer"), ChemDrawerItemRender.Unbaked.CODEC);
    }
    @SubscribeEvent
    public void setup(FMLClientSetupEvent event) {
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.player.ItemTooltipEvent e) -> {
            var modifier = e.getItemStack().get(FCSingletons.CHEM_STORAGE_MODIFIER);
            if (modifier != null) e.getToolTip().add(Math.min(4, e.getToolTip().size()), modifier.getTooltip(net.minecraft.network.chat.Component.translatable("storageupgrade.obj.chem_storage")).copy().withStyle(net.minecraft.ChatFormatting.GRAY));
        });
    }
}
