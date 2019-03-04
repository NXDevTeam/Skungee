package me.limeglass.skungee.bungeecord.handlers.returnables;

import java.net.InetAddress;
import me.limeglass.skungee.bungeecord.handlercontroller.SkungeeExecutor;
import me.limeglass.skungee.bungeecord.sockets.BungeeSockets;
import me.limeglass.skungee.objects.packets.BungeePacket;
import me.limeglass.skungee.objects.packets.BungeePacketType;
import me.limeglass.skungee.objects.packets.SkungeePacket;
import me.limeglass.skungee.objects.packets.SkungeePacketType;

public class EvaluateHandler extends SkungeeExecutor {

	static {
		registerHandler(new EvaluateHandler(), SkungeePacketType.EVALUATE);
	}
	
	@Override
	public void executePacket(SkungeePacket packet, InetAddress address) {
		if (packet.getObject() != null && packet.getSetObject() != null) {
			String[] evaluations = (String[]) packet.getObject();
			String[] evalServers = (String[]) packet.getSetObject();
			BungeePacket evalPacket = new BungeePacket(false, BungeePacketType.EVALUATE, evaluations);
			for (String server : evalServers) {
				BungeeSockets.send(evalPacket, serverTracker.getServer(server));
			}
		}
	}

}
