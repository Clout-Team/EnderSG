package com.cloutteam.rexcantor64.endersg.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.cloutteam.rexcantor64.endersg.Main;

public class AdminCMD implements CommandExecutor {

	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
		if (!s.hasPermission("admin")) {
			s.sendMessage("Unknown command. Type \"/help\" for help.");
			return false;
		}

		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("restart")) {
				Main.get().reloadGame();
				return true;
			}
			if (args[0].equalsIgnoreCase("start")) {
				Main.get().startGame();
				return true;
			}
			if (args[0].equalsIgnoreCase("forcedecay")) {
				Main.get().startBorderShrinking();
				return true;
			}
		}

		s.sendMessage("/" + label + " restart");
		s.sendMessage("/" + label + " start");
		s.sendMessage("/" + label + " forcedecay");

		return true;
	}

}
