package mytown.core.utils.command;

import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommand;
import net.minecraft.server.MinecraftServer;

import com.esotericsoftware.reflectasm.MethodAccess;

public class CommandUtils {
	private static CommandHandler commandHandler;
	private static MethodAccess access;
	private static int method = -1;
	
	public static void init() throws Exception {
		commandHandler = (CommandHandler)MinecraftServer.getServer().getCommandManager();
		access = MethodAccess.get(CommandHandler.class);
		try {
			method = access.getIndex("registerCommand", ICommand.class, String.class);
		} catch(Exception e) {
			method = -1;
		}
	}
	
	public static void registerCommand(ICommand command, String permNode, boolean enabled) {
		if (!enabled || command == null) return;
		if (permNode.trim().isEmpty()) permNode = command.getClass().getName();
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
}