package games.yinga.Announcer.Commands

import com.mongodb.client.model.Filters
import games.yinga.Announcer.Main
import org.bson.Document
import org.bukkit.entity.Player

class AdminCommand(private val plugin: Main) {

    public fun execute(player: Player, args: List<String>) {

        if (!player.hasPermission("announcer.admin")) {
            plugin.sendMissingPermsMessage(player)
            return
        }

        if (args.size < 1) {
            plugin.sendUsageMessage(player, plugin.config.getStringList("messages.USAGE_ADMIN"))
            return
        }

        when (args[0].lowercase()) {
            "add" -> {

                if (args.size < 3) {
                    plugin.sendUsageMessage(
                            player,
                            plugin.config.getStringList("messages.USAGE_ADMIN_ADD")
                    )
                    return
                }

                val name = args[1]
                val lines = args.subList(2, args.size).joinToString(" ").split(";")

                if (plugin.db
                                .getCollection("Announcements")
                                .find(Filters.eq("name", name))
                                .first() != null
                ) {
					plugin.sendMessage(player, "ERROR_ANNOUNCEMENT_EXISTS", name)
					return
				}

                plugin.db
                        .getCollection("Announcements")
                        .insertOne(Document("name", name).append("lines", lines))
                plugin.sendMessage(player, "SUCCESS_ANNOUNCEMENT_ADD", name)
            }
            "remove" -> {
                if (args.size < 2) {
                    plugin.sendUsageMessage(
                            player,
                            plugin.config.getStringList("messages.USAGE_ADMIN_REMOVE")
                    )
                    return
                }
                val name = args[1]
                plugin.db.getCollection("Announcements").deleteOne(Filters.eq("name", name))
                plugin.sendMessage(player, "SUCCESS_ANNOUNCEMENT_REMOVE", name)
            }
            "edit" -> {
                if (args.size < 3) {
                    plugin.sendUsageMessage(
                            player,
                            plugin.config.getStringList("messages.USAGE_ADMIN_EDIT")
                    )
                    return
                }

                val name = args[1]
                val lines = args.subList(2, args.size).joinToString(" ").split(";")

                plugin.db
                        .getCollection("Announcements")
                        .replaceOne(
                                Filters.eq("name", name),
                                Document("name", name).append("lines", lines)
                        )
                plugin.sendMessage(player, "SUCCESS_ANNOUNCEMENT_EDIT", name)
            }
            else -> {
                plugin.sendUsageMessage(player, plugin.config.getStringList("messages.USAGE_ADMIN"))
            }
        }
    }
}
