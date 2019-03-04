package me.limeglass.skungee.spigot.elements.expressions;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.ExpressionType;
import me.limeglass.skungee.objects.packets.SkungeePacket;
import me.limeglass.skungee.objects.packets.SkungeePacketType;
import me.limeglass.skungee.spigot.lang.SkungeeExpression;
import me.limeglass.skungee.spigot.utils.annotations.ExpressionProperty;
import me.limeglass.skungee.spigot.utils.annotations.Patterns;
import me.limeglass.skungee.spigot.utils.annotations.Single;

@Name("Bungeecord script server")
@Description("Returns the name of the server that the script calling this syntax is from.")
@Patterns({"[name of] this [script[s]] [bungee[[ ]cord]] server", "[bungee[[ ]cord]] server [name] of this script", "current [bungee[[ ]cord]]server"})
@ExpressionProperty(ExpressionType.SIMPLE)
@Single
public class ExprBungeeCurrentServer extends SkungeeExpression<String> {
	
	@Override
	protected String[] get(Event event) {
		String name = (String) sockets.send(new SkungeePacket(true, SkungeePacketType.CURRENTSERVER, Bukkit.getPort()));
		if (name == null)
			return null;
		return new String[] {name};
	}

}
