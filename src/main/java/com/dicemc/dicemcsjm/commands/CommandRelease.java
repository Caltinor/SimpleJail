package com.dicemc.dicemcsjm.commands;

import com.dicemc.dicemcsjm.Config;
import com.dicemc.dicemcsjm.WSD;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class CommandRelease implements Command<CommandSource>{
	private static final CommandRelease CMD = new CommandRelease();
	
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal("release")
				.requires((p) -> p.hasPermissionLevel(Config.JAILER_PERM_LEVEL.get()))
				.then(Commands.argument("target", EntityArgument.player())
				.executes(CMD)));
	}

	@Override
	public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
		WSD wsd = WSD.get(context.getSource().getServer().getWorld(World.OVERWORLD));
		ServerPlayerEntity player = EntityArgument.getPlayer(context, "target");
		if (!wsd.getJailed().containsKey(player.getUniqueID())) {
			context.getSource().sendFeedback(new TranslationTextComponent("msg.error.noplayer"), false);
			return 0;
		}			
		wsd.getJailed().remove(player.getUniqueID());
		wsd.markDirty();
		ServerWorld world = context.getSource().getWorld();
		player.forceSetPosition(world.getSpawnPoint().getX(), world.getSpawnPoint().getY()+1, world.getSpawnPoint().getZ());
		return 0;
	}
	
}
