package com.github.glodblock.functionalchemical.common.inventory;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.chemical.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;

public record ChemicalContents(ChemicalResource resource, long amount) {
    public static final ChemicalContents EMPTY = new ChemicalContents(ChemicalResource.EMPTY, 0);
    public static final Codec<ChemicalContents> CODEC = RecordCodecBuilder.create(i -> i.group(
        ChemicalResource.CODEC.fieldOf("id").forGetter(ChemicalContents::resource),
        Codec.LONG.fieldOf("amount").forGetter(ChemicalContents::amount)).apply(i,ChemicalContents::new));
    public ChemicalContents {
        if (amount < 0) throw new IllegalArgumentException("Negative chemical count");
        if (resource.isEmpty() || amount == 0) { resource=ChemicalResource.EMPTY; amount=0; }
    }
    public boolean isEmpty() { return amount == 0; }
    public long getAmount() { return amount; }
    public Chemical getChemical() { return resource.getChemical(); }
    public int getChemicalTint() { return getChemical().getTint(); }
    public Component getTextComponent() { return resource.getTextComponent(); }
    public boolean is(Chemical chemical) { return getChemical()==chemical; }
    public boolean is(TagKey<Chemical> tag) { return resource.is(tag); }
    public boolean isRadioactive() { return getChemical().isRadioactive(); }
    public ChemicalContents copy() { return this; }
    public ChemicalContents copyWithAmount(long count) { return new ChemicalContents(resource,count); }
    public Tag save(HolderLookup.Provider registries) { return CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE),this).getOrThrow(); }
    public static ChemicalContents parseOptional(HolderLookup.Provider registries,CompoundTag tag) {
        return CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE),tag).result().orElse(EMPTY);
    }
}
