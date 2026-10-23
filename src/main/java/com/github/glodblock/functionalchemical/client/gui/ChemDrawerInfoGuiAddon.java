package com.github.glodblock.functionalchemical.client.gui;

import com.github.glodblock.functionalchemical.common.tileentities.ChemicalDrawerTile;
import com.github.glodblock.functionalchemical.util.FCUtil;
import com.hrznstudio.titanium.client.screen.addon.BasicScreenAddon;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import net.minecraft.client.renderer.RenderPipelines;
import com.github.glodblock.functionalchemical.common.inventory.ChemicalContents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.InventoryMenu;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class ChemDrawerInfoGuiAddon extends BasicScreenAddon {

    private final Identifier gui;
    private final int slotAmount;
    private final boolean locked;
    private final Function<Integer, Pair<Integer, Integer>> slotPosition;
    private final Supplier<ChemicalDrawerTile.SyncTank> chemTankSupplier;
    private final Supplier<ChemicalDrawerTile.SyncFilter> filterSupplier;
    private final Function<Integer, Long> slotMaxAmount;

    public ChemDrawerInfoGuiAddon(int posX, int posY, Identifier gui, int slotAmount, boolean locked, Function<Integer, Pair<Integer, Integer>> slotPosition, Supplier<ChemicalDrawerTile.SyncTank> chemTankSupplier, Supplier<ChemicalDrawerTile.SyncFilter> filterSupplier, Function<Integer, Long> slotMaxAmount) {
        super(posX, posY);
        this.gui = gui;
        this.slotAmount = slotAmount;
        this.locked = locked;
        this.slotPosition = slotPosition;
        this.chemTankSupplier = chemTankSupplier;
        this.filterSupplier = filterSupplier;
        this.slotMaxAmount = slotMaxAmount;
    }

    public static Rect2i getSizeForSlots(int currentSlot, int slotAmount) {
        if (slotAmount == 1) {
            return new Rect2i(9, 9, 30, 30);
        }
        if (slotAmount == 2) {
            if (currentSlot == 0) return new Rect2i(0, 30, 48, 13);
            if (currentSlot == 1) return new Rect2i(0, 6, 48, 13);
        }
        if (slotAmount == 4) {
            if (currentSlot == 0) return new Rect2i(30, 30, 16, 16);
            if (currentSlot == 1) return new Rect2i(2, 30, 16, 16);
            if (currentSlot == 2) return new Rect2i(30, 2, 16, 16);
            if (currentSlot == 3) return new Rect2i(2, 2, 16, 16);
        }
        return new Rect2i(0, 0, 0, 0);
    }

    public static Rect2i getSizeForHoverSlots(int currentSlot, int slotAmount) {
        if (slotAmount == 1) {
            return new Rect2i(9, 9, 30, 30);
        }
        if (slotAmount == 2) {
            if (currentSlot == 0) return new Rect2i(6, 30, 36, 12);
            if (currentSlot == 1) return new Rect2i(6, 6, 36, 12);
        }
        if (slotAmount == 4) {
            if (currentSlot == 0) return new Rect2i(30, 30, 12, 12);
            if (currentSlot == 1) return new Rect2i(6, 30, 12, 12);
            if (currentSlot == 2) return new Rect2i(30, 6, 12, 12);
            if (currentSlot == 3) return new Rect2i(6, 6, 12, 12);
        }
        return new Rect2i(0, 0, 0, 0);
    }

    @Override
    public int getXSize() {
        return 0;
    }

    @Override
    public int getYSize() {
        return 0;
    }

    @Override
    public void drawBackgroundLayer(GuiGraphicsExtractor graphics, Screen screen, IAssetProvider provider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
        for (var i = 0; i < this.slotAmount; i++) {
            var stack = this.chemTankSupplier.get().handler().getChemicalInTank(i);
            final ChemicalContents chemStack;
            if (stack.isEmpty()) {
                if (this.locked) {
                    chemStack = this.filterSupplier.get().filter()[i];
                } else {
                    chemStack = ChemicalContents.EMPTY;
                }
            } else {
                chemStack = stack;
            }
            if (!chemStack.isEmpty()) {
                renderChem(graphics, guiX, guiY, chemStack, i, this.slotAmount);
            }
        }
        var size = 16 * 2 + 16;
        graphics.blit(RenderPipelines.GUI_TEXTURED, this.gui, guiX + getPosX(), guiY + getPosY(), 0, 0, size, size, size, size);
        var font = Minecraft.getInstance().font;
        for (var i = 0; i < this.slotAmount; i++) {
            var chemStack = this.chemTankSupplier.get().handler().getChemicalInTank(i);
            if (!chemStack.isEmpty()) {
                var x = guiX + slotPosition.apply(i).getLeft() + getPosX();
                var y = guiY + slotPosition.apply(i).getRight() + getPosY();
                var amount = FCUtil.getFormatedChemBigNumber(chemStack.getAmount()) + "/" + FCUtil.getFormatedChemBigNumber(slotMaxAmount.apply(i));
                var scale = 0.5f;
                graphics.pose().pushMatrix();
                graphics.pose().scale(scale, scale);
                graphics.text(font, amount, (int) ((x + 17 - (float) font.width(amount) / 2) / scale), (int) ((y + 12) / scale), 0xFFFFFFFF, true);
                graphics.pose().popMatrix();
            }
        }
    }

    @Override
    public void drawForegroundLayer(GuiGraphicsExtractor graphics, Screen screen, IAssetProvider provider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
        for (var i = 0; i < this.slotAmount; i++) {
            var rect = getSizeForHoverSlots(i, this.slotAmount);
            var x = rect.getX() + getPosX() + guiX;
            var y = rect.getY() + getPosY() + guiY;
            if (mouseX > x && mouseX < x + rect.getWidth() && mouseY > y && mouseY < y + rect.getHeight()) {
                x = getPosX() + rect.getX();
                y = getPosY() + rect.getY();
                graphics.fill(x, y, x + rect.getWidth(), y + rect.getHeight(), -2130706433);
                var componentList = new ArrayList<Component>();
                ChemicalContents over = this.chemTankSupplier.get().handler().getChemicalInTank(i);
                if (over.isEmpty()) {
                    componentList.add(Component.translatable("gui.functionalchemical.chemical").withStyle(ChatFormatting.GOLD).append(Component.literal("Empty").withStyle(ChatFormatting.WHITE)));
                } else {
                    componentList.add(Component.translatable("gui.functionalchemical.chemical").withStyle(ChatFormatting.GOLD).append(over.getTextComponent().copy().withStyle(ChatFormatting.WHITE)));
                    var amount = FCUtil.getFormatedChemBigNumber(over.getAmount()) + "/" + FCUtil.getFormatedChemBigNumber(this.slotMaxAmount.apply(i));
                    componentList.add(Component.translatable("gui.functionalstorage.amount").withStyle(ChatFormatting.GOLD).append(Component.literal(amount).withStyle(ChatFormatting.WHITE)));
                }
                componentList.add(Component.translatable("gui.functionalstorage.slot").withStyle(ChatFormatting.GOLD).append(Component.literal(i + "").withStyle(ChatFormatting.WHITE)));
                graphics.setTooltipForNextFrame(Minecraft.getInstance().font, componentList, Optional.empty(), mouseX - guiX, mouseY - guiY);
            }
        }
    }

    public void renderChem(GuiGraphicsExtractor graphics, int guiX, int guiY, ChemicalContents chemStack, int slot, int slotAmount) {
        var sprite = mekanism.client.render.MekanismRenderer.getSprite(chemStack.getChemical().getIcon());
        Rect2i rect = getSizeForSlots(slot, slotAmount);
        int tint = net.minecraft.util.ARGB.opaque(chemStack.getChemicalTint());
        for (int x = 0; x < rect.getWidth(); x += 16) {
            for (int y = 0; y < rect.getHeight(); y += 16) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, getPosX() + guiX + rect.getX() + x,
                        getPosY() + guiY + rect.getY() + y, Math.min(16, rect.getWidth() - x), Math.min(16, rect.getHeight() - y), tint);
            }
        }
    }
}
