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

package games.yinga.Announcer

import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bson.Document
import com.mongodb.MongoClientSettings
import com.mongodb.ConnectionString
import com.mongodb.client.MongoDatabase
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.model.Filters
import games.yinga.Announcer.Commands.AnnounceCommand
import games.yinga.Announer.Command.TabComplete

class Main : JavaPlugin() {

	lateinit private var mongoClient: MongoClient
	lateinit var db: MongoDatabase

	private var announcementIndex = 0
	
	override fun onEnable() {
		logger.info("Announcer Enabled")

		saveDefaultConfig()

		initDatabase()
		initTimer()

		getCommand("announce")?.setExecutor(AnnounceCommand(this))
		getCommand("announce")?.tabCompleter = TabComplete(this)

	}

	private fun initDatabase() {
		val settings = MongoClientSettings.builder()
						.applyConnectionString(ConnectionString(config.getString("database.url")!!))
						.build()
		
		mongoClient = MongoClients.create(settings)
		db = mongoClient.getDatabase(config.getString("database.name")!!)

		logger.info("Connected to Database")
	}

	private fun initTimer() {
		object : BukkitRunnable() {
			override fun run() {
				val collection = db.getCollection("Announcements")

				if(collection.countDocuments() - 1 < announcementIndex){
					announcementIndex = 0
				}

				collection.find().forEachIndexed { currentIndex, document ->
					if (currentIndex == announcementIndex) {
						sendAnnouncement(document)
						return@forEachIndexed
					}
				}

				announcementIndex++
			}
		}.runTaskTimer(this, 0, config.getLong("delay") * 20)
	}

	private fun sendAnnouncement(announcement: Document) {

		val name = announcement.getString("name")

		Bukkit.getOnlinePlayers().forEach {player ->
			val settings = db.getCollection("PlayerSettings").find(Filters.eq("uuid", player.uniqueId.toString())).first()

			if(settings != null) {

				val globalSettings = settings["global"] as Document

				var announcementEnabled = true
				var announcementSound = true

				if(settings[name] != null) {
					val announcementSettings = settings[name] as Document
					announcementEnabled = announcementSettings.getBoolean("enabled", true)
					announcementSound = announcementSettings.getBoolean("sound", true)
				}

				val globalEnabled = globalSettings.getBoolean("enabled", true)
				val globalSound = globalSettings.getBoolean("sound", true)
				
				if(!globalEnabled || !announcementEnabled) {
					return
				}

				if(globalSound && announcementSound) {
					player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1F, 1F)
				}
				
			} else {
				player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1F, 1F)
			}

			var message = ""
			
			message += config.getString("prefix")

			announcement.getList("lines", String::class.java).forEach {line -> 
				message += "\n&r" + line
			}

			message += "\n&r" + config.getString("suffix")?.replace("%name%", name)

			player.sendMessage(ChatColor.translateAlternateColorCodes('&', message))

		}
	}

	fun sendUsageMessage(player: Player, message: List<String>) {
		var output = config.getStringList("messages.USAGE_PREFIX").joinToString("\n") + "\n" + message.joinToString("\n") + "\n&r" + config.getStringList("messages.USAGE_SUFFIX").joinToString("\n")
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', output))
	}

	fun sendMissingPermsMessage(player: Player) {
		var output = config.getStringList("messages.MISSING_PERMISSION").joinToString("\n")
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', output))
	}

	fun sendMessage(player: Player, id: String, name: String?) {
		var output = config.getStringList("messages." + id).joinToString("\n")
		if(name != null) {
			output = output.replace("%name%", name)
		}
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', output))
	}

}