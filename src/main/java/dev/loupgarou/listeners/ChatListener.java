package dev.loupgarou.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import dev.loupgarou.classes.LGPlayer;

public class ChatListener implements Listener{
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChat(AsyncPlayerChatEvent e) {
		if(e.isCancelled()) return;
		
		LGPlayer.thePlayer(e.getPlayer()).onChat(e.getMessage());
		e.setCancelled(true);
	}
}
