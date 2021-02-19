package com.dicemc.dicemcsjm;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.dicemc.dicemcsjm.SimpleJail.Type;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

public class WSD extends WorldSavedData{
	private static final String DATA_NAME = SimpleJail.MOD_ID + "_jaildata";
	
	private Map<UUID, Sentence> jailMap = new HashMap<UUID, Sentence>();
	private BlockPos jailPos = new BlockPos(0, 255, 0);
	
	public Map<UUID, Sentence> getJailed() {return jailMap;}
	public BlockPos getJailPos() {return jailPos;}
	public void setJailPos(BlockPos pos) {jailPos = pos;}
	
	public WSD() {super(DATA_NAME);}

	@Override
	public void read(CompoundNBT nbt) {
		jailPos = BlockPos.fromLong(nbt.getLong("jailpos"));
		ListNBT list = nbt.getList("jailmap", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			Sentence stc = new Sentence(list.getCompound(i).getLong("duration"), Type.values()[list.getCompound(i).getInt("severity")]);
			jailMap.put(list.getCompound(i).getUniqueId("pid"), stc);
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound = new CompoundNBT();
		compound.putLong("jailpos", jailPos.toLong());
		ListNBT list = new ListNBT();
		for (Map.Entry<UUID, Sentence> map : jailMap.entrySet()) {
			CompoundNBT nbt = new CompoundNBT();
			nbt.putUniqueId("pid", map.getKey());
			nbt.putLong("duration", map.getValue().duration);
			nbt.putInt("severity", map.getValue().severity.ordinal());
			list.add(nbt);
		}
		compound.put("jailmap", list);
		return compound;
	}
	
	public static WSD get(ServerWorld world) {
		return world.getSavedData().getOrCreate(WSD::new, DATA_NAME);
	}
}
