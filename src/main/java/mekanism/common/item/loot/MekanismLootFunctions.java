package mekanism.common.item.loot;

import com.mojang.serialization.Codec;
import mekanism.common.Mekanism;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryObject;

public class MekanismLootFunctions {
    public static final DeferredRegister<LootItemFunctionType> REGISTER = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, Mekanism.MODID);

    public static final RegistryObject<LootItemFunctionType> PERSONAL_STORAGE_LOOT_FUNC = REGISTER.register("personal_storage_contents", ()-> new LootItemFunctionType(Codec.unit(PersonalStorageContentsLootFunction.INSTANCE)));
}