package mekanism.common.content.blocktype;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import mekanism.api.text.ILangEntry;
import mekanism.api.tier.ITier;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeCustomShape;
import mekanism.common.block.attribute.Attributes.AttributeComputerIntegration;
import mekanism.common.block.attribute.Attributes.AttributeLight;
import mekanism.common.block.interfaces.ITypeBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockType {

    private final ILangEntry description;

    private final Map<Class<? extends Attribute>, Attribute> attributeMap = new HashMap<>();

    public BlockType(ILangEntry description) {
        this.description = description;
    }

    public boolean has(Class<? extends Attribute> type) {
        return attributeMap.containsKey(type);
    }

    @SuppressWarnings("unchecked")
    public <T extends Attribute> T get(Class<T> type) {
        return (T) attributeMap.get(type);
    }

    @SafeVarargs
    protected final void setFrom(BlockTypeTile<?> tile, Class<? extends Attribute>... types) {
        for (Class<? extends Attribute> type : types) {
            attributeMap.put(type, tile.get(type));
        }
    }

    public void add(Attribute... attrs) {
        for (Attribute attr : attrs) {
            attributeMap.put(attr.getClass(), attr);
        }
    }

    @SafeVarargs
    public final void remove(Class<? extends Attribute>... attrs) {
        for (Class<? extends Attribute> attr : attrs) {
            attributeMap.remove(attr);
        }
    }

    public Collection<Attribute> getAll() {
        return attributeMap.values();
    }

    @Nonnull
    public ILangEntry getDescription() {
        return description;
    }

    public static boolean is(Block block, BlockType... types) {
        if (block instanceof ITypeBlock) {
            for (BlockType type : types) {
                if (((ITypeBlock) block).getType() == type) {
                    return true;
                }
            }
        }
        return false;
    }

    public static BlockType get(Block block) {
        return block instanceof ITypeBlock ? ((ITypeBlock) block).getType() : null;
    }

    public static class BlockTypeBuilder<BLOCK extends BlockType, T extends BlockTypeBuilder<BLOCK, T>> {

        protected final BLOCK holder;

        protected BlockTypeBuilder(BLOCK holder) {
            this.holder = holder;
        }

        public static BlockTypeBuilder<BlockType, ?> createBlock(ILangEntry description) {
            return new BlockTypeBuilder<>(new BlockType(description));
        }

        @SuppressWarnings("unchecked")
        public T getThis() {
            return (T) this;
        }

        /**
         * This is the same as {@link #with(Attribute...)} except exists to make it more clear that we are replacing/overriding an existing attribute we added.
         */
        public final T replace(Attribute... attrs) {
            return with(attrs);
        }

        public final T with(Attribute... attrs) {
            holder.add(attrs);
            return getThis();
        }

        @SafeVarargs
        public final T without(Class<? extends Attribute>... attrs) {
            holder.remove(attrs);
            return getThis();
        }

        public T withCustomShape(VoxelShape[] shape) {
            return with(new AttributeCustomShape(shape));
        }

        public T withLight(int light) {
            return with(new AttributeLight(light));
        }

        public T withComputerSupport(String name) {
            return with(new AttributeComputerIntegration(name));
        }

        public T withComputerSupport(ITier tier, String name) {
            return withComputerSupport(tier.getBaseTier().getLowerName() + name);
        }

        public BLOCK build() {
            return holder;
        }
    }
}
