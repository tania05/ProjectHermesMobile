package ca.projecthermes.projecthermes.util;

public interface IObservableListener<T> {
    void update(T arg);
    void error(Exception e);
}

