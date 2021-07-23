package com.expl0itz.worldwidechat.commands;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.expl0itz.worldwidechat.WorldwideChat;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestPlayerCommands {

	private static ServerMock server;
	private static WorldwideChat plugin;
	private static PlayerMock playerMock;
	private static PlayerMock secondPlayerMock;
	private static PlayerMock thirdPlayerMock;
	private static PlayerMock fourthPlayerMock;

	@BeforeAll
	public static void setUp() {
		server = MockBukkit.mock();
		plugin = (WorldwideChat) MockBukkit.load(WorldwideChat.class);
		playerMock = server.addPlayer();
		playerMock.setName("player1");
		secondPlayerMock = server.addPlayer();
		secondPlayerMock.setName("player2");
		thirdPlayerMock = server.addPlayer();
		thirdPlayerMock.setName("player3");
		fourthPlayerMock = server.addPlayer();
		fourthPlayerMock.setName("player4");
		playerMock.addAttachment(plugin, "worldwidechat.wwct", true);
		playerMock.addAttachment(plugin, "worldwidechat.wwct.otherplayers", true);
		playerMock.addAttachment(plugin, "worldwidechat.wwcg", true);
		playerMock.addAttachment(plugin, "worldwidechat.wwctb", true);
		playerMock.addAttachment(plugin, "worldwidechat.wwctb.otherplayers", true);
		playerMock.addAttachment(plugin, "worldwidechat.wwcts", true);
		playerMock.addAttachment(plugin, "worldwidechat.wwcts.otherplayers", true);
		playerMock.addAttachment(plugin, "worldwidechat.wwcti", true);
		playerMock.addAttachment(plugin, "worldwidechat.wwcti.otherplayers", true);
		playerMock.addAttachment(plugin, "worldwidechat.wwctrl", true);
		playerMock.addAttachment(plugin, "worldwidechat.wwcs", true);
		secondPlayerMock.addAttachment(plugin, "worldwidechat.wwct", true);
		secondPlayerMock.addAttachment(plugin, "worldwidechat.wwct.otherplayers", true);
		thirdPlayerMock.addAttachment(plugin, "worldwidechat.wwct", true);
		thirdPlayerMock.addAttachment(plugin, "worldwidechat.wwct.otherplayers", true);
		fourthPlayerMock.addAttachment(plugin, "worldwidechat.wwct", true);
		fourthPlayerMock.addAttachment(plugin, "worldwidechat.wwct.otherplayers", true);
	}

	@AfterAll
	public static void tearDown() {
		MockBukkit.unmock();
	}

	@Test
	@Order(1)
	public void testTranslateCommandPlayerSourceTarget() {
		/* User runs /wwct en es */
		playerMock.performCommand("worldwidechat:wwct en es");
		playerMock.assertSaid("§4§l[§x§5§7§5§7§c§4§lWWC§4§l]§d Now translating all of your chat from en to es.");
	}
	
	@Test
	@Order(2)
	public void testTranslateCommandPlayerSourceTargetOther() {
		/* User runs /wwct player2 en es */
		playerMock.performCommand("worldwidechat:wwct player2 en es");
		playerMock.assertSaid("§4§l[§x§5§7§5§7§c§4§lWWC§4§l]§d Now translating the chat of player2 from en to es.");
	}
	
	@Test
	@Order(3)
	public void testTranslateCommandPlayerTarget() {
		/* User runs /wwct es */
		thirdPlayerMock.performCommand("worldwidechat:wwct es");
		thirdPlayerMock.assertSaid("§4§l[§x§5§7§5§7§c§4§lWWC§4§l]§d Now translating all of your chat to es.");
	}
	
	@Test
	@Order(4)
	public void testTranslateCommandPlayerTargetOther() {
		/* User runs /wwct player4 fr */
		thirdPlayerMock.performCommand("worldwidechat:wwct player4 fr");
		thirdPlayerMock.assertSaid("§4§l[§x§5§7§5§7§c§4§lWWC§4§l]§d Now translating the chat of player4 to fr.");
	}
	
	@Test
	@Order(5)
	public void testGlobalTranslateCommandPlayer() {
		/* User runs /wwcg en es */
		playerMock.performCommand("worldwidechat:wwcg en es");
		playerMock.assertSaid("§4§l[§x§5§7§5§7§c§4§lWWC§4§l]§d Now translating global chat from en to es.");
	}
	
	@Test
	@Order(6)
	public void testBookTranslateCommandPlayer() {
		/* User runs /wwctb */
		playerMock.performCommand("worldwidechat:wwctb");
		playerMock.assertSaid("§4§l[§x§5§7§5§7§c§4§lWWC§4§l]§d Book translation enabled. Right-click while holding a book to translate it!");
	}
	
	@Test
	@Order(7)
	public void testBookTranslateCommandPlayerOther() {
		/* User runs /wwctb player2 */
		playerMock.performCommand("worldwidechat:wwctb player2");
		playerMock.assertSaid("§4§l[§x§5§7§5§7§c§4§lWWC§4§l]§d Book translation enabled for player2.");
	}
	
	@Test
	@Order(8)
	public void testSignTranslateCommandPlayer() {
		/* User runs /wwcts */
		playerMock.performCommand("worldwidechat:wwcts");
		playerMock.assertSaid("§4§l[§x§5§7§5§7§c§4§lWWC§4§l]§d Sign translation enabled. Right-click on a sign to translate it!");
	}
	
	@Test
	@Order(9)
	public void testSignTranslateCommandPlayerOther() {
		/* User runs /wwcts player2 */
		playerMock.performCommand("worldwidechat:wwcts player2");
		playerMock.assertSaid("§4§l[§x§5§7§5§7§c§4§lWWC§4§l]§d Sign translation enabled for player2.");
	}
	
	@Test
	@Order(10)
	public void testItemTranslateCommandPlayer() {
		/* User runs /wwcti */
		playerMock.performCommand("worldwidechat:wwcti");
		playerMock.assertSaid("§4§l[§x§5§7§5§7§c§4§lWWC§4§l]§d Item translation enabled. Right-click with an item in your main hand to translate it!");
	}
	
	@Test
	@Order(11)
	public void testItemTranslateCommandPlayerOther() {
		/* User runs /wwcti player2 */
		playerMock.performCommand("worldwidechat:wwcti player2");
		playerMock.assertSaid("§4§l[§x§5§7§5§7§c§4§lWWC§4§l]§d Item translation enabled for player2.");
	}
	
	@Test
	@Order(12)
	public void testRateLimitTranslateCommandPlayer() {
		/* User runs /wwctrl 5 */
		playerMock.performCommand("worldwidechat:wwctrl 5");
		playerMock.assertSaid("§4§l[§x§5§7§5§7§c§4§lWWC§4§l]§d Set your rate limit to 5 seconds.");
	}
	
	@Test
	@Order(13)
	public void testRateLimitTranslateCommandPlayerOther() {
		/* User runs /wwctrl player2 5 */
		playerMock.performCommand("worldwidechat:wwctrl player2 5");
		playerMock.assertSaid("§4§l[§x§5§7§5§7§c§4§lWWC§4§l]§d Set the rate limit of player2 to 5 seconds.");
	}
	
	@Test
	@Order(14)
	public void testStatsCommandPlayer() {
		/* User runs /wwcs */
		playerMock.performCommand("worldwidechat:wwcs");
		playerMock.assertSaid("§4§l[§x§5§7§5§7§c§4§lWWC§4§l]§r There are no records yet available for player1.");
	}
	
	@Test
	@Order(15)
	public void testStatsCommandPlayerOther() {
		/* User runs /wwcs player2 */
		playerMock.performCommand("worldwidechat:wwcs player2");
		playerMock.assertSaid("§4§l[§x§5§7§5§7§c§4§lWWC§4§l]§r There are no records yet available for player2.");
	}
}