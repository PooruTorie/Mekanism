package mekanism.tools.client.render.item;

import mekanism.client.render.item.ISTERProvider.MekRenderProperties;
import net.minecraftforge.client.IItemRenderProperties;

//This class is used to prevent class loading issues on the server without having to use OnlyIn hacks
public class ToolsISTERProvider {

    private ToolsISTERProvider() {
    }

    public static IItemRenderProperties shield() {
        return new MekRenderProperties(RenderMekanismShieldItem.RENDERER);
    }

    public static IItemRenderProperties glowArmor() {
        return GLOW_ARMOR;
    }

    //TODO - 1.18: Uncomment once https://github.com/MinecraftForge/MinecraftForge/pull/8349 is merged
    private static final IItemRenderProperties GLOW_ARMOR = new IItemRenderProperties() {
        /*@Override
        public Model getArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> _default) {
            return GlowArmor.wrap(_default);
        }*/
    };
}