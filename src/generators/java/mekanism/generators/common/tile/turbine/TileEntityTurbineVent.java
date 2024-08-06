package mekanism.generators.common.tile.turbine;

import java.util.Collections;
import java.util.Set;
import mekanism.api.IContentsListener;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.lib.multiblock.IMultiblockEjector;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.util.FluidUtils;
import mekanism.generators.common.content.turbine.TurbineMultiblockData;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntityTurbineVent extends TileEntityTurbineCasing {

    public TileEntityTurbineVent(BlockPos pos, BlockState state) {
        super(GeneratorsBlocks.TURBINE_VENT, pos, state);
    }
}