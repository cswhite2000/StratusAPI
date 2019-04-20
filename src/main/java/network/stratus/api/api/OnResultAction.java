package network.stratus.api.api;


import org.json.simple.JSONObject;

import java.util.UUID;

public interface OnResultAction {
    public void onResponse(JSONObject jsonObject, UUID uuid);
}
