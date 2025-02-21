package mekanism.generators.common;

import java.util.concurrent.CompletableFuture;
import mekanism.common.BasePackMetadataGenerator;
import mekanism.common.MekanismDataGenerator;
import mekanism.generators.client.GeneratorsBlockStateProvider;
import mekanism.generators.client.GeneratorsItemModelProvider;
import mekanism.generators.client.GeneratorsLangProvider;
import mekanism.generators.client.GeneratorsSoundProvider;
import mekanism.generators.client.GeneratorsSpriteSourceProvider;
import mekanism.generators.common.loot.GeneratorsLootProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = MekanismGenerators.MODID, bus = Bus.MOD)
public class GeneratorsDataGenerator {

    private GeneratorsDataGenerator() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        MekanismDataGenerator.bootstrapConfigs(MekanismGenerators.MODID);
        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        gen.addProvider(true, new BasePackMetadataGenerator(output, GeneratorsLang.PACK_DESCRIPTION));
        //Client side data generators
        MekanismDataGenerator.addProvider(gen, event.includeClient(), GeneratorsLangProvider::new);
        gen.addProvider(event.includeClient(), new GeneratorsSoundProvider(output, existingFileHelper));
        gen.addProvider(event.includeClient(), new GeneratorsSpriteSourceProvider(output, existingFileHelper));
        gen.addProvider(event.includeClient(), new GeneratorsItemModelProvider(output, existingFileHelper));
        gen.addProvider(event.includeClient(), new GeneratorsBlockStateProvider(output, existingFileHelper));
        //Server side data generators
        gen.addProvider(event.includeServer(), new GeneratorsTagProvider(output, lookupProvider, existingFileHelper));
        MekanismDataGenerator.addProvider(gen, event.includeServer(), GeneratorsLootProvider::new);
        gen.addProvider(event.includeServer(), new GeneratorsRecipeProvider(output, existingFileHelper));
        gen.addProvider(event.includeServer(), new GeneratorsAdvancementProvider(output, existingFileHelper));
    }
}