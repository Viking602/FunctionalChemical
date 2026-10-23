package com.github.glodblock.functionalchemical.common.inventory;

import java.util.function.*;
import com.github.glodblock.functionalchemical.common.tileentities.ChemicalDrawerTile;
import mekanism.api.AutomationType;
import mekanism.api.chemical.*;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.functions.ConstantPredicates;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public class ChemicalDrawerTank extends BasicChemicalTank {
    private final Supplier<ChemicalDrawerTile> host;
    private long capacity;
    public ChemicalDrawerTank(long capacity, Supplier<ChemicalDrawerTile> host, Predicate<ChemicalResource> filter) { this(capacity,host,filter,null); }
    public ChemicalDrawerTank(long capacity, Supplier<ChemicalDrawerTile> host, Predicate<ChemicalResource> filter, ChemicalAttributeValidator validator) {
        super(0, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), filter, null, null, validator, () -> host.get().onChange());
        this.host=host;this.capacity=capacity;
    }
    public void setCapacity(long value) {
        if (value < 0) throw new IllegalArgumentException("Negative capacity");
        if (super.amountAsLong() > value) setContents(resource(),value,null);
        capacity=value;
    }
    @Override public long capacityAsLong(ChemicalResource resource) {
        if (!resource.isEmpty() && !isValid(resource)) return 0;
        return host.get().isCreative() ? Long.MAX_VALUE : capacity;
    }
    @Override public long amountAsLong() { return host.get().isCreative() && !asStack().isEmpty() ? Long.MAX_VALUE : super.amountAsLong(); }
    @Override public int insert(ChemicalResource resource,int amount,TransactionContext tx,AutomationType type) {
        int inserted=super.insert(resource,amount,tx,type);
        if (host.get().isVoid() && isValid(resource) && (host.get().isLocked() || resource.equals(resource()))) return amount;
        return inserted;
    }
    @Override public int extract(ChemicalResource resource,int amount,TransactionContext tx,AutomationType type) {
        TransferPreconditions.checkNonEmptyNonNegative(resource,amount);
        if (host.get().isCreative()) return !isEmpty() && resource.equals(resource()) ? amount : 0;
        return super.extract(resource,amount,tx,type);
    }
    public ChemicalContents getStack() { return new ChemicalContents(resource(),amountAsLong()); }
    public long getStored() { return amountAsLong(); }
    public void setEmpty() { setContents(ChemicalResource.EMPTY,0,null); }
    public CompoundTag serializeNBT(HolderLookup.Provider registries) {
        var result=new CompoundTag();
        if (!isEmpty()) result.put("stored", new ChemicalContents(resource(),super.amountAsLong()).save(registries));
        return result;
    }
    public void deserializeNBT(HolderLookup.Provider registries,CompoundTag nbt) {
        var contents=ChemicalContents.parseOptional(registries,nbt.getCompoundOrEmpty("stored"));
        setContents(contents.resource(),contents.amount(),null);
    }
}
