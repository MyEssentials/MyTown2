package mytown.entities.flag;

import java.util.ArrayList;
import java.util.List;

import mytown.MyTown;
import mytown.core.utils.Log;
import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage;

/**
 * Handles all the flag stuffs!
 * @author Joe Goett
 */
public class FlagHandler {
	private static List<ISerializer> serializers = new ArrayList<ISerializer>();
	private static Log log = MyTown.instance.log.createChild("Datasource");
	
	// Add the default Flag ISerializer
	static {
		addSerializer(new BaseFlagSerializer());
	}
	
	private FlagHandler() {}
	
	public static void addSerializer(ISerializer serializer) {
		serializers.add(serializer);
	}
		
	public static Flag<?> deserialize(String name, String value, String type) {
		for (ISerializer s : serializers) {
			if (s.getFlagTypes().contains(type)) {
				return s.deserialize(name, value, type);
			}
		}
		return null;
	}
	
	public static String serialize(Flag<?> flag) {
		for (ISerializer s : serializers) {
			if (s.getFlagTypes().contains(flag.getType().getName())) {
				return s.serialize(flag);
			}
		}
		return null;
	}

	public static void imc(IMCMessage msg) {
		String[] keyParts = msg.key.split("|");
		if (keyParts.length < 2) return;
		
		if (keyParts[1] == "register") {
			try {
				addSerializer((ISerializer)Class.forName(msg.getStringValue()).newInstance());
			} catch (Exception e) {
				log.warn("Failed to register the flag serializer %s from mod %s", e, msg.getStringValue(), msg.getSender());
			}
		}
	}
}