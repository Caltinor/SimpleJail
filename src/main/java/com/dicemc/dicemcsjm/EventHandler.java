package com.dicemc.dicemcsjm;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import com.dicemc.dicemcsjm.SimpleJail.Type;
import com.dicemc.dicemcsjm.commands.CommandRelease;
import com.dicemc.dicemcsjm.commands.CommandRoot;
import com.dicemc.dicemcsjm.commands.CommandSet;
import com.dicemc.dicemcsjm.commands.CommandSetRelease;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fmlserverevents.FMLServerStartingEvent;

@Mod.EventBusSubscriber(modid=SimpleJail.MOD_ID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandler {
	private static final Logger LOG = Logger.getLogger(SimpleJail.MOD_ID);
	
	@SubscribeEvent
	public static void onServerStart(FMLServerStartingEvent event) {
		SimpleJail.jailServer = event.getServer();
		Iterable<ServerLevel> worlds = event.getServer().getAllLevels();
		for (ServerLevel world : worlds) {
			BlockPos p = WSD.get(world).getJailPos("default");
			BlockPos r = WSD.get(world).getJailReleasePos("default");
			if ((p.getX() == 0 && p.getY() == 0 && p.getZ() ==0) || (r.getX() == 0 && r.getY() == 0 && r.getZ() ==0)) {
				BlockPos s = world.getSharedSpawnPos();
				Prison defaultPrison = WSD.get(world).getPrison("default");
				defaultPrison.jailPos = s;
				defaultPrison.releasePos = s;
				WSD.get(world).setJail(defaultPrison);
				p = s;
			}	
			LOG.info("SimpleJail "+world.dimension().toString()+" Jail Position: ["+p.getX()+", "+p.getY()+", "+p.getZ()+"]");
		}
		
	}
	
	@SubscribeEvent
	public static void onCommandRegister(RegisterCommandsEvent event) {
		CommandRoot.register(event.getDispatcher());
		CommandSet.register(event.getDispatcher());
		CommandRelease.register(event.getDispatcher());
		CommandSetRelease.register(event.getDispatcher());
	}

	@SubscribeEvent
	public static void onChat(ServerChatEvent event) {
		UUID pid = event.getPlayer().getUUID();
		event.setCanceled(
				WSD.get(event.getPlayer().getLevel()).getJailed().containsKey(pid) ?
						!WSD.get(event.getPlayer().getLevel()).getJailed().get(pid).severity.equals(Type.DETAINED) 
						: false	);
	}
	
	@SubscribeEvent
	public static void onCommand(CommandEvent event) throws CommandSyntaxException {
		if (event.getParseResults().getContext().getSource().hasPermission(2)) return;
		UUID pid = null;
		try {pid = event.getParseResults().getContext().getSource().getPlayerOrException().getUUID();}
		catch (CommandSyntaxException e) {return;}
		if (WSD.get(event.getParseResults().getContext().getSource().getLevel()).getJailed().containsKey(pid) 
				&& !WSD.get(event.getParseResults().getContext().getSource().getLevel()).getJailed().get(pid).severity.equals(Type.DETAINED))
			event.setCanceled(true);
	}
	
	@SubscribeEvent
	public static void onBreak(BreakEvent event) {
		if (!event.getWorld().isClientSide()) { 
			ServerLevel world = event.getPlayer().getServer().getLevel(Level.OVERWORLD);
			event.setCanceled(
					WSD.get(world).getJailed().containsKey(event.getPlayer().getUUID()) ?
							!WSD.get(world).getJailed().get(event.getPlayer().getUUID()).severity.equals(Type.SILENCED) 
							: false	);
		}
	}
	
	@SubscribeEvent
	public static void onServerTick(ServerTickEvent event) {		
		if (event.phase.equals(Phase.START) /*&& event.side.equals(LogicalSide.SERVER)*/) {
			for (ServerLevel world : SimpleJail.jailServer.getAllLevels())
				if ((world.getGameTime() % 200) == 0) {
					for (Map.Entry<UUID, Sentence> population : WSD.get(world).getJailed().entrySet()) {
						ServerPlayer player = world.getServer().getPlayerList().getPlayer(population.getKey());
						if (player == null) continue;					
						//loop through and release people who are not jailed anymore					
						if (population.getValue().duration <= System.currentTimeMillis()) {
							WSD.get(world).getJailed().remove(population.getKey());
							WSD.get(world).setDirty();
							if (!population.getValue().severity.equals(Type.SILENCED)) {
								player.getInventory().load(population.getValue().inv);
								BlockPos r = WSD.get(world).getJailReleasePos(population.getValue().prison);
								player.setPosRaw(r.getX(), r.getY()+1, r.getZ());
							}
						}
						//check for out of place players and return them to jail
						else {
							if (population.getValue().severity.equals(Type.SILENCED)) {continue;}					
							BlockPos c = player.blockPosition();
							BlockPos p = WSD.get(world).getJailPos(population.getValue().prison);
							int leash = WSD.get(world).getJailLeash(population.getValue().prison);
							if (!player.getLevel().equals(world)) {
								player.teleportTo(world, p.getX(), p.getY(), p.getZ(), player.bob, player.getXRot());
							}
							else if (c.distSqr(p.getX(), p.getY(), p.getZ(), true) >= leash) {
								player.setPosRaw(p.getX(), p.getY(), p.getZ());
							}
						}
					}				
				}
		}
	}
}
