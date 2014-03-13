package mytown.chat.config;

import java.io.File;
import java.util.List;

import mytown.chat.MyTownChat;
import mytown.chat.channels.ChannelHandler;
import mytown.chat.channels.ChannelUser;
import mytown.chat.config.api.UsersConfig;
import net.minecraftforge.common.ConfigCategory;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;
import net.minecraftforge.common.Property.Type;

public class UserLoader extends Configuration implements UsersConfig {
	ConfigCategory users;
	ChannelHandler channelHandler;
	
	public UserLoader(File file){
		super(file);
		users = getCategory("users");
		channelHandler = MyTownChat.INSTANCE.chatHandler.getChannelHandler();
	}
	
	@Override
	public void loadUsers() {
		for (Property userProp : users.getValues().values()){
			ChannelUser user = new ChannelUser(userProp.getName());
			user.joinChannels(userProp.getStringList());
		}
	}

	@Override
	public void saveUser(ChannelUser user) {
		users.put(user.getName(), new Property(user.getName(), (String[]) user.getChannels().toArray(), Type.STRING));
	}

	@Override
	public void saveUsers(List<ChannelUser> users) {
		for (ChannelUser user : users){
			saveUser(user);
		}
		save();
	}
}