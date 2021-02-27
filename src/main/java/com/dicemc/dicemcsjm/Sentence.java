package com.dicemc.dicemcsjm;

import com.dicemc.dicemcsjm.SimpleJail.Type;

import net.minecraft.nbt.ListNBT;

public class Sentence {
	public long duration;
	public Type severity;
	public String prison;
	public ListNBT inv;
	
	public Sentence(long duration, Type type, String prison, ListNBT inv) {
		this.duration = duration;
		this.severity = type;
		this.prison = prison;
		this.inv = inv;
	}
}
