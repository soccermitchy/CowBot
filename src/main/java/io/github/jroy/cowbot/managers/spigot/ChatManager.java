package io.github.jroy.cowbot.managers.spigot;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.github.jroy.cowbot.CowBot;
import io.github.jroy.cowbot.managers.base.SpigotModule;
import io.github.jroy.cowbot.utils.AsyncFinishedChatEvent;
import io.github.jroy.cowbot.utils.ChatEnum;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.Map;

public class ChatManager extends SpigotModule {

  private CowBot cowBot;

  Map<String, ChatEnum> chatEnumCache = new HashMap<>();

  public ChatManager(CowBot cowBot) {
    super("Chat Manager", cowBot);
    this.cowBot = cowBot;
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onChat(AsyncPlayerChatEvent event) {
    if (event.getPlayer().hasPermission("trevor.admin") && event.getMessage().equalsIgnoreCase("hey trevor can you purge the normie cache")) {
      chatEnumCache.clear();
      cowBot.getServer().getScheduler().runTaskLaterAsynchronously(cowBot, () -> cowBot.getServer().broadcastMessage(ChatColor.AQUA + "[Trevor from Cowchop] " + ChatColor.WHITE + "sure dad :)"), 10);
    }
    String prefix = "";
    if (event.getPlayer().hasPermission("trevor.admin")) {
      prefix = ChatColor.GRAY + "[" + ChatColor.RED + "Admin" + ChatColor.GRAY + "] ";
    } else if (event.getPlayer().hasPermission("trevor.mod")) {
      prefix = ChatColor.GRAY + "[" + ChatColor.GREEN + "Mod" + ChatColor.GRAY + "] ";
    } else if (event.getPlayer().hasPermission("trevor.twitch")) {
      prefix = ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + "Twitch" + ChatColor.GRAY + "] ";
    } else if (event.getPlayer().hasPermission("trevor.content")) {
      prefix = ChatColor.GRAY + "[" + ChatColor.DARK_GREEN + "Content" + ChatColor.GRAY + "] ";
    } else if (event.getPlayer().hasPermission("trevor.gay")) {
      prefix = ChatColor.RED + "[" + ChatColor.GOLD + "G" + ChatColor.YELLOW + "a" + ChatColor.GREEN + "y" + ChatColor.LIGHT_PURPLE + "] ";
    }

    ChatEnum chatEnum = chatEnumCache.getOrDefault(event.getPlayer().getName(), ChatEnum.UNKNOWN);
    boolean hasChatEnum = chatEnum != null && chatEnum != ChatEnum.UNKNOWN;
    event.setFormat(prefix + ChatColor.GRAY + "<" + (hasChatEnum ? chatEnum.getChatColor() : "") + (hasChatEnum ? ChatColor.stripColor(event.getPlayer().getDisplayName()) : event.getPlayer().getDisplayName()) + ChatColor.GRAY + "> " + ChatColor.WHITE + event.getMessage().replaceAll("(?:[^%]|^)(?:(%%)+|)(%)(?:[^%])\n", "%%"));
    cowBot.getServer().getPluginManager().callEvent(new AsyncFinishedChatEvent(prefix, event.getPlayer().getDisplayName(), event.getMessage()));
  }

  @SuppressWarnings("UnstableApiUsage")
  @EventHandler(priority = EventPriority.HIGHEST)
  public void onJoin(PlayerJoinEvent event) {
    if (!chatEnumCache.containsKey(event.getPlayer().getName()) || chatEnumCache.get(event.getPlayer().getName()).equals(ChatEnum.UNKNOWN)) {
      chatEnumCache.put(event.getPlayer().getName(), ChatEnum.UNKNOWN);
      Bukkit.getScheduler().runTaskLaterAsynchronously(cowBot, () -> {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("trevorrequest|" + cowBot.getCurrentServer().getServerName());
        out.writeUTF(event.getPlayer().getName());
        event.getPlayer().sendPluginMessage(cowBot, "trevor:main", out.toByteArray());
      }, 30);
    }
  }
}