package io.github.jroy.cowbot.commands.discord;

import io.github.jroy.cowbot.commands.discord.base.CommandBase;
import io.github.jroy.cowbot.commands.discord.base.CommandEvent;
import io.github.jroy.cowbot.managers.proxy.DiscordManager;
import io.github.jroy.cowbot.utils.ATLauncherUtils;
import io.github.jroy.cowbot.utils.Constants;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;

import java.sql.SQLException;
import java.time.Instant;

public class LinkCommand extends CommandBase {

  private DiscordManager discordManager;

  public LinkCommand(DiscordManager discordManager) {
    super("link", "<minecraft username>", "Links your minecraft username with your discord account.");
    this.discordManager = discordManager;
    setDisabled(true);
  }

  @Override
  protected void executeCommand(CommandEvent e) {
    if (!e.getTextChannel().getId().equalsIgnoreCase(Constants.MC_CHANNEL_ID)) {
      return;
    }

    if ((Instant.now().getEpochSecond() - e.getMember().getTimeCreated().toEpochSecond()) <= 432000) {
      e.replyError("To prevent abuse, your linkage request was blocked. Please try again in a few days.");
      //noinspection ConstantConditions
      e.getGuild().getTextChannelById(Constants.SHOUT_CHANNEL_ID).sendMessage("New User Warning: " + e.getAuthor().getName() + "#" + e.getAuthor().getDiscriminator()).queue();
      return;
    }

    if (e.getMember().getRoles().isEmpty()) {
      e.reply("The minecraft server is for profileable users only! Pay Schlatt or no dice.");
      return;
    }

    String userId = e.getMember().getUser().getId();
    if (discordManager.getDatabaseManager().isBanned(userId)) {
      try {
        e.replyError("You are banned from the server for the following reason: " + discordManager.getDatabaseManager().getBanReason(userId));
      } catch (SQLException ex) {
        e.replyError("You are banned from the server but I honestly don't give two shits why.");
      }
      return;
    }

    if (e.getArgs().isEmpty()) {
      e.reply(invalid);
      return;
    }

    final Runnable addAtlPlayer = () -> ATLauncherUtils.addPlayer(e.getSplitArgs()[0]);
    if (discordManager.getDatabaseManager().isLinked(userId)) {
      try {
        String pastName = discordManager.getDatabaseManager().getUsernameFromDiscordId(userId);
        discordManager.getDatabaseManager().updateUser(userId, e.getSplitArgs()[0]);
        e.reply("Updated " + e.getMember().getAsMention() + "'s current Minecraft name to " + e.getSplitArgs()[0]);
        if (ProxyServer.getInstance().getPlayer(pastName) != null) {
          ProxyServer.getInstance().getPlayer(pastName).disconnect(new TextComponent("Your username has been updated!\nTo prevent freeloading, you've been disconnected\nfuck you"));
        }
        new Thread(() -> ATLauncherUtils.removePlayer(pastName)).start();
        new Thread(addAtlPlayer).start();
      } catch (SQLException ex) {
        e.replyError("Error while updating your Minecraft name: " + ex.getMessage());
        ex.printStackTrace();
      }
      return;
    }
    try {
      discordManager.getDatabaseManager().linkUser(userId, e.getSplitArgs()[0]);
      e.reply("Added " + e.getMember().getAsMention() + " to the whitelist with the username " + e.getSplitArgs()[0]);
      new Thread(addAtlPlayer).start();
    } catch (SQLException ex) {
      e.replyError("Error white linking your Minecraft name: " + ex.getMessage());
      ex.printStackTrace();
    }

  }
}
