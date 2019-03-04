package me.limeglass.skungee.spigot.elements.expressions;

import java.util.Set;
import org.bukkit.event.Event;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import me.limeglass.skungee.objects.packets.SkungeePacket;
import me.limeglass.skungee.objects.packets.SkungeePacketType;
import me.limeglass.skungee.spigot.lang.SkungeePropertyExpression;
import me.limeglass.skungee.spigot.utils.Utils;
import me.limeglass.skungee.spigot.utils.annotations.Properties;
import me.limeglass.skungee.spigot.utils.annotations.PropertiesAddition;

@Name("Bungeecord player address")
@Description("Returns the ip address(es) of the defined Bungeecord player(s).")
@Properties({"strings/players", "bungee[[ ]cord] ip [address[es]]", "{1}[(all [[of] the]|the)]"})
@PropertiesAddition("[(player|uuid)[s]]")
public class ExprBungeePlayerAddress extends SkungeePropertyExpression<Object, String> {

	@Override
	protected String[] get(Event event, Object[] skungeePlayers) {
		if (isNull(event)) return null;
		@SuppressWarnings("unchecked")
		Set<String> addresses = (Set<String>) sockets.send(new SkungeePacket(true, SkungeePacketType.PLAYERIP, Utils.toSkungeePlayers(skungeePlayers)));
		return (addresses != null) ? addresses.toArray(new String[addresses.size()]) : null;
	}

}
