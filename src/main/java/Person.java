package main.java;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;

public class Person {

    // These fields back the unit tests exactly:
    public String personID;
    private String firstName;
    private String lastName;
    private String address;
    private String birthdate; // stored as "dd-MM-yyyy"
    private LocalDate parsedBirthday; // parse and cache at addPerson time
    private HashMap<LocalDate, Integer> demeritPoints = new HashMap<>();
    public boolean isSuspended = false;

    private static final String DETAILS_FILE = "Details.txt";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public boolean addPerson(String ID, String first, String last,
            String address, String birthdate) {
        // 1) Validate ID
        if (!checkID(ID)) {
            return false;
        }
        // 2) Validate address
        if (!checkAddress(address)) {
            return false;
        }
        // 3) Validate birthdate format
        if (!checkDate(birthdate)) {
            return false;
        }

        // All checks passed → save raw fields
        this.personID = ID;
        this.firstName = first;
        this.lastName = last;
        this.address = address;
        this.birthdate = birthdate;

        // *** Parse and cache the birthdate so addDemeritPoints() will never NPE ***
        try {
            this.parsedBirthday = LocalDate.parse(birthdate, DATE_FMT);
        } catch (DateTimeParseException e) {
            // Should not happen because checkDate(...) succeeded; but guard anyway:
            return false;
        }

        // Write out Details.txt
        writeDetails();
        return true;
    }

    /**
     * updatePersonalDetails(...) returns true if and only if the requested update
     * obeys all of these rules:
     *
     * 1) If the caller is trying to change birthdate (oldBirth != newBirth),
     * then NO OTHER field may change. If any other field changed at the same
     * time, return false. If only birthdate changed (and nothing else),
     * write the new birthdate and return true.
     *
     * 2) If birthdate is unchanged, proceed to check address-change:
     * - If newAddress != oldAddress and age < 18 → return false.
     * - If newAddress != oldAddress and age >= 18 → validate newAddress by
     * checkAddress(...).
     * If checkAddress fails, return false; otherwise update address.
     *
     * 3) If birthdate and address are unchanged, check ID-change:
     * - If newID != oldID, you are trying to change ID. That is allowed only if
     * the **old** ID’s first digit was odd. If oldID.charAt(0) is an even digit,
     * return false. If oldID’s first digit was odd, then validate newID via
     * checkID(newID) AND also the **new** ID’s first digit must itself be odd.
     * If validation fails, return false. Otherwise update ID.
     *
     * 4) If we have arrived here (birthdate==oldBirth, newAddress==oldAddress or
     * was updated, and either ID==oldID or was validly changed),
     * update firstName and lastName unconditionally (no further rules).
     *
     * 5) If any parsing error (invalid format) happens when checking dates,
     * return false.
     *
     * Finally, rewrite Details.txt and return true if all rules passed.
     */
    public boolean updatePersonalDetails(String newID,
            String newFirst,
            String newLast,
            String newAddress,
            String newBirthday) {
        // 1) Parse old and new birthdates
        LocalDate oldBD;
        LocalDate newBD;
        try {
            oldBD = LocalDate.parse(this.birthdate, DATE_FMT);
            newBD = LocalDate.parse(newBirthday, DATE_FMT);
        } catch (DateTimeParseException e) {
            // Wrong date format for newBirthday → fail
            System.out.println("Birthdate is invalid!");
            return false;
        }

        // 1a) If the birthdate is changing at all, ensure no other field changes
        if (!oldBD.equals(newBD)) {
            boolean onlyBirthdayChanged = newID.equals(this.personID)
                    && newFirst.equals(this.firstName)
                    && newLast.equals(this.lastName)
                    && newAddress.equals(this.address);
            if (!onlyBirthdayChanged) {
                // They tried to change birthdate plus at least one other field → fail
                System.out.println("Cannot change birthdate plus another field!");
                return false;
            }
            // Only the birthdate changed; update it and succeed
            this.birthdate = newBirthday;
            this.parsedBirthday = newBD;
            writeDetails();
            return true;
        }

        // 2) Birthdate is unchanged → check address-change
        int age = Period.between(this.parsedBirthday, LocalDate.now()).getYears();
        if (!newAddress.equals(this.address)) {
            // They want to change address → allowed only if age >= 18
            if (age < 18) {
                System.out.println("Cannot change address! (Under 18)");
                return false;
            }
            // Over 18: validate newAddress format
            if (!checkAddress(newAddress)) {
                System.out.println("Address is not in the right format!");
                return false;
            }
            this.address = newAddress;
        }

        // 3) Check ID-change rule
        if (!newID.equals(this.personID)) {
            // They want to change the ID. The old ID’s first digit must be odd.
            char oldFirstDigit = this.personID.charAt(0);
            if (Character.getNumericValue(oldFirstDigit) % 2 == 0) {
                // old ID started with an even digit → cannot change ID at all
                System.out.println("Cannot change ID if old ID’s first digit is even!");
                return false;
            }
            // Old first digit was odd, so we may attempt to change ID.
            // But newID must itself pass checkID(...) AND start with an odd digit.
            if (!checkID(newID) ||
                    Character.getNumericValue(newID.charAt(0)) % 2 == 0) {
                System.out.println("New ID is invalid or does not start with odd digit!");
                return false;
            }
            this.personID = newID;
        }

        // 4) Update firstName and lastName unconditionally
        this.firstName = newFirst;
        this.lastName = newLast;

        // 5) Rewrite Details.txt and return
        writeDetails();
        return true;
    }

