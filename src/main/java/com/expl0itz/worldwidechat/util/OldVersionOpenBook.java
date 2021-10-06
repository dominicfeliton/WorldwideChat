package com.expl0itz.worldwidechat.util;

import java.lang.reflect.Method;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.expl0itz.worldwidechat.util.ReflectionUtils.PackageType;

public class OldVersionOpenBook {

	// Written by jedk1. Thank you!
	
	private static boolean initialised = false;
	private static boolean isNewer = true;
	private static Method getHandle;
	private static Method openBook;
	   
	static {
	    try {
	        getHandle = ReflectionUtils.getMethod("CraftPlayer", PackageType.CRAFTBUKKIT_ENTITY, "getHandle");
	        try {
	            // 1.9+
	            openBook = ReflectionUtils.getMethod("EntityPlayer", PackageType.MINECRAFT_SERVER, "a", PackageType.MINECRAFT_SERVER.getClass("ItemStack"), PackageType.MINECRAFT_SERVER.getClass("EnumHand"));
	        } catch (ClassNotFoundException e) {
	            // 1.8
	        	isNewer = false;
	            openBook = ReflectionUtils.getMethod("EntityPlayer", PackageType.MINECRAFT_SERVER, "openBook", PackageType.MINECRAFT_SERVER.getClass("ItemStack"));
	        }
	        initialised = true;
	    } catch (ReflectiveOperationException e) {
	        e.printStackTrace();
	        Bukkit.getServer().getLogger().warning("Cannot force open book!");
	        initialised = false;
	    }
    }  
	   
	/**
	   * Open a "Virtual" Book ItemStack.
	   * @param i Book ItemStack.
	   * @param p Player that will open the book.
	   * @return
	   */
	public static boolean openBook(ItemStack i, Player p) {
	    if (!initialised) return false;
	    return (isNewer ? openBook119(i, p) : openBook118(i, p));
	}
	   
	private static boolean openBook118(ItemStack i, Player p) {
        try {
		    ItemStack held = (ItemStack) Player.class.getMethod("getItemInHand").invoke(p);
			try {
		        Player.class.getMethod("setItemInHand", ItemStack.class).invoke(p, i);
		        sendPacket(i, p);
			} catch (ReflectiveOperationException e) {
			    e.printStackTrace();
				initialised = false;
			}
			Player.class.getMethod("setItemInHand", ItemStack.class).invoke(p, held);
		    return initialised;
		} catch (Exception e) {
	        initialised = false;
			e.printStackTrace();
			return initialised;
		}
	}
	   
	private static boolean openBook119(ItemStack i, Player p) {
        ItemStack held = p.getInventory().getItemInMainHand();
		try {
		    p.getInventory().setItemInMainHand(i);
		    sendPacket(i, p);
		} catch (ReflectiveOperationException e) {
		    e.printStackTrace();
		    initialised = false;
		}
		p.getInventory().setItemInMainHand(held);
		return initialised;
	}

	private static void sendPacket(ItemStack i, Player p) throws ReflectiveOperationException {
	    if (isNewer) {
	        Object entityplayer = getHandle.invoke(p);
	        Class<?> enumHand = PackageType.MINECRAFT_SERVER.getClass("EnumHand");
	        Object[] enumArray = enumHand.getEnumConstants();
	        openBook.invoke(entityplayer, getItemStack(i), enumArray[0]);
	    } else {
	    	Object entityplayer = getHandle.invoke(p);	
	        openBook.invoke(entityplayer, getItemStack(i));
	    }
	}

	public static Object getItemStack(ItemStack item) {
	    try {
	        Method asNMSCopy = ReflectionUtils.getMethod(PackageType.CRAFTBUKKIT_INVENTORY.getClass("CraftItemStack"), "asNMSCopy", ItemStack.class);
	        return asNMSCopy.invoke(PackageType.CRAFTBUKKIT_INVENTORY.getClass("CraftItemStack"), item);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}
    
	/**
	 * Set the pages of the book in JSON format.
	 * @param metadata BookMeta of the Book ItemStack.
	 * @param pages Each page to be added to the book.
	 */
	@SuppressWarnings("unchecked")
	public static void setPages(BookMeta metadata, List<String> pages) {
	    List<Object> p;
	    Object page;
	    try {
	        p = (List<Object>) ReflectionUtils.getField(PackageType.CRAFTBUKKIT_INVENTORY.getClass("CraftMetaBook"), true, "pages").get(metadata);
	        for (String text : pages) {
	            page = ReflectionUtils.invokeMethod(ReflectionUtils.PackageType.MINECRAFT_SERVER.getClass("IChatBaseComponent$ChatSerializer").getDeclaredConstructor().newInstance(), "a", text);
	            p.add(page);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
}
