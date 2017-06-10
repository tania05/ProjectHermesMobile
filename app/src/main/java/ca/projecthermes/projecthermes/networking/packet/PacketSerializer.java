package ca.projecthermes.projecthermes.networking.packet;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import ca.projecthermes.projecthermes.IHermesLogger;
import ca.projecthermes.projecthermes.networking.payload.IPayload;

public class PacketSerializer implements IPacketSerializer {
    private static final Gson gson = new Gson();

    private final IHermesLogger _logger;
    private final ArrayList<IPacket> _packetTypes;

    public PacketSerializer(
        @NotNull IHermesLogger logger,
        @NotNull ArrayList<IPacket> packetTypes
    ) {
        _logger = logger;
        _packetTypes = packetTypes;
    }

    @Override
    public IPayload deserialize(String json) {
        _logger.i("Attempting to deserialize json " + json);
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

        for (IPacket packet : _packetTypes) {
            if (packet.getPacketType() == type) {
                Object finalObject = gson.fromJson(payloadObject, packet.getPayloadClazz());

                if (!(finalObject instanceof IPayload)) {
                    _logger.wtf("Deserialize a payload this is not an instance of IPayload");
                    return null;
                }

                return (IPayload) finalObject;
            }
        }

        _logger.e("Tried to deserialize unknown packet with type " + type);
        return null;
    }

    public String serialize(Object payload) {
        assert payload != null;

        Class objClazz = payload.getClass();
        for (IPacket packet : _packetTypes) {
            if (packet.getPayloadClazz().equals(objClazz)) {
                JsonObject ret = new JsonObject();
                ret.addProperty("type", packet.getPacketType());
                ret.add("payload", gson.toJsonTree(payload));
                return gson.toJson(ret);
            }
        }

        _logger.wtf("Attempted to serialize undefined payload.");
        throw new RuntimeException("Invalid payload to serialize");
    }
}
