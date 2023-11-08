package games.yinga.Announcer.Commands

import org.bukkit.entity.Player
import org.bson.Document
import games.yinga.Announcer.Main
import com.mongodb.client.model.Filters

class ManageCommand(private val plugin: Main) {

	public fun execute(player: Player, args: List<String>) {

		if(args.size < 2) {
			plugin.sendUsageMessage(player, plugin.config.getStringList("messages.USAGE_MANAGE"))
			return
		}

		val collection = plugin.db.getCollection("PlayerSettings")
		var document = collection.find(Filters.eq("uuid", player.uniqueId.toString())).first()

		if(document == null) {
			document = Document("uuid", player.uniqueId.toString())
						.append("global", Document("enabled", true)
											.append("sound", true)
						)
			collection.insertOne(document!!)
		}

		val name = args[0]
		val action = args[1]

		if(document.get(name) == null) {
			document.append(name, Document("enabled", true)
									.append("sound", true)
			)		
		}

		val announcementDocument = document[name] as Document

		when(action.lowercase()) {
			"enable" -> {
				announcementDocument.set("enabled", true)
				plugin.sendMessage(player, "SUCCESS_ANNOUNCEMENT_ENABLED", name)
			}
			"disable" -> {
				announcementDocument.set("enabled", false)
				plugin.sendMessage(player, "SUCCESS_ANNOUNCEMENT_DISABLED", name)
			}
			"sound" -> {
				if(args.size < 3) {
					plugin.sendUsageMessage(player, plugin.config.getStringList("messages.USAGE_MANAGE_SOUND"))
					return
				}
				val soundAction = args[2]
				when(soundAction.lowercase()) {
					"enable" -> {
						announcementDocument.set("sound", true)
						plugin.sendMessage(player, "SUCCESS_SOUND_ENABLED", name)
					}
					"disable" -> {
						announcementDocument.set("sound", false)
						plugin.sendMessage(player, "SUCCESS_SOUND_ENABLED", name)
					}
					else -> {
						plugin.sendUsageMessage(player, plugin.config.getStringList("messages.USAGE_MANAGE_SOUND"))
					}
				}
			}
			else -> {
				plugin.sendUsageMessage(player, plugin.config.getStringList("messages.USAGE_MANAGE"))
			}
		}

		document[name] = announcementDocument
		collection.replaceOne(Filters.eq("uuid", player.uniqueId.toString()), document)

	}

}