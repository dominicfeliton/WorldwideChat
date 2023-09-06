package com.badskater0729.worldwidechat.runnables;

import java.util.concurrent.TimeUnit;

import com.badskater0729.worldwidechat.WorldwideChat;

import static com.badskater0729.worldwidechat.util.CommonRefs.debugMsg;

public class SyncUserData implements Runnable {

	private WorldwideChat main = WorldwideChat.instance;
	
	@Override
	public void run() {
		final long startTime = System.nanoTime();
		try {
			debugMsg("Starting SyncUserData!!!");
			main.getConfigManager().syncData();
			final long duration = System.nanoTime() - startTime;
			debugMsg("Automatic user data sync completed in " + TimeUnit.MILLISECONDS.convert(duration, TimeUnit.NANOSECONDS) + " ms.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
