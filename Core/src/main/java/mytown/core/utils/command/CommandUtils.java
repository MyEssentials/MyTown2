package mytown.core.utils.command;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import mytown.core.Log;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommand;
import net.minecraft.server.MinecraftServer;

import com.esotericsoftware.reflectasm.MethodAccess;

public class CommandUtils {
	private static Log log;
	private static CommandHandler commandHandler;
	private static MethodAccess access;
	private static int method = -1;

	public static Map<String, String> permissionList = new Hashtable<String, String>();
	public static Map<String, String> permissionListAdmin = new Hashtable<String, String>();

	public static void init() throws Exception {
		log = new Log("CommandUtils");
		log.getLogger().setLevel(Level.ALL);
		commandHandler = (CommandHandler) MinecraftServer.getServer().getCommandManager();
		access = MethodAccess.get(CommandHandler.class);
		try {
			log.finest("Attempting to retrieve registerCommand(ICommand, String)");
			method = access.getIndex("registerCommand", ICommand.class, String.class);
		} catch (Exception e) {
			log.finest("Defaulting to standard registerCommand(ICommand)");
			method = -1;
		}
	}

	public static void registerCommand(ICommand command, String permNode, boolean enabled) {
		if (!enabled || command == null) return;
		if (permNode.trim().isEmpty()) permNode = command.getClass().getName();
		log.finest("Registering command (%s) %s with perm node %s", command.getClass().getName(), command.getCommandName(), permNode);
		if (method == -1) {
			commandHandler.registerCommand(command);
		} else {
			access.invoke(commandHandler, method, command, permNode);
		}
	}

	public static void registerCommand(ICommand command, String permNode) {
		registerCommand(command, permNode, true);
	}

	public static void registerCommand(ICommand command, boolean enabled) {
		if (command == null) return;
		String permNode = command.getClass().getName();
		if (command.getClass().isAnnotationPresent(Permission.class)) {
			permNode = command.getClass().getAnnotation(Permission.class).node();
		}
		registerCommand(command, permNode, enabled);
	}

	public static void registerCommand(ICommand command) {
		registerCommand(command, true);
	}

	public static boolean doesStringStartWith(String str1, String str2) {
		return str2.regionMatches(true, 0, str1, 0, str1.length());
	}

	public static List<String> getListOfStringsMatchingLastWord(String[] strings1, String... strings2) {
		String s1 = strings1[strings1.length - 1];
		ArrayList<String> arraylist = new ArrayList<String>();
		for (String str : strings2) {
			String s2 = str;
			if (doesStringStartWith(s1, s2)) {
				arraylist.add(s2);
			}
		}
		return arraylist;
	}
}