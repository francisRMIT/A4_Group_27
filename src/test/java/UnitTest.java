package Main.java;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import Main.java.Person;

public class UnitTest {
    Person p1 = new Person();

    @Test
    void testUnit() {
        assertEquals(true,
                p1.addPerson("56s_ad&fAB", "John", "Doe", "32|Highland Street|Melbourne|Victoria|Australia",
                        "27-02-2005"));
    }

}
