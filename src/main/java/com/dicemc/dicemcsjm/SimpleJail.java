package com.dicemc.dicemcsjm;

import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(SimpleJail.MOD_ID)
public class SimpleJail {
	public static final String MOD_ID = "dicemcsjm";
	
	public static enum Type {DETAINED, SILENCED, SOLITARY}
	public static enum Interval {MINUTES, HOURS, DAYS, WEEKS, MONTHS, YEARS}
	
	public static ServerWorld jailWorld;
	
	public SimpleJail() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);
		
		MinecraftForge.EVENT_BUS.register(this);
	}	
}
