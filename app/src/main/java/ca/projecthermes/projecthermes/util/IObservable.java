package ca.projecthermes.projecthermes.util;

public interface IObservable<T> {
    void subscribe(IObservableListener<T> listener);
}
