package net.chennj.remotectrl.client;

import java.nio.channels.SelectionKey;

import net.chennj.remotectrl.common.Errno;

public interface MessageHandler {
	
	public Errno processMessage(SelectionKey key);
}
