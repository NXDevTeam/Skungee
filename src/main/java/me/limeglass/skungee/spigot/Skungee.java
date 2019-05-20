package me.limeglass.skungee.spigot;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import me.limeglass.skungee.EncryptionUtil;
import me.limeglass.skungee.SpigotConfigSaver;
import me.limeglass.skungee.objects.packets.SkungeePacket;
import me.limeglass.skungee.objects.packets.SkungeePacketType;
import me.limeglass.skungee.spigot.elements.Register;
import me.limeglass.skungee.spigot.sockets.Reciever;
import me.limeglass.skungee.spigot.sockets.Sockets;
import me.limeglass.skungee.spigot.utils.ReflectionUtil;
import me.limeglass.skungee.spigot.utils.Utils;
import net.md_5.bungee.api.ChatColor;

/**
 * Spigot side Skungee
 */
public class Skungee extends JavaPlugin {

	private final Map<String, FileConfiguration> files = new HashMap<>();
	private final String packageName = "me.limeglass.skungee.spigot";
	private final static String prefix = "&8[&cSkungee&8] &e";
	private final static String nameplate = "[Skungee] ";
	private EncryptionUtil encryption;
	private static Skungee instance;
	private SkriptAddon addon;
	private Reciever reciever;
	private Metrics metrics;
	private Sockets sockets;
	private boolean skript;

