package com.expl0itz.worldwidechat.runnables;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

public class SyncUserData implements Runnable {

	private WorldwideChat main = WorldwideChat.getInstance();
	
	@Override
	public void run() {
		final long startTime = System.nanoTime();
		try {
			CommonDefinitions.sendDebugMessage("Starting SyncUserData!!!");
			main.getConfigManager().syncData();
			final long duration = System.nanoTime() - startTime;
			CommonDefinitions.sendDebugMessage(CommonDefinitions.getMessage("wwcConfigSyncTime", new String[] {duration + ""}));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
