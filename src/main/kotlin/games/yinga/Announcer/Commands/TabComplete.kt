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

package games.yinga.Announer.Command

import games.yinga.Announcer.Main
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class TabComplete(private val plugin: Main) : TabCompleter {

    override fun onTabComplete(
            sender: CommandSender,
            command: Command,
            alias: String,
            args: Array<out String>
    ): List<String> {

        if (command.name.lowercase() != "announce") {
            return emptyList()
        }

        var completions: MutableList<String> = mutableListOf()
        var ignoreOnInput = false

        when (args.size) {
            1 -> {
                completions = mutableListOf("manage")
                if (sender.hasPermission("announcer.admin")) {
                    completions.add("admin")
                }
            }
            2 -> {
                when (args[0].lowercase()) {
                    "admin" -> {
                        completions = mutableListOf("add", "remove", "edit")
                    }
                    "manage" -> {
                        completions = mutableListOf("global")
                        plugin.db.getCollection("Announcements").find().forEach { announcement ->
                            completions.add(announcement.getString("name").lowercase())
                        }
                    }
                }
            }
            3 -> {
                when (args[0].lowercase()) {
                    "admin" -> {
						val action = args[1].lowercase()
						if (action == "edit" || action == "remove") {
                            plugin.db.getCollection("Announcements").find().forEach { announcement ->
								completions.add(announcement.getString("name").lowercase())
							}
                        } else {
							completions = mutableListOf("<name>")
                        	ignoreOnInput = true
						}
                    }
                    "manage" -> {
                        completions = mutableListOf("enable", "disable", "sound")
                    }
                }
            }
            4 -> {
                when (args[0].lowercase()) {
                    "admin" -> {
                        val action = args[1].lowercase()
                        if (action == "add" || action == "edit") {
                            completions = mutableListOf("<content>")
                            ignoreOnInput = true
                        }
                    }
                    "manage" -> {
                        completions = mutableListOf("enable", "disable")
                    }
                }
            }
        }

        val input = args[args.size - 1].lowercase()

        if (ignoreOnInput && input != "") {
            return emptyList()
        }

        val matches = completions.filter { it.startsWith(input) }

        return if (matches.isEmpty()) completions else matches
    }
}
