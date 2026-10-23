package com.github.glodblock.functionalchemical.client.render.item;

import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.buuz135.functionalstorage.util.Utils;
import com.github.glodblock.functionalchemical.common.blocks.ChemicalDrawerBlock;
import com.github.glodblock.functionalchemical.common.inventory.ChemicalContents;
import com.github.glodblock.functionalchemical.client.render.tesr.ChemDrawerTileTESR;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3fc;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;

public class ChemDrawerItemRender implements SpecialModelRenderer<ChemDrawerItemRender.RenderData> {
    public record RenderData(List<ChemicalContents> contents, ControllableDrawerTile.DrawerOptions options) {}

    @Override public RenderData extractArgument(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem item) || !(item.getBlock() instanceof ChemicalDrawerBlock block) || !stack.has(FSAttachments.TILE)) return null;
        var tag = stack.get(FSAttachments.TILE);
        if (tag.contains("TitaniumData")) tag = tag.getCompoundOrEmpty("TitaniumData");
        var options = new ControllableDrawerTile.DrawerOptions();
        options.deserializeNBT(Utils.registryAccess(), tag.getCompoundOrEmpty("drawerOptions"));
        var tanks = tag.getCompoundOrEmpty("chemTank");
        List<ChemicalContents> contents = new ArrayList<>();
        for (int slot = 0; slot < block.getType().getSlots(); slot++) {
            contents.add(ChemicalContents.parseOptional(Utils.registryAccess(), tanks.getCompoundOrEmpty("#" + slot).getCompoundOrEmpty("stored")));
        }
        return new RenderData(List.copyOf(contents), options);
    }

    @Override public void submit(RenderData data, PoseStack pose, SubmitNodeCollector collector, int light, int overlay, boolean foil, int outlineColor) {
        if (data == null) return;
        pose.mulPose(Axis.YP.rotationDegrees(180));
        pose.translate(-1, 0, -1);
        int slots = data.contents().size();
        for (int slot = 0; slot < slots; slot++) {
            var chemical = data.contents().get(slot);
            if (chemical.isEmpty()) continue;
            pose.pushPose();
            if (slots == 2 && slot == 1) pose.translate(0, 0.5, 0);
            if (slots == 4) pose.translate(slot % 2 == 0 ? 0.5 : 0, slot >= 2 ? 0.5 : 0, 0);
            var bounds = new AABB(1 / 16D, 1.25 / 16D, 1 / 16D, (slots == 4 ? 8 : 15) / 16D, (1.25 + (slots == 1 ? 12.5 : 5.5)) / 16D, 15 / 16D);
            ChemDrawerTileTESR.renderChemStack(pose, collector, light, chemical, chemical.getAmount(), chemical.getAmount(), 0.007F, data.options(), bounds, slots == 4);
            pose.popPose();
        }
    }
    @Override public void getExtents(Consumer<Vector3fc> output) {}
    public enum Unbaked implements SpecialModelRenderer.Unbaked<RenderData> {
        INSTANCE;
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(INSTANCE);
        @Override public ChemDrawerItemRender bake(SpecialModelRenderer.BakingContext context) { return new ChemDrawerItemRender(); }
        @Override public MapCodec<Unbaked> type() { return CODEC; }
    }
}
