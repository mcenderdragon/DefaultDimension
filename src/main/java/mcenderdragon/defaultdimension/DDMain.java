package mcenderdragon.defaultdimension;

import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

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
	public void onServerStarting(FMLServerStartingEvent event) 
	{
	   CommandDimensinalSpread.register(event.getCommandDispatcher());
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
		if(event.getPlayer() instanceof ServerPlayerEntity)
		{
			ServerPlayerEntity pl = (ServerPlayerEntity) event.getPlayer();
			
			if(pl.getSpawnDimension() == DimensionType.OVERWORLD)
			{
				pl.setSpawnDimenion(event.getOriginal().getSpawnDimension());
			}
		}
	}
	
	@SubscribeEvent
	public void onEntitySpawnFirst(EntityJoinWorldEvent event)
	{
		Entity e = event.getEntity();
		if(!e.world.isRemote)
		{
			if(e instanceof ServerPlayerEntity)
			{
				ServerPlayerEntity pl = (ServerPlayerEntity) event.getEntity();
				DimensionType type = DDConfig.COMMON.getDefaultDimension();
				
				if(pl.getSpawnDimension() == DimensionType.OVERWORLD && type != DimensionType.OVERWORLD)
				{
					pl.setSpawnDimenion(type);
				}
				
				
				CompoundNBT nbt = pl.getPersistentData().getCompound(PlayerEntity.PERSISTED_NBT_TAG);
				CompoundNBT modData = nbt.getCompound(MOD_ID);
				//modData.putBoolean("isSpawnDimensionSet", false);
				
				if(!modData.getBoolean("isSpawnDimensionSet"))
				{
					
					if(pl.getSpawnDimension()!=type)
					{
						pl.setSpawnDimenion(type);
					}
					
					if(pl.dimension != type)
					{
						MinecraftServer server = pl.getServer();
						ServerWorld world = server.getWorld(type);
						respawnIn(world, pl);
					}
					
					modData.putBoolean("isSpawnDimensionSet", true);
				}
				
				nbt.put(MOD_ID, modData);
				pl.getPersistentData().put(PlayerEntity.PERSISTED_NBT_TAG, nbt);
			}
		}
		
	}
	
	public static void respawnIn(ServerWorld world, ServerPlayerEntity pl)
	{
		LOGGER.info("Changing Player {} dimension to {}",pl, world.dimension.getType());
		pl.teleport(world, pl.posX, pl.posY, pl.posZ, pl.rotationYaw, pl.rotationPitch);
		
		BlockPos spawn = pl.getBedLocation(world.dimension.getType());
		boolean forced = pl.isSpawnForced(world.dimension.getType());
		Optional<Vec3d> optional;
		if(spawn!=null && World.isValid(spawn))
		{
			optional = PlayerEntity.func_213822_a(world, spawn, forced);
		}
		else
		{
			optional = Optional.empty();
		}
		BlockPos worldSpawn = world.getSpawnPoint();
		Vec3d spawnPos = optional.orElse(new Vec3d(worldSpawn.getX() + 0.5, worldSpawn.getY() + 0.2, worldSpawn.getZ() + 0.5));
		pl.teleport(world, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), pl.rotationYaw, pl.rotationPitch);
		LOGGER.info("Seting player {} position to {}", pl, spawnPos);
		pl.addPotionEffect(new EffectInstance(Effects.RESISTANCE, 20 * 5, 20, true, false));
	}
	
	
	@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
	public static class RegistryEvents 
	{
		@SubscribeEvent
		public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
			// register a new block here
			LOGGER.info("HELLO from Register Block");
		}
	}
}
