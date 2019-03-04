package me.limeglass.skungee.spigot.elements.bungeetablistplus;

import org.bukkit.event.Event;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import codecrafter47.bungeetablistplus.api.bungee.CustomTablist;
import me.limeglass.skungee.objects.packets.SkungeePacket;
import me.limeglass.skungee.objects.packets.SkungeePacketType;
import me.limeglass.skungee.spigot.lang.SkungeeExpression;
import me.limeglass.skungee.spigot.utils.annotations.Disabled;
import me.limeglass.skungee.spigot.utils.annotations.Patterns;
import me.limeglass.skungee.spigot.utils.annotations.RegisterType;
import me.limeglass.skungee.spigot.utils.annotations.Single;

@Name("BungeeTabListPlus - New tablist")
@Description("Returns a new BungeeTabListPlus CustomTablist.")
@Patterns("[a] [new] [custom] bungee[[ ]tab[list]][[ ]plus] [tab[ ]]list [(of|with) size %number%]")
@RegisterType("customtablist")
@Disabled
@Single
public class ExprCustomTablist extends SkungeeExpression<CustomTablist> {
	
	@Override
	protected CustomTablist[] get(Event event) {
		int size = 80;
		if (!areNull(event))
			size = expressions.getInt(event, 0);
		return (CustomTablist[]) sockets.send(new SkungeePacket(true, SkungeePacketType.BTLP_TABLIST, size));
	}

}
