package jp.thelow.public_dev.discord;

import java.util.Collection;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageBulkDeleteEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.MessageDeleteEvent;
import discord4j.core.event.domain.message.MessageUpdateEvent;
import discord4j.discordjson.json.MessageData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DiscordPlugin extends JavaPlugin implements Listener {

  private DiscordClient discordClient;

  public static DiscordPlugin plugin;

  private ThreadGroup threadGroup = new ThreadGroup("discordListener");
  {
    threadGroup.setDaemon(true);
  }

  @Override
  public void onEnable() {
    DiscordPlugin.plugin = this;

    discordClient = DiscordClient.create(getConfig().getString("token"));
    updateOp();

    //コマンド登録
    getCommand("oplist").setExecutor(new OplistCommand());

    //discordリスナー登録
    setupDiscordListener();

    //Bukkitリスナー登録
    getServer().getPluginManager().registerEvents(this, this);

    //設定ファイル作成
    getConfig().options().copyDefaults(true);
    saveDefaultConfig();
  }

  @Override
  public void onDisable() {
    threadGroup.interrupt();
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent e) {
    if (OPManager.isExist(e.getPlayer())) {
      if (!e.getPlayer().isOp()) {
        e.getPlayer().setOp(true);
      }
      return;
    }

    e.getPlayer().kickPlayer("開発者discordの#mcid-listに名前がありません。自身のmcidが存在するか確認してください。");
  }

  public void updateOp() {

    Util.execAsync(() -> {
      synchronized (DiscordClient.class) {

        Snowflake channelId = Snowflake.of(getConfig().getLong("channel_id"));
        Snowflake messageId = Snowflake.of(getConfig().getLong("first_message_id"));

        Flux<MessageData> flux = discordClient.getChannelById(channelId)
            .getMessagesAfter(messageId);

        OPManager.clear();
        for (MessageData messageData : flux.toIterable()) {
          String content = messageData.content();
          if (content == null || content.isEmpty()) {
            continue;
          }

          for (String string : content.split("\n")) {
            OPManager.addMcid(string);
          }
        }
        return "OK";
      }
    }, s -> {
      //OP保持者の確認
      Set<OfflinePlayer> operators = Bukkit.getOperators();
      for (OfflinePlayer offlinePlayer : operators) {
        if (!OPManager.isExist(offlinePlayer)) {
          offlinePlayer.setOp(false);
        }
      }

      //オンラインプレイヤーの確認
      Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
      for (Player player : onlinePlayers) {
        if (!OPManager.isExist(player)) {
          player.kickPlayer("開発者discordの#mcid-listに名前がありません。自身のmcidが存在するか確認してください。");
        }
      }
    });

  }

  public void setupDiscordListener() {
    Thread updateThread = new Thread(threadGroup, () -> {
      discordClient
          .withGateway(client -> client.on(MessageUpdateEvent.class, event -> {
            updateOp();
            return Mono.empty();
          }))
          .block();
    });
    updateThread.setDaemon(true);
    updateThread.start();

    Thread createThread = new Thread(threadGroup, () -> {
      discordClient
          .withGateway(client -> client.on(MessageCreateEvent.class, event -> {
            updateOp();
            return Mono.empty();
          }))
          .block();
    });
    createThread.setDaemon(true);
    createThread.start();

    Thread deleteThread = new Thread(threadGroup, () -> {
      discordClient
          .withGateway(client -> client.on(MessageDeleteEvent.class, event -> {
            updateOp();
            return Mono.empty();
          }))
          .block();
    });
    deleteThread.setDaemon(true);
    deleteThread.start();

    Thread bulkDeleteThread = new Thread(threadGroup, () -> {
      discordClient
          .withGateway(client -> client.on(MessageBulkDeleteEvent.class, event -> {
            updateOp();
            return Mono.empty();
          }))
          .block();
    });
    bulkDeleteThread.setDaemon(true);
    bulkDeleteThread.start();
  }

}
