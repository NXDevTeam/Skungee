package me.limeglass.skungee.spigot.elements.conditions;

import org.bukkit.event.Event;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import me.limeglass.skungee.objects.SkungeePlayer;
import me.limeglass.skungee.objects.packets.SkungeePacket;
import me.limeglass.skungee.objects.packets.SkungeePacketType;
import me.limeglass.skungee.spigot.lang.SkungeeCondition;
import me.limeglass.skungee.spigot.utils.Utils;
import me.limeglass.skungee.spigot.utils.annotations.Patterns;

@Name("Bungeecord player forge")
@Description("Check if a player from the Bungeecord is using the Forge client.")
@Patterns("[bungee[[ ]cord]] [(player|uuid)] %string/player% (1�(has|is (running|using))|2�(is(n't (running|using)| not))) [the] forge [client]")
public class CondPlayerForge extends SkungeeCondition {

	public boolean check(Event event) {
		if (areNull(event))
			return false;
		SkungeePlayer[] players = Utils.toSkungeePlayers(expressions.get(0).getSingle(event));
		return (boolean) sockets.send(new SkungeePacket(true, SkungeePacketType.ISUSINGFORGE, players)) ? isNegated() : !isNegated();
	}

}
