package src.test.java;

import org.junit.jupiter.api.Test;

import src.main.java.Person;

import static org.junit.jupiter.api.Assertions;

public class UnitTest {
    Person p1 = new Person();

    void testUnit() {
        assertEquals(true,
                p1.addPerson("56s_ad&fAB", "John", "Doe", "32|Highland Street|Melbourne|Victoria|Australia",
                        "27-02-2005"));
    }

}
