package com.github.glodblock.functionalchemical.common;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.item.component.SizeProvider;
import com.github.glodblock.functionalchemical.FunctionalChemical;
import com.github.glodblock.functionalchemical.common.blocks.ChemicalDrawerBlock;
import com.github.glodblock.functionalchemical.common.blocks.FramedChemicalDrawerBlock;
import com.github.glodblock.functionalchemical.common.blocks.FramedRadioactiveDrawerBlock;
import com.github.glodblock.functionalchemical.common.blocks.RadioactiveDrawerBlock;
import com.github.glodblock.functionalchemical.common.items.DecayUpgradeItem;
import com.glodblock.github.glodium.util.GlodUtil;
import com.hrznstudio.titanium.tab.TitaniumTab;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class FCSingletons {

    public static final TitaniumTab TAB = new TitaniumTab(FunctionalChemical.id("main"));

    public static final DataComponentType<SizeProvider> CHEM_STORAGE_MODIFIER = DataComponentType.<SizeProvider>builder().persistent(SizeProvider.CODEC).build();

    public static ChemicalDrawerBlock CHEM_DRAWER_1;
    public static ChemicalDrawerBlock CHEM_DRAWER_2;
    public static ChemicalDrawerBlock CHEM_DRAWER_4;
    public static ChemicalDrawerBlock RADIOACTIVE_DRAWER;

    public static FramedChemicalDrawerBlock FRAMED_CHEM_DRAWER_1;
    public static FramedChemicalDrawerBlock FRAMED_CHEM_DRAWER_2;
    public static FramedChemicalDrawerBlock FRAMED_CHEM_DRAWER_4;
    public static FramedRadioactiveDrawerBlock FRAMED_RADIOACTIVE_DRAWER;

    public static DecayUpgradeItem DECAY_UPGRADE;
    public static DecayUpgradeItem ADV_DECAY_UPGRADE;

    public static void init(FCRegistryHandler handler) {
        handler.block("chem_1", () -> CHEM_DRAWER_1 = new ChemicalDrawerBlock(FunctionalStorage.DrawerType.X_1, blockProperties("chem_1")));
        handler.block("chem_2", () -> CHEM_DRAWER_2 = new ChemicalDrawerBlock(FunctionalStorage.DrawerType.X_2, blockProperties("chem_2")));
        handler.block("chem_4", () -> CHEM_DRAWER_4 = new ChemicalDrawerBlock(FunctionalStorage.DrawerType.X_4, blockProperties("chem_4")));
        handler.block("framed_chem_1", () -> FRAMED_CHEM_DRAWER_1 = new FramedChemicalDrawerBlock(FunctionalStorage.DrawerType.X_1, blockProperties("framed_chem_1")));
        handler.block("framed_chem_2", () -> FRAMED_CHEM_DRAWER_2 = new FramedChemicalDrawerBlock(FunctionalStorage.DrawerType.X_2, blockProperties("framed_chem_2")));
        handler.block("framed_chem_4", () -> FRAMED_CHEM_DRAWER_4 = new FramedChemicalDrawerBlock(FunctionalStorage.DrawerType.X_4, blockProperties("framed_chem_4")));
        handler.block("radioactive_drawer", () -> RADIOACTIVE_DRAWER = new RadioactiveDrawerBlock(blockProperties("radioactive_drawer")));
        handler.block("framed_radioactive_drawer", () -> FRAMED_RADIOACTIVE_DRAWER = new FramedRadioactiveDrawerBlock(blockProperties("framed_radioactive_drawer")));
        handler.item("decay_upgrade", () -> DECAY_UPGRADE = new DecayUpgradeItem(itemProperties("decay_upgrade"), false));
        handler.item("adv_decay_upgrade", () -> ADV_DECAY_UPGRADE = new DecayUpgradeItem(itemProperties("adv_decay_upgrade").rarity(Rarity.EPIC), true));
    }

    private static BlockBehaviour.Properties blockProperties(String name) {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS).setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, FunctionalChemical.id(name)));
    }
    private static Item.Properties itemProperties(String name) {
        return new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, FunctionalChemical.id(name)));
    }

    public static DataComponentType<SizeProvider> getChemStorageModifier() {
        return CHEM_STORAGE_MODIFIER;
    }

}
