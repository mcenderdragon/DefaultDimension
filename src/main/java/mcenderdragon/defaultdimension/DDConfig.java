package mcenderdragon.defaultdimension;

import org.lwjgl.system.FunctionProviderLocal;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.Type;

public class DDConfig 
{
	private static final ForgeConfigSpec.Builder BUILDER  = new ForgeConfigSpec.Builder();
	
	public static final Common COMMON = new Common(BUILDER);
	
	private static final ForgeConfigSpec CONFIG_SPEC = BUILDER.build();
	
	
	public static void registerConfig(ModContainer mod)
	{
		if(!DDMain.MOD_ID.equals(mod.getModId()))
			throw new IllegalArgumentException("The provied ModContainer has not the not the ID " + DDMain.MOD_ID);

		mod.addConfig(new ModConfig(Type.COMMON, CONFIG_SPEC, mod));
	}
	
	@SubscribeEvent
    public static void onLoad(final ModConfig.Loading configEvent) 
	{
        DDMain.LOGGER.debug("Loaded config file {}", configEvent.getConfig().getFileName());
    }

    @SubscribeEvent
    public static void onFileChange(final ModConfig.ConfigReloading configEvent) 
    {
    	
    }
	
	public static class Common
	{
		
		public final ConfigValue<String> defaultDimension;

		Common(ForgeConfigSpec.Builder builder) 
		{
			builder.push("common");
			
			defaultDimension = builder
					.comment("The dimension the player will (re)spawn in.")
					.define("default_dimension", "minecraft:overworld");
			
			
			builder.pop();
		}
		
		public DimensionType getDefaultDimension()
		{
			DimensionType t = DimensionType.byName(new ResourceLocation(defaultDimension.get()));
			if(t==null)
			{
				DDMain.LOGGER.warn("Could not find dimensions {}", defaultDimension.get());
				t = DimensionType.OVERWORLD;
			}
			return t;
		}
	}
}
