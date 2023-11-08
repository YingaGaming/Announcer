package games.yinga.Announcer.Commands

import games.yinga.Announcer.Main
import games.yinga.Announcer.Commands.AdminCommand
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.Command
import org.bukkit.entity.Player

class AnnounceCommand(private val plugin: Main) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

		if(!(sender is Player)) {
			sender.sendMessage("You're not a player")
			return true;
		}

		val player: Player = sender

		if(args.size < 1){
			plugin.sendUsageMessage(player, plugin.config.getStringList("messages.USAGE"))
			return true
		}

		when (args[0].lowercase()) {
			"admin" -> {
				AdminCommand(plugin).execute(player, args.drop(1))
			}
			"manage" -> {
				ManageCommand(plugin).execute(player, args.drop(1))
			}
		}

		return true;
    }
}