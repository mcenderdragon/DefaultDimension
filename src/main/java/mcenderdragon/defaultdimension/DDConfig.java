package mcenderdragon.defaultdimension;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
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
		
		public final BooleanValue futurepackStartMenu;

		Common(ForgeConfigSpec.Builder builder) 
		{
			builder.push("client");
			
			futurepackStartMenu = builder
					.comment("Show custom loading Screen")
					.define("futurepack_start_menu", false);
			
			
			builder.pop();
		}
	}
}
