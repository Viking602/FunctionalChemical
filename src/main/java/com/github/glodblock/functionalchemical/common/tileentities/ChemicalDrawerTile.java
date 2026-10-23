package com.github.glodblock.functionalchemical.common.tileentities;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.config.FunctionalStorageConfig;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.DrawerProperties;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.buuz135.functionalstorage.item.StorageUpgradeItem;
import com.buuz135.functionalstorage.item.UpgradeItem;
import com.buuz135.functionalstorage.item.component.SizeProvider;
import com.github.glodblock.functionalchemical.FunctionalChemical;
import com.github.glodblock.functionalchemical.client.gui.ChemDrawerInfoGuiAddon;
import com.github.glodblock.functionalchemical.common.FCSingletons;
import com.github.glodblock.functionalchemical.common.cap.ChemicalHost;
import com.github.glodblock.functionalchemical.common.cap.MoveChemicalBehavior;
import com.github.glodblock.functionalchemical.common.inventory.ChemicalDrawerTank;
import com.github.glodblock.functionalchemical.common.inventory.MultiSlotChemicalHandler;
import com.github.glodblock.functionalchemical.config.FunctionalChemicalConfig;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
import com.hrznstudio.titanium.component.inventory.InventoryComponent;
import mekanism.api.chemical.Chemical;
import com.github.glodblock.functionalchemical.common.inventory.ChemicalContents;
import mekanism.api.chemical.ChemicalResource;
import net.neoforged.neoforge.transfer.ResourceHandler;
import mekanism.api.radiation.IRadiationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.hrznstudio.titanium.nbthandler.INBTSerializable;
import org.jetbrains.annotations.NotNull;

public class ChemicalDrawerTile extends ControllableDrawerTile<ChemicalDrawerTile> implements ChemicalHost {

    private static final MoveChemicalBehavior CHEM_PUSHER = new MoveChemicalBehavior(true, FunctionalChemicalConfig.UPGRADE_PUSH_CHEMICAL);
    private static final MoveChemicalBehavior CHEM_PULLER = new MoveChemicalBehavior(false, FunctionalChemicalConfig.UPGRADE_PULL_CHEMICAL);
    protected MultiSlotChemicalHandler handler;
    @Save
    protected final SyncTank chemTank;
    @Save
    private final SyncFilter filter;
    protected final FunctionalStorage.DrawerType type;

    public ChemicalDrawerTile(BasicTileBlock<ChemicalDrawerTile> base, BlockEntityType<ChemicalDrawerTile> blockEntityType, BlockPos pos, BlockState state, FunctionalStorage.DrawerType type) {
        this(base, blockEntityType, pos, state, type, 2);
    }

    public ChemicalDrawerTile(BasicTileBlock<ChemicalDrawerTile> base, BlockEntityType<ChemicalDrawerTile> blockEntityType, BlockPos pos, BlockState state, FunctionalStorage.DrawerType type, double baseModifier) {
        super(base, blockEntityType, pos, state, new DrawerProperties((int) (type.getSlotAmount() * baseModifier), FCSingletons::getChemStorageModifier));
        this.type = type;
        this.filter = new SyncFilter(type.getSlots());
        this.handler = this.createHandler(type.getSlots(), this.getCapacity());
        this.chemTank = new SyncTank(this.handler);
        this.getUtilityUpgrades().setInputFilter((stack, slot) -> stack.getItem() != FunctionalStorage.COLLECTOR_UPGRADE.get() && stack.getItem() instanceof UpgradeItem && ((UpgradeItem) stack.getItem()).getType() == UpgradeItem.Type.UTILITY);
    }

    protected MultiSlotChemicalHandler createHandler(int size, long cap) {
        return new MultiSlotChemicalHandler(size, slot -> new ChemicalDrawerTank(cap, () -> this, c -> this.checkFilter(slot, c.getChemical())));
    }

    public long getCapacity() {
        if (this.isCreative()) {
            return Long.MAX_VALUE;
        }
        return this.getTankCapacity(this.getStorageMultiplier());
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        onRemove();
        super.preRemoveSideEffects(pos, state);
    }

    public void onRemove() {
        if (this.level != null && this.isServer() && IRadiationManager.INSTANCE.isRadiationEnabled() && !this.isCreative()) {
            IRadiationManager.INSTANCE.dumpRadiation(
                    this.level,
                    this.worldPosition,
                    java.util.Arrays.asList(this.handler.getInternalTanks()),
                    false, null
            );
        }
    }

