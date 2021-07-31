package com.expl0itz.worldwidechat.commands;

import com.expl0itz.worldwidechat.WorldwideChat;

import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;

public class TestPlayerCommands {

	private ServerMock server;
	private WorldwideChat plugin;
	private PlayerMock playerMock;
	private PlayerMock secondPlayerMock;
	private PlayerMock thirdPlayerMock;
	private PlayerMock fourthPlayerMock;

	public TestPlayerCommands(ServerMock server, WorldwideChat plugin, PlayerMock p1, PlayerMock p2, PlayerMock p3, PlayerMock p4) {
		this.server = server;
		this.plugin = plugin;
		playerMock = p1;
		secondPlayerMock = p2;
		thirdPlayerMock = p3;
		fourthPlayerMock = p4;
	}

	public void testTranslateCommandPlayerSourceTarget() {
		/* User runs /wwct en es */
		playerMock.performCommand("worldwidechat:wwct en es");
		playerMock.assertSaid("§4§l[§x§5§7§5§7§c§4§lWWC§4§l]§d Now translating all of your chat from en to es.");
	}
	
	public void testTranslateCommandPlayerSourceTargetOther() {
		/* User runs /wwct player2 en es */
		playerMock.performCommand("worldwidechat:wwct player2 en es");
		playerMock.assertSaid("§4§l[§x§5§7§5§7§c§4§lWWC§4§l]§d Now translating the chat of player2 from en to es.");
	}
	
	public void testTranslateCommandPlayerTarget() {
		/* User runs /wwct es */
		thirdPlayerMock.performCommand("worldwidechat:wwct es");
		thirdPlayerMock.assertSaid("§4§l[§x§5§7§5§7§c§4§lWWC§4§l]§d Now translating all of your chat to es.");
	}
	
	public void testTranslateCommandPlayerTargetOther() {
		/* User runs /wwct player4 fr */
		thirdPlayerMock.performCommand("worldwidechat:wwct player4 fr");
		thirdPlayerMock.assertSaid("§4§l[§x§5§7§5§7§c§4§lWWC§4§l]§d Now translating the chat of player4 to fr.");
	}
	
	public void testGlobalTranslateCommandPlayer() {
		/* User runs /wwcg en es */
		playerMock.performCommand("worldwidechat:wwcg en es");
		playerMock.assertSaid("§4§l[§x§5§7§5§7§c§4§lWWC§4§l]§d Now translating global chat from en to es.");
	}
	
	public void testBookTranslateCommandPlayer() {
		/* User runs /wwctb */
		playerMock.performCommand("worldwidechat:wwctb");
		playerMock.assertSaid("§4§l[§x§5§7§5§7§c§4§lWWC§4§l]§d Book translation enabled. Right-click while holding a book to translate it!");
	}
	
	public void testBookTranslateCommandPlayerOther() {
		/* User runs /wwctb player2 */
		playerMock.performCommand("worldwidechat:wwctb player2");
		playerMock.assertSaid("§4§l[§x§5§7§5§7§c§4§lWWC§4§l]§d Book translation enabled for player2.");
	}
	
	public void testSignTranslateCommandPlayer() {
		/* User runs /wwcts */
		playerMock.performCommand("worldwidechat:wwcts");
		playerMock.assertSaid("§4§l[§x§5§7§5§7§c§4§lWWC§4§l]§d Sign translation enabled. Right-click on a sign to translate it!");
	}
	
	public void testSignTranslateCommandPlayerOther() {
		/* User runs /wwcts player2 */
		playerMock.performCommand("worldwidechat:wwcts player2");
		playerMock.assertSaid("§4§l[§x§5§7§5§7§c§4§lWWC§4§l]§d Sign translation enabled for player2.");
	}
	
	public void testItemTranslateCommandPlayer() {
		/* User runs /wwcti */
		playerMock.performCommand("worldwidechat:wwcti");
		playerMock.assertSaid("§4§l[§x§5§7§5§7§c§4§lWWC§4§l]§d Item translation enabled. Right-click with an item in your main hand to translate it!");
	}
	
	public void testItemTranslateCommandPlayerOther() {
		/* User runs /wwcti player2 */
		playerMock.performCommand("worldwidechat:wwcti player2");
		playerMock.assertSaid("§4§l[§x§5§7§5§7§c§4§lWWC§4§l]§d Item translation enabled for player2.");
	}
	
	public void testRateLimitTranslateCommandPlayer() {
		/* User runs /wwctrl 5 */
		playerMock.performCommand("worldwidechat:wwctrl 5");
		playerMock.assertSaid("§4§l[§x§5§7§5§7§c§4§lWWC§4§l]§d Set your rate limit to 5 seconds.");
	}
	
	public void testRateLimitTranslateCommandPlayerOther() {
		/* User runs /wwctrl player2 5 */
		playerMock.performCommand("worldwidechat:wwctrl player2 5");
		playerMock.assertSaid("§4§l[§x§5§7§5§7§c§4§lWWC§4§l]§d Set the rate limit of player2 to 5 seconds.");
	}
	
	public void testStatsCommandPlayer() {
		/* User runs /wwcs */
		playerMock.performCommand("worldwidechat:wwcs");
		playerMock.assertSaid("§4§l[§x§5§7§5§7§c§4§lWWC§4§l]§r There are no records yet available for player1.");
	}
	
	public void testStatsCommandPlayerOther() {
		/* User runs /wwcs player2 */
		playerMock.performCommand("worldwidechat:wwcs player2");
		playerMock.assertSaid("§4§l[§x§5§7§5§7§c§4§lWWC§4§l]§r There are no records yet available for player2.");
	}
}