package com.github.glodblock.functionalchemical.common.tileentities;

import com.buuz135.functionalstorage.block.tile.FramedTile;
import com.buuz135.functionalstorage.util.CustomFramedDrawerModelData;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;

import javax.annotation.Nonnull;
import java.util.HashMap;

public class FramedRadioactiveDrawerTile extends RadioactiveDrawerTile implements FramedTile {

    @Save
    private CustomFramedDrawerModelData framedDrawerModelData = new CustomFramedDrawerModelData(new HashMap<>());

    public FramedRadioactiveDrawerTile(BasicTileBlock<ChemicalDrawerTile> base, BlockEntityType<ChemicalDrawerTile> blockEntityType, BlockPos pos, BlockState state) {
        super(base, blockEntityType, pos, state);
    }

    @Override
    public CustomFramedDrawerModelData getFramedDrawerModelData() {
        return this.framedDrawerModelData;
    }

    @Override
    public void setFramedDrawerModelData(CustomFramedDrawerModelData framedDrawerModelData) {
        this.framedDrawerModelData = framedDrawerModelData;
        this.markForUpdate();
        if (this.level != null && this.level.isClientSide()) {
            this.requestModelDataUpdate();
        }
    }

    @Nonnull
    public ModelData getModelData() {
        return ModelData.of(CustomFramedDrawerModelData.MODEL_PROPERTY, this.framedDrawerModelData);
    }

}