	public void onEnable() {
		instance = this;
		saveDefaultConfig();
		File config = new File(getDataFolder(), "config.yml");
		if (!getDescription().getVersion().equals(getConfig().getString("version"))) {
			consoleMessage("&dNew update found! Updating files now...");
			if (config.exists())
				new SpigotConfigSaver(this).execute();
		}
		for (String name : Arrays.asList("config", "syntax")) { //replace config with future files here
			File file = new File(getDataFolder(), name + ".yml");
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				saveResource(file.getName(), false);
			}
			FileConfiguration configuration = new YamlConfiguration();
			try {
				configuration.load(file);
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
			}
			files.put(name, configuration);
		}
		encryption = new EncryptionUtil(this);
		encryption.hashFile();
		if (getConfig().getBoolean("reciever.enabled", false)) {
			this.reciever = new Reciever(this);
		} else {
			this.sockets = new Sockets(this);
		}
		Plugin plugin = Bukkit.getPluginManager().getPlugin("Skript");
		if (plugin != null && plugin.isEnabled()) {
			skript = true;
			addon = Skript.registerAddon(this).setLanguageFileDirectory("lang");
		}
		metrics = new Metrics(this);
		Register.metrics(metrics); //statically registers everything. TODO clean.
		if (!getConfig().getBoolean("DisableRegisteredInfo", false))
			Bukkit.getLogger().info(nameplate + "has been enabled!");
	}

	public void loadSockets() {
		this.sockets = new Sockets(this);
	}

	public void onDisable() {
		sockets.send(new SkungeePacket(true, SkungeePacketType.DISCONNECT, Bukkit.getPort()));
		sockets.disconnect();
		getServer().getScheduler().cancelTasks(this);
	}

	public final static void exception(Throwable cause, String... info) {
		Map<String, PluginDescriptionFile> plugins = new HashMap<String, PluginDescriptionFile>();
		for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
			if (!plugin.getDescription().getName().equals("Skungee")) {
				String[] parts = plugin.getDescription().getMain().split("\\.");
				StringBuilder name = new StringBuilder(plugin.getDescription().getMain().length());
				for (int i = 0; i < parts.length - 1; i++) {
					name.append(parts[i]).append('.');
				}
				plugins.put(name.toString(), plugin.getDescription());
			}
		}
		infoMessage();
		infoMessage(getNameplate() + "Severe Error:");
		infoMessage(info);
		infoMessage();
		infoMessage("Something went wrong within Skungee.");
		infoMessage("Please report this error to the developers of Skungee so we can fix this from happening in the future.");
		Set<PluginDescriptionFile> stackPlugins = new HashSet<>();
		for (StackTraceElement stackTrace : Thread.currentThread().getStackTrace()) {
			for (Entry<String, PluginDescriptionFile> entry : plugins.entrySet()) {
				if (stackTrace.getClassName().contains(entry.getKey())) {
					stackPlugins.add(entry.getValue());
				}
			}
		}
		if (!stackPlugins.isEmpty()) {
			infoMessage();
			infoMessage("It looks like you are using some plugin(s) that aren't allowing Skungee to work properly.");
			infoMessage("Following plugins are probably related to this error in some way:");
			StringBuilder pluginsMessage = new StringBuilder();
			for (PluginDescriptionFile desc : stackPlugins) {
				pluginsMessage.append(desc.getName());
				pluginsMessage.append(" ");
			}
			infoMessage(pluginsMessage.toString());
			infoMessage("You should try disabling those plugins one by one, trying to find which one causes it.");
			infoMessage("If the error doesn't disappear even after disabling all listed plugins, it is probably a Skungee issue.");
		}
		infoMessage();
		infoMessage("Report this at https://github.com/TheLimeGlass/Skungee/issues", "You can also message one of the Skungee developers this error.");
		infoMessage();
		infoMessage("Stack trace:");
		boolean first = true;
		while (cause != null) {
			infoMessage((first ? "" : "Caused by: ") + cause.toString());
			for (final StackTraceElement e : cause.getStackTrace())
				infoMessage("    at " + e.toString());
			cause = cause.getCause();
			first = false;
		}
		infoMessage();
		infoMessage("Information:");
		infoMessage("  Skungee: " + getInstance().getConfig().getString("version"));
		infoMessage("  Bukkit: " + Bukkit.getBukkitVersion());
		infoMessage("  Minecraft: " + ReflectionUtil.getVersion());
		infoMessage("  Java: " + System.getProperty("java.version") + " (" + System.getProperty("java.vm.name") + " " + System.getProperty("java.vm.version") + ")");
		infoMessage("  OS: " + System.getProperty("os.name") + " " + System.getProperty("os.arch") + " " + System.getProperty("os.version"));
		infoMessage();
		infoMessage("Running CraftBukkit: " + Skript.isRunningCraftBukkit());
		infoMessage();
		infoMessage("Thread: " + Thread.currentThread());
		infoMessage();
		infoMessage("End of Error.");
		infoMessage();
	}

	public static Skungee getInstance() {
		return instance;
	}

	public boolean isSkriptPresent() {
		return skript;
	}

	public Metrics getMetrics() {
		return metrics;
	}

	public Sockets getSockets() {
		return sockets;
	}

	public Optional<ServerSocket> getReciever() {
		if (reciever == null)
			return Optional.empty();
		return Optional.of(reciever.getReciever());
	}

	public String getPackageName() {
		return packageName;
	}

	public static String getPrefix() {
		return prefix;
	}

	public static String getNameplate() {
		return nameplate;
	}

	public EncryptionUtil getEncrypter() {
		return encryption;
	}

	public SkriptAddon getAddonInstance() {
		return addon;
	}

	//Grabs a FileConfiguration of a defined name. The name can't contain .yml in it.
	public FileConfiguration getConfiguration(String file) {
		return (files.containsKey(file)) ? files.get(file) : null;
	}

	public static void save(String configuration) {
		try {
			File configurationFile = new File(instance.getDataFolder(), configuration + ".yml");
			instance.getConfiguration(configuration).save(configurationFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void debugMessage(@Nullable String... messages) {
		if (instance.getConfig().getBoolean("debug")) {
			for (String text : messages) consoleMessage("&b" + text);
		}
	}

	public static void infoMessage(@Nullable String... messages) {
		if (messages != null && messages.length > 0) {
			for (String text : messages) Bukkit.getLogger().info(getNameplate() + text);
		} else {
			Bukkit.getLogger().info("");
		}
	}

	public static void consoleMessage(@Nullable String... messages) {
		if (instance.getConfig().getBoolean("DisableConsoleMessages", false)) return;
		if (messages != null && messages.length > 0) {
			for (String text : messages) {
				if (instance.getConfig().getBoolean("DisableConsoleColour", false)) infoMessage(ChatColor.stripColor(Utils.cc(text)));
				else Bukkit.getConsoleSender().sendMessage(Utils.cc(prefix + text));
			}
		} else {
			Bukkit.getLogger().info("");
		}
	}

}
