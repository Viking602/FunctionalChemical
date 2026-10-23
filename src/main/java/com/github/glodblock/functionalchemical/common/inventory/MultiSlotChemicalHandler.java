package com.github.glodblock.functionalchemical.common.inventory;
import java.util.function.IntFunction;
import mekanism.api.AutomationType;
import mekanism.api.chemical.ChemicalResource;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
public class MultiSlotChemicalHandler implements ResourceHandler<ChemicalResource> {
    private final ChemicalDrawerTank[] tanks;
    public MultiSlotChemicalHandler(int size,IntFunction<? extends ChemicalDrawerTank> factory) {
        tanks=new ChemicalDrawerTank[size];for(int i=0;i<size;i++)tanks[i]=factory.apply(i);
    }
    public ChemicalDrawerTank[] getInternalTanks() { return tanks; }
    public ChemicalContents getChemicalInTank(int slot) { return tanks[slot].getStack(); }
    @Override public int size() { return tanks.length; }
    @Override public ChemicalResource getResource(int slot) { return tanks[slot].resource(); }
    @Override public long getAmountAsLong(int slot) { return tanks[slot].amountAsLong(); }
    @Override public long getCapacityAsLong(int slot,ChemicalResource resource) { return tanks[slot].capacityAsLong(resource); }
    @Override public boolean isValid(int slot,ChemicalResource resource) { return tanks[slot].isValid(resource); }
    @Override public int insert(int slot,ChemicalResource resource,int amount,TransactionContext tx) { return tanks[slot].insert(resource,amount,tx,AutomationType.EXTERNAL); }
    @Override public int extract(int slot,ChemicalResource resource,int amount,TransactionContext tx) { return tanks[slot].extract(resource,amount,tx,AutomationType.EXTERNAL); }
}
