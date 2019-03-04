package me.limeglass.skungee.bungeecord.sockets;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.eclipse.jdt.annotation.Nullable;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.config.Configuration;
import me.limeglass.skungee.EncryptionUtil;
import me.limeglass.skungee.UniversalSkungee;
import me.limeglass.skungee.bungeecord.Skungee;
import me.limeglass.skungee.bungeecord.handlercontroller.SkungeeHandler;
import me.limeglass.skungee.bungeecord.handlers.SkungeePacketHandler;
import me.limeglass.skungee.objects.events.BungeeReceivedEvent;
import me.limeglass.skungee.objects.events.BungeeReturningEvent;
import me.limeglass.skungee.objects.packets.SkungeePacket;

public class BungeeRunnable implements Runnable {

	private final Configuration configuration;
	private final InetAddress address;
	private final Skungee instance;
	private final Socket socket;

	public BungeeRunnable(Socket socket) {
		this.instance = Skungee.getInstance();
		this.configuration = instance.getConfig();
		this.address = socket.getInetAddress();
		this.socket = socket;
	}

	@Override
	public void run() {
		Configuration configuration = Skungee.getInstance().getConfig();
		if (configuration.getBoolean("security.breaches.enabled", false)) {
			List<String> addresses = configuration.getStringList("security.breaches.blacklisted");
			if (!configuration.getBoolean("security.breaches.blacklist-is-whitelist", false)) {
				if (BungeeSockets.blocked.contains(address) || addresses.contains(address.getHostName())) return;
			} else if (!addresses.contains(address.getHostName()))
				return;
		}
		try {
			String algorithm = configuration.getString("security.encryption.cipherAlgorithm", "AES/CBC/PKCS5Padding");
			String keyString = configuration.getString("security.encryption.cipherKey", "insert 16 length");
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
			EncryptionUtil encryption = Skungee.getEncrypter();
			Object object = objectInputStream.readObject();
			if (object != null) {
				SkungeePacket packet = null;
				try {
					if (configuration.getBoolean("security.encryption.enabled", false)) {
						packet = (SkungeePacket) encryption.decrypt(keyString, algorithm, (byte[]) object);
					} else {
						packet = (SkungeePacket) object;
					}
				} catch (ClassCastException e) {
					Skungee.consoleMessage("", "Some security settings didn't match for the incoming packet.", "Make sure all your security options on the Spigot servers match the same as in the Bungeecord Skungee config.yml", "The packet could not be read, thus being cancelled.");
					attempt(address, null);
					if (configuration.getBoolean("security.debug"))
						Skungee.exception(e, "Could not decrypt packet " + UniversalSkungee.getPacketDebug(packet));
					objectInputStream.close();
					objectOutputStream.close();
					return;
				}
				BungeeReceivedEvent event = new BungeeReceivedEvent(packet, address);
				ProxyServer.getInstance().getPluginManager().callEvent(event);
				if (event.isCancelled()) {
					objectInputStream.close();
					objectOutputStream.close();
					return;
				}
				if (packet.getPassword() != null) {
					if (configuration.getBoolean("security.password.hash", true)) {
						byte[] password = encryption.hash();
						if (configuration.getBoolean("security.password.hashFile", false) && encryption.isFileHashed()) {
							password = encryption.getHashFromFile();
						}
						if (!Arrays.equals(password, packet.getPassword())) {
							incorrectPassword(packet);
							objectInputStream.close();
							objectOutputStream.close();
							return;
						}
					} else {
						String password = (String) encryption.deserialize(packet.getPassword());
						if (!password.equals(configuration.getString("security.password.password"))){
							incorrectPassword(packet);
							objectInputStream.close();
							objectOutputStream.close();
							return;
						}
					}
				} else if (configuration.getBoolean("security.password.enabled", false)) {
					incorrectPassword(packet);
					objectInputStream.close();
					objectOutputStream.close();
					return;
				}
				Optional<SkungeeHandler> handler = SkungeeHandler.getHandler(packet.getType());
				Object packetData = null;
				if (handler.isPresent()) {
					packetData = handler.get().callPacket(packet, address);
				} else {
					if (packet.getName() != null) {
						Optional<SkungeeHandler> externalHandler = SkungeeHandler.getHandler(packet.getName());
						if (externalHandler.isPresent()) {
							packetData = externalHandler.get().callPacket(packet, address);
						}
					} else {
						packetData = SkungeePacketHandler.handlePacket(packet, address);
					}
				}
				if (packetData != null && packet.isReturnable()) {
					BungeeReturningEvent returning = new BungeeReturningEvent(packet, packetData, address);
					ProxyServer.getInstance().getPluginManager().callEvent(returning);
					packetData = returning.getObject();
					Skungee.debugMessage("MADE IT HERE " + packetData.toString());
					if (configuration.getBoolean("security.encryption.enabled", false)) {
						byte[] serialized = encryption.serialize(packetData);
						byte[] encrypted = encryption.encrypt(keyString, algorithm, serialized);
						objectOutputStream.writeObject(encrypted);
					} else {
						objectOutputStream.writeObject(packetData);
					}
				}
			}
			objectInputStream.close();
			objectOutputStream.close();
		} catch(IOException | ClassNotFoundException e) {
			if (configuration.getBoolean("security.debug"))
				Skungee.exception(e, "Could not read incoming packet");
		}
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void incorrectPassword(SkungeePacket packet) {
		attempt(address, packet);
		Skungee.consoleMessage("&cA SkungeePacket with an incorrect password has just been recieved and blocked!");
		Skungee.consoleMessage("&cThe packet came from: " + socket.getInetAddress());
		Skungee.consoleMessage("&cThe packet type was: " + packet.getType());
		//insert more data maybe
	}
	
	private void attempt(InetAddress address, @Nullable SkungeePacket packet) {
		if (configuration.getBoolean("security.breaches.enabled", false)) {
			int attempts = 0;
			if (BungeeSockets.attempts.containsKey(address)) {
				attempts = BungeeSockets.attempts.get(address);
				BungeeSockets.attempts.remove(address, attempts);
			}
			attempts++;
			Skungee.consoleMessage(attempts + "");
			BungeeSockets.attempts.put(address, attempts);
			if (attempts >= configuration.getInt("security.breaches.attempts", 30)) {
				if (configuration.getBoolean("security.breaches.log", false)) {
					log("", "&cA BungeePacket with an incorrect password has just been recieved and blocked!", "&cThe packet came from: " + socket.getInetAddress());
					if (packet != null) log("&cThe packet type was: " + packet.getType());
				}
				if (configuration.getBoolean("security.breaches.shutdown", false)) {
					ProxyServer.getInstance().stop();
				}
				if (configuration.getBoolean("security.breaches.blockAddress", false)) {
					if (!BungeeSockets.blocked.contains(address)) BungeeSockets.blocked.add(address);
				}
			}
		}
	}
	
	private void log(String... strings) {
		File breaches = new File(Skungee.getInstance().getDataFolder(), "breaches.log");
		try {
			if (!breaches.exists()) breaches.createNewFile();
			Logger logger = Logger.getLogger("log");
			FileHandler handler = new FileHandler(Skungee.getInstance().getDataFolder() + File.separator + "breaches.log");
			handler.setFormatter(new SimpleFormatter());
			logger.addHandler(handler);
			for (String string : strings) {
				logger.info(string);
			}
		} catch (SecurityException | IOException e) {
			Skungee.exception(e, "Error logging a breach.");
		}
	}
}