package com.dicemc.dicemcsjm.commands;

import com.dicemc.dicemcsjm.Config;
import com.dicemc.dicemcsjm.WSD;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;

public class CommandRelease implements Command<CommandSourceStack>{
	private static final CommandRelease CMD = new CommandRelease();
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("release")
				.requires((p) -> p.hasPermission(Config.JAILER_PERM_LEVEL.get()))
				.then(Commands.argument("target", EntityArgument.player())
						.executes(CMD)));
	}

	@Override
	public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		ServerPlayer player = EntityArgument.getPlayer(context, "target");
		boolean found = false;
		
		for (ServerLevel world : context.getSource().getServer().getAllLevels()) {
			WSD wsd = WSD.get(world);			
			if (wsd.getJailed().containsKey(player.getUUID())) {
				wsd.getJailed().get(player.getUUID()).duration = System.currentTimeMillis();
				wsd.setDirty();
				context.getSource().sendSuccess(new TranslatableComponent("msg.release.success", player.getDisplayName()), false);
				found = true;
				break;
			}			
		}
		if (!found) context.getSource().sendFailure(new TranslatableComponent("msg.error.noplayer"));
		return 0;
	}
	
}
