package com.badskater0729.worldwidechat.runnables;

import java.util.concurrent.TimeUnit;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.util.CommonDefinitions;

public class SyncUserData implements Runnable {

	private WorldwideChat main = WorldwideChat.instance;
	
	@Override
	public void run() {
		final long startTime = System.nanoTime();
		try {
			CommonDefinitions.sendDebugMessage("Starting SyncUserData!!!");
			main.getConfigManager().syncData();
			final long duration = System.nanoTime() - startTime;
			CommonDefinitions.sendDebugMessage("Automatic user data sync completed in " + TimeUnit.MILLISECONDS.convert(duration, TimeUnit.NANOSECONDS) + " ms.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
