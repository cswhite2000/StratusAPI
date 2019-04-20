package network.stratus.api.listeners;

import network.stratus.api.api.RequestHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

public class PlayerListener implements Listener {

    RequestHandler requestHandler;
    JavaPlugin plugin;
    HashMap<UUID, PermissionAttachment> attachmentHashMap;
    List<String> realms;


    public PlayerListener(RequestHandler requestHandler, JavaPlugin plugin, List<String> realms) {
        this.requestHandler = requestHandler;
        this.plugin = plugin;
        this.realms = realms;

        attachmentHashMap = new HashMap<>();
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {

        //The lambda will be run on the main thread, but the http request will not
        requestHandler.getPlayerInformation(event.getPlayer().getUniqueId(), (jsonObject, uuid) -> {

            Boolean banned = (Boolean)jsonObject.get("banned");

            Player player = Bukkit.getPlayer(uuid);

            if (banned) {
                player.kickPlayer("You're banned!");
            }

            PermissionAttachment attachment = player.addAttachment(plugin);

            LinkedHashMap<String, LinkedHashMap> hashMap = (LinkedHashMap<String, LinkedHashMap>) jsonObject.get("permissions");

            hashMap.keySet().forEach((key) -> {
                if (realms.contains(key)) {
                    LinkedHashMap<String, Boolean> permissionsHashMap = (LinkedHashMap<String, Boolean>) hashMap.get(key);
                    permissionsHashMap.forEach(attachment::setPermission);

                    if (permissionsHashMap.keySet().contains("op")) {
                        player.setOp(true);
                    }
                }
            });
            attachmentHashMap.put(uuid, attachment);

            if (!player.hasPermission("network.stratus.login")) {
                player.kickPlayer("You do not have permission to be on this server!");
            }

            ArrayList<HashMap<String, Object>> flairs = (ArrayList<HashMap<String, Object>>) jsonObject.get("flairs");

            HashMap<String, Object> chosenflair = null;

            for (HashMap<String, Object> stringStringHashMap : flairs) {
                if (realms.contains(stringStringHashMap.get("realm"))) {
                    if (chosenflair == null
                        || (Integer)chosenflair.get("priority") > (Integer)stringStringHashMap.get("priority")) {
                        chosenflair = stringStringHashMap;
                    }
                }
            }

            if (chosenflair != null) {
                player.setDisplayName(chosenflair.get("text") + "§r" + player.getDisplayName());
            }

        }, (jsonObject, uuid) -> {
            //api call failed, so kick the player;
            Player player = Bukkit.getPlayer(uuid);
            player.kickPlayer("Please join the main stratus server before joining this server");
        });
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        attachmentHashMap.remove(event.getPlayer().getUniqueId());
    }

}
