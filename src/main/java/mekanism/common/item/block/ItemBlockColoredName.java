package mekanism.common.item.block;

import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.block.interfaces.IColoredBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

//TODO: Do we want an interface for getting the block easier with the correct type
public class ItemBlockColoredName extends BlockItem {

    public <BLOCK extends Block & IColoredBlock> ItemBlockColoredName(BLOCK block) {
        this(block, new Item.Properties());
    }

    public <BLOCK extends Block & IColoredBlock> ItemBlockColoredName(BLOCK block, Item.Properties properties) {
        super(block, properties);
    }

    @NotNull
    @Override
    public Component getName(@NotNull ItemStack stack) {
        EnumColor color = getColor(stack);
        if (color == EnumColor.BLACK) {
            color = EnumColor.DARK_GRAY;
        }
        return TextComponentUtil.build(color, super.getName(stack));
    }

    private EnumColor getColor(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBlockColoredName itemBlock) {
            return ((IColoredBlock) itemBlock.getBlock()).getColor();
        }
        return EnumColor.BLACK;
    }
}