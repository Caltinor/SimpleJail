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
	private Map<String, BlockPos> jailPos = new HashMap<String, BlockPos>();
	private BlockPos defaultJail = new BlockPos(0,255, 0);
	
	public Map<UUID, Sentence> getJailed() {return jailMap;}
	public BlockPos getJailPos(String prison) {return jailPos.getOrDefault(prison, defaultJail);}
	public void setJailPos(String prisonName, BlockPos pos) {
		if (prisonName.equalsIgnoreCase("default")) {
			defaultJail = pos;
			return;
		}
		jailPos.put(prisonName, pos);
	}
	
	public WSD() {super(DATA_NAME);}

	@Override
	public void read(CompoundNBT nbt) {
		defaultJail = BlockPos.fromLong(nbt.getLong("defaultprison"));
		ListNBT list = nbt.getList("prisons", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			jailPos.put(list.getCompound(i).getString("prison"), BlockPos.fromLong(list.getCompound(i).getLong("pos")));
		}
		list = nbt.getList("jailmap", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			UUID pid = list.getCompound(i).getUniqueId("pid");
			ListNBT invList = list.getCompound(i).getList("inv", Constants.NBT.TAG_COMPOUND);
			Sentence stc = new Sentence(
					list.getCompound(i).getLong("duration"), 
					Type.values()[list.getCompound(i).getInt("severity")],
					list.getCompound(i).getString("prison"),
					invList);
			jailMap.put(pid, stc);
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound = new CompoundNBT();
		compound.putLong("defaultprison", defaultJail.toLong());
		ListNBT list = new ListNBT();
		for (Map.Entry<String, BlockPos> map : jailPos.entrySet()) {
			CompoundNBT nbt = new CompoundNBT();
			nbt.putString("prison", map.getKey());
			nbt.putLong("pos", map.getValue().toLong());
			list.add(nbt);
		}
		compound.put("prisons", list);
		list = new ListNBT();
		for (Map.Entry<UUID, Sentence> map : jailMap.entrySet()) {
			CompoundNBT nbt = new CompoundNBT();
			nbt.putUniqueId("pid", map.getKey());
			nbt.putLong("duration", map.getValue().duration);
			nbt.putInt("severity", map.getValue().severity.ordinal());
			nbt.putString("prison", map.getValue().prison);
			nbt.put("inv", map.getValue().inv);
			list.add(nbt);
		}
		compound.put("jailmap", list);
		return compound;
	}
	
	public static WSD get(ServerWorld world) {
		return world.getSavedData().getOrCreate(WSD::new, DATA_NAME);
	}
}
