package ca.projecthermes.projecthermes.util;

import java.util.ArrayList;

public class Source<T> implements IObservable<T>, ISource<T>, IObservableSource<T> {
    private final ArrayList<IObservableListener<T>> _listeners;

    public Source() {
        _listeners = new ArrayList<>();
    }

    @Override
    public void subscribe(IObservableListener<T> listener) {
        synchronized (this) {
            _listeners.add(listener);
        }
    }

    @Override
    public void update(T arg) {
        synchronized (this) {
            for (IObservableListener<T> listener : _listeners) {
                listener.update(arg);
            }
        }
    }

    @Override
    public void error(Exception e) {
        synchronized (this) {
            for (IObservableListener<T> listener : _listeners) {
                listener.error(e);
            }
        }
    }
}
