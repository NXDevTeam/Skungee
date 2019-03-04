package me.limeglass.skungee.api;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.limeglass.skungee.objects.packets.SkungeePacket;

/**
 * 
 * Called when the returned value from Bungeecord comes back from the SkungeePacket.
 *
 */
public class SkungeeReturnedEvent extends Event {
	
	private static final HandlerList handlers = new HandlerList();
	private final SkungeePacket packet;
	private Object object;
	
	public SkungeeReturnedEvent(SkungeePacket packet, Object object) {
		this.object = object;
		this.packet = packet;
	}
	
	public SkungeePacket getPacket() {
		return packet;
	}
	
	public Object getReturnedObject() {
		return object;
	}
	
	public void setReturnedObject(Object object) {
		this.object = object;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}