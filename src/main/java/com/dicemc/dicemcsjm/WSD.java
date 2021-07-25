package com.dicemc.dicemcsjm;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.dicemc.dicemcsjm.SimpleJail.Type;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.util.Constants;

public class WSD extends SavedData{
	private static final String DATA_NAME = SimpleJail.MOD_ID + "_jaildata";
	
	private Map<UUID, Sentence> jailMap = new HashMap<UUID, Sentence>();
	private Map<String, Prison> jailPos = new HashMap<String, Prison>();
	private Prison defaultJail = new Prison("default", new BlockPos(0, 0, 0), new BlockPos(0, 0, 0), 100);
	
	public Map<UUID, Sentence> getJailed() {return jailMap;}
	public BlockPos getJailPos(String prison) {return jailPos.getOrDefault(prison, defaultJail).jailPos;}
	public BlockPos getJailReleasePos(String prison) {return jailPos.getOrDefault(prison, defaultJail).releasePos;}
	public int getJailLeash(String prison) {return jailPos.getOrDefault(prison, defaultJail).leash;}
	public Prison getPrison(String prison) {return jailPos.getOrDefault(prison, defaultJail);}
	public void setJail(Prison prison) {
		if (prison.name.equalsIgnoreCase("default")) {
			defaultJail = prison;
			return;
		}
		jailPos.put(prison.name, prison);
	}
	public boolean existingJail(String prison) {return jailPos.containsKey(prison) || prison.equalsIgnoreCase("default");}
	
	public WSD(CompoundTag nbt) {this.load(nbt);}
	public WSD() {}

	public void load(CompoundTag nbt) {
		defaultJail = new Prison(nbt.getCompound("defaultprison"));
		ListTag list = nbt.getList("prisons", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			Prison prison = new Prison(list.getCompound(i));
			jailPos.put(prison.name, prison);
		}
		list = nbt.getList("jailmap", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			UUID pid = list.getCompound(i).getUUID("pid");
			ListTag invList = list.getCompound(i).getList("inv", Constants.NBT.TAG_COMPOUND);
			Sentence stc = new Sentence(
					list.getCompound(i).getLong("duration"), 
					Type.values()[list.getCompound(i).getInt("severity")],
					list.getCompound(i).getString("prison"),
					invList);
			jailMap.put(pid, stc);
		}
	}

	@Override
	public CompoundTag save(CompoundTag compound) {
		compound = new CompoundTag();
		compound.put("defaultprison", defaultJail.toNBT(new CompoundTag()));
		ListTag list = new ListTag();
		for (Map.Entry<String, Prison> map : jailPos.entrySet()) {
			list.add(map.getValue().toNBT(new CompoundTag()));
		}
		compound.put("prisons", list);
		list = new ListTag();
		for (Map.Entry<UUID, Sentence> map : jailMap.entrySet()) {
			CompoundTag nbt = new CompoundTag();
			nbt.putUUID("pid", map.getKey());
			nbt.putLong("duration", map.getValue().duration);
			nbt.putInt("severity", map.getValue().severity.ordinal());
			nbt.putString("prison", map.getValue().prison);
			nbt.put("inv", map.getValue().inv);
			list.add(nbt);
		}
		compound.put("jailmap", list);
		return compound;
	}
	
	public static WSD get(ServerLevel world) {
		return world.getDataStorage().computeIfAbsent(WSD::new, WSD::new, DATA_NAME);
	}
}
