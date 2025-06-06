import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;

/**
 * Person.java
 *
 * Implements:
 * - addPerson(...)
 * - updatePersonalDetails(...)
 * - addDemeritPoints(...)
 *
 * The only change from the previous version is in addDemeritPoints(...) and
 * countDemerits(...):
 * now we sum prior offenses in the TWO‐YEAR window _relative to the offense
 * date being added_,
 * rather than comparing everything to LocalDate.now().
 */
public class Person {

    public String personID;
    private String firstName;
    private String lastName;
    private String address;
    private String birthdate; // stored as "dd-MM-yyyy"
    private LocalDate parsedBirthday; // parse and cache at addPerson time

    // Map of offenseDate → points
    private HashMap<LocalDate, Integer> demeritPoints = new HashMap<>();
    public boolean isSuspended = false;

    private static final String DETAILS_FILE = "Details.txt";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    /**
     * addPerson(...) returns true if and only if:
     * 1) checkID(ID) == true
     * 2) checkAddress(address) == true
     * 3) checkDate(birthdate) == true
     *
     * On success, it caches parsedBirthday and writes a five‐line Details.txt.
     */
    public boolean addPerson(String ID, String first, String last,
            String address, String birthdate) {
        if (!checkID(ID)) {
            return false;
        }
        if (!checkAddress(address)) {
            return false;
        }
        if (!checkDate(birthdate)) {
            return false;
        }

        this.personID = ID;
        this.firstName = first;
        this.lastName = last;
        this.address = address;
        this.birthdate = birthdate;

        try {
            this.parsedBirthday = LocalDate.parse(birthdate, DATE_FMT);
        } catch (DateTimeParseException e) {
            // Should not happen if checkDate(...) passed, but just in case:
            return false;
        }

        writeDetails();
        return true;
    }

    /**
     * updatePersonalDetails(...) enforces these rules:
     *
     * 1) If birthdate changes, no other field may change. Otherwise fail.
     * If only birthdate changed, update it (and parsedBirthday) and write the file
     * → return true.
     *
     * 2) If birthdate is unchanged, check address-change:
     * - If newAddress != oldAddress and age < 18 → return false.
     * - Else if newAddress != oldAddress and age ≥ 18, validate via
     * checkAddress(...).
     *
     * 3) If address is unchanged (or just updated), check ID-change:
     * - If newID != oldID, allowed only if oldID.charAt(0) is odd.
     * Then newID must pass checkID(...) and newID.charAt(0) must also be odd.
     *
     * 4) Finally, update firstName/lastName unconditionally, rewrite file, return
     * true.
     *
     * 5) Any parse error on newBirthday → return false immediately.
     */
    public boolean updatePersonalDetails(String newID,
            String newFirst,
            String newLast,
            String newAddress,
            String newBirthday) {
        // 1) Parse old vs. new birthdates
        LocalDate oldBD, newBD;
        try {
            oldBD = LocalDate.parse(this.birthdate, DATE_FMT);
            newBD = LocalDate.parse(newBirthday, DATE_FMT);
        } catch (DateTimeParseException e) {
            // Wrong date format for newBirthday
            System.out.println("Birthdate is invalid!");
            return false;
        }

        // 1a) If birthdate changed at all, no other field may change
        if (!oldBD.equals(newBD)) {
            boolean onlyBirthdayChanged = newID.equals(this.personID)
                    && newFirst.equals(this.firstName)
                    && newLast.equals(this.lastName)
                    && newAddress.equals(this.address);
            if (!onlyBirthdayChanged) {
                System.out.println("Cannot change birthdate plus another field!");
                return false;
            }
            // Only birthdate changed → update and return
            this.birthdate = newBirthday;
            this.parsedBirthday = newBD;
            writeDetails();
            return true;
        }

        // 2) Birthdate unchanged → check address‐change
        int age = Period.between(this.parsedBirthday, LocalDate.now()).getYears();
        if (!newAddress.equals(this.address)) {
            // Change address is allowed only if age ≥ 18
            if (age < 18) {
                System.out.println("Cannot change address! (Under 18)");
                return false;
            }
            if (!checkAddress(newAddress)) {
                System.out.println("Address is not in the right format!");
                return false;
            }
            this.address = newAddress;
        }

        // 3) Check ID‐change rule
        if (!newID.equals(this.personID)) {
            char oldFirstDigit = this.personID.charAt(0);
            if (Character.getNumericValue(oldFirstDigit) % 2 == 0) {
                System.out.println("Cannot change ID if old ID’s first digit is even!");
                return false;
            }
            // Old first digit was odd → now validate newID
            if (!checkID(newID) ||
                    Character.getNumericValue(newID.charAt(0)) % 2 == 0) {
                System.out.println("New ID is invalid or does not start with odd digit!");
                return false;
            }
            this.personID = newID;
        }

        // 4) Update first/last name unconditionally
        this.firstName = newFirst;
        this.lastName = newLast;

        writeDetails();
        return true;
    }

