package ca.projecthermes.projecthermes.networking.payload;

import java.util.ArrayList;

public class TransmissionRequest implements IPayload {
    public boolean isRequesting;
    public ArrayList<byte[]> messageIdentifiers;

    public TransmissionRequest(boolean isRequesting, ArrayList<byte[]> messageIdentifiers) {
        this.isRequesting = isRequesting;
        this.messageIdentifiers = messageIdentifiers;
    }

    @Override
    public String toString() {
        return "{TransmissionRequest [isRequesting]:" + isRequesting + " [messageIdentifiers]:" + messageIdentifiers.toString() + "}";
    }
}
