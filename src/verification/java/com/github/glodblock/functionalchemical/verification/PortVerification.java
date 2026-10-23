package com.github.glodblock.functionalchemical.verification;

import java.nio.file.*;
import java.util.*;
import com.google.gson.GsonBuilder;
import com.github.glodblock.functionalchemical.FunctionalChemical;
import com.github.glodblock.functionalchemical.common.*;
import com.github.glodblock.functionalchemical.common.blocks.*;
import com.github.glodblock.functionalchemical.common.tileentities.*;
import com.github.glodblock.functionalchemical.common.inventory.*;
import com.github.glodblock.functionalchemical.common.cap.*;
import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.StorageControllerTile;
import com.buuz135.functionalstorage.item.LinkingToolItem;
import com.buuz135.functionalstorage.item.StorageUpgradeItem;
import mekanism.api.AutomationType;
import mekanism.api.chemical.ChemicalResource;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

@Mod("functionalchemical_verification")
public final class PortVerification {
    private static final List<String> checks = new ArrayList<>();
    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); checks.add(message); }
    public PortVerification() {
        if (Boolean.getBoolean("functionalchemical.verify")) NeoForge.EVENT_BUS.addListener(PortVerification::verify);
    }
    private static int insert(ResourceHandler<ChemicalResource> h, int slot, ChemicalResource resource, int amount) {
        try (var tx = Transaction.openRoot()) { int result = h.insert(slot, resource, amount, tx); tx.commit(); return result; }
    }
    private static void verify(ServerStartedEvent event) {
        var server = event.getServer();
        var result = new LinkedHashMap<String, Object>();
        try {
            var level = server.overworld();
            var oxygen = ChemicalResource.of(MekanismChemicals.OXYGEN.get());
            var hydrogen = ChemicalResource.of(MekanismChemicals.HYDROGEN.get());
            var waste = ChemicalResource.of(MekanismChemicals.NUCLEAR_WASTE.get());
            List<ChemicalDrawerBlock> blocks = List.of(FCSingletons.CHEM_DRAWER_1, FCSingletons.CHEM_DRAWER_2, FCSingletons.CHEM_DRAWER_4,
                FCSingletons.FRAMED_CHEM_DRAWER_1, FCSingletons.FRAMED_CHEM_DRAWER_2, FCSingletons.FRAMED_CHEM_DRAWER_4,
                FCSingletons.RADIOACTIVE_DRAWER, FCSingletons.FRAMED_RADIOACTIVE_DRAWER);
            int x = 0;
            for (var block : blocks) {
                var pos = new BlockPos(x++, 80, 0);
                level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
                level.setBlockAndUpdate(pos, block.defaultBlockState());
                var tile = (ChemicalDrawerTile) level.getBlockEntity(pos);
                var handler = level.getCapability(Capabilities.CHEMICAL.block(), pos, Direction.UP);
                check(handler != null && handler.size() == block.getType().getSlots(), "registered chemical capability " + block);
                boolean radioactive = block instanceof RadioactiveDrawerBlock;
                var resource = radioactive ? waste : oxygen;
                var invalid = radioactive ? oxygen : waste;
                try (var tx = Transaction.openRoot()) {
                    check(handler.insert(0, resource, 1000, tx) == 1000, "simulated insertion " + block);
                    check(handler.insert(0, invalid, 1, tx) == 0, "rejects invalid chemical " + block);
                }
                check(handler.getAmountAsLong(0) == 0, "insert rollback " + block);
                try (var tx = Transaction.openRoot()) {
                    handler.insert(0, resource, 1000, tx);
                    try (var child = Transaction.open(tx)) { handler.extract(0, resource, 300, child); }
                    check(handler.getAmountAsLong(0) == 1000, "nested rollback " + block);
                    tx.commit();
                }
                var saved = tile.getChemTank().serializeNBT(server.registryAccess());
                var copy = (ChemicalDrawerTile) block.newBlockEntity(pos, block.defaultBlockState());
                copy.getChemTank().deserializeNBT(server.registryAccess(), saved);
                check(copy.getChemicalHandler().getAmountAsLong(0) == 1000, "persistent tank quantity " + block);
                check(copy.getChemicalHandler().getResource(0).equals(resource), "persistent chemical identity " + block);
                var reloaded=(ChemicalDrawerTile)BlockEntity.loadStatic(pos,block.defaultBlockState(),tile.saveWithFullMetadata(server.registryAccess()),server.registryAccess());
                check(reloaded!=null && reloaded.getChemicalHandler().getAmountAsLong(0)==1000,"complete tile save reload " + block);
                tile.setLocked(true);
                try (var tx = Transaction.openRoot()) { handler.extract(0, resource, 1000, tx); tx.commit(); }
                check(insert(handler,0,invalid,1000) == 0, "empty locked tank filter " + block);
                check(insert(handler,0,resource,2000) == 2000, "locked tank accepts original chemical " + block);
                var id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block);
                check(server.getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE,id)).isPresent(), "recipe loaded " + id);
            }
            var big = (ChemicalDrawerTile) level.getBlockEntity(new BlockPos(0,80,0));
            big.setLocked(false);
            var tank = big.getChemTank().handler().getInternalTanks()[0];
            tank.setCapacity(8_000_000_000L);
            tank.setContents(oxygen, 6_000_000_000L, null);
            var persisted = tank.serializeNBT(server.registryAccess());
            tank.setEmpty(); tank.deserializeNBT(server.registryAccess(),persisted);
            check(tank.amountAsLong() == 6_000_000_000L,"64-bit chemical persistence");
            try (var tx = Transaction.openRoot()) {
                check(tank.extract(oxygen,Integer.MAX_VALUE,tx,AutomationType.EXTERNAL) == Integer.MAX_VALUE,"large extraction respects per-call int bound");
            }
            check(tank.amountAsLong() == 6_000_000_000L,"large extraction rollback");
            tank.setEmpty();
            for (var tier : StorageUpgradeItem.StorageTier.values()) {
                check(new ItemStack(FunctionalStorage.STORAGE_UPGRADES.get(tier).get()).has(FCSingletons.CHEM_STORAGE_MODIFIER),"storage upgrade chemical multiplier " + tier);
            }
            big.getStorageUpgrades().setStackInSlot(0, new ItemStack(FunctionalStorage.CREATIVE_UPGRADE.get()));
            check(big.isCreative(),"creative upgrade enabled");
            check(insert(big.getChemicalHandler(),0,oxygen,1)==1,"creative tank seed");
            check(tank.amountAsLong()==Long.MAX_VALUE,"creative amount saturates at long maximum");
            try (var tx = Transaction.openRoot()) { check(tank.extract(oxygen,123456,tx,AutomationType.EXTERNAL)==123456,"creative extraction"); tx.commit(); }
            check(tank.amountAsLong()==Long.MAX_VALUE,"creative extraction preserves contents");
            big.getStorageUpgrades().setStackInSlot(0,ItemStack.EMPTY);
            tank.setEmpty();
            var target = (ChemicalDrawerTile) level.getBlockEntity(new BlockPos(1,80,0));
            target.setLocked(false); target.getChemTank().handler().getInternalTanks()[0].setEmpty();
            insert(big.getChemicalHandler(),0,oxygen,10000);
            // Direction defaults to DOWN; use an actual adjacent drawer below the source.
            var down = big.getBlockPos().below();
            level.setBlockAndUpdate(down,FCSingletons.CHEM_DRAWER_1.defaultBlockState());
            var below = (ChemicalDrawerTile) level.getBlockEntity(down);
            below.getChemTank().handler().getInternalTanks()[0].setEmpty();
            var upgrade = new ItemStack(FunctionalStorage.PUSHING_UPGRADE.get());
            var direction = com.buuz135.functionalstorage.item.UpgradeItem.getDirection(upgrade);
            if (direction != Direction.DOWN) {
                down = big.getBlockPos().relative(direction);
                level.setBlockAndUpdate(down,FCSingletons.CHEM_DRAWER_1.defaultBlockState());
                below = (ChemicalDrawerTile) level.getBlockEntity(down);
                below.getChemTank().handler().getInternalTanks()[0].setEmpty();
            }
            new MoveChemicalBehavior(true,750).work(level,big.getBlockPos(),big,upgrade,0);
            check(below.getChemicalHandler().getAmountAsLong(0)==750,"pushing upgrade transfers chemicals");
            new MoveChemicalBehavior(false,250).work(level,big.getBlockPos(),big,upgrade,0);
            check(below.getChemicalHandler().getAmountAsLong(0)==500,"pulling upgrade transfers chemicals");
            var controllerPos = new BlockPos(0,80,3);
            level.setBlockAndUpdate(controllerPos,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(controllerPos,FunctionalStorage.DRAWER_CONTROLLER.block().get().defaultBlockState());
            var controller = (StorageControllerTile<?>)level.getBlockEntity(controllerPos);
            controller.getConnectedDrawers().setLevel(level);
            controller.addConnectedDrawers(LinkingToolItem.ActionMode.ADD,big.getBlockPos(),target.getBlockPos());
            controller.getConnectedDrawers().rebuild();
            var combined = level.getCapability(Capabilities.CHEMICAL.block(),controllerPos,Direction.UP);
            check(combined != null && combined.size()==3,"controller exposes all linked chemical slots: " + (combined==null ? "null" : combined.size()) + ", linked=" + controller.getConnectedDrawers().getConnectedDrawers());
            long before=big.getChemicalHandler().getAmountAsLong(0);
            try(var tx=Transaction.openRoot()){ check(combined.extract(0,oxygen,100,tx)==100,"controller chemical extraction"); }
            check(big.getChemicalHandler().getAmountAsLong(0)==before,"controller transaction rollback");
            controller.getConnectedDrawers().rebuild();
            check(combined.size()==3,"controller rebuild avoids duplicate slots");
            result.put("status","passed");
        } catch (Throwable failure) {
            failure.printStackTrace();result.put("status","failed");result.put("error",failure.toString());
        } finally {
            result.put("checks",checks);
            try { Files.writeString(server.getServerDirectory().resolve("functionalchemical-verification.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result)); }
            catch(Exception failure){failure.printStackTrace();}
            server.halt(false);
        }
    }
}
