package ca.projecthermes.projecthermes.networking;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;

import ca.projecthermes.projecthermes.networking.payload.Heartbeat;

public class Packet<T> {

    private static final ArrayList<Packet> packetTypes = new ArrayList<Packet>();
    private static final Gson gson = new Gson();

    public static Packet<Heartbeat> HEARTBEAT_PACKET = new Packet<>(0, Heartbeat.class);

    private int packetType;
    private Class<T> clazz;



    public Packet(int packetType, Class<T> clazz) {
        this.packetType = packetType;
        this.clazz = clazz;

        packetTypes.add(this);
    }

    private T deserialize(JsonObject jsonObject) {
        return gson.fromJson(jsonObject, this.clazz);
    }

    public static String serialize(Object payload) {
        assert payload != null;

        Class objClazz = payload.getClass();
        for (Packet packet : packetTypes) {
            if (packet.clazz.equals(objClazz)) {
                JsonObject ret = new JsonObject();
                ret.addProperty("type", packet.packetType);
                ret.add("payload", gson.toJsonTree(payload));
                return gson.toJson(ret);
            }
        }

        Log.wtf("hermes", "Attempted to serialize undefined payload.");
        throw new RuntimeException("Invalid payload to serialize");
    }

    // returns null when not possible to deserialize
    public static Object deserialize(String json) {
        JsonElement jsonElement = gson.fromJson(json, JsonElement.class);

        if (!jsonElement.isJsonObject()) {
            return null;
        }

        JsonObject jsonObject = jsonElement.getAsJsonObject();

        JsonElement typeElement = jsonObject.get("type");
        if (typeElement == null) {
            return null;
        }
        if (!typeElement.isJsonPrimitive()) {
            return null;
        }
        JsonPrimitive typePrimitive = typeElement.getAsJsonPrimitive();
        if (!typePrimitive.isNumber()) {
            return null;
        }
        int type = typePrimitive.getAsInt();

        JsonElement payloadElement = jsonObject.get("payload");
        if (payloadElement == null) {
            return null;
        }
        if (!payloadElement.isJsonObject()) {
            return null;
        }
        JsonObject payloadObject = payloadElement.getAsJsonObject();

        for (Packet packet : packetTypes) {
            if (packet.packetType == type) {
                return packet.deserialize(payloadObject);
            }
        }

        Log.e("hermes", "Tried to deserialize unknown packet with type " + type);
        return null;
    }
}
