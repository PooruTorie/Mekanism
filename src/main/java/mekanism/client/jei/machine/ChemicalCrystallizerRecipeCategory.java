package mekanism.client.jei.machine;

import java.util.Collections;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.GasToItemStackRecipe;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.registries.MekanismBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;

public class ChemicalCrystallizerRecipeCategory extends BaseRecipeCategory<GasToItemStackRecipe> {

    public ChemicalCrystallizerRecipeCategory(IGuiHelper helper) {
        super(helper, "mekanism:gui/chemical_crystallizer.png", MekanismBlocks.CHEMICAL_CRYSTALLIZER, 5, 3, 147, 79);
    }

    @Override
    protected void addGuiElements() {
        guiElements.add(GuiGasGauge.getDummy(GuiGauge.Type.STANDARD, this, 5, 4));
        guiElements.add(new GuiSlot(SlotType.EXTRA, this, 5, 64).with(SlotOverlay.PLUS));
        guiElements.add(new GuiSlot(SlotType.OUTPUT, this, 130, 56));
        guiElements.add(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return (double) timer.getValue() / 20F;
            }
        }, ProgressBar.LARGE_RIGHT, this, 51, 60));
    }

    @Override
    public Class<? extends GasToItemStackRecipe> getRecipeClass() {
        return GasToItemStackRecipe.class;
    }

    @Override
    public void setIngredients(GasToItemStackRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(MekanismJEI.TYPE_GAS, Collections.singletonList(recipe.getInput().getRepresentations()));
        ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getOutputDefinition()));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, GasToItemStackRecipe recipe, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        itemStacks.init(0, false, 130 - xOffset, 56 - yOffset);
        itemStacks.set(0, recipe.getOutputDefinition());
        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        initGas(gasStacks, 0, true, 6 - xOffset, 5 - yOffset, 16, 58, recipe.getInput().getRepresentations(), true);
    }
}