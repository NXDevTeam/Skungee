package me.limeglass.skungee.objects.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.limeglass.skungee.objects.packets.ServerPingPacket;

public class SkungeePingEvent extends Event {
	
	private static final HandlerList handlers = new HandlerList();
	private final ServerPingPacket packet;
	
	public SkungeePingEvent(ServerPingPacket packet) {
		super(true);
		this.packet = packet;
	}
	
	public ServerPingPacket getPacket() {
		return packet;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}