    public RenderStatus getRenderStack(int slot) {
        var stored = this.handler.getChemicalInTank(slot);
        if (stored.isEmpty()) {
            if (this.isLocked()) {
                return new RenderStatus(this.filter.filter()[slot], true);
            } else {
                return new RenderStatus(ChemicalContents.EMPTY, false);
            }
        } else {
            return new RenderStatus(stored, false);
        }
    }

    public FunctionalStorage.DrawerType getDrawerType() {
        return type;
    }

    protected boolean checkFilter(int slot, Chemical stack) {
        if (!this.isLocked()) {
            return true;
        }
        return this.filter.filter()[slot].is(stack);
    }

    public SyncTank getChemTank() {
        return this.chemTank;
    }

    protected long getTankCapacity(double storageMultiplier) {
        return (long) Math.floor(1000D * storageMultiplier);
    }

    @Override
    public int getStorageSlotAmount() {
        return 4;
    }

    @Override
    public void setLocked(boolean locked) {
        super.setLocked(locked);
        for (int i = 0; i < this.type.getSlots(); i++) {
            this.filter.filter()[i] = this.handler.getChemicalInTank(i).copy();
            if (!this.filter.filter()[i].isEmpty()) {
                this.filter.filter()[i] = this.filter.filter()[i].copyWithAmount(1);
            }
        }
        this.syncObject(this.filter);
        this.markDirty();
    }

