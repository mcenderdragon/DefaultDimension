package mcenderdragon.defaultdimension;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod(DDMain.MOD_ID)
public class DDMain 
{
	// Directly reference a log4j logger.
	public static final Logger LOGGER = LogManager.getLogger();
	public static final String MOD_ID = "defaultdimension";

	public DDMain() 
	{
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

		// Register ourselves for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.register(this);
		
		DDConfig.registerConfig(ModLoadingContext.get().getActiveContainer());
	}

	/**
	 * Preinit
	 */
	private void setup(final FMLCommonSetupEvent event)
	{
		// preinit code
	   
	}

	private void doClientStuff(final FMLClientSetupEvent event) 
	{
		
	}

	private void enqueueIMC(final InterModEnqueueEvent event)
	{
		// some example code to dispatch IMC to another mod
//		InterModComms.sendTo("examplemod", "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
	}

	private void processIMC(final InterModProcessEvent event)
	{
		// some example code to receive and process InterModComms from other mods
//		LOGGER.info("Got IMC {}", event.getIMCStream().
//				map(m->m.getMessageSupplier().get()).
//				collect(Collectors.toList()));
	}
   
	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event) 
	{
	   CommandDimensinalSpread.register(event.getServer().getCommands().getDispatcher());
//		LOGGER.info("HELLO from server starting");
	}

	@SubscribeEvent
	public void onPlayeRespawn(PlayerEvent.PlayerRespawnEvent event)
	{
		
	}
	
	@SubscribeEvent
	public void onPlayeChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event)
	{
		
	}
	
	@SubscribeEvent
	public void onPlayeCloned(PlayerEvent.Clone event)
	{	
		Player player = event.getOriginal();

		if(player instanceof ServerPlayer)
		{
			ServerPlayer pl = (ServerPlayer) event.getOriginal();
			
			if(pl.getRespawnDimension() == Level.OVERWORLD)
			{
				pl.setRespawnPosition(pl.getRespawnDimension(), null, 0, false, false);
			}
		}
	}
	
	@SubscribeEvent
	public void onEntitySpawnFirst(EntityJoinLevelEvent event)
	{
		Entity e = event.getEntity();
		if(!e.getLevel().isClientSide())
		{
			if(e instanceof ServerPlayer)
			{
				ServerPlayer pl = (ServerPlayer) event.getEntity();
				ResourceKey<Level> type = DDConfig.COMMON.getDefaultDimension(e.getServer());
				
				if(pl.getRespawnDimension() == Level.OVERWORLD && type != Level.OVERWORLD)
				{
					pl.setRespawnPosition(type, null, 0, false, false);
				}
				
				CompoundTag nbt = pl.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
				CompoundTag modData = nbt.getCompound(MOD_ID);
				//modData.putBoolean("isSpawnDimensionSet", false);
				
				if(!modData.getBoolean("isSpawnDimensionSet"))
				{
					
					if(pl.getRespawnDimension()!=type)
					{
						pl.setRespawnPosition(type, null, 0, false, false);
					}
					
					if(pl.getLevel().dimension() != type)
					{
						MinecraftServer server = pl.getServer();
						ServerLevel world = server.getLevel(type);
						respawnIn(world, pl);
					}
					
					modData.putBoolean("isSpawnDimensionSet", true);
				}
				
				nbt.put(MOD_ID, modData);
				pl.getPersistentData().put(Player.PERSISTED_NBT_TAG, nbt);
			}
		}
		
	}
	
	public static void respawnIn(ServerLevel world, ServerPlayer pl)
	{
		LOGGER.info("Changing Player {} dimension to {}",pl, world.dimensionType());
		pl.teleportTo(world, pl.getX(), pl.getY(), pl.getZ(), pl.getYRot(), pl.getXRot());
		
		BlockPos spawn = pl.getRespawnPosition();
		boolean forced = pl.isRespawnForced();
		Optional<Vec3> optional;
		if(spawn!=null && Level.isInSpawnableBounds(spawn))
		{
			optional = Player.findRespawnPositionAndUseSpawnBlock(world, spawn, 0, forced, true);
		}
		else
		{
			optional = Optional.empty();
		}
		BlockPos worldSpawn = world.getSharedSpawnPos();
		Vec3 spawnPos = optional.orElse(new Vec3(worldSpawn.getX() + 0.5, worldSpawn.getY() + 0.2, worldSpawn.getZ() + 0.5));
		pl.teleportTo(world, spawnPos.get(Axis.X), spawnPos.get(Axis.Y), spawnPos.get(Axis.Z), pl.getXRot(), pl.getYRot());
		LOGGER.info("Seting player {} position to {}", pl, spawnPos);
		pl.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 5, 20, true, false));
	}

	@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
	public static class RegistryEvents 
	{
		@SubscribeEvent
		public static void register(RegisterEvent event) {
			event.register(ForgeRegistries.Keys.BLOCKS,
				helper -> {
					LOGGER.info("HELLO from Register Block");
				}
			);
		}
	}
}
