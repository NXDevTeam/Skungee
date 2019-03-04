package me.limeglass.skungee.spigot.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import me.limeglass.skungee.objects.ConnectReason;
import me.limeglass.skungee.objects.SkungeePlayer;
import me.limeglass.skungee.objects.packets.SkungeePacket;
import me.limeglass.skungee.objects.packets.SkungeePacketType;
import me.limeglass.skungee.spigot.lang.SkungeeEffect;
import me.limeglass.skungee.spigot.utils.Utils;
import me.limeglass.skungee.spigot.utils.annotations.Patterns;
import me.limeglass.skungee.spigot.utils.annotations.RegisterEnum;

import org.bukkit.event.Event;

@Name("Bungeecord connect players")
@Description("Send bungeecord players to different servers.")
@Patterns({"[skungee] (send|connect) bungee[[ ]cord] [(player|uuid)[s]] %strings/players% to [bungee[[ ]cord]] [server[s]] %string% [with reason %-connectreason%]", "[skungee] (send|connect) [(player|uuid)[s]] %strings/players% to [bungee[[ ]cord]] server[s] %string% [with reason %-connectreason%]"})
@RegisterEnum(ExprClass = ConnectReason.class, value = "connectreason")
public class EffConnectServer extends SkungeeEffect {

	@Override
	protected void execute(Event event) {
		if (isNull(event, 0) || isNull(event, 1))
			return;
		SkungeePlayer[] players = Utils.toSkungeePlayers(expressions.get(0).getAll(event));
		ConnectReason reason = ConnectReason.PLUGIN;
		if (!isNull(event, 2))
			reason = (ConnectReason)expressions.get(2).getSingle(event);
		sockets.send(new SkungeePacket(false, SkungeePacketType.CONNECTPLAYER, expressions.get(1).getSingle(event), reason.name(), players));
	}

}
