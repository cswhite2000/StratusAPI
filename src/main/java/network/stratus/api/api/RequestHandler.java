package network.stratus.api.api;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.json.simple.JSONObject;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import javax.ws.rs.core.MediaType;
import java.net.URISyntaxException;
import java.util.UUID;

public class RequestHandler {

    Client client;
    String url;
    JavaPlugin javaPlugin;

    public RequestHandler(String ip, String port, JavaPlugin plugin) throws URISyntaxException {
        ClientConfig config = new ClientConfig();
        config.register(JacksonJsonProvider.class);
        client = ClientBuilder.newClient(config);

        url = "https://" + ip + ":" + port + "/users/";
        javaPlugin = plugin;
    }

    public void getPlayerInformation(UUID uuid, OnResultAction onResultAction, OnResultAction onFailure) {

        new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject jsonObject = client.target(url + "by_uuid/" + uuid)
                        .request(MediaType.APPLICATION_JSON)
                        .property(ClientProperties.FOLLOW_REDIRECTS, Boolean.TRUE)
                        .get(JSONObject.class);

                    Bukkit.getScheduler().runTask(javaPlugin, () -> onResultAction.onResponse(jsonObject, uuid));
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(url + "by_uuid/" + uuid);
                    Bukkit.getScheduler().runTask(javaPlugin, () -> onFailure.onResponse(null, uuid));
                }


            }
        }.run();

    }

}