    /**
     * addDemeritPoints(...) returns "Success" or "Failure" according to:
     *
     * 1) Parse offenseDate (dd-MM-yyyy). If parse fails → return "Failure".
     * 2) If points < 1 or points > 6 → return "Failure".
     * 3) Add (offenseDate → points) into the map.
     *
     * 4) Recompute total points for all offenses whose date ≥ (offenseDate minus 2
     * years).
     * If age (as of offenseDate) < 21, threshold=6; else threshold=12.
     * If sum > threshold → isSuspended = true; else false.
     *
     * Always return "Success" if the date‐and‐points checks pass, even if
     * suspension flips to true.
     */
    public String addDemeritPoints(String offenseDate, int points) {
        // 1) Parse offenseDate
        LocalDate offenseLD;
        try {
            offenseLD = LocalDate.parse(offenseDate, DATE_FMT);
        } catch (DateTimeParseException e) {
            return "Failure";
        }
        // 2) Check points range
        if (points < 1 || points > 6) {
            return "Failure";
        }

        // 3) Compute age _as of that offense date_
        int ageAtOffense = Period.between(this.parsedBirthday, offenseLD).getYears();

        // 4) Insert this offense into the map
        demeritPoints.put(offenseLD, points);

        // 5) Recompute total points within two years _relative to offenseLD_
        int threshold = (ageAtOffense < 21) ? 6 : 12;
        boolean nowSuspended = countDemerits(offenseLD, threshold);
        this.isSuspended = nowSuspended;

        return "Success";
    }

    // ─────────────────────────────────────────────────────────────────
    // H E L P E R M E T H O D S
    // ─────────────────────────────────────────────────────────────────

    /**
     * checkID(...) returns true only if:
     * - length == 10
     * - first two chars are digits
     * - at least two chars in the string are non‐alphanumeric
     * - last two chars (indexes 8 and 9) are uppercase A–Z
     */
    public boolean checkID(String ID) {
        if (ID == null || ID.length() != 10) {
            System.out.println("ID is not 10 characters long!");
            return false;
        }
        // first two must be digits
        if (!Character.isDigit(ID.charAt(0)) || !Character.isDigit(ID.charAt(1))) {
            System.out.println("First two digits are not ints!");
            return false;
        }
        // count non‐alphanumeric
        int specialCount = 0;
        for (int i = 0; i < 10; i++) {
            char c = ID.charAt(i);
            if (!Character.isLetterOrDigit(c)) {
                specialCount++;
            }
        }
        if (specialCount < 2) {
            System.out.println("There are fewer than 2 special characters!");
            return false;
        }
        // last two chars must be uppercase A–Z
        char c8 = ID.charAt(8);
        char c9 = ID.charAt(9);
        if (!(c8 >= 'A' && c8 <= 'Z') || !(c9 >= 'A' && c9 <= 'Z')) {
            System.out.println("Last two characters are not uppercase A–Z!");
            return false;
        }
        return true;
    }

    /**
     * checkAddress(...) returns true only if:
     * - address.split("\\|") has exactly 5 parts
     * - parts[3].equals("Victoria")
     * - parts[4].equals("Australia")
     */
    public boolean checkAddress(String address) {
        if (address == null) {
            System.out.println("Address is null!");
            return false;
        }
        String[] parts = address.split("\\|");
        if (parts.length != 5) {
            System.out.println("Address does not have exactly 5 parts!");
            return false;
        }
        String state = parts[3];
        String country = parts[4];
        if (!"Victoria".equals(state) || !"Australia".equals(country)) {
            System.out.println("State is not Victoria or Country is not Australia!");
            return false;
        }
        return true;
    }

    /**
     * checkDate(...) returns true only if the string can be parsed via
     * LocalDate.parse(..., DateTimeFormatter.ofPattern("dd-MM-yyyy")).
     */
    public boolean checkDate(String date) {
        if (date == null) {
            System.out.println("Date is null!");
            return false;
        }
        try {
            LocalDate.parse(date, DATE_FMT);
            return true;
        } catch (DateTimeParseException e) {
            System.out.println("Date is in the incorrect format!");
            return false;
        }
    }

    /**
     * writeDetails() overwrites “Details.txt” with exactly five lines:
     *
     * ID: <personID>
     * First Name: <firstName>
     * Last Name: <lastName>
     * Address: <address>
     * Birthdate: <birthdate>
     */
    private void writeDetails() {
        try {
            FileWriter writer = new FileWriter(new File(DETAILS_FILE), false);
            writer.write("ID: " + this.personID + "\n");
            writer.write("First Name: " + this.firstName + "\n");
            writer.write("Last Name: " + this.lastName + "\n");
            writer.write("Address: " + this.address + "\n");
            writer.write("Birthdate: " + this.birthdate + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * countDemerits(referenceDate, threshold):
     * Sum up all offenses whose date is ≥ (referenceDate minus 2 years).
     * If sum > threshold, return true (suspended); else return false.
     *
     * We do an inclusive check: any offenseDate d satisfying
     * d.isAfter(referenceDate.minusYears(2)) ||
     * d.equals(referenceDate.minusYears(2))
     * is counted.
     */
    private boolean countDemerits(LocalDate referenceDate, int threshold) {
        LocalDate windowStart = referenceDate.minusYears(2);
        int sum = 0;
        for (LocalDate d : demeritPoints.keySet()) {
            // include if d ≥ windowStart
            if (!d.isBefore(windowStart)) {
                sum += demeritPoints.get(d);
            }
        }
        return (sum > threshold);
    }
}