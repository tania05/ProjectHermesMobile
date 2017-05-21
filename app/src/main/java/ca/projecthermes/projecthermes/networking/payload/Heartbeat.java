package ca.projecthermes.projecthermes.networking.payload;
public class Heartbeat implements IPayload {
    public boolean isReply;

    public Heartbeat(boolean isReply) {
        this.isReply = isReply;
    }

    @Override
    public String toString() {
        return "{Heartbeat [isReply]:" + isReply + "}";
    }
}
