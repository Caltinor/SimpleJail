package com.dicemc.dicemcsjm.commands;

import com.dicemc.dicemcsjm.Config;
import com.dicemc.dicemcsjm.WSD;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class CommandSet implements Command<CommandSource>{
	private static final CommandSet CMD = new CommandSet();
	
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal("setjail")
				.requires((p) -> p.hasPermissionLevel(Config.JAILER_PERM_LEVEL.get()))
				.executes(CMD));		
	}

	@Override
	public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
		BlockPos p = context.getSource().asPlayer().getPosition();
		WSD.get(context.getSource().getServer().getWorld(World.OVERWORLD)).setJailPos(p);
		WSD.get(context.getSource().getServer().getWorld(World.OVERWORLD)).markDirty();
		context.getSource().asPlayer().sendMessage(new TranslationTextComponent("msg.setjail", p.toString()), context.getSource().asPlayer().getUniqueID());
		return 0;
	}
	
}
