// Copyright (C) 2023 Marcus Huber (xenorio) <dev@xenorio.xyz>
// 
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as
// published by the Free Software Foundation, either version 3 of the
// License, or (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
// 
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

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