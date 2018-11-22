package ri.wse.support.flexjson;


import flexjson.JSONSerializer;
import ri.wse.model.Result;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Builder class that holds several static factory methods for creating JSONSerializers with different include & exclude
 * rules
 */
public class JSONSerializerBuilder {


    private static final List<String> GLOBAL_INCLUDES = new LinkedList<>();

    static {
        GLOBAL_INCLUDES.add("id");
        GLOBAL_INCLUDES.add("entityCreationTimestamp");
        GLOBAL_INCLUDES.add("lastUpdatedTimestamp");
        GLOBAL_INCLUDES.add("entityVersion");
    }

    /**
     * Returns a list of paths to be excluded when creating a JSON serializer.
     * This list should be used for all JSON serializers created for the
     * application.
     *
     * @return a list of exclude paths to be used when creating a JSON
     * serializer for the app.
     */
    private static List<String> getGlobalExcludes() {
        List<String> excludes = new LinkedList<>();
        excludes.add("*.class");
        excludes.add("*.handler");
        excludes.add("*.hibernateLazyInitializer");
        return excludes;
    }

    /**
     * Gets a list of exclude expressions.
     *
     * @param clazz         the class to be inspected to exclude all fields but the ones sent in the include list.
     * @param attributeName the attribute name to be used as prefix in the exclude expressions.
     * @param include       the list of fields to include.
     */
    @SuppressWarnings("rawtypes")
    private static List<String> getExcludesForObject(Class clazz, String attributeName, List<String> include) {
        String prefix = "";

        if (attributeName != null && !attributeName.equals("")) {
            prefix = attributeName + ".";
        }
        List<String> excludes = new LinkedList<>();

        List<Field> fields = getInheritedPrivateFields(clazz);
        for (Field field : fields) {
            String fieldName = field.getName();

            if (!GLOBAL_INCLUDES.contains(fieldName) && (include == null || !include.contains(fieldName))) {
                excludes.add(prefix + fieldName);
            }
        }

        return excludes;
    }

    /**
     * Returns a list of the field methods for a given class, including the
     * inherited fields.
     *
     * @param type class type that will be introspected
     * @return a list of all the fields in the class hierarchy.
     */
    private static List<Field> getInheritedPrivateFields(Class<?> type) {
        List<Field> result = new ArrayList<>();

        Class<?> i = type;
        while (i != null && i != Object.class) {
            result.addAll(Arrays.asList(i.getDeclaredFields()));
            i = i.getSuperclass();
        }

        return result;
    }

    /**
     * Basic factory method that returns a JSONSerializer that realizes a shallow
     * serialization of objects excluding the objects' class names.
     *
     * @return an instance of JSONSerializer that does shallow serialization excluding classnames
     */
    public static JSONSerializer getBasicSerializer() {
        JSONSerializer serializer = new JSONSerializer();

        serializer.setExcludes(getGlobalExcludes());

        return serializer;
    }

    public static JSONSerializer getChildrenSerializer(){
        JSONSerializer serializer = getBasicSerializer();
        List<String> excludes = new LinkedList<>();
        List<String> tempIncludes;

        excludes.addAll(getGlobalExcludes());

        tempIncludes = new LinkedList<>();
        tempIncludes.add("relevantData");
        tempIncludes.add("symptomsText");

        excludes.addAll(JSONSerializerBuilder.getExcludesForObject(Result.class, "manager", tempIncludes));

        serializer.setExcludes(excludes);

        return serializer;

    }

}