package com.dicemc.dicemcsjm.commands;

import com.dicemc.dicemcsjm.Config;
import com.dicemc.dicemcsjm.Prison;
import com.dicemc.dicemcsjm.WSD;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;

public class CommandSet{
	
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal("setjail")
				.requires((p) -> p.hasPermissionLevel(Config.JAILER_PERM_LEVEL.get()))
				.executes((p) -> {return run(p);})
				.then(Commands.argument("leash", IntegerArgumentType.integer())
					.executes((p) -> {return runWithLeash(p);})
					.then(Commands.argument("prison", StringArgumentType.word())
						.executes((p) -> {return runWithPrison(p);}))));		
	}

	public static int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
		WSD wsd = WSD.get(context.getSource().getWorld());
		BlockPos p = context.getSource().asPlayer().getPosition();
		BlockPos r = wsd.getJailReleasePos("default");
		wsd.setJail(new Prison("default", p, r, 100));
		wsd.markDirty();
		context.getSource().asPlayer().sendMessage(new TranslationTextComponent("msg.setjail", "default", p.toString()), context.getSource().asPlayer().getUniqueID());
		return 0;
	}
	
	public static int runWithLeash(CommandContext<CommandSource> context) throws CommandSyntaxException {
		WSD wsd = WSD.get(context.getSource().getWorld());
		BlockPos p = context.getSource().asPlayer().getPosition();
		BlockPos r = wsd.getJailReleasePos("default");
		int leash = IntegerArgumentType.getInteger(context, "leash");
		wsd.setJail(new Prison("default", p, r, leash));
		wsd.markDirty();
		context.getSource().asPlayer().sendMessage(new TranslationTextComponent("msg.setjail", "default", p.toString()), context.getSource().asPlayer().getUniqueID());
		return 0;
	}
	
	public static int runWithPrison(CommandContext<CommandSource> context) throws CommandSyntaxException {
		WSD wsd = WSD.get(context.getSource().getWorld());
		String prison = StringArgumentType.getString(context, "prison");
		BlockPos p = context.getSource().asPlayer().getPosition();
		BlockPos r = wsd.existingJail(prison) ? wsd.getJailReleasePos(prison) : context.getSource().getWorld().getSpawnPoint();
		int leash = IntegerArgumentType.getInteger(context, "leash");
		wsd.setJail(new Prison(prison, p, r, leash));
		wsd.markDirty();
		context.getSource().asPlayer().sendMessage(new TranslationTextComponent("msg.setjail", prison, p.toString()), context.getSource().asPlayer().getUniqueID());
		return 0;
	}
	
}
