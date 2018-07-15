package pisd.works;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by nkokhelox on 2017/04/29.
 */
public class InstanceDiffTest {
    @Test
    public void getDiffExcluding() throws Exception {
        GrandChild instance1 = new GrandChild(null);
        GrandChild instance2 = new GrandChild(null);
        instance1.childPackageField = 1;
        instance2.childPackageField = 2;

        HashSet<String> excludeFields = new HashSet<>();
        excludeFields.add("childPackageField");

        InstanceDiff<GrandChild> diff = new InstanceDiff<>(instance1, instance2);

        assertFalse("Instances are different when un-filtered", diff.getDiff().isEmpty());

        assertTrue(
                "No differences detected when childPackageField is explicitly ignored/excluded.",
                diff.getDiffExcluding(excludeFields).isEmpty());
    }

    @Test
    public void getDiffOfEqualInstances() throws Exception {
        GrandChild instance1 = new GrandChild(null);
        GrandChild instance2 = new GrandChild(null);

        HashSet<String> diffOfFields = new HashSet<>();
        diffOfFields.add("grandChildPrivateField");
        diffOfFields.add("childPackageField");
        diffOfFields.add("parentPackageField");

        assertTrue(
                "No differences from the specified fields",
                new InstanceDiff<>(instance1, instance2).getDiffOf(diffOfFields).isEmpty());
    }

    @Test
    public void getDiffOfNonExistingFields() throws Exception {
        GrandChild instance1 = new GrandChild(null);
        GrandChild instance2 = new GrandChild(null);

        HashSet<String> diffOfFields = new HashSet<>();
        diffOfFields.add("does");
        diffOfFields.add("not");
        diffOfFields.add("exist");

        assertTrue(
                "No differences, specified fields does not exist",
                new InstanceDiff<>(instance1, instance2).getDiffOf(diffOfFields).isEmpty());
    }

    @Test
    public void getDiffOfFoundDiffFields() throws Exception {

        GrandChild instance1 = new GrandChild("instance1");
        instance1.parentPackageField = 1;
        instance1.grandChildPrivateField = 1;
        instance1.childPackageField = 1;

        HashSet<String> diffOfFields = new HashSet<>();
        diffOfFields.add("grandChildPrivateField");
        diffOfFields.add("childPackageField");
        diffOfFields.add("parentPackageField");

        Object[] diff = new InstanceDiff<>(instance1, new GrandChild("instance2")).getDiffOf(diffOfFields).toArray();
        Arrays.sort(diff);
        assertEquals("Instances are different by the value of 1 super class field.", 3, diff.length);
        assertEquals("Field name", "childPackageField", ((InstanceDiff.FieldDiff) diff[0]).getFieldName());
        assertEquals("Field name", "grandChildPrivateField", ((InstanceDiff.FieldDiff) diff[1]).getFieldName());
        assertEquals("Field name", "parentPackageField", ((InstanceDiff.FieldDiff) diff[2]).getFieldName());
    }

    @Test
    public void getDiffEqualInstances() throws Exception {
        assertTrue(
                "Instances are equal, thus diff should be empty.",
                new InstanceDiff<>(new GrandChild(null), new GrandChild(null)).getDiff().isEmpty()
        );
    }

    @Test
    public void getDiffSuperFields() throws Exception {
        GrandChild instance1 = new GrandChild(null);
        GrandChild instance2 = new GrandChild(null);
        instance1.parentPackageField = 1;
        instance2.parentPackageField = 2;

        Object[] diff = new InstanceDiff<>(instance1, instance2).getDiff().toArray();
        assertEquals("Instances are different by the value of 1 super class field.", 1, diff.length);

        InstanceDiff.FieldDiff difference = ((InstanceDiff.FieldDiff) diff[0]);
        assertEquals("Field name", "parentPackageField", difference.getFieldName());
        assertEquals("Instance1 Field value", 1, difference.getFirstInstanceValue());
        assertEquals("Instance2 Field value", 2, difference.getSecondInstanceValue());
    }

    @Test
    public void getDiffStopBeforeDiff() throws Exception {
        GrandChild instance1 = new GrandChild(null);
        GrandChild instance2 = new GrandChild(null);
        instance1.parentPackageField = 1;
        instance2.parentPackageField = 2;
        int detectUptoParentLevel = 1;
        Set<InstanceDiff.FieldDiff<Object>> diff = new InstanceDiff<>(instance1, instance2, detectUptoParentLevel).getDiff();

        assertTrue(
                "IStop diff detection before getting to the super class where there's difference.",
                diff.isEmpty());
    }

    @Test
    public void getDiffOfSameObject() throws Exception {
        GrandChild instance1 = new GrandChild(null);
        Set<InstanceDiff.FieldDiff<Object>> diff = new InstanceDiff<>(instance1, instance1).getDiff();

        assertTrue(
                "Reference to the same object as instance 1 & 2",
                diff.isEmpty());
    }

    @Test
    public void getDiffInstancesOfDifferentClasses() throws Exception {
        Set<InstanceDiff.FieldDiff<Object>> diff = new InstanceDiff<>(new String[1], "String").getDiff();

        assertEquals(1, diff.size());
        assertTrue("Instances of different classes are reporting the different class's names",
                diff.removeIf(o -> ConstantKeys.DIFF_CLASS_TYPES.equals(o.getFieldName()))
        );
    }

    class Parent {
        int parentPackageField;
        private Object parentPrivateField;

        Parent(Object parentPrivateField) {
            this.parentPrivateField = parentPrivateField;
        }
    }

    class Child extends Parent {
        Object childPackageField;

        Child(Object parentPrivateField) {
            super(parentPrivateField);
        }
    }

    class GrandChild extends Child {
        private Object grandChildPrivateField;

        GrandChild(Object parentPrivateField) {
            super(parentPrivateField);
            grandChildPrivateField = parentPrivateField;
        }
    }

}
