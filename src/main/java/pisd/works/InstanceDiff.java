package pisd.works;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * Determines the differences between the fields of the 2 instances of the same class.
 * First check if instances are equal.
 * Check if all fields are the same by traversing though the inheritance hierarchy.
 * Compare the field values using the normal equality check.
 * <p>
 * {@author nkokhelox <Nkokhelo E. Mhlongo>}
 */
public class InstanceDiff {
    private final Object instance2;
    private final Object instance1;
    private final int climbLevel;

    private enum DiffStrategy {
        EXCLUDE_FIELDS, DIFF_OF_FIELDS, ALL_FIELDS
    }

    private class DiffDecision {
        Set<String> fieldNameSet;
        DiffStrategy fieldSetUsage;

        DiffDecision(DiffStrategy fieldSetUsage) {
            this(fieldSetUsage, null);
        }

        DiffDecision(DiffStrategy fieldSetUsage, Set<String> fieldNameSet) {
            this.fieldNameSet = fieldNameSet;
            this.fieldSetUsage = fieldSetUsage;
        }

        boolean continueFieldDiffFor(String fieldName) {
            boolean fieldNotExcluded =
                    fieldSetUsage == DiffStrategy.EXCLUDE_FIELDS &&
                            !fieldNameSet.contains(fieldName);

            boolean specificFieldDiff =
                    fieldSetUsage == DiffStrategy.DIFF_OF_FIELDS &&
                            !fieldNameSet.isEmpty() &&
                            fieldNameSet.contains(fieldName) &&
                            fieldNameSet.remove(fieldName);

            return fieldNameSet == null || fieldSetUsage == DiffStrategy.ALL_FIELDS || fieldNotExcluded || specificFieldDiff;
        }

        boolean continueFieldDiffFor(Class klass, int currentLevel) {
            return !(Object.class.equals(klass)) && (currentLevel <= climbLevel || climbLevel < 0);
        }

        boolean areValueDiff(Object value1, Object value2) {
            if (value1 == value2) {
                return false;
            }
            return (value1 == null && value2 != null)
                    || (value1 != null && value2 == null)
                    || (!value1.equals(value2));
        }
    }

    public static class FieldDiff implements Comparable {
        private final String fieldName;
        private final Object inst1FieldValue;
        private final Object inst2FieldValue;

        public FieldDiff(String fieldName, Object inst1FieldValue, Object inst2FieldValue) {
            this.fieldName = fieldName;
            this.inst1FieldValue = inst1FieldValue;
            this.inst2FieldValue = inst2FieldValue;
        }

        public String getFieldName() {
            return fieldName;
        }

        public Object getInst1FieldValue() {
            return inst1FieldValue;
        }

        public Object getInst2FieldValue() {
            return inst2FieldValue;
        }

        @Override
        public int hashCode() {
            return super.hashCode() + (fieldName != null ? fieldName.hashCode() : 0);
        }

        public int compareTo(Object obj) {
            FieldDiff that = (FieldDiff) obj;
            return this.fieldName.compareTo(that.fieldName);
        }
    }

    /**
     * All fields all classes
     *
     * @param instance1 first instance
     * @param instance2 second instance
     * @return the {@code non-null} {@link Set}&lt;{@link FieldDiff}&gt; for the fields with different values.
     * @since Origin
     */
    public InstanceDiff(Object instance1, Object instance2) {
        this(instance1, instance2, -1);
    }

    /**
     * @param instance1  first instance
     * @param instance2  second instance
     * @param climbLevel {@code < 0} go through every super class,
     *                   {@code = 0} don't include super classes fields,
     *                   {@code >= 1} include fields from n super classes. e.g.
     *                   <i>if {@code climbLevel = 5} then include field of up to 5 super classes.</i>
     * @since Origin
     */
    public InstanceDiff(Object instance1, Object instance2, int climbLevel) {
        this.instance1 = instance1;
        this.instance2 = instance2;
        this.climbLevel = climbLevel;
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
        Set diff = new HashSet();
        if(instance1 == instance2){//same reference = same thing
            return diff;
        }
        else if (instance1 != null && instance2 != null && !instance1.equals(instance2)) {
            int hierachyLevel = 0;
            Class klass = instance1.getClass();
            if (klass == instance2.getClass()) {
                while (decider.continueFieldDiffFor(klass, hierachyLevel)) {
                    for (Field field : klass.getDeclaredFields()) {
                        field.setAccessible(true);
                        String fieldName = field.getName();
                        if (decider.continueFieldDiffFor(fieldName)) {
                            Object inst1Fv = field.get(instance1);
                            Object inst2Fv = field.get(instance2);
                            if (decider.areValueDiff(inst1Fv, inst2Fv)) {
                                diff.add(new FieldDiff(fieldName, inst1Fv, inst2Fv));
                            }
                        }
                    }
                    klass = klass.getSuperclass();
                    hierachyLevel++;
                }
            } else {
                diff.add(new FieldDiff(ConstantKeys.DIFF_CLASS_TYPES, instance1.getClass().getName(), instance2.getClass().getName()));
            }
        }
        return diff;
    }
}
