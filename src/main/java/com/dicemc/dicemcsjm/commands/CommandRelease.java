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
		ServerPlayerEntity player = EntityArgument.getPlayer(context, "target");
		boolean found = false;
		
		for (ServerWorld world : context.getSource().getServer().getWorlds()) {
			WSD wsd = WSD.get(world);			
			if (wsd.getJailed().containsKey(player.getUniqueID())) {
				wsd.getJailed().get(player.getUniqueID()).duration = System.currentTimeMillis();
				wsd.markDirty();
				context.getSource().sendFeedback(new TranslationTextComponent("msg.release.success", player.getDisplayName()), false);
				found = true;
				break;
			}			
		}
		if (!found) context.getSource().sendErrorMessage(new TranslationTextComponent("msg.error.noplayer"));
		return 0;
	}
	
}
