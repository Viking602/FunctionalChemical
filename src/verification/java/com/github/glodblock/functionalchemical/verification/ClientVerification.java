package com.github.glodblock.functionalchemical.verification;

import java.nio.file.*;
import java.util.*;
import com.google.gson.GsonBuilder;
import com.github.glodblock.functionalchemical.*;
import com.github.glodblock.functionalchemical.common.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@Mod(value="functionalchemical_verification", dist=Dist.CLIENT)
public final class ClientVerification {
    private static boolean started, finished;
    private static int ticks, stage;
    private static final List<String> checks = new ArrayList<>();
    private static long rendered;
    public ClientVerification(net.neoforged.bus.api.IEventBus bus) {
        if (Boolean.getBoolean("functionalchemical.verify.client")) {
            NeoForge.EVENT_BUS.addListener(ClientVerification::tick);
            bus.addListener(net.neoforged.bus.api.EventPriority.LOWEST,(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers e) -> {
                e.registerBlockEntityRenderer(FCRegistryHandler.INSTANCE.<com.github.glodblock.functionalchemical.common.tileentities.ChemicalDrawerTile>getTileType(FCSingletons.CHEM_DRAWER_1), ctx -> new com.github.glodblock.functionalchemical.client.render.tesr.ChemDrawerTileTESR(ctx) {
                    @Override public void submit(RenderState state, com.mojang.blaze3d.vertex.PoseStack pose, net.minecraft.client.renderer.SubmitNodeCollector collector, net.minecraft.client.renderer.state.level.CameraRenderState camera) {
                        rendered++;
                        super.submit(state,pose,collector,camera);
                    }
                });
            });
        }
    }
    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); checks.add(message); }
    private static void tick(ClientTickEvent.Post event) {
        if (finished) return;
        var mc = Minecraft.getInstance();
        mc.options.pauseOnLostFocus = false;
        try {
            if (!started) {
                if (!(mc.screen instanceof TitleScreen) || mc.getOverlay()!=null) return;
                check(mc.gameDirectory.toPath().toAbsolutePath().normalize().getFileName().toString().equals("test-functionalchemical-client-20261011"), "isolated client fixture");
                started = true;
                mc.createWorldOpenFlows().createFreshLevel("functionalchemical-"+System.currentTimeMillis(),
                    new LevelSettings("Functional Chemical verification", GameType.CREATIVE,
                        new LevelSettings.DifficultySettings(Difficulty.PEACEFUL, false, false), true, WorldDataConfiguration.DEFAULT),
                    new WorldOptions(602,false,false), WorldPresets::createFlatWorldDimensions, mc.screen);
                return;
            }
            if (++ticks > 5000) throw new AssertionError("Client world timeout");
            if (mc.player==null || mc.level==null || mc.getSingleplayerServer()==null || mc.player.tickCount<50 || ticks%80!=0) return;
            if (stage==0) {
                var inventory = new SimpleContainer(27);
                var server=mc.getSingleplayerServer();
                server.submit(() -> {
                    var level=server.overworld();
                    for(int x=-2;x<11;x++) for(int z=-8;z<3;z++) level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x,79,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
                    var blocks=List.of(FCSingletons.CHEM_DRAWER_1,FCSingletons.CHEM_DRAWER_2,FCSingletons.CHEM_DRAWER_4,
                        FCSingletons.FRAMED_CHEM_DRAWER_1,FCSingletons.FRAMED_CHEM_DRAWER_2,FCSingletons.FRAMED_CHEM_DRAWER_4,
                        FCSingletons.RADIOACTIVE_DRAWER,FCSingletons.FRAMED_RADIOACTIVE_DRAWER);
                    int index=0;
                    for(var block:blocks) {
                        var pos=new net.minecraft.core.BlockPos(index,80,0);
                        level.setBlockAndUpdate(pos,block.defaultBlockState());
                        var tile=(com.github.glodblock.functionalchemical.common.tileentities.ChemicalDrawerTile)level.getBlockEntity(pos);
                        var resource=mekanism.api.chemical.ChemicalResource.of(block instanceof com.github.glodblock.functionalchemical.common.blocks.RadioactiveDrawerBlock
                            ? mekanism.common.registries.MekanismChemicals.NUCLEAR_WASTE.get() : mekanism.common.registries.MekanismChemicals.OXYGEN.get());
                        for(int slot=0;slot<block.getType().getSlots();slot++) {
                            try(var tx=net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
                                tile.getChemicalHandler().insert(slot,resource,5000*(slot+1),tx);tx.commit();
                            }
                        }
                        if(tile instanceof com.buuz135.functionalstorage.block.tile.FramedTile framed) {
                            framed.setFramedDrawerModelData(new com.buuz135.functionalstorage.util.CustomFramedDrawerModelData(Map.of("side",net.minecraft.world.item.Items.DIAMOND_BLOCK,"front",net.minecraft.world.item.Items.GOLD_BLOCK)));
                        }
                        var stack=new ItemStack(block);
                        stack.set(com.buuz135.functionalstorage.item.FSAttachments.TILE,tile.saveWithoutMetadata(server.registryAccess()));
                        if(tile instanceof com.buuz135.functionalstorage.block.tile.FramedTile framed) stack.set(com.buuz135.functionalstorage.item.FSAttachments.STYLE,framed.getFramedDrawerModelData().serializeNBT(server.registryAccess()));
                        inventory.setItem(index++,stack);
                    }
                    inventory.setItem(index++,new ItemStack(FCSingletons.DECAY_UPGRADE));
                    inventory.setItem(index,new ItemStack(FCSingletons.ADV_DECAY_UPGRADE));
                    var player=server.getPlayerList().getPlayer(mc.player.getUUID());
                    player.teleportTo(level,3.5,80,-6,Set.of(),0,0,false);
                    player.openMenu(new SimpleMenuProvider((id,inv,p)->new ChestMenu(MenuType.GENERIC_9x3,id,inv,inventory,3), Component.literal("Functional Chemical 26.1.2")));
                }).join();
                for(var item:inventory.getItems()) {
                    if(item.isEmpty())continue;
                    var id=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item.getItem());
                    check(mc.getResourceManager().getResource(FunctionalChemical.id("items/"+id.getPath()+".json")).isPresent(),"item model present "+id);
                }
                stage++;
            } else if (stage==1) {
                check(mc.screen instanceof net.minecraft.client.gui.screens.inventory.ContainerScreen,"all mod items render in inventory");
                Screenshot.takeScreenshot(mc.getMainRenderTarget(), screenshot -> {
                    try (screenshot) { screenshot.writeToFile(mc.gameDirectory.toPath().resolve("functionalchemical-items.png")); }
                    catch (Exception e) { finish(false,e.toString()); }
                });
                mc.getSingleplayerServer().submit(() -> mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()).closeContainer()).join();
                stage++;
            } else if (stage==2) {
                check(rendered>0,"chemical world renderer submits geometry: "+rendered);
                for(int x=0;x<8;x++) {
                    var tile=(com.github.glodblock.functionalchemical.common.tileentities.ChemicalDrawerTile)mc.level.getBlockEntity(new net.minecraft.core.BlockPos(x,80,0));
                    check(tile!=null && tile.getChemicalHandler().getAmountAsLong(0)==5000,"world chemical quantity synchronized for drawer "+x);
                    check(mc.getBlockEntityRenderDispatcher().getRenderer(tile)!=null,"world chemical renderer registered for drawer "+x);
                }
                Screenshot.takeScreenshot(mc.getMainRenderTarget(), screenshot -> {
                    try(screenshot) { screenshot.writeToFile(mc.gameDirectory.toPath().resolve("functionalchemical-world.png")); }
                    catch(Exception e) { finish(false,e.toString()); }
                });
                mc.getSingleplayerServer().submit(() -> {
                    var server=mc.getSingleplayerServer();
                    var drawer=(com.github.glodblock.functionalchemical.common.tileentities.ChemicalDrawerTile)server.overworld().getBlockEntity(new net.minecraft.core.BlockPos(0,80,0));
                    drawer.openGui(server.getPlayerList().getPlayer(mc.player.getUUID()));
                }).join();
                stage++;
            } else {
                check(mc.screen instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen,"chemical drawer GUI opens and renders contents");
                Screenshot.takeScreenshot(mc.getMainRenderTarget(), screenshot -> {
                    try(screenshot) { screenshot.writeToFile(mc.gameDirectory.toPath().resolve("functionalchemical-gui.png")); }
                    catch(Exception e) { finish(false,e.toString()); }
                });
                finish(true,"");
            }
        } catch (Throwable failure) { failure.printStackTrace(); finish(false,failure.toString()); }
    }
    private static void finish(boolean passed,String error) {
        if (finished) return; finished=true;
        var mc=Minecraft.getInstance();
        try { Files.writeString(mc.gameDirectory.toPath().resolve("functionalchemical-client-verification.json"), new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("passed",passed,"error",error,"checks",checks))); }
        catch (Exception e) { e.printStackTrace(); }
        mc.stop();
    }
}
