package com.badskater0729.worldwidechat.runnables;

import java.util.concurrent.TimeUnit;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.util.CommonRefs;

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