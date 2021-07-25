package com.dicemc.dicemcsjm;

import com.dicemc.dicemcsjm.SimpleJail.Type;

import net.minecraft.nbt.ListTag;

public class Sentence {
	public long duration;
	public Type severity;
	public String prison;
	public ListTag inv;
	
	public Sentence(long duration, Type type, String prison, ListTag inv) {
		this.duration = duration;
		this.severity = type;
		this.prison = prison;
		this.inv = inv;
	}
}
