package network.stratus.api.api;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.json.simple.JSONObject;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import javax.ws.rs.core.MediaType;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.UUID;

public class RequestHandler {

    Client client;
    String url;
    JavaPlugin javaPlugin;

    public RequestHandler(String ip, String port, JavaPlugin plugin) throws URISyntaxException {
        ClientConfig config = new ClientConfig();
        config.register(JacksonJsonProvider.class);
        HostnameVerifier hostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();

        try {
            client = createSLLClient(config);
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        url = "https://" + ip + ":" + port + "/users/";
        javaPlugin = plugin;
    }

    /**
     * Creates the sll client.
     *
     * @param clientConfig
     *            the client config
     * @return the client config
     * @throws KeyManagementException
     *             the key management exception
     * @throws NoSuchAlgorithmException
     *             the no such algorithm exception
     */
    private Client createSLLClient(ClientConfig clientConfig)
        throws KeyManagementException, NoSuchAlgorithmException {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        } };

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        ClientBuilder.newClient(clientConfig);

        Client client = ClientBuilder.newBuilder()
            .sslContext(sc)
            .hostnameVerifier(new HostnameVerifier() {
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            })
            .withConfig(clientConfig).build();

        return client;
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
