package com.dominicfeliton.worldwidechat.runnables;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.storage.DataStorageUtils;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class SyncUserData {

    private WorldwideChat main = WorldwideChat.instance;

    private CommonRefs refs = main.getServerFactory().getCommonRefs();

    private Player player = null;

    public SyncUserData() {
        sync();
    }

    public SyncUserData(Player player) {
        this.player = player;
        sync();
    }

    private void sync() {
        final long startTime = System.nanoTime();
        try {
            refs.debugMsg(refs.getPlainMsg("wwcSyncUserDataStart"));
            DataStorageUtils.syncData();

            final long converted = TimeUnit.MILLISECONDS.convert(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
            String storageOption;
            if (main.isSQLConnValid(true)) storageOption = "MySQL";
            else if (main.isPostgresConnValid(true)) storageOption = "PostgreSQL";
            else if (main.isMongoConnValid(true)) storageOption = "MongoDB";
            else storageOption = "YAML";

            refs.debugMsg(refs.getPlainMsg("wwcSyncUserDataComplete", new String[]{"&6" + storageOption, "&e(" + converted + "ms)"}, "&a"));
            if (player != null) {
                refs.sendMsg("wwcSyncUserDataComplete", new String[]{"&6" + storageOption, "&e(" + converted + "ms)"}, "&a", player);
            }
        } catch (Exception e) {
            e.printStackTrace();
            main.getLogger().severe(refs.getPlainMsg("wwcCouldNotSaveOhNo"));
            if (player != null) {
                refs.sendMsg("wwcCouldNotSaveOhNo", "", "&c", player);
            }
        }
    }
}
