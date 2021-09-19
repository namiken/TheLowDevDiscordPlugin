package jp.thelow.public_dev.discord;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.OfflinePlayer;

public class OPManager {

  private static Set<String> mcidList = new HashSet<>();

  public static void addMcid(String mcid) {
    mcidList.add(mcid.toLowerCase());
  }

  public static void clear() {
    mcidList.clear();
  }

  public static boolean isExist(OfflinePlayer offlinePlayer) {
    if (offlinePlayer == null) { return false; }
    if (offlinePlayer.getName() == null) { return false; }

    return mcidList.contains(offlinePlayer.getName().toLowerCase());
  }
}
