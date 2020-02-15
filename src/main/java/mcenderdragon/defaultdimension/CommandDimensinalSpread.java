package mcenderdragon.defaultdimension;

import java.awt.color.CMMException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import java.util.stream.StreamSupport;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;

public class CommandDimensinalSpread 
{
	public static void register(CommandDispatcher<CommandSource> dispatcher) 
	{
		dispatcher.register(Commands.literal("spreaddimensional").requires((p_198721_0_) -> 
			{
				return p_198721_0_.hasPermissionLevel(2);
			}).then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("dimensions", StringArgumentType.greedyString())
				.executes((p_198718_0_) -> {
					return spreadPlayers(p_198718_0_.getSource(), EntityArgument.getPlayers(p_198718_0_, "targets"), StringArgumentType.getString(p_198718_0_, "dimensions"));
				}))
			)
		);
	}
	
	public static int spreadPlayers(CommandSource executor, Collection<ServerPlayerEntity> players, String dimensions)
	{
		try
		{
		ResourceLocation[] dim = Arrays.stream(dimensions.split(",")).map(s -> s.replaceAll("[^a-z0-9/._/-/:]", "")).map(ResourceLocation::new).toArray(ResourceLocation[]::new);
		ArrayList<DimensionType> dims = new ArrayList<DimensionType>(dim.length);
		for(ResourceLocation r : dim)
		{
			DimensionType type = DimensionType.byName(r);
			if(type==null || !r.equals(type.getRegistryName()))
			{
				executor.sendErrorMessage(new StringTextComponent("Did not found dimension " + r));
				return 0;
			}
			else
			{
				dims.add(type);
			}
		}
		
		if(dims.isEmpty())
		{
			executor.sendErrorMessage(new StringTextComponent("Dimension list was empty"));
			return 0;
		}
		
		Random r = new Random();
		
		for(ServerPlayerEntity e : players)
		{
			DimensionType target = dims.get(r.nextInt(dims.size()));
			ServerWorld world = e.getServer().getWorld(target);
			
			DDMain.respawnIn(world, e);
		}
		}
		catch(Exception e)
		{
			throw new CommandException(new StringTextComponent(e.getMessage()));
		}
		return Command.SINGLE_SUCCESS;
	}
}
