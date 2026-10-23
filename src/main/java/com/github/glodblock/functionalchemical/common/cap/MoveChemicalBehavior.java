package com.github.glodblock.functionalchemical.common.cap;

import com.buuz135.functionalstorage.block.config.FunctionalStorageConfig;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.item.UpgradeItem;
import com.buuz135.functionalstorage.item.component.FunctionalUpgradeBehavior;
import com.github.glodblock.functionalchemical.common.tileentities.ChemicalDrawerTile;
import com.github.glodblock.functionalchemical.config.FunctionalChemicalConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record MoveChemicalBehavior(boolean drawerIsSource, int chemPerOperation) implements FunctionalUpgradeBehavior {

    public static final MapCodec<MoveChemicalBehavior> CODEC = RecordCodecBuilder.mapCodec((in) -> in.group(Codec.BOOL.fieldOf("drawer_is_source").forGetter(MoveChemicalBehavior::drawerIsSource), Codec.INT.fieldOf("chem_per_operation").forGetter(MoveChemicalBehavior::chemPerOperation)).apply(in, MoveChemicalBehavior::new));

    @Override
    public void work(Level level, BlockPos pos, ControllableDrawerTile<?> dr, ItemStack upgradeStack, int upgradeSlot) {
        if (dr instanceof ChemicalDrawerTile drawer) {
            Direction direction = UpgradeItem.getDirection(upgradeStack);
            var otherChemHandler = level.getCapability(Capabilities.CHEMICAL.block(), pos.relative(direction), direction.getOpposite());
            if (otherChemHandler != null) {
                var own = drawer.getChemicalHandler();
                try (var tx = Transaction.openRoot()) {
                    ResourceHandlerUtil.moveFirst(drawerIsSource ? own : otherChemHandler,
                        drawerIsSource ? otherChemHandler : own, r -> true, chemPerOperation, tx);
                    tx.commit();
                }
            }
        }
    }

    @Override
    public MapCodec<? extends FunctionalUpgradeBehavior> codec() {
        return CODEC;
    }

}
