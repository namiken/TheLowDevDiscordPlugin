package jp.thelow.public_dev.discord;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class Util {
  public static <T> void execAsync(Supplier<T> asyncFunc, Consumer<T> syncFunc) {
    new BukkitRunnable() {
      @Override
      public void run() {
        try {
          T result = asyncFunc.get();
          Bukkit.getScheduler().callSyncMethod(DiscordPlugin.plugin, () -> {
            try {
              syncFunc.accept(result);
            } catch (Exception e) {
              e.printStackTrace();
            }
            return null;
          });
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

    }.runTaskAsynchronously(DiscordPlugin.plugin);
  }
}
