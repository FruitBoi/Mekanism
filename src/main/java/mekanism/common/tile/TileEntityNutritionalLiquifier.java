package mekanism.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.ItemStackToGasCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.GasInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.NutritionalLiquifierIRecipe;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismGases;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.slot.EnergySlotInfo;
import mekanism.common.tile.component.config.slot.GasSlotInfo;
import mekanism.common.tile.prefab.TileEntityOperationalMachine;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.Food;
import net.minecraft.item.ItemStack;

public class TileEntityNutritionalLiquifier extends TileEntityOperationalMachine<ItemStackToGasRecipe> {

    public static final int MAX_GAS = 10_000;
    public BasicGasTank gasTank;
    public int gasOutput = 256;

    private final IOutputHandler<@NonNull GasStack> outputHandler;
    private final IInputHandler<@NonNull ItemStack> inputHandler;

    private MachineEnergyContainer<TileEntityNutritionalLiquifier> energyContainer;
    private InputInventorySlot inputSlot;
    private GasInventorySlot outputSlot;
    private EnergyInventorySlot energySlot;

    public TileEntityNutritionalLiquifier() {
        super(MekanismBlocks.NUTRITIONAL_LIQUIFIER, 100);
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.GAS, TransmissionType.ENERGY);
        configComponent.setupItemIOConfig(inputSlot, outputSlot, energySlot);
        configComponent.setupOutputConfig(TransmissionType.GAS, new GasSlotInfo(true, false, gasTank), RelativeSide.RIGHT);
        configComponent.setupInputConfig(TransmissionType.ENERGY, new EnergySlotInfo(true, false, energyContainer));

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.GAS);

        inputHandler = InputHelper.getInputHandler(inputSlot);
        outputHandler = OutputHelper.getOutputHandler(gasTank);
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        outputSlot.drainTank();
        cachedRecipe = getUpdatedCache(0);
        if (cachedRecipe != null) {
            cachedRecipe.process();
        }
    }

    @Nonnull
    @Override
    public MekanismRecipeType<ItemStackToGasRecipe> getRecipeType() {
        return null;
    }

    @Nullable
    @Override
    public CachedRecipe<ItemStackToGasRecipe> getCachedRecipe(int cacheIndex) {
        return cachedRecipe;
    }

    @Nullable
    @Override
    public ItemStackToGasRecipe getRecipe(int cacheIndex) {
        ItemStack stack = inputHandler.getInput();
        if (stack.isEmpty() || !stack.getItem().isFood()) {
            return null;
        }
        Food food = stack.getItem().getFood();
        return new NutritionalLiquifierIRecipe(null, ItemStackIngredient.from(stack.getItem()), MekanismGases.NUTRITIONAL_PASTE.getGasStack(food.getHealing() * 50));
    }

    @Nullable
    @Override
    public CachedRecipe<ItemStackToGasRecipe> createNewCachedRecipe(@Nonnull ItemStackToGasRecipe recipe, int cacheIndex) {
        return new ItemStackToGasCachedRecipe(recipe, inputHandler, outputHandler)
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setActive(this::setActive)
              .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
              .setRequiredTicks(() -> ticksRequired)
              .setOnFinish(() -> markDirty(false))
              .setOperatingTicksChanged(this::setOperatingTicks);
    }

    @Nonnull
    @Override
    protected IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks() {
        ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSideGasWithConfig(this::getDirection, this::getConfig);
        builder.addTank(gasTank = BasicGasTank.output(MAX_GAS, this));
        return builder.build();
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers() {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this));
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addSlot(inputSlot = InputInventorySlot.at(item -> item.getItem().isFood(), this, 26, 36), RelativeSide.LEFT);
        builder.addSlot(outputSlot = GasInventorySlot.drain(gasTank, this, 155, 25), RelativeSide.RIGHT);
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getWorld, this, 155, 5), RelativeSide.BOTTOM, RelativeSide.TOP);
        outputSlot.setSlotOverlay(SlotOverlay.PLUS);
        return builder.build();
    }

    public MachineEnergyContainer<TileEntityNutritionalLiquifier> getEnergyContainer() {
        return energyContainer;
    }
}