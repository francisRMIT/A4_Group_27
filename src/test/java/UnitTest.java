// package main.java;

import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.*;

class UnitTest {

    private static final String DETAILS_FILE = "Details.txt";

    @BeforeEach
    void clearDetailsFile() throws IOException {
        // Ensure a fresh Details.txt before each test
        File f = new File(DETAILS_FILE);
        if (f.exists()) {
            f.delete();
        }
    }

    // -------------------------------------------------------
    // 5 TESTS FOR addPerson(...)
    // -------------------------------------------------------

    @Test
    @DisplayName("TC 1.1.1-A: addPerson with fully valid ID/address/birthdate → returns true")
    void testAddPerson_AllValid_ReturnsTrue() throws IOException {
        Person p = new Person();

        // Attempt to add a person with all valid inputs
        boolean result = p.addPerson(
                "25%_d&ABXY",
                "John",
                "Doe",
                "32|Highland Street|Melbourne|Victoria|Australia",
                "15-11-1990");
        assertTrue(result, "Expected addPerson() to return true for valid input");

        // Verify Details.txt was created and contains the correct fields
        assertTrue(Files.exists(Paths.get(DETAILS_FILE)));
        String contents = new String(Files.readAllBytes(Paths.get(DETAILS_FILE)));
        assertTrue(contents.contains("ID: 25%_d&ABXY"));
        assertTrue(contents.contains("First Name: John"));
        assertTrue(contents.contains("Last Name: Doe"));
        assertTrue(contents.contains("Address: 32|Highland Street|Melbourne|Victoria|Australia"));
        assertTrue(contents.contains("Birthdate: 15-11-1990"));
    }

    @Test
    @DisplayName("TC 1.1.1-B: addPerson with invalid ID (length=9) → returns false")
    void testAddPerson_InvalidIDLength_ReturnsFalse() throws IOException {
        Person p = new Person();

        // ID length is 9 instead of 10
        boolean result = p.addPerson(
                "29!_%ABCD",
                "Jane",
                "Smith",
                "32|Highland Street|Melbourne|Victoria|Australia",
                "15-11-1990");
        assertFalse(result, "Expected addPerson() to return false when ID length != 10");
        assertFalse(Files.exists(Paths.get(DETAILS_FILE)), "Details.txt should not be created on failure");
    }

    @Test
    @DisplayName("TC 1.1.1-C: addPerson with valid ID + invalid address (wrong State) → returns false")
    void testAddPerson_InvalidAddress_ReturnsFalse() throws IOException {
        Person p = new Person();

        // State is "NewSouthWales" instead of "Victoria"
        boolean result = p.addPerson(
                "38_@#ABCDZ",
                "Alice",
                "Brown",
                "32|Highland Street|Melbourne|NewSouthWales|Australia",
                "15-11-1990");
        assertFalse(result, "Expected addPerson() to return false when state != Victoria");
        assertFalse(Files.exists(Paths.get(DETAILS_FILE)));
    }

    @Test
    @DisplayName("TC 1.1.1-D: addPerson with valid ID/address + invalid birthdate format → returns false")
    void testAddPerson_InvalidBirthdateFormat_ReturnsFalse() throws IOException {
        Person p = new Person();

        // Birthdate uses "YYYY-MM-DD" instead of "DD-MM-YYYY"
        boolean result = p.addPerson(
                "47-%&?ABCD",
                "Bob",
                "Miller",
                "12|Queen Street|Geelong|Victoria|Australia",
                "1990-11-15");
        assertFalse(result, "Expected addPerson() to return false for wrong date format");
        assertFalse(Files.exists(Paths.get(DETAILS_FILE)));
    }

    @Test
    @DisplayName("TC 1.1.1-E: addPerson with invalid ID (last two chars not uppercase letters) → returns false")
    void testAddPerson_InvalidID_LastTwoChars_ReturnsFalse() throws IOException {
        Person p = new Person();

        // Last two characters 'c' and '9' are not uppercase letters A–Z
        boolean result = p.addPerson(
                "29%_d&Abc9",
                "Carl",
                "Jones",
                "29|Bourke Street|Melbourne|Victoria|Australia",
                "01-01-2000");
        assertFalse(result, "Expected addPerson() to return false when last two chars are invalid");
        assertFalse(Files.exists(Paths.get(DETAILS_FILE)));
    }

