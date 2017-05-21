package ca.projecthermes.projecthermes.util;

import org.jetbrains.annotations.Contract;

public class TimeManager implements ITimeManager {
    public long getTime() { return System.currentTimeMillis(); }
}
