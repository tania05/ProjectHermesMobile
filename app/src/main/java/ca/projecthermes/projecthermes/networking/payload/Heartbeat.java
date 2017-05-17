package ca.projecthermes.projecthermes.networking.payload;
public class Heartbeat {
    public boolean isReply;

    public Heartbeat(boolean isReply) {
        this.isReply = isReply;
    }
}
