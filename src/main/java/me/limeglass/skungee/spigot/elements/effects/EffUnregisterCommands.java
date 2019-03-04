package me.limeglass.skungee.spigot.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import me.limeglass.skungee.objects.packets.SkungeePacket;
import me.limeglass.skungee.objects.packets.SkungeePacketType;
import me.limeglass.skungee.spigot.lang.SkungeeEffect;
import me.limeglass.skungee.spigot.utils.annotations.Patterns;

import org.bukkit.event.Event;

@Name("Bungeecord unregister commands")
@Description("Unregister the commands from the defiend plugin(s).")
@Patterns("unregister [the] commands from [the] [bungee[[ ]cord]] plugin[s] %strings%")
public class EffUnregisterCommands extends SkungeeEffect {

	@Override
	protected void execute(Event event) {
		if (areNull(event)) return;
		sockets.send(new SkungeePacket(false, SkungeePacketType.UNREGISTERCOMMANDS, expressions.getAll(event, String.class)));
	}

}