package com.expl0itz.worldwidechat.inventory;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;

public class EnchantGlowEffect extends Enchantment {

	  public EnchantGlowEffect(NamespacedKey id) {
	      super(id);
	  }

	  @Override
	  public boolean canEnchantItem(ItemStack arg0) {
	      return false;
	  }

	  @Override
	  public boolean conflictsWith(Enchantment arg0) {
	      return false;
	  }

	  @Override
	  public EnchantmentTarget getItemTarget() {
	      return null;
	  }

	  @Override
	  public int getMaxLevel() {
	      return 2;
	  }

	  @Override
	  public String getName() {
	      return "wwc_glow";
	  }
	  
	  @Override
	  public int getStartLevel() {
	      return 1;
	  }

	@Override
	public boolean isTreasure() {
		return false;
	}

	@Override
	public boolean isCursed() {
		return false;
	}
}
