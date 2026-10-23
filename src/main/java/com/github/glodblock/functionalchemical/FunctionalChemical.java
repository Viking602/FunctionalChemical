package com.github.glodblock.functionalchemical;

import com.github.glodblock.functionalchemical.client.ClientRegistryHandler;
import com.github.glodblock.functionalchemical.common.FCSingletons;
import com.github.glodblock.functionalchemical.common.FCRegistryHandler;
import com.github.glodblock.functionalchemical.common.tileentities.ChemicalDrawerTile;
import com.github.glodblock.functionalchemical.common.tileentities.FramedChemicalDrawerTile;
import com.github.glodblock.functionalchemical.common.tileentities.FramedRadioactiveDrawerTile;
import com.github.glodblock.functionalchemical.common.tileentities.RadioactiveDrawerTile;
import com.hrznstudio.titanium.module.ModuleController;
import com.hrznstudio.titanium.nbthandler.NBTManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(FunctionalChemical.MODID)
public class FunctionalChemical extends ModuleController {

    public static final String MODID = "functionalchemical";
    public static FunctionalChemical INSTANCE;

    public FunctionalChemical(IEventBus bus, ModContainer container) {
        super(container);
        assert INSTANCE == null;
        INSTANCE = this;
        FCSingletons.init(FCRegistryHandler.INSTANCE);
        if (FMLEnvironment.getDist().isClient()) {
            bus.register(ClientRegistryHandler.INSTANCE);
        }
        bus.register(FCRegistryHandler.INSTANCE);
    }

    @Override
    protected void initModules() {
        this.addCreativeTab("main", () -> new ItemStack(FCSingletons.CHEM_DRAWER_1), MODID, FCSingletons.TAB);
        NBTManager.getInstance().scanTileClassForAnnotations(ChemicalDrawerTile.class);
        NBTManager.getInstance().scanTileClassForAnnotations(RadioactiveDrawerTile.class);
        NBTManager.getInstance().scanTileClassForAnnotations(FramedChemicalDrawerTile.class);
        NBTManager.getInstance().scanTileClassForAnnotations(FramedRadioactiveDrawerTile.class);
    }

    public static Identifier id(String id) {
        return Identifier.fromNamespaceAndPath(MODID, id);
    }

}
