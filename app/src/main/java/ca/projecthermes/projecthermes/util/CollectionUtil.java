package ca.projecthermes.projecthermes.util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class CollectionUtil {
    public static <T, U> ArrayList<U> map(@NotNull Collection<T> originalArray, @NotNull IMapCallback<T, U> callback) {
        ArrayList<U> ret = new ArrayList<>(originalArray.size());
        for (T val : originalArray) {
            ret.add(callback.convert(val));
        }

        return ret;
    }

    public interface IMapCallback<T, U> {
        U convert(T val);
    }
}
