package com.dicemc.dicemcsjm.commands;

import com.dicemc.dicemcsjm.Config;
import com.dicemc.dicemcsjm.Prison;
import com.dicemc.dicemcsjm.WSD;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;

public class CommandSet{
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("setjail")
				.requires((p) -> p.hasPermission(Config.JAILER_PERM_LEVEL.get()))
				.executes((p) -> {return run(p);})
				.then(Commands.argument("leash", IntegerArgumentType.integer())
					.executes((p) -> {return runWithLeash(p);})
					.then(Commands.argument("prison", StringArgumentType.word())
						.executes((p) -> {return runWithPrison(p);}))));		
	}

	public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		WSD wsd = WSD.get(context.getSource().getLevel());
		BlockPos p = context.getSource().getPlayerOrException().blockPosition();
		BlockPos r = wsd.getJailReleasePos("default");
		wsd.setJail(new Prison("default", p, r, 100));
		wsd.setDirty();
		context.getSource().getPlayerOrException().sendMessage(new TranslatableComponent("msg.setjail", "default", p.toString()), context.getSource().getPlayerOrException().getUUID());
		return 0;
	}
	
	public static int runWithLeash(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		WSD wsd = WSD.get(context.getSource().getLevel());
		BlockPos p = context.getSource().getPlayerOrException().blockPosition();
		BlockPos r = wsd.getJailReleasePos("default");
		int leash = IntegerArgumentType.getInteger(context, "leash");
		wsd.setJail(new Prison("default", p, r, leash));
		wsd.setDirty();
		context.getSource().getPlayerOrException().sendMessage(new TranslatableComponent("msg.setjail", "default", p.toString()), context.getSource().getPlayerOrException().getUUID());
		return 0;
	}
	
	public static int runWithPrison(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		WSD wsd = WSD.get(context.getSource().getLevel());
		String prison = StringArgumentType.getString(context, "prison");
		BlockPos p = context.getSource().getPlayerOrException().blockPosition();
		BlockPos r = wsd.existingJail(prison) ? wsd.getJailReleasePos(prison) : context.getSource().getLevel().getSharedSpawnPos();
		int leash = IntegerArgumentType.getInteger(context, "leash");
		wsd.setJail(new Prison(prison, p, r, leash));
		wsd.setDirty();
		context.getSource().getPlayerOrException().sendMessage(new TranslatableComponent("msg.setjail", prison, p.toString()), context.getSource().getPlayerOrException().getUUID());
		return 0;
	}
	
}
