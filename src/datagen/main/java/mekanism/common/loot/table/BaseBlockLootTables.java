package mekanism.common.loot.table;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.block.BlockCardboardBox;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.block.attribute.Attributes.AttributeInventory;
import mekanism.common.block.attribute.Attributes.AttributeRedstone;
import mekanism.common.block.attribute.Attributes.AttributeSecurity;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.lib.frequency.IFrequencyHandler;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.tile.interfaces.ISustainedData;
import mekanism.common.util.EnumUtils;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.items.IItemHandler;

public abstract class BaseBlockLootTables extends BlockLoot {

    private static final LootItemCondition.Builder HAS_SILK_TOUCH = MatchTool.toolMatches(ItemPredicate.Builder.item()
          .hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.Ints.atLeast(1))));

    private final Set<Block> knownBlocks = new ObjectOpenHashSet<>();
    private final Set<Block> toSkip = new ObjectOpenHashSet<>();

    @Override
    protected abstract void addTables();

    @Override
    protected void add(@Nonnull Block block, @Nonnull LootTable.Builder table) {
        //Overwrite the core register method to add to our list of known blocks
        super.add(block, table);
        knownBlocks.add(block);
    }

    @Nonnull
    @Override
    protected Iterable<Block> getKnownBlocks() {
        return knownBlocks;
    }

    protected void skip(IBlockProvider... blockProviders) {
        for (IBlockProvider blockProvider : blockProviders) {
            toSkip.add(blockProvider.getBlock());
        }
    }

    protected boolean skipBlock(Block block) {
        //Skip any blocks that we already registered a table for or have marked to skip
        return knownBlocks.contains(block) || toSkip.contains(block);
    }

    protected static LootTable.Builder droppingWithFortuneOrRandomly(Block block, ItemLike item, UniformGenerator range) {
        return createSilkTouchDispatchTable(block, applyExplosionDecay(block, LootItem.lootTableItem(item.asItem())
              .apply(SetItemCountFunction.setCount(range))
              .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))
        ));
    }

    //IBlockProvider versions of BlockLootTable methods, modified to support varargs
    protected void dropSelf(List<IBlockProvider> blockProviders) {
        for (IBlockProvider blockProvider : blockProviders) {
            Block block = blockProvider.getBlock();
            if (!skipBlock(block)) {
                dropSelf(block);
            }
        }
    }

    protected void add(Function<Block, Builder> factory, Collection<? extends IBlockProvider> blockProviders) {
        for (IBlockProvider blockProvider : blockProviders) {
            add(blockProvider.getBlock(), factory);
        }
    }

    protected void add(Function<Block, Builder> factory, IBlockProvider... blockProviders) {
        for (IBlockProvider blockProvider : blockProviders) {
            add(blockProvider.getBlock(), factory);
        }
    }

    protected void dropSelfWithContents(List<IBlockProvider> blockProviders) {
        //TODO: See if there is other stuff we want to be transferring which we currently do not
        // For example, when writing this we added dump mode for chemical tanks to getting transferred to the item
        for (IBlockProvider blockProvider : blockProviders) {
            Block block = blockProvider.getBlock();
            if (skipBlock(block)) {
                continue;
            }
            CopyNbtFunction.Builder nbtBuilder = CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY);
            boolean hasData = false;
            boolean hasContents = false;
            @Nullable
            BlockEntity tile = null;
            if (block instanceof IHasTileEntity) {
                tile = ((IHasTileEntity<?>) block).newBlockEntity(BlockPos.ZERO, block.defaultBlockState());
            }
            if (tile instanceof IFrequencyHandler && ((IFrequencyHandler) tile).getFrequencyComponent().hasCustomFrequencies()) {
                nbtBuilder.copy(NBTConstants.COMPONENT_FREQUENCY, NBTConstants.MEK_DATA + "." + NBTConstants.COMPONENT_FREQUENCY);
                hasData = true;
            }
            if (Attribute.has(block, AttributeSecurity.class)) {
                //TODO: Should we just save the entire security component?
                nbtBuilder.copy(NBTConstants.COMPONENT_SECURITY + "." + NBTConstants.OWNER_UUID, NBTConstants.MEK_DATA + "." + NBTConstants.OWNER_UUID);
                nbtBuilder.copy(NBTConstants.COMPONENT_SECURITY + "." + NBTConstants.SECURITY_MODE, NBTConstants.MEK_DATA + "." + NBTConstants.SECURITY_MODE);
                hasData = true;
            }
            if (Attribute.has(block, AttributeUpgradeSupport.class)) {
                nbtBuilder.copy(NBTConstants.COMPONENT_UPGRADE, NBTConstants.MEK_DATA + "." + NBTConstants.COMPONENT_UPGRADE);
                hasData = true;
            }
            if (tile instanceof ISideConfiguration) {
                nbtBuilder.copy(NBTConstants.COMPONENT_CONFIG, NBTConstants.MEK_DATA + "." + NBTConstants.COMPONENT_CONFIG);
                nbtBuilder.copy(NBTConstants.COMPONENT_EJECTOR, NBTConstants.MEK_DATA + "." + NBTConstants.COMPONENT_EJECTOR);
                hasData = true;
            }
            if (tile instanceof ISustainedData) {
                Set<Entry<String, String>> remapEntries = ((ISustainedData) tile).getTileDataRemap().entrySet();
                for (Entry<String, String> remapEntry : remapEntries) {
                    nbtBuilder.copy(remapEntry.getKey(), NBTConstants.MEK_DATA + "." + remapEntry.getValue());
                }
                if (!remapEntries.isEmpty()) {
                    hasData = true;
                }
            }
            if (Attribute.has(block, AttributeRedstone.class)) {
                nbtBuilder.copy(NBTConstants.CONTROL_TYPE, NBTConstants.MEK_DATA + "." + NBTConstants.CONTROL_TYPE);
                hasData = true;
            }
            if (tile instanceof TileEntityMekanism) {
                TileEntityMekanism tileEntity = (TileEntityMekanism) tile;
                for (SubstanceType type : EnumUtils.SUBSTANCES) {
                    if (tileEntity.handles(type) && !type.getContainers(tileEntity).isEmpty()) {
                        nbtBuilder.copy(type.getContainerTag(), NBTConstants.MEK_DATA + "." + type.getContainerTag());
                        hasData = true;
                        if (type != SubstanceType.ENERGY && type != SubstanceType.HEAT) {
                            hasContents = true;
                        }
                    }
                }
            }
            if (Attribute.has(block, AttributeInventory.class)) {
                //If the block has an inventory, copy the inventory slots,
                // but if it is an IItemHandler, which for most cases of ours it will be,
                // then only copy the slots if we actually have any slots because otherwise maybe something just went wrong
                if (!(tile instanceof IItemHandler) || ((IItemHandler) tile).getSlots() > 0) {
                    //If we don't actually handle saving an inventory (such as the quantum entangloporter, don't actually add it as something to copy)
                    if (!(tile instanceof TileEntityMekanism) || ((TileEntityMekanism) tile).persistInventory()) {
                        nbtBuilder.copy(NBTConstants.ITEMS, NBTConstants.MEK_DATA + "." + NBTConstants.ITEMS);
                        hasData = true;
                        hasContents = true;
                    }
                }
            }
            if (block instanceof BlockCardboardBox) {
                //TODO: Do this better so that it doesn't have to be as hard coded to being a cardboard box
                nbtBuilder.copy(NBTConstants.DATA, NBTConstants.MEK_DATA + "." + NBTConstants.DATA);
                hasData = true;
            }
            if (!hasData) {
                //To keep the json as clean as possible don't bother even registering a blank accept function if we have no
                // persistent data that we want to copy. Also log a warning so that we don't have to attempt to check against
                // that block
                dropSelf(block);
            } else {
                add(block, LootTable.lootTable().withPool(applyExplosionCondition(hasContents, LootPool.lootPool()
                      .name("main")
                      .setRolls(ConstantValue.exactly(1))
                      .add(LootItem.lootTableItem(block).apply(nbtBuilder))
                )));
            }
        }
    }

    /**
     * Like vanilla's {@link BlockLoot#applyExplosionCondition(ItemLike, ConditionUserBuilder)} except with a boolean for if it is explosion resistant.
     */
    private static <T> T applyExplosionCondition(boolean explosionResistant, ConditionUserBuilder<T> condition) {
        return explosionResistant ? condition.unwrap() : condition.when(ExplosionCondition.survivesExplosion());
    }

    /**
     * Like vanilla's {@link BlockLoot#createSlabItemTable(Block)} except with a named pool
     */
    @Nonnull
    protected static LootTable.Builder createSlabItemTable(Block slab) {
        return LootTable.lootTable().withPool(LootPool.lootPool()
              .name("main")
              .setRolls(ConstantValue.exactly(1))
              .add(applyExplosionDecay(slab, LootItem.lootTableItem(slab)
                          .apply(SetItemCountFunction.setCount(ConstantValue.exactly(2))
                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(slab)
                                      .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SlabBlock.TYPE, SlabType.DOUBLE)))
                          )
                    )
              )
        );
    }

    /**
     * Like vanilla's {@link BlockLoot#dropOther(Block, ItemLike)} except with a named pool
     */
    @Override
    public void dropOther(@Nonnull Block block, @Nonnull ItemLike drop) {
        add(block, createSingleItemTable(drop));
    }

    /**
     * Like vanilla's {@link BlockLoot#createSingleItemTable(ItemLike)} except with a named pool
     */
    @Nonnull
    protected static LootTable.Builder createSingleItemTable(ItemLike item) {
        return LootTable.lootTable().withPool(applyExplosionCondition(item, LootPool.lootPool()
              .name("main")
              .setRolls(ConstantValue.exactly(1))
              .add(LootItem.lootTableItem(item))
        ));
    }

    /**
     * Like vanilla's {@link BlockLoot#createSingleItemTableWithSilkTouch(Block, ItemLike, NumberProvider)} except with a named pool
     */
    @Nonnull
    protected static LootTable.Builder createSingleItemTableWithSilkTouch(@Nonnull Block block, @Nonnull ItemLike item, @Nonnull NumberProvider range) {
        return createSilkTouchDispatchTable(block, applyExplosionDecay(block, LootItem.lootTableItem(item).apply(SetItemCountFunction.setCount(range))));
    }

    /**
     * Like vanilla's {@link BlockLoot#createSilkTouchDispatchTable(Block, LootPoolEntryContainer.Builder)} except with a named pool
     */
    @Nonnull
    protected static LootTable.Builder createSilkTouchDispatchTable(@Nonnull Block block, @Nonnull LootPoolEntryContainer.Builder<?> builder) {
        return createSelfDropDispatchTable(block, HAS_SILK_TOUCH, builder);
    }

    /**
     * Like vanilla's {@link BlockLoot#createSelfDropDispatchTable(Block, LootItemCondition.Builder, LootPoolEntryContainer.Builder)} except with a named pool
     */
    @Nonnull
    protected static LootTable.Builder createSelfDropDispatchTable(@Nonnull Block block, @Nonnull LootItemCondition.Builder conditionBuilder,
          @Nonnull LootPoolEntryContainer.Builder<?> entry) {
        return LootTable.lootTable().withPool(LootPool.lootPool()
              .name("main")
              .setRolls(ConstantValue.exactly(1))
              .add(LootItem.lootTableItem(block)
                    .when(conditionBuilder)
                    .otherwise(entry)
              )
        );
    }
}