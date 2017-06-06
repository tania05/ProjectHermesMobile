package ca.projecthermes.projecthermes.util;

public interface ISource<T> {
    void update(T arg);

    void error(Exception e);
}
