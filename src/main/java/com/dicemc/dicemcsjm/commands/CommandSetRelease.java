package com.dicemc.dicemcsjm.commands;

import com.dicemc.dicemcsjm.Config;
import com.dicemc.dicemcsjm.Prison;
import com.dicemc.dicemcsjm.WSD;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class CommandSetRelease{
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("setrelease")
				.requires((p) -> p.hasPermission(Config.JAILER_PERM_LEVEL.get()))
				.executes((p) -> {return run(p);})
				.then(Commands.argument("prison", StringArgumentType.word())
						.executes((p) -> {return runWithPrison(p);})));
	}
	
	public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		WSD wsd = WSD.get(context.getSource().getLevel());
		BlockPos p = context.getSource().getPlayerOrException().blockPosition();
		Prison ogp = wsd.getPrison("default");
		ogp.releasePos = p;
		wsd.setJail(ogp);
		context.getSource().getPlayerOrException().sendSystemMessage(Component.translatable("msg.setrelease.success", "Default", p.toString()));
		return 0;
	}
	
	public static int runWithPrison(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		WSD wsd = WSD.get(context.getSource().getLevel());
		BlockPos p = context.getSource().getPlayerOrException().blockPosition();
		String prison = StringArgumentType.getString(context, "prison");
		if (wsd.existingJail(prison)) {
			Prison ogp = wsd.getPrison(prison);
			ogp.releasePos = p;
			wsd.setJail(ogp);
			context.getSource().getPlayerOrException().sendSystemMessage(Component.translatable("msg.setrelease.success", prison, p.toString()));
			return 0;
		}
		context.getSource().getPlayerOrException().sendSystemMessage(Component.translatable("msg.setrelease.failure", prison));
		return 1;
	}
}
