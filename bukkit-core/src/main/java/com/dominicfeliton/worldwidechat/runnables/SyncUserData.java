package com.dominicfeliton.worldwidechat.runnables;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.CommonRefs;

import java.util.concurrent.TimeUnit;

public class SyncUserData implements Runnable {

	private WorldwideChat main = WorldwideChat.instance;

	private CommonRefs refs = main.getServerFactory().getCommonRefs();
	
	@Override
	public void run() {
		final long startTime = System.nanoTime();
		try {
			refs.debugMsg("Starting SyncUserData!!!");
			main.getConfigManager().syncData();
			final long duration = System.nanoTime() - startTime;
			refs.debugMsg("Automatic user data sync completed in " + TimeUnit.MILLISECONDS.convert(duration, TimeUnit.NANOSECONDS) + " ms.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
