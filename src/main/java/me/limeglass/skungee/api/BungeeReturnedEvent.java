package me.limeglass.skungee.api;

import me.limeglass.skungee.objects.ConnectedServer;
import me.limeglass.skungee.objects.packets.BungeePacket;
import net.md_5.bungee.api.plugin.Event;

/**
 * 
 * Called when the returned value from Spigot comes back from the BungeePacket.
 *
 */
public class BungeeReturnedEvent extends Event {
	
	private final ConnectedServer server;
	private final BungeePacket packet;
	private Object object;
	
	public BungeeReturnedEvent(BungeePacket packet, Object object, ConnectedServer server) {
		this.server = server;
		this.object = object;
		this.packet = packet;
	}
	
	/**
	 * The server that the BungeePacket is being sent too.
	 * 
	 * @return The ConnectedServer that is being sent.
	 */
	public ConnectedServer getConnectedServer() {
		return server;
	}
	
	public BungeePacket getPacket() {
		return packet;
	}
	
	public Object getReturnedObject() {
		return object;
	}
	
	public void setReturnedObject(Object object) {
		this.object = object;
	}

}