    // -------------------------------------------------------
    // 5 TESTS FOR updatePersonalDetails(...)
    // -------------------------------------------------------

    @Test
    @DisplayName("TC 1.1.1-F: updatePersonalDetails denies address change if under 18 → returns false")
    void testUpdatePersonalDetails_Under18ChangeAddress_ReturnsFalse() throws IOException {
        Person p = new Person();
        // Add a 16-year-old person
        boolean added = p.addPerson(
                "34*#d&ABCD",
                "Alice",
                "Smith",
                "10|King Street|Bendigo|Victoria|Australia",
                "01-01-2008");
        assertTrue(added, "Precondition: Should be able to add the 16-year-old person");

        // Attempt to change address for someone under 18
        boolean updated = p.updatePersonalDetails(
                "34*#d&ABCD",
                "Alice",
                "Smith",
                "20|Queen Street|Ballarat|Victoria|Australia",
                "01-01-2008");
        assertFalse(updated, "Expected updatePersonalDetails() to return false for under-18 address change");

        // Verify file still shows the old address
        String contents = new String(Files.readAllBytes(Paths.get(DETAILS_FILE)));
        assertTrue(contents.contains("Address: 10|King Street|Bendigo|Victoria|Australia"));
    }

    @Test
    @DisplayName("TC 1.1.1-G: updatePersonalDetails changing birthdate + another field → returns false")
    void testUpdatePersonalDetails_ChangeBirthdateAndLastName_ReturnsFalse() throws IOException {
        Person p = new Person();
        // Add an adult person
        boolean added = p.addPerson(
                "25_%$ABXYZ",
                "Bob",
                "Jones",
                "50|Market Street|Geelong|Victoria|Australia",
                "15-05-1990");
        assertTrue(added, "Precondition: Should be able to add Bob (age 34)");

        // Attempt to change both birthdate and lastName in the same call
        boolean updated = p.updatePersonalDetails(
                "25_%$ABXYZ",
                "Bob",
                "Johnson",
                "50|Market Street|Geelong|Victoria|Australia",
                "16-05-1990");
        assertFalse(updated, "Expected updatePersonalDetails() to return false for changing birthdate + another field");

        // Verify file still shows old birthdate and lastName
        String contents = new String(Files.readAllBytes(Paths.get(DETAILS_FILE)));
        assertTrue(contents.contains("Last Name: Jones"));
        assertTrue(contents.contains("Birthdate: 15-05-1990"));
    }

    @Test
    @DisplayName("TC 1.1.1-H: updatePersonalDetails changing ID when old ID starts with even digit → returns false")
    void testUpdatePersonalDetails_ChangeID_ForbiddenEvenDigit_ReturnsFalse() throws IOException {
        Person p = new Person();
        // Add a person whose ID starts with '2' (even)
        boolean added = p.addPerson(
                "24%_!ABCDY",
                "Carol",
                "Nguyen",
                "15|Oxford Street|Melbourne|Victoria|Australia",
                "20-02-1980");
        assertTrue(added, "Precondition: Should be able to add Carol");

        // Attempt to change ID (forbidden because first digit=2) + change firstName
        boolean updated = p.updatePersonalDetails(
                "35#%ABXYZY",
                "Caroline",
                "Nguyen",
                "15|Oxford Street|Melbourne|Victoria|Australia",
                "20-02-1980");
        assertFalse(updated, "Expected updatePersonalDetails() to return false when changing forbidden ID");

        // Verify file still shows the old ID and firstName
        String contents = new String(Files.readAllBytes(Paths.get(DETAILS_FILE)));
        assertTrue(contents.contains("ID: 24%_!ABCDY"));
        assertTrue(contents.contains("First Name: Carol"));
    }

