package pisd.works;

import static org.junit.Assert.*;

import com.sun.org.apache.xpath.internal.operations.String;
import org.junit.*;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by nkokhelox on 2017/04/29.
 */
public class InstanceDiffTest {
    @Test
    public void getDiffExcluding() throws Exception {
        GrandChild instance1 = new GrandChild(null);
        GrandChild instance2 = new GrandChild(null);
        instance1.pcf = 1;
        instance2.pcf = 2;

        HashSet excludeFields = new HashSet<String>();
        excludeFields.add("pcf");

        InstanceDiff diff = new InstanceDiff(instance1, instance2);

        assertFalse("Instances are different when un-filtered", diff.getDiff().isEmpty());

        assertTrue(
                "No differences detected when pcf is explicitly ignored/excluded.",
                diff.getDiffExcluding(excludeFields).isEmpty());
    }

    @Test
    public void getDiffOfEqualInstances() throws Exception {
        GrandChild instance1 = new GrandChild(null);
        GrandChild instance2 = new GrandChild(null);

        HashSet diffOfFields = new HashSet<String>();
        diffOfFields.add("gcpf");
        diffOfFields.add("pcf");
        diffOfFields.add("superField");

        assertTrue(
                "No differences from the specified fields",
                new InstanceDiff(instance1, instance2).getDiffOf(diffOfFields).isEmpty());
    }

    @Test
    public void getDiffOfNonExistingFields() throws Exception {
        GrandChild instance1 = new GrandChild(null);
        GrandChild instance2 = new GrandChild(null);

        HashSet diffOfFields = new HashSet<String>();
        diffOfFields.add("does");
        diffOfFields.add("not");
        diffOfFields.add("exist");

        assertTrue(
                "No differences, specified fields does not exist",
                new InstanceDiff(instance1, instance2).getDiffOf(diffOfFields).isEmpty());
    }

    @Test
    public void getDiffOfFoundDiffFields() throws Exception {

        GrandChild instance1 = new GrandChild("inst1");
        instance1.superField = 1;
        instance1.gcpf = 1;
        instance1.pcf = 1;

        HashSet diffOfFields = new HashSet<String>();
        diffOfFields.add("gcpf");
        diffOfFields.add("pcf");
        diffOfFields.add("superField");

        Object[] diff = new InstanceDiff(instance1, new GrandChild(null)).getDiffOf(diffOfFields).toArray();
        Arrays.sort(diff);
        assertEquals("Instances are different by the value of 1 super class field.", 3, diff.length);
        assertEquals("Field name", "gcpf", ((InstanceDiff.FieldDiff) diff[0]).getFieldName());
        assertEquals("Field name", "pcf", ((InstanceDiff.FieldDiff) diff[1]).getFieldName());
        assertEquals("Field name", "superField", ((InstanceDiff.FieldDiff) diff[2]).getFieldName());
    }

    @Test
    public void getDiffEqualInstances() throws Exception {
        assertTrue("Instances are equal, thus diff should be empty.", new InstanceDiff(new GrandChild(null), new GrandChild(null)).getDiff().isEmpty());
    }

    @Test
    public void getDiffSuperFields() throws Exception {
        GrandChild instance1 = new GrandChild(null);
        GrandChild instance2 = new GrandChild(null);
        instance1.superField = 1;
        instance2.superField = 2;

        Object[] diff = new InstanceDiff(instance1, instance2).getDiff().toArray();
        assertEquals("Instances are different by the value of 1 super class field.", 1, diff.length);

        InstanceDiff.FieldDiff difference = ((InstanceDiff.FieldDiff) diff[0]);
        assertEquals("Field name", "superField", difference.getFieldName());
        assertEquals("Instance1 Field value", 1, difference.getInst1FieldValue());
        assertEquals("Instance2 Field value", 2, difference.getInst2FieldValue());
    }

    @Test
    public void getDiffStopBeforeDiff() throws Exception {
        GrandChild instance1 = new GrandChild(null);
        GrandChild instance2 = new GrandChild(null);
        instance1.superField = 1;
        instance2.superField = 2;
        int detectUptoParentLevel = 1;
        Set<InstanceDiff.FieldDiff> diff = new InstanceDiff(instance1, instance2, detectUptoParentLevel).getDiff();

        assertTrue(
                "IStop diff detection before getting to the super class where there's difference.",
                diff.isEmpty());
    }

    @Test
    public void getDiffInstancesOfDifferentClasses() throws Exception {
        Set<InstanceDiff.FieldDiff> diff = new InstanceDiff(new String[1], "String").getDiff();

        assertEquals(1, diff.size());
        assertTrue("Instances of different classes are reporting the different class's names",
                diff.removeIf( o -> ConstantKeys.DIFF_CLASS_TYPES.equals(o.getFieldName()))
                );
    }

    class Parent {
        int superField;
        private Object ppf;

        public Parent(Object ppf) {
            this.ppf = ppf;
        }
    }

    class Child extends Parent {
        Object pcf;

        public Child(Object ppf) {
            super(ppf);
        }
    }

    class GrandChild extends Child {
        private Object gcpf;

        public GrandChild(Object ppf) {
            super(ppf);
            gcpf = ppf;
        }
    }

}
