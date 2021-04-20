package com.dicemc.dicemcsjm;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

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
	public Prison(CompoundNBT nbt) {
		name = nbt.getString("prison");
		jailPos = BlockPos.fromLong(nbt.getLong("pos"));
		releasePos = BlockPos.fromLong(nbt.getLong("release"));
		leash = nbt.getInt("leash");
	}
	
	public CompoundNBT toNBT(CompoundNBT nbt) {
		nbt.putString("prison", name);
		nbt.putLong("pos", jailPos.toLong());
		nbt.putLong("release", releasePos.toLong());
		nbt.putInt("leash", leash);
		return nbt;
	}
}