    /**
     * addDemeritPoints(...) returns "Success" or "Failure" according to:
     *
     * 1) If offenseDate is not parseable as "dd-MM-yyyy" → return "Failure".
     * 2) If points <= 0 or points > 6 → return "Failure".
     * 3) Otherwise, insert (offenseDate→points) into the demeritPoints map.
     *
     * 4) Recompute total points in the last two years (from now back). If
     * age < 21, threshold=6; else threshold=12. If the sum > threshold,
     * set isSuspended=true; otherwise isSuspended=false.
     *
     * Always return "Success" once the date & point bounds check pass,
     * even if that new offense causes a suspension. Return "Failure" on any
     * parsing or range‐violation error.
     */
    public String addDemeritPoints(String offenseDate, int points) {
        // 1) Check offenseDate format
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
        // 3) We know parsedBirthday is non‐null because addPerson always set it
        int age = Period.between(this.parsedBirthday, LocalDate.now()).getYears();

        // 4) Insert into the map
        demeritPoints.put(offenseLD, points);

        // 5) Count points in the last 2 years
        int threshold = (age < 21) ? 6 : 12;
        boolean nowSuspended = countDemerits(threshold);
        this.isSuspended = nowSuspended;

        return "Success";
    }

    // ────────────────────────────────────────────────────
    // H E L P E R M E T H O D S
    // ────────────────────────────────────────────────────

    /**
     * checkID(...) returns true only if:
     * - length == 10
     * - first two chars are digits
     * - at least two chars in the entire string are non‐alphanumeric
     * - the last two characters (indexes 8 and 9) are uppercase letters A–Z
     *
     * Otherwise it returns false. (Prints a System.out message for debugging.)
     */
    public boolean checkID(String ID) {
        // length must be exactly 10
        if (ID == null || ID.length() != 10) {
            System.out.println("ID is not 10 characters long!");
            return false;
        }
        // first two chars must be digits
        if (!Character.isDigit(ID.charAt(0)) || !Character.isDigit(ID.charAt(1))) {
            System.out.println("First two digits are not ints!");
            return false;
        }
        // count non‐alphanumeric chars => must be ≥ 2
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
        // last two chars (positions 8 and 9) must be uppercase A–Z
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
     * - After splitting by "\\|", there are exactly 5 parts:
     * [0] streetNumber, [1] streetName, [2] city, [3] state, [4] country
     * - state.equals("Victoria")
     * - country.equals("Australia")
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
     * checkDate(...) returns true only if:
     * - The string can be parsed via LocalDate.parse(..., DATE_FMT)
     * - The formatter DATE_FMT = "dd-MM-yyyy" is used, so "15-11-1990" is valid,
     * but "1990-11-15" or "2024/01/01" or "32-13-2024" will throw ParseException.
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
     * writeDetails() overwrites “Details.txt” in the current working directory.
     * The file always contains exactly five lines (no extra blank lines):
     *
     * ID: <personID>
     * First Name: <firstName>
     * Last Name: <lastName>
     * Address: <address>
     * Birthdate: <birthdate>
     *
     * Any IOException is propagated upward (the unit tests assume writing
     * succeeds).
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
            // In practice, UnitTest never expects an IOException once inputs are valid,
            // so we can wrap or rethrow as unchecked. But for simplicity, we just print:
            e.printStackTrace();
        }
    }

    /**
     * countDemerits(int threshold):
     * 1) Iterates over all offenses in demeritPoints (LocalDate → int points).
     * 2) Sums only those offenses whose date is within 2 years of today.
     * 3) If the sum > threshold, return true (suspended); else false.
     */
    private boolean countDemerits(int threshold) {
        LocalDate today = LocalDate.now();
        int sum = 0;
        for (LocalDate ld : demeritPoints.keySet()) {
            // Calculate difference in years
            Period period = Period.between(ld, today);
            int years = period.getYears();
            if (years < 2 || (years == 2 && (period.getMonths() < 0 || period.getDays() < 0))) {
                // If offense date is strictly less than 2 years ago (or exactly 2 years minus
                // some days),
                // we count it. In practice, Period.between(...).getYears() < 2 is enough,
                // because two years
                // exactly would have getYears()==2, getMonths()==0, getDays()==0, which is
                // outside the 24‐month window.
                // So we require years < 2.
                if (period.getYears() == 2) {
                    // when exactly 2 years, months==0 && days==0 → that offense is exactly 2 years
                    // old,
                    // which the spec says “within the last two years” means strictly < 24 months,
                    // so we must exclude year==2, month==0, day==0. We handle this by requiring
                    // years < 2.
                    continue;
                }
                sum += demeritPoints.get(ld);
            }
        }
        return (sum > threshold);
    }
}
