package pisd.works;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Determines the differences between the fields of the 2 instances of the same class.
 * First check if instances are equal.
 * Check if all fields are the same by traversing though the inheritance hierarchy.
 * Compare the field values using the normal equality check.
 * <p>
 * {@author nkokhelox}
 */
public class InstanceDiff<T> {
    private final T firstInstance;
    private final T secondInstance;
    private final int hierarchyClimbToLevel;

    private enum DiffStrategy {
        EXCLUDE_FIELDS, DIFF_OF_FIELDS, ALL_FIELDS
    }

    private final class DiffDecision {
        Set<String> fieldNameSet;
        DiffStrategy strategy;

        DiffDecision(DiffStrategy strategy) {
            this(strategy, null);
        }

        DiffDecision(DiffStrategy strategy, Set<String> fieldNameSet) {
            this.fieldNameSet = fieldNameSet;
            this.strategy = strategy;
        }

        Stream<Field> filteredStream(Stream<Field> declaredFields) {
            switch (strategy) {
                case EXCLUDE_FIELDS:
                    return declaredFields.filter(x -> !fieldNameSet.contains(x.getName()));
                case DIFF_OF_FIELDS:
                    return declaredFields.filter(x -> fieldNameSet.contains(x.getName()));
                default:
                    return declaredFields;
            }
        }

        boolean considerHierarchyLevel(Class klass, int currentLevel) {
            return !(Object.class.equals(klass)) && (currentLevel <= hierarchyClimbToLevel || hierarchyClimbToLevel < 0);
        }

        boolean areEqual(Object firstInstanceValue, Object secondInstanceValue) {
            return (firstInstanceValue == secondInstanceValue) || ((firstInstanceValue != null && secondInstanceValue != null) && firstInstanceValue.equals(secondInstanceValue));
        }

    }

    public static final class FieldDiff<T> implements Comparable {
        private final String fieldName;
        private final T inst1FieldValue;
        private final T inst2FieldValue;

        public FieldDiff(String fieldName, T inst1FieldValue, T inst2FieldValue) {
            this.fieldName = fieldName;
            this.inst1FieldValue = inst1FieldValue;
            this.inst2FieldValue = inst2FieldValue;
        }

        public String getFieldName() {
            return fieldName;
        }

        public T getFirstInstanceValue() {
            return inst1FieldValue;
        }

        public T getSecondInstanceValue() {
            return inst2FieldValue;
        }

        @Override
        public int hashCode() {
            return super.hashCode() + (fieldName == null ? 0 : fieldName.hashCode());
        }

        public int compareTo(Object obj) {
            FieldDiff that = (FieldDiff) obj;
            return this.fieldName.compareTo(that.fieldName);
        }
    }

    /**
     * Compare all fields for all classes in the hierarchy
     *
     * @param firstInstance  first instance
     * @param secondInstance second instance
     * @return the {@code non-null} {@link Set}&lt;{@link FieldDiff}&gt; for the fields with different values.
     * @since Origin
     */
    public InstanceDiff(T firstInstance, T secondInstance) {
        this(firstInstance, secondInstance, -1);
    }

    /**
     * Compare all fields for upto super class number(hierarchyClimbToLevel) in the hierarchy ladder
     *
     * @param firstInstance         first instance
     * @param secondInstance        second instance
     * @param hierarchyClimbToLevel {@code < 0} go through every super class,
     *                              {@code = 0} don't include super classes fields,
     *                              {@code >= 1} include fields from n super classes. e.g.
     *                              <i>if {@code hierarchyClimbToLevel = 5} then include fields of up to 5 super classes.</i>
     * @since Origin
     */
    public InstanceDiff(T firstInstance, T secondInstance, int hierarchyClimbToLevel) {
        this.firstInstance = firstInstance;
        this.secondInstance = secondInstance;
        this.hierarchyClimbToLevel = hierarchyClimbToLevel;
    }

    /**
     * @param ignoreFields a set of field names to be excluded/ignored when doing instance diff.
     * @return the {@code non-null} {@link Set}&lt;{@link FieldDiff}&gt; for the fields with different values.
     * @since Origin
     */
    public Set<FieldDiff> getDiffExcluding(Set<String> ignoreFields) throws Exception {
        return getDiff(new DiffDecision(DiffStrategy.EXCLUDE_FIELDS, ignoreFields));
    }

    /**
     * @param fieldsToDiff a set of field names to be interrogated for differences from the provided instances.
     * @return the {@code non-null} {@link Set}&lt;{@link FieldDiff}&gt; for the fields with different values.
     * @since Origin
     */
    public Set<FieldDiff> getDiffOf(Set<String> fieldsToDiff) throws Exception {
        return getDiff(new DiffDecision(DiffStrategy.DIFF_OF_FIELDS, fieldsToDiff));
    }

    /**
     * @return the {@code non-null} {@link Set}&lt;{@link FieldDiff}&gt; for the fields with different values.
     * @since Origin
     */
    public Set<FieldDiff> getDiff() throws Exception {
        return getDiff(new DiffDecision(DiffStrategy.ALL_FIELDS));
    }


    private Set<FieldDiff> getDiff(DiffDecision decider) throws Exception {
        Set<FieldDiff> differenceSet = new HashSet();

        if (firstInstance == secondInstance) { //same reference = same thing thus everything must be the same.
            return differenceSet;
        } else if (firstInstance != null && secondInstance != null && !firstInstance.equals(secondInstance)) {
            int hierarchyLevel = 0;
            Class klass = firstInstance.getClass();
            if (klass == secondInstance.getClass()) {
                while (decider.considerHierarchyLevel(klass, hierarchyLevel)) {
                    for (Field field : decider.filteredStream(Arrays.stream(klass.getDeclaredFields())).collect(Collectors.toSet())) {
                        field.setAccessible(true);
                        Object firstInstanceValue = field.get(firstInstance);
                        Object secondInstanceValue = field.get(secondInstance);
                        if (!decider.areEqual(firstInstanceValue, secondInstanceValue)) {
                            differenceSet.add(new FieldDiff(field.getName(), firstInstanceValue, secondInstanceValue));
                        }
                    }
                    klass = klass.getSuperclass();
                    hierarchyLevel++;
                }
            } else {
                differenceSet.add(new FieldDiff(ConstantKeys.DIFF_CLASS_TYPES, firstInstance.getClass().getName(), secondInstance.getClass().getName()));
            }
        }
        return differenceSet;
    }

}
