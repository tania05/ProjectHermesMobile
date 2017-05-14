package ca.projecthermes.projecthermes.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

public class ErrorCodeHelper {

    public static ArrayList<String> findPossibleConstantsForInt(int code, Class clazz) {
        ArrayList<String> possibilities = new ArrayList<String>();

        Field[] fields = clazz.getFields();
        for (Field field : fields) {

            // Field must be "public static"
            int requiredModifiers = Modifier.STATIC | Modifier.PUBLIC;
            if ((field.getModifiers() & requiredModifiers) != requiredModifiers) {
                continue;
            }

            // Field must be of int type
            if (field.getType() != int.class) {
                continue;
            }

            // Field must have the same value as the searched code.
            try {
                int value = field.getInt(null);
                if (value != code) {
                    continue;
                }
            } catch (IllegalAccessException e) {
                continue;
            }



            possibilities.add(field.getDeclaringClass().getCanonicalName() + "::" + field.getName());
        }

        return possibilities;
    }

    /**
     * Finds potential constants to help describe a status or error code in human-readable terms.
     * @param code The error or status code to decipher
     * @param clazzes An array of classes that contain constants which describe the status or error code.
     * @return An array of strings of constants that may be related to the status or error code.
     */
    public static ArrayList<String> findPossibleConstantsForInt(int code, ArrayList<Class> clazzes) {
        ArrayList<String> possibilities = new ArrayList<String>();

        for (Class clazz : clazzes) {
            possibilities.addAll(findPossibleConstantsForInt(code, clazz));
        }

        return possibilities;
    }
}
