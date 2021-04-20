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

import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

@Mod.EventBusSubscriber(modid=SimpleJail.MOD_ID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandler {
	private static final Logger LOG = Logger.getLogger(SimpleJail.MOD_ID);
	
	@SubscribeEvent
	public static void onServerStart(FMLServerStartingEvent event) {
		Iterable<ServerWorld> worlds = event.getServer().getWorlds();
		for (ServerWorld world : worlds) {
			BlockPos p = WSD.get(world).getJailPos("default");
			if (p.getX() == 0 && p.getY() == 0 && p.getZ() ==0) {
				BlockPos s = world.getSpawnPoint();
				Prison defaultPrison = WSD.get(world).getPrison("default");
				defaultPrison.jailPos = s;
				defaultPrison.releasePos = s;
				WSD.get(world).setJail(defaultPrison);
				p = s;
			}	
			LOG.info("SimpleJail "+world.getDimensionKey().toString()+" Jail Position: ["+p.getX()+", "+p.getY()+", "+p.getZ()+"]");
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
		UUID pid = event.getPlayer().getUniqueID();
		event.setCanceled(
				WSD.get(event.getPlayer().getServerWorld()).getJailed().containsKey(pid) ?
						!WSD.get(event.getPlayer().getServerWorld()).getJailed().get(pid).severity.equals(Type.DETAINED) 
						: false	);
	}
	
	@SubscribeEvent
	public static void onCommand(CommandEvent event) throws CommandSyntaxException {
		if (event.getParseResults().getContext().getSource().hasPermissionLevel(4)) return;
		UUID pid = event.getParseResults().getContext().getSource().asPlayer().getUniqueID();
		event.setCanceled(
				WSD.get(event.getParseResults().getContext().getSource().getWorld()).getJailed().containsKey(pid) ?
						!WSD.get(event.getParseResults().getContext().getSource().getWorld()).getJailed().get(pid).severity.equals(Type.DETAINED) 
						: false	);
	}
	
	@SubscribeEvent
	public static void onBreak(BreakEvent event) {
		if (!event.getWorld().isRemote()) { 
			ServerWorld world = event.getPlayer().getServer().getWorld(World.OVERWORLD);
			event.setCanceled(
					WSD.get(world).getJailed().containsKey(event.getPlayer().getUniqueID()) ?
							!WSD.get(world).getJailed().get(event.getPlayer().getUniqueID()).severity.equals(Type.SILENCED) 
							: false	);
		}
	}
	
	@SubscribeEvent
	public static void onServerTick(ServerTickEvent event) {		
		if (event.phase.equals(Phase.START) && event.side.equals(LogicalSide.SERVER)) {
			if (SimpleJail.jailWorld == null) return;
			ServerWorld world = SimpleJail.jailWorld;			
			if ((world.getGameTime() % 200) == 0) {
				for (Map.Entry<UUID, Sentence> population : WSD.get(world).getJailed().entrySet()) {
					ServerPlayerEntity player = world.getServer().getPlayerList().getPlayerByUUID(population.getKey());
					if (player == null) continue;					
					//loop through and release people who are not jailed anymore					
					if (population.getValue().duration <= System.currentTimeMillis()) {
						WSD.get(world).getJailed().remove(population.getKey());
						WSD.get(world).markDirty();
						if (!population.getValue().severity.equals(Type.SILENCED)) {
							player.inventory.read(population.getValue().inv);
							BlockPos r = WSD.get(world).getJailReleasePos(population.getValue().prison);
							player.forceSetPosition(r.getX(), r.getY()+1, r.getZ());
						}
					}
					//check for out of place players and return them to jail
					else {
						if (population.getValue().severity.equals(Type.SILENCED)) {continue;}					
						BlockPos c = player.getPosition();
						BlockPos p = WSD.get(world).getJailPos(population.getValue().prison);
						int leash = WSD.get(world).getJailLeash(population.getValue().prison);
						if (c.distanceSq(p.getX(), p.getY(), p.getZ(), true) >= leash)
							player.forceSetPosition(p.getX(), p.getY(), p.getZ());
					}
				}				
			}
		}
	}
}
