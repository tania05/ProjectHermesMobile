package ca.projecthermes.projecthermes.util;

import java.lang.reflect.InvocationTargetException;

import ca.projecthermes.projecthermes.exceptions.NoDefaultConstructorException;

public class DefaultFactory<T> implements IFactory<T> {

    private final Class<T> _constructedClazz;

    public DefaultFactory(Class<T> constructedClazz) {
        _constructedClazz = constructedClazz;
    }

    @Override
    public T create() {
        try {
            return _constructedClazz.getConstructor().newInstance();
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new NoDefaultConstructorException();
        }
    }
}
