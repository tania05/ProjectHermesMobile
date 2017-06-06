package ca.projecthermes.projecthermes.networking.packet;

import ca.projecthermes.projecthermes.networking.payload.IPayload;

/**
 * Created by brand_000 on 2017-05-20.
 */

public interface IPacketSerializer {
    IPayload deserialize(String json);
    String serialize(Object payload);
}