    @Override
    public boolean isEverythingEmpty() {
        if (this.getPriority() != 0) {
            return false;
        }
        if (this.isLocked()) {
            return false;
        }
        for (var tank : this.handler.getInternalTanks()) {
            if (!tank.getStack().isEmpty()) {
                return false;
            }
        }
        for (int i = 0; i < this.getStorageUpgrades().getSlots(); i++) {
            if (!this.getStorageUpgrades().getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        for (int i = 0; i < this.getUtilityUpgrades().getSlots(); i++) {
            if (!this.getUtilityUpgrades().getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void initClient() {
        super.initClient();
        var slotName = "";
        if (type.getSlots() == 2) {
            slotName = "_2";
        }
        if (type.getSlots() == 4) {
            slotName = "_4";
        }
        String finalSlotName = slotName;
        addGuiAddonFactory(() -> new ChemDrawerInfoGuiAddon(64, 16,
                FunctionalChemical.id("textures/block/chem_front" + finalSlotName + ".png"),
                type.getSlots(),
                this.isLocked(),
                type.getSlotPosition(),
                this::getChemTank,
                () -> this.filter,
                i -> this.getCapacity()
        ));
    }

    @Override
    public void serverTick(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState stateOwn, @NotNull ChemicalDrawerTile drawer) {
        super.serverTick(level, pos, stateOwn, drawer);
        if (level.getGameTime() % (long) FunctionalStorageConfig.UPGRADE_TICK == 0) {
            for (int i = 0; i < this.getUtilityUpgrades().getSlots(); i++) {
                var stack = this.getUtilityUpgrades().getStackInSlot(i);
                if (!stack.isEmpty()) {
                    var item = stack.getItem();
                    if (item == FunctionalStorage.PUSHING_UPGRADE.get()) {
                        CHEM_PUSHER.work(this.level, this.getBlockPos(), this, stack, i);
                    }
                    if (item == FunctionalStorage.PULLING_UPGRADE.get()) {
                        CHEM_PULLER.work(this.level, this.getBlockPos(), this, stack, i);
                    }
                }
            }
        }
    }

    @Override
    public InventoryComponent<ControllableDrawerTile<ChemicalDrawerTile>> getStorageUpgradesConstructor() {
        return new InventoryComponent<ControllableDrawerTile<ChemicalDrawerTile>>("storage_upgrades", 10, 70, this.getStorageSlotAmount()) {
            @NotNull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (ChemicalDrawerTile.this.isStorageUpgradeLocked()) {
                    return ItemStack.EMPTY;
                } else {
                    ItemStack stack = this.getStackInSlot(slot);
                    if (stack.has(FCSingletons.CHEM_STORAGE_MODIFIER)) {
                        ItemStack[] replacement = new ItemStack[this.getSlots()];
                        replacement[slot] = ItemStack.EMPTY;
                        var newSize = SizeProvider.calculateAsFactor(this, FCSingletons::getChemStorageModifier, (float) ChemicalDrawerTile.this.baseSize, replacement);
                        if (!ChemicalDrawerTile.this.canChangeMultiplier(newSize)) {
                            return ItemStack.EMPTY;
                        }
                    }
                    return super.extractItem(slot, amount, simulate);
                }
            }
        }
        .setInputFilter((stack, integer) -> {
            if (this.isStorageUpgradeLocked()) {
                return false;
            } else if (stack.getItem() == FunctionalStorage.STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.IRON).get()) {
                return true;
            } else {
                return stack.has(FCSingletons.CHEM_STORAGE_MODIFIER) || stack.is(FunctionalStorage.CREATIVE_UPGRADE);
            }
        })
        .setOnSlotChanged((stack, integer) -> {
            setNeedsUpgradeCache(true);
            this.setCapacity(this.getCapacity());
            this.syncObject(this.chemTank);
            this.markDirty();
        })
        .setSlotLimit(1);
    }

    protected boolean canChangeMultiplier(double newSizeMultiplier) {
        for (var tank : this.handler.getInternalTanks()) {
            if (tank.getStored() > this.getTankCapacity(newSizeMultiplier)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void syncObject(@NotNull Object object) {
        if (this.level != null) {
            super.syncObject(object);
        }
    }

    @Override
    public void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        this.setCapacity(this.getCapacity());
    }

    @Override
    public @NotNull ChemicalDrawerTile getSelf() {
        return this;
    }

    protected void setCapacity(long capacity) {
        for (var mt : this.handler.getInternalTanks()) {
            mt.setCapacity(capacity);
        }
    }

    public void markDirty() {
        if (this.level != null) {
            this.level.blockEntityChanged(this.worldPosition);
        }
    }

    public void onChange() {
        this.markDirty();
        this.syncObject(this.chemTank);
    }

    @Override
    public ResourceHandler<ChemicalResource> getChemicalHandler() {
        return this.handler;
    }

    public record SyncFilter(ChemicalContents[] filter) implements INBTSerializable<CompoundTag> {

        public SyncFilter(int slot) {
            this(new ChemicalContents[slot]);
            for (int i = 0; i < slot; i++) {
                this.filter[i] = ChemicalContents.EMPTY;
            }
        }

        @Override
        public CompoundTag serializeNBT(HolderLookup.@NotNull Provider lookup) {
            var filterTag = new CompoundTag();
            for (int i = 0; i < this.filter.length; i++) {
                if (!this.filter[i].isEmpty()) {
                    filterTag.put("#" + i, this.filter[i].save(lookup));
                }
            }
            return filterTag;
        }

        @Override
        public void deserializeNBT(HolderLookup.@NotNull Provider lookup, @NotNull CompoundTag nbt) {
            for (int i = 0; i < this.filter.length; i++) {
                if (nbt.contains("#" + i)) {
                    this.filter[i] = ChemicalContents.parseOptional(lookup, nbt.getCompoundOrEmpty("#" + i));
                } else {
                    this.filter[i] = ChemicalContents.EMPTY;
                }
            }
        }

    }

    public record SyncTank(MultiSlotChemicalHandler handler) implements INBTSerializable<CompoundTag> {

        @Override
        public CompoundTag serializeNBT(HolderLookup.@NotNull Provider lookup) {
            var chemTankTag = new CompoundTag();
            var tanks = this.handler.getInternalTanks();
            for (int i = 0; i < tanks.length; i++) {
                if (!tanks[i].isEmpty()) {
                    chemTankTag.put("#" + i, tanks[i].serializeNBT(lookup));
                }
            }
            return chemTankTag;
        }

        @Override
        public void deserializeNBT(HolderLookup.@NotNull Provider lookup, @NotNull CompoundTag nbt) {
            var tanks = this.handler.getInternalTanks();
            for (int i = 0; i < tanks.length; i++) {
                if (nbt.contains("#" + i)) {
                    tanks[i].deserializeNBT(lookup, nbt.getCompoundOrEmpty("#" + i));
                } else {
                    tanks[i].setEmpty();
                }
            }
        }

    }

    public record RenderStatus(ChemicalContents stack, boolean isFilter) {

        public boolean valid() {
            return this.stack != null && !this.stack.isEmpty();
        }

    }

}
