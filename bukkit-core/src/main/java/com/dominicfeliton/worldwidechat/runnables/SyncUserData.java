package com.dominicfeliton.worldwidechat.runnables;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class SyncUserData implements Runnable {

	private WorldwideChat main = WorldwideChat.instance;

	private CommonRefs refs = main.getServerFactory().getCommonRefs();

	private Player player = null;

	public SyncUserData() {}

	public SyncUserData(Player player) {
		this.player = player;
	}

	@Override
	public void run() {
		final long startTime = System.nanoTime();
		try {
			refs.debugMsg(refs.getMsg("wwcSyncUserDataStart", null));
			main.getConfigManager().syncData();

			final long converted = TimeUnit.MILLISECONDS.convert(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
			String storageOption;
			if (main.isSQLConnValid(true)) storageOption = "MySQL";
			else if (main.isPostgresConnValid(true)) storageOption = "PostgreSQL";
			else if (main.isMongoConnValid(true)) storageOption = "MongoDB";
			else storageOption = "YAML";

			refs.debugMsg(refs.serial(refs.getFancyMsg("wwcSyncUserDataComplete", new String[] {"&6"+storageOption, "&e(" + converted + "ms)"}, "&a",null)));
			if (player != null) {
				refs.sendFancyMsg("wwcSyncUserDataComplete", new String[] {"&6"+storageOption, "&e(" + converted + "ms)"}, "&a", player);
			}
		} catch (Exception e) {
			e.printStackTrace();
			main.getLogger().severe(refs.getMsg("wwcCouldNotSaveOhNo", null));
			if (player != null) {
				refs.sendFancyMsg("wwcCouldNotSaveOhNo", "", "&c", player);
			}
		}
	}
}
