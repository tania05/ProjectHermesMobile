package ca.projecthermes.projecthermes.util;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.Set;

public class BundleHelper {
    @NonNull
    public static String describeContents(Bundle bundle) {
        if (bundle == null) {
            return "null";
        }


        StringBuilder builder = new StringBuilder();
        builder.append("{");

        boolean isFirstKey = true;
        for (String key : bundle.keySet()) {
            if (!isFirstKey) {
                builder.append(", ");
            }
            isFirstKey = false;

            Object value = bundle.get(key);
            builder.append(String.format("%s=%s", key, value));
        }

        builder.append("}");
        return builder.toString();
    }
}
