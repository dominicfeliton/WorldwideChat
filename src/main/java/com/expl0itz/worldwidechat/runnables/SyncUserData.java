package com.expl0itz.worldwidechat.runnables;

import java.util.concurrent.TimeUnit;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

public class SyncUserData implements Runnable {

	private WorldwideChat main = WorldwideChat.instance;
	
	@Override
	public void run() {
		final long startTime = System.nanoTime();
		try {
			CommonDefinitions.sendDebugMessage("Starting SyncUserData!!!");
			main.getConfigManager().syncData(false);
			final long duration = System.nanoTime() - startTime;
			CommonDefinitions.sendDebugMessage("Automatic user data sync completed in " + TimeUnit.MILLISECONDS.convert(duration, TimeUnit.NANOSECONDS) + " ms.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
