package mcenderdragon.defaultdimension;

import java.util.*;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;


public class CommandDimensinalSpread 
{
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("spreaddimensional").requires((p_198721_0_) -> 
			{
				return p_198721_0_.hasPermission(2);
			}).then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("dimensions", StringArgumentType.greedyString())
				.executes((p_198718_0_) -> {
					return spreadPlayers(p_198718_0_.getSource(), EntityArgument.getPlayers(p_198718_0_, "targets"), StringArgumentType.getString(p_198718_0_, "dimensions"));
				}))
			)
		);
	}
	
	public static int spreadPlayers(CommandSourceStack executor, Collection<ServerPlayer> players, String dimensions)
	{
		try
		{
			ResourceLocation[] dim = Arrays.stream(dimensions.split(",")).map(s -> s.replaceAll("[^a-z0-9/._/-/:]", "")).map(ResourceLocation::new).toArray(ResourceLocation[]::new);
			ArrayList<ResourceKey<Level>> dims = new ArrayList<ResourceKey<Level>>(dim.length);
			MinecraftServer server = executor.getServer();

			@SuppressWarnings("deprecation")
			Map<ResourceKey<Level>, ServerLevel> map = server.forgeGetWorldMap();

			for(ResourceLocation r : dim)
			{
				ResourceKey<Level> type = ResourceKey.create(ResourceKey.createRegistryKey(new ResourceLocation("minecraft", "dimension")), r);

				if (map.containsKey(type)) {
					dims.add(type);
				} else {
					executor.sendFailure(Component.literal("Did not find dimension " + r));
					return 0;
				}
			}
		
			if(dims.isEmpty())
			{
				executor.sendFailure(Component.literal("Dimension list was empty"));
				return 0;
			}
		
			Random r = new Random();

			for(ServerPlayer e : players)
			{
				ResourceKey<Level> target = dims.get(r.nextInt(dims.size()));
				ServerLevel world = e.getServer().getLevel(target);

				DDMain.respawnIn(world, e);
			}
		}
		catch(Exception e)
		{
			throw new CommandRuntimeException(Component.literal(e.getMessage()));
		}
		return Command.SINGLE_SUCCESS;
	}
}
