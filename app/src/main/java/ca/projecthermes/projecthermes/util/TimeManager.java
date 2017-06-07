package ca.projecthermes.projecthermes.util;

public class TimeManager implements ITimeManager {
    public long getTime() { return System.currentTimeMillis(); }
}