    @Test
    @DisplayName("TC 1.1.1-I: updatePersonalDetails valid update of lastName + address for adult with odd-digit ID → returns true")
    void testUpdatePersonalDetails_ValidUpdateLastNameAddress_ReturnsTrue() throws IOException {
        Person p = new Person();
        // Add an adult with ID starting '3' (odd)
        boolean added = p.addPerson(
                "35%_&ABXYZ",
                "David",
                "Lee",
                "5|Church Street|Ballarat|Victoria|Australia",
                "05-05-1995");
        assertTrue(added, "Precondition: Should be able to add David");

        // Change lastName and address (both allowed)
        boolean updated = p.updatePersonalDetails(
                "35%_&ABXYZ",
                "David",
                "Leighton",
                "55|High Street|Swan Hill|Victoria|Australia",
                "05-05-1995");
        assertTrue(updated, "Expected updatePersonalDetails() to return true for valid changes");

        // Verify file now reflects new lastName and address
        String contents = new String(Files.readAllBytes(Paths.get(DETAILS_FILE)));
        assertTrue(contents.contains("Last Name: Leighton"));
        assertTrue(contents.contains("Address: 55|High Street|Swan Hill|Victoria|Australia"));
    }

    @Test
    @DisplayName("TC 1.1.1-J: updatePersonalDetails invalid new birthdate format → returns false")
    void testUpdatePersonalDetails_InvalidNewBirthdateFormat_ReturnsFalse() throws IOException {
        Person p = new Person();
        // Add a person with valid data
        boolean added = p.addPerson(
                "39%#&ABWXY",
                "Eve",
                "Clark",
                "1|Station Road|Warrnambool|Victoria|Australia",
                "10-10-2000");
        assertTrue(added, "Precondition: Should be able to add Eve");

        // Attempt to change birthdate using "YYYY-MM-DD" format
        boolean updated = p.updatePersonalDetails(
                "39%#&ABWXY",
                "Eve",
                "Clark",
                "1|Station Road|Warrnambool|Victoria|Australia",
                "2000-10-10");
        assertFalse(updated, "Expected updatePersonalDetails() to return false for wrong birthdate format");

        // Verify file still shows old birthdate
        String contents = new String(Files.readAllBytes(Paths.get(DETAILS_FILE)));
        assertTrue(contents.contains("Birthdate: 10-10-2000"));
    }

    // -------------------------------------------------------
    // 5 TESTS FOR addDemeritPoints(...)
    // -------------------------------------------------------

    @Test
    @DisplayName("TC 1.1.1-K: addDemeritPoints under-21 with first offense 4 points → 'Success', no suspension")
    void testAddDemeritPoints_Under21_NoSuspension_ReturnsSuccess() throws IOException {
        Person p = new Person();
        // Add an under-21 person (age 19)
        boolean added = p.addPerson(
                "21%_#ABXYZ",
                "Frank",
                "Morris",
                "10|Main Street|Geelong|Victoria|Australia",
                "01-01-2005");
        assertTrue(added, "Precondition: Should be able to add Frank");

        // First offense of 4 points → should succeed and not suspend
        String result = p.addDemeritPoints("15-06-2023", 4);
        assertEquals("Success", result);
        assertFalse(p.isSuspended, "Under-21 with only 4 points should not be suspended");
    }

    @Test
    @DisplayName("TC 1.1.1-L: addDemeritPoints with points=7 (out of range) → 'Failure'")
    void testAddDemeritPoints_InvalidPointsOutOfRange_ReturnsFailure() throws IOException {
        Person p = new Person();
        // Add an over-21 person (age 24)
        boolean added = p.addPerson(
                "22%_#ABXYZ",
                "Grace",
                "Hopper",
                "15|Victoria Street|Ballarat|Victoria|Australia",
                "01-01-2000");
        assertTrue(added, "Precondition: Should be able to add Grace");

        // Attempt to add 7 points (invalid) → should fail
        String result = p.addDemeritPoints("01-01-2024", 7);
        assertEquals("Failure", result, "Expected Failure when points > 6");
        assertFalse(p.isSuspended, "No suspension if the method fails");
    }

    @Test
    @DisplayName("TC 1.1.1-M: addDemeritPoints invalid offense date format → 'Failure'")
    void testAddDemeritPoints_InvalidDateFormat_ReturnsFailure() throws IOException {
        Person p = new Person();
        // Add an over-21 person (age 31)
        boolean added = p.addPerson(
                "23%_#ABXYZ",
                "Hank",
                "Pym",
                "5|King Street|Ballarat|Victoria|Australia",
                "01-01-1993");
        assertTrue(added, "Precondition: Should be able to add Hank");

        // Invalid date format (YYYY/MM/DD) → should fail
        String result = p.addDemeritPoints("2024/01/01", 5);
        assertEquals("Failure", result);
        assertFalse(p.isSuspended, "No suspension if the method fails");
    }

