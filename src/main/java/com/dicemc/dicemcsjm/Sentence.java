package com.dicemc.dicemcsjm;

import com.dicemc.dicemcsjm.SimpleJail.Type;

public class Sentence {
	public long duration;
	public Type severity;
	
	public Sentence(long duration, Type type) {
		this.duration = duration;
		this.severity = type;
	}
}
