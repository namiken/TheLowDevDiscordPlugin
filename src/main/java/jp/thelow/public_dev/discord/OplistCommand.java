package jp.thelow.public_dev.discord;

import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class OplistCommand implements CommandExecutor {

  @Override
  public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {

    Set<OfflinePlayer> operators = Bukkit.getOperators();
    String collect = operators.stream().map(p -> p.getName() + "/" + p.getUniqueId()).collect(Collectors.joining(", "));
    arg0.sendMessage(collect);

    return true;
  }
}
