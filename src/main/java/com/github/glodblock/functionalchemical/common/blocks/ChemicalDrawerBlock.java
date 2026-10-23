package com.github.glodblock.functionalchemical.common.blocks;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.Drawer;
import com.buuz135.functionalstorage.block.DrawerBlock;
import com.buuz135.functionalstorage.block.FramedBlock;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.buuz135.functionalstorage.util.Utils;
import com.github.glodblock.functionalchemical.common.FCSingletons;
import com.github.glodblock.functionalchemical.common.FCRegistryHandler;
import com.github.glodblock.functionalchemical.common.tileentities.ChemicalDrawerTile;
import com.github.glodblock.functionalchemical.util.FCUtil;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import com.hrznstudio.titanium.util.TileUtil;
import mekanism.api.SerializationConstants;
import com.github.glodblock.functionalchemical.common.inventory.ChemicalContents;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ChemicalDrawerBlock extends Drawer<ChemicalDrawerTile> {

    private final FunctionalStorage.DrawerType type;

    public ChemicalDrawerBlock(FunctionalStorage.DrawerType type, BlockBehaviour.Properties properties) {
        super("chem_" + type.getSlots(), properties, ChemicalDrawerTile.class);
        this.type = type;
        this.setItemGroup(FCSingletons.TAB);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(Drawer.FACING_HORIZONTAL_CUSTOM, Direction.NORTH)
                .setValue(DrawerBlock.LOCKED, false)
        );
    }

    private static List<VoxelShape> getShapes(BlockState state, FunctionalStorage.DrawerType type) {
        List<VoxelShape> boxes = new ArrayList<>(DrawerBlock.CACHED_SHAPES.get(type).get(state.getValue(Drawer.FACING_HORIZONTAL_CUSTOM)));
        VoxelShape total = Shapes.block();
        boxes.add(total);
        return boxes;
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<?> getTileEntityFactory() {
        return (blockPos, state) -> new ChemicalDrawerTile(this, FCRegistryHandler.INSTANCE.getTileType(this), blockPos, state, this.type);
    }

    @Override
    public List<VoxelShape> getBoundingBoxes(@NotNull BlockState state, @NotNull BlockGetter source, @NotNull BlockPos pos) {
        return getShapes(state, this.type);
    }



    public FunctionalStorage.DrawerType getType() {
        return this.type;
    }

    @Override
    public Collection<VoxelShape> getHitShapes(BlockState state) {
        return DrawerBlock.getDefaultHitShapes(this.type, state);
    }

    public void appendHoverText(ItemStack itemStack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (itemStack.has(FSAttachments.TILE)) {
            CompoundTag root = itemStack.getOrDefault(FSAttachments.TILE, new CompoundTag());
            CompoundTag data = root.contains("TitaniumData") ? root.getCompoundOrEmpty("TitaniumData") : root;
            CompoundTag tileTag = data.getCompoundOrEmpty("chemTank");
            tooltip.add(Component.translatable("drawer.block.contents").withStyle(ChatFormatting.GRAY));
            for (int i = 0; i < this.type.getSlots(); ++i) {
                var stackTag = tileTag.getCompoundOrEmpty("#" + i).getCompoundOrEmpty(SerializationConstants.STORED);
                var stack = stackTag.isEmpty() ? ChemicalContents.EMPTY : ChemicalContents.parseOptional(Utils.registryAccess(), stackTag);
                if (!stack.isEmpty()) {
                    tooltip.add(Component.literal(" - " + ChatFormatting.YELLOW + FCUtil.getFormatedChemBigNumber(stack.getAmount()) + ChatFormatting.WHITE + " of ").append(stack.getTextComponent().copy().withStyle(ChatFormatting.GOLD)));
                }
            }
            tooltip.add(Component.translatable("drawer.block.upgrades").withStyle(ChatFormatting.GRAY));
            boolean anyupgrade = false;
            if (data.getBooleanOr("isCreative", false)) {
                tooltip.add(Component.literal("- ").withStyle(ChatFormatting.GRAY).append(Component.translatable("drawer.block.upgrades.is_creative").withStyle(ChatFormatting.LIGHT_PURPLE)));
                anyupgrade = true;
            }
            if (data.getBooleanOr("isVoid", false)) {
                tooltip.add(Component.literal("- ").withStyle(ChatFormatting.GRAY).append(Component.translatable("drawer.block.upgrades.is_void").withStyle(ChatFormatting.BLUE)));
                anyupgrade = true;
            }
            if (!anyupgrade) {
                tooltip.add(Component.literal("- ").withStyle(ChatFormatting.GRAY).append(Component.translatable("drawer.block.upgrades.none").withStyle(ChatFormatting.GRAY)));
            }
        }
        if (this instanceof FramedBlock) {
            tooltip.add(Component.translatable("frameddrawer.use").withStyle(ChatFormatting.GRAY));
        }
    }

}
