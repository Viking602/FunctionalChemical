package com.github.glodblock.functionalchemical.common.tileentities;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.item.UpgradeItem;
import com.github.glodblock.functionalchemical.common.inventory.MultiSlotChemicalHandler;
import com.github.glodblock.functionalchemical.common.inventory.RadioactiveDrawerTank;
import com.github.glodblock.functionalchemical.common.items.DecayUpgradeItem;
import com.hrznstudio.titanium.block.BasicTileBlock;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import mekanism.api.AutomationType;
import mekanism.api.MekanismAPITags;
import mekanism.common.config.MekanismConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class RadioactiveDrawerTile extends ChemicalDrawerTile {

    public RadioactiveDrawerTile(BasicTileBlock<ChemicalDrawerTile> base, BlockEntityType<ChemicalDrawerTile> blockEntityType, BlockPos pos, BlockState state) {
        super(base, blockEntityType, pos, state, FunctionalStorage.DrawerType.X_1, 0.25);
        this.getUtilityUpgrades().setInputFilter((stack, slot) -> {
            var item = stack.getItem();
            if (item == FunctionalStorage.COLLECTOR_UPGRADE.get() || item == FunctionalStorage.VOID_UPGRADE.get()) {
                return false;
            } else {
                return item instanceof UpgradeItem && ((UpgradeItem) item).getType() == UpgradeItem.Type.UTILITY;
            }
        });
    }

    @Override
    public boolean isVoid() {
        return false;
    }

    @Override
    protected MultiSlotChemicalHandler createHandler(int size, long cap) {
        return new MultiSlotChemicalHandler(size, slot -> new RadioactiveDrawerTank(cap, () -> this, c -> this.checkFilter(slot, c.getChemical())));
    }

    @Override
    public void serverTick(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState stateOwn, @NotNull ChemicalDrawerTile drawer) {
        super.serverTick(level, pos, stateOwn, drawer);
        // Use Mek config settings
        if (level.getGameTime() % MekanismConfig.general.radioactiveWasteBarrelProcessTicks.get() == 0) {
            for (int i = 0; i < this.getUtilityUpgrades().getSlots(); i++) {
                var stack = this.getUtilityUpgrades().getStackInSlot(i);
                if (!stack.isEmpty()) {
                    int rate = getDecayRate(stack.getItem());
                    if (rate > 0) {
                        var toDecay = MekanismConfig.general.radioactiveWasteBarrelDecayAmount.get() * rate;
                        for (var tank : this.handler.getInternalTanks()) {
                            var chem = tank.getStack();
                            if (!chem.isEmpty() && !chem.is(MekanismAPITags.Chemicals.WASTE_BARREL_DECAY_BLACKLIST)) {
                                try (var tx = Transaction.openRoot()) {
                                    tank.extract(tank.resource(), (int)Math.min(Integer.MAX_VALUE, toDecay), tx, AutomationType.INTERNAL);
                                    tx.commit();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static int getDecayRate(ItemLike item) {
        if (item instanceof DecayUpgradeItem decay) {
            return decay.getSpeed();
        }
        return 0;
    }

}
