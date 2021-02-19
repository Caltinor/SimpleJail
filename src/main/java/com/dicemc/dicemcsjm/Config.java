package com.dicemc.dicemcsjm;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
	public static ForgeConfigSpec SERVER_CONFIG;
	
	public static ForgeConfigSpec.ConfigValue<Integer> JAILER_PERM_LEVEL;
	
	static {
		ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
	
		SERVER_BUILDER.comment("Server Settings").push("Server");
		JAILER_PERM_LEVEL = SERVER_BUILDER.comment("")
				.defineInRange("Jailer Perm Level", 4, 1, 4, Integer.class);
		SERVER_BUILDER.pop();
		
		SERVER_CONFIG = SERVER_BUILDER.build();
	}
	
}
