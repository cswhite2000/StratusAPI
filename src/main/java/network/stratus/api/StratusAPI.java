package network.stratus.api;

import network.stratus.api.api.RequestHandler;
import network.stratus.api.listeners.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URISyntaxException;

public class StratusAPI extends JavaPlugin {

    private RequestHandler requestHandler;

    @Override
    public void onEnable() {

        String ip = getConfig().getString("ip");
        String port = getConfig().getString("port");
        System.out.println(ip + ":" + port);
        Bukkit.broadcastMessage(ip);
        Bukkit.broadcastMessage(port);

        try {
            requestHandler = new RequestHandler(ip, port, this);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        Bukkit.getPluginManager().registerEvents(
            new PlayerListener(requestHandler, this, getConfig().getStringList("realms")), this);
    }
}
