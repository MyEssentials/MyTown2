package mytown.chat.config.api;

import java.util.List;

import mytown.chat.channels.ChannelUser;

public interface UsersConfig {
	public void loadUsers();

	public void saveUser(ChannelUser user);

	public void saveUsers(List<ChannelUser> users);
}