    @Test
    @DisplayName("TC 1.1.1-N: under-21 existing 5 points + adding 2 points → 'Success', isSuspended=true")
    void testAddDemeritPoints_Under21_ExceedsThresholdWithinTwoYears() throws IOException {
        Person p = new Person();
        // Add an under-21 person (age 18)
        boolean added = p.addPerson(
                "24%_#ABXYZ",
                "Isla",
                "Fisher",
                "20|Queen Street|Melbourne|Victoria|Australia",
                "01-01-2006");
        assertTrue(added, "Precondition: Should be able to add Isla");

        // Preload 5 points within 2 years
        String first = p.addDemeritPoints("01-07-2023", 5);
        assertEquals("Success", first);
        assertFalse(p.isSuspended, "Still not suspended at 5 points (< 6)");

        // Add 2 more points (total 7 > 6) → should suspend
        String result = p.addDemeritPoints("01-08-2023", 2);
        assertEquals("Success", result);
        assertTrue(p.isSuspended, "Should be suspended once total > 6 within 2 years");
    }

    @Test
    @DisplayName("TC 1.1.1-O: over-21 existing 10 points + adding 3 points → 'Success', isSuspended=true")
    void testAddDemeritPoints_Over21_ExceedsThresholdWithinTwoYears() throws IOException {
        Person p = new Person();
        // Add an over-21 person (age 29)
        boolean added = p.addPerson(
                "25%_#ABXYZ",
                "Jack",
                "Sparrow",
                "1|Pirate Way|Geelong|Victoria|Australia",
                "01-01-1995");
        assertTrue(added, "Precondition: Should be able to add Jack");

        // Preload two 5-point offenses (total 10)
        String first = p.addDemeritPoints("01-01-2023", 5);
        String second = p.addDemeritPoints("01-06-2023", 5);
        assertEquals("Success", first);
        assertEquals("Success", second);
        assertFalse(p.isSuspended, "10 points does not exceed 12 threshold for over-21");

        // Add 3 more points (total 13 > 12) → should suspend
        String result = p.addDemeritPoints("01-07-2023", 3);
        assertEquals("Success", result);
        assertTrue(p.isSuspended, "Should be suspended once total > 12 within 2 years");
    }

    // -------------------------------------------------------
    // OPTIONAL EXTRA EDGE CASES (merge if strictly needed only 5 for
    // addDemeritPoints)
    // -------------------------------------------------------

    @Test
    @DisplayName("TC 1.1.1-P: under-21 with 6 old points (>2 years ago) + adding 1 new point → 'Success', no suspension")
    void testAddDemeritPoints_Under21_OldPointsIgnored_NoSuspension() throws IOException {
        Person p = new Person();
        // Add an under-21 person (age 19)
        boolean added = p.addPerson(
                "26%_#ABXYZ",
                "Kelly",
                "Rowland",
                "2|Music Lane|Ballarat|Victoria|Australia",
                "01-01-2005");
        assertTrue(added, "Precondition: Should be able to add Kelly");

        // Preload 6 points from >2 years ago (should not count)
        String old = p.addDemeritPoints("01-01-2021", 6);
        assertEquals("Success", old);
        assertFalse(p.isSuspended, "Old offense outside 2-year window should not suspend");

        // Add 1 point now → total in window = 1, no suspension
        String result = p.addDemeritPoints("01-06-2024", 1);
        assertEquals("Success", result);
        assertFalse(p.isSuspended, "Should not be suspended because old points are ignored");
    }

    @Test
    @DisplayName("TC 1.1.1-Q: addDemeritPoints with offense date '32-13-2024' (invalid) → 'Failure'")
    void testAddDemeritPoints_InvalidDateValueFormat_ReturnsFailure() throws IOException {
        Person p = new Person();
        // Add an over-21 person (age 34)
        boolean added = p.addPerson(
                "27%_#ABXYZ",
                "Lily",
                "Allen",
                "3|Pop Road|Melbourne|Victoria|Australia",
                "01-01-1990");
        assertTrue(added, "Precondition: Should be able to add Lily");

        // Invalid offense date value → should fail
        String result = p.addDemeritPoints("32-13-2024", 3);
        assertEquals("Failure", result, "Expected Failure for invalid date values");
        assertFalse(p.isSuspended, "No suspension if the date is invalid");
    }
}