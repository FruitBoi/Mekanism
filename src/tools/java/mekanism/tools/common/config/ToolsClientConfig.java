package mekanism.tools.common.config;

import mekanism.common.config.BaseMekanismConfig;
import mekanism.common.config.value.CachedBooleanValue;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ToolsClientConfig extends BaseMekanismConfig {

    private final ModConfigSpec configSpec;

    public final CachedBooleanValue displayDurabilityTooltips;

    public ToolsClientConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("Mekanism Tools Client Config. This config only exists on the client").push("tools-client");
        this.displayDurabilityTooltips = CachedBooleanValue.wrap(this, builder.comment("Enable durability tooltips for Mekanism Tools gear.")
              .define("displayDurabilityTooltips", true));
        builder.pop();
        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "tools-client";
    }

    @Override
    public String getTranslation() {
        return "Client Config";
    }

    @Override
    public ModConfigSpec getConfigSpec() {
        return configSpec;
    }

    @Override
    public Type getConfigType() {
        return Type.CLIENT;
    }
}