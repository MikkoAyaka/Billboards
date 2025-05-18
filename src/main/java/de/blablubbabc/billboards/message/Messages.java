package de.blablubbabc.billboards.message;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Messages {

	private static String[] messages;

	// loads messages from the messages.yml configuration file into memory
	public static void loadMessages(File messagesFile, Logger logger) {
		Message[] messageIDs = Message.values();
		messages = new String[Message.values().length];

		Map<String, CustomizableMessage> defaults = new HashMap<>();

		// initialize default messages
		for (Message message : Message.values()) {
			addDefault(defaults, message, message.defaultText, message.note);
		}

		// load the message file
		FileConfiguration config = YamlConfiguration.loadConfiguration(messagesFile);

		// for each message ID
		for (Message messageID : messageIDs) {
			// get default for this message
			CustomizableMessage messageData = defaults.get(messageID.name());

			// if default is missing, log an error and use some fake data for
			// now so that the plugin can run
			if (messageData == null) {
				logger.severe("Missing message for " + messageID.name() + ".  Please contact the developer.");
				messageData = new CustomizableMessage(messageID, "Missing message!  ID: " + messageID.name() + ".  Please contact a server admin.", null);
			}

			// read the message from the file, use default if necessary
			messages[messageID.ordinal()] = config.getString(messageID.name() + ".Text", messageData.text);
			config.set(messageID.name() + ".Text", messages[messageID.ordinal()]);
			// translate colors
			messages[messageID.ordinal()] = ChatColor.translateAlternateColorCodes('&', messages[messageID.ordinal()]);

			if (messageData.notes != null) {
				messageData.notes = config.getString(messageID.name() + ".Notes", messageData.notes);
				config.set(messageID.name() + ".Notes", messageData.notes);
			}
		}

		// save any changes
		try {
			config.save(messagesFile);
		} catch (IOException exception) {
			logger.severe("Unable to write to the configuration file at \"" + messagesFile.getName() + "\"");
		}

		defaults.clear();
		System.gc();
	}

	// helper for above, adds a default message and notes to go with a message
	private static void addDefault(Map<String, CustomizableMessage> defaults, Message id, String text, String notes) {
		CustomizableMessage message = new CustomizableMessage(id, text, notes);
		defaults.put(id.name(), message);
	}

	// gets a message from memory
	public static String getMessage(Message messageID) {
		return messages[messageID.ordinal()];
	}

	public static String getPlayerNameOrUnknown(String playerName) {
		return playerName != null ? playerName : Message.UNKNOWN_NAME.get();
	}

	public static String getUUIDStringOrUnknown(UUID uuid) {
		return uuid != null ? uuid.toString() : Message.UNKNOWN_UUID.get();
	}
}
