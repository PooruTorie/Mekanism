package mekanism.client.gui.machine;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nonnull;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.bar.GuiHorizontalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.machine.TileEntityIsotopicCentrifuge;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;

public class GuiIsotopicCentrifuge extends GuiConfigurableTile<TileEntityIsotopicCentrifuge, MekanismTileContainer<TileEntityIsotopicCentrifuge>> {

    public GuiIsotopicCentrifuge(MekanismTileContainer<TileEntityIsotopicCentrifuge> container, Inventory inv, Component title) {
        super(container, inv, title);
        inventoryLabelY += 2;
        titleLabelY = 4;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiHorizontalPowerBar(this, tile.getEnergyContainer(), 115, 75));
        addRenderableWidget(new GuiEnergyTab(this, tile.getEnergyContainer(), tile::getEnergyUsed));
        addRenderableWidget(new GuiGasGauge(() -> tile.inputTank, () -> tile.getGasTanks(null), GaugeType.STANDARD, this, 25, 13));
        addRenderableWidget(new GuiGasGauge(() -> tile.outputTank, () -> tile.getGasTanks(null), GaugeType.STANDARD, this, 133, 13));
        addRenderableWidget(new GuiProgress(tile::getActive, ProgressType.LARGE_RIGHT, this, 64, 39).jeiCategory(tile));
    }

    @Override
    protected void drawForegroundText(@Nonnull PoseStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, playerInventoryTitle, inventoryLabelX, inventoryLabelY, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}
