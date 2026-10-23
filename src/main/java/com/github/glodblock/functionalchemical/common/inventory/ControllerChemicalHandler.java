package com.github.glodblock.functionalchemical.common.inventory;
import java.util.*;
import com.github.glodblock.functionalchemical.util.asm.ChemicalController;
import mekanism.api.chemical.ChemicalResource;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
public class ControllerChemicalHandler implements ResourceHandler<ChemicalResource> {
    private final List<IndexedHandler> handlers=new ArrayList<>();
    private final ChemicalController controller;
    public ControllerChemicalHandler(ChemicalController controller) { this.controller=controller;refresh(); }
    public void refresh() {
        handlers.clear();
        for(var handler:controller.getChemicalModule().getHandlers())for(int i=0;i<handler.size();i++)handlers.add(new IndexedHandler(handler,i));
    }
    @Override public int size(){return handlers.size();}
    @Override public ChemicalResource getResource(int slot){var h=handlers.get(slot);return h.handler.getResource(h.index);}
    @Override public long getAmountAsLong(int slot){var h=handlers.get(slot);return h.handler.getAmountAsLong(h.index);}
    @Override public long getCapacityAsLong(int slot,ChemicalResource resource){var h=handlers.get(slot);return h.handler.getCapacityAsLong(h.index,resource);}
    @Override public boolean isValid(int slot,ChemicalResource resource){var h=handlers.get(slot);return h.handler.isValid(h.index,resource);}
    @Override public int insert(int slot,ChemicalResource resource,int amount,TransactionContext tx){var h=handlers.get(slot);return h.handler.insert(h.index,resource,amount,tx);}
    @Override public int extract(int slot,ChemicalResource resource,int amount,TransactionContext tx){var h=handlers.get(slot);return h.handler.extract(h.index,resource,amount,tx);}
    private record IndexedHandler(ResourceHandler<ChemicalResource> handler,int index){}
}
