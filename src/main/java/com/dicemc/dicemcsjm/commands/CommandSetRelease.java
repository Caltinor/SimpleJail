package com.dicemc.dicemcsjm.commands;

import com.dicemc.dicemcsjm.Config;
import com.dicemc.dicemcsjm.Prison;
import com.dicemc.dicemcsjm.WSD;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class CommandSetRelease{
	
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal("setrelease")
				.requires((p) -> p.hasPermissionLevel(Config.JAILER_PERM_LEVEL.get()))
				.executes((p) -> {return run(p);})
				.then(Commands.argument("prison", StringArgumentType.word())
						.executes((p) -> {return runWithPrison(p);})));
	}
	
	public static int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
		WSD wsd = WSD.get(context.getSource().getServer().getWorld(World.OVERWORLD));
		BlockPos p = context.getSource().asPlayer().getPosition();
		Prison ogp = wsd.getPrison("default");
		ogp.releasePos = p;
		wsd.setJail(ogp);
		context.getSource().asPlayer().sendMessage(new TranslationTextComponent("msg.setrelease.success", "Default", p.toString()), context.getSource().asPlayer().getUniqueID());
		return 0;
	}
	
	public static int runWithPrison(CommandContext<CommandSource> context) throws CommandSyntaxException {
		WSD wsd = WSD.get(context.getSource().getServer().getWorld(World.OVERWORLD));
		BlockPos p = context.getSource().asPlayer().getPosition();
		String prison = StringArgumentType.getString(context, "prison");
		if (wsd.existingJail(prison)) {
			Prison ogp = wsd.getPrison(prison);
			ogp.releasePos = p;
			wsd.setJail(ogp);
			context.getSource().asPlayer().sendMessage(new TranslationTextComponent("msg.setrelease.success", prison, p.toString()), context.getSource().asPlayer().getUniqueID());
			return 0;
		}
		context.getSource().asPlayer().sendMessage(new TranslationTextComponent("msg.setrelease.failure", prison), context.getSource().asPlayer().getUniqueID());
		return 1;
	}
}
