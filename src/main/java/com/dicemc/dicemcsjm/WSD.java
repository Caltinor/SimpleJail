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
	
	public WSD() {super(DATA_NAME);}

	@Override
	public void read(CompoundNBT nbt) {
		defaultJail = new Prison(nbt.getCompound("defaultprison"));
		ListNBT list = nbt.getList("prisons", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			Prison prison = new Prison(list.getCompound(i));
			jailPos.put(prison.name, prison);
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
		compound.put("defaultprison", defaultJail.toNBT(new CompoundNBT()));
		ListNBT list = new ListNBT();
		for (Map.Entry<String, Prison> map : jailPos.entrySet()) {
			list.add(map.getValue().toNBT(new CompoundNBT()));
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
