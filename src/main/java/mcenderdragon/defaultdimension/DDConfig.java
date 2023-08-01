package mcenderdragon.defaultdimension;

import java.util.Map;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.config.ModConfigEvent;

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
    public static void onLoad(final ModConfigEvent.Loading configEvent)
	{
        DDMain.LOGGER.debug("Loaded config file {}", configEvent.getConfig().getFileName());
    }

    @SubscribeEvent
    public static void onFileChange(final ModConfigEvent.Loading configEvent)
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
		
		public ResourceKey<Level> getDefaultDimension(MinecraftServer server)
		{
			@SuppressWarnings("deprecation")
			Map<ResourceKey<Level>, ServerLevel> map = server.forgeGetWorldMap();

			ResourceKey<Level> worldKey = ResourceKey.create(ResourceKey.createRegistryKey(new ResourceLocation("minecraft", "dimension")), new ResourceLocation(defaultDimension.get()));

			if (map.containsKey(worldKey)) {
				return worldKey;
			} else {
				DDMain.LOGGER.warn("Could not find dimensions {}", defaultDimension.get());
				return Level.OVERWORLD;
			}
		}
	}
}
