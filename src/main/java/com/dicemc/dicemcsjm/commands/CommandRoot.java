package com.dicemc.dicemcsjm.commands;

import com.dicemc.dicemcsjm.Config;
import com.dicemc.dicemcsjm.Sentence;
import com.dicemc.dicemcsjm.WSD;
import com.dicemc.dicemcsjm.SimpleJail.Interval;
import com.dicemc.dicemcsjm.SimpleJail.Type;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class CommandRoot implements Command<CommandSource>{
	private static final CommandRoot CMD = new CommandRoot();
	
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal("jail")
				.requires((p) -> p.hasPermissionLevel(Config.JAILER_PERM_LEVEL.get()))
				.then(Commands.argument("target", EntityArgument.player())
					.then(Commands.argument("judgement", StringArgumentType.word())
						.suggests((c, b) -> b
								.suggest(Type.DETAINED.name().toLowerCase())
								.suggest(Type.SILENCED.name().toLowerCase())
								.suggest(Type.SOLITARY.name().toLowerCase())
								.buildFuture())
						.then(Commands.argument("duration", IntegerArgumentType.integer(1))
							.then(Commands.argument("interval", StringArgumentType.word())
									.suggests((c, b) -> b
									.suggest(Interval.MINUTES.name().toLowerCase())
									.suggest(Interval.HOURS.name().toLowerCase())
									.suggest(Interval.DAYS.name().toLowerCase())
									.suggest(Interval.WEEKS.name().toLowerCase())
									.suggest(Interval.MONTHS.name().toLowerCase())
									.suggest(Interval.YEARS.name().toLowerCase())
									.buildFuture())
				.executes(CMD))))));
	}

	@Override
	public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
		WSD wsd = WSD.get(context.getSource().getServer().getWorld(World.OVERWORLD));
		//arguments to variables
		ServerPlayerEntity convicted = EntityArgument.getPlayer(context, "target");
		Type type = parseType(StringArgumentType.getString(context, "judgement").toUpperCase());
		int duration = IntegerArgumentType.getInteger(context, "duration");
		Interval interval = parseInterval(StringArgumentType.getString(context, "interval").toUpperCase());
		//processing and saving
		long release = System.currentTimeMillis() + durationToLong(duration, interval);
		Sentence stc = new Sentence(release, type);
		wsd.getJailed().put(convicted.getUniqueID(), stc);
		wsd.markDirty();
		//update player location to jail if severity is not silenced		
		if (!type.equals(Type.SILENCED)) {
			BlockPos p = wsd.getJailPos();
			convicted.setBedPosition(p);
			convicted.forceSetPosition(p.getX(), p.getY(), p.getZ());
		}
		context.getSource().getServer().getPlayerList()
			.func_232641_a_(new TranslationTextComponent("msg.jail.success", convicted.getName(), type.toString(), String.valueOf(duration) + " " + interval.toString())
				, ChatType.CHAT, context.getSource().asPlayer().getUniqueID());
		return 0;
	}
	
	private long durationToLong(int duration, Interval interval) {
		long minute = 60000l;
		switch (interval) {
		case MINUTES: {return duration * minute;}
		case HOURS: {return duration * minute * 60l;}
		case DAYS: {return duration * minute * 1440l;}
		case WEEKS: {return duration * minute * 10080l;}
		case MONTHS: {return duration * minute * 302400l;}
		case YEARS: {return duration * minute * 3628800l;}
		default:}
		return 0l;
	}
	
	private Type parseType(String str) {
		switch (str) {
		case "DETAINED": {return Type.DETAINED;}
		case "SILENCED": {return Type.SILENCED;}
		case "SOLITARY": {return Type.SOLITARY;}
		default:}
		return Type.DETAINED;
	}
	
	private Interval parseInterval(String str) {
		switch (str) {
		case "MINUTES": {return Interval.MINUTES;}
		case "HOURS": {return Interval.HOURS;}
		case "DAYS": {return Interval.DAYS;}
		case "WEEKS": {return Interval.WEEKS;}
		case "MONTHS": {return Interval.MONTHS;}
		case "YEARS": {return Interval.YEARS;}
		default:}
		return Interval.MINUTES;
	}
}
