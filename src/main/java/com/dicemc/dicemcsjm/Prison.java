package com.dicemc.dicemcsjm;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;

public class Prison {
	public String name;
	public BlockPos jailPos, releasePos;
	public int leash;
	
	public Prison(String name, BlockPos jailPos, BlockPos releasePos, int leash) {
		this.name = name;
		this.jailPos = jailPos;
		this.releasePos = releasePos;
		this.leash = leash;
	}
	public Prison(String name, BlockPos jailPos) {
		this(name, jailPos, jailPos, 100);
	}
	public Prison(String name, BlockPos jailPos, int leash) {
		this(name, jailPos, jailPos, leash);
	}
	public Prison(CompoundTag nbt) {
		name = nbt.getString("prison");
		jailPos = BlockPos.of(nbt.getLong("pos"));
		releasePos = BlockPos.of(nbt.getLong("release"));
		leash = nbt.getInt("leash");
	}
	
	public CompoundTag toNBT(CompoundTag nbt) {
		nbt.putString("prison", name);
		nbt.putLong("pos", jailPos.asLong());
		nbt.putLong("release", releasePos.asLong());
		nbt.putInt("leash", leash);
		return nbt;
	}
}
