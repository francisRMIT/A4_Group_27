package src.Main;

import java.util.HashMap;
import java.util.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Period;

public class Person {
    public String personID;
    private String firstName;
    private String lastName;
    private String address;
    private String birthdate;
    private LocalDate parsedBirthday, newParsedBirthday;
    // Assignment says to use date, but i think it would be better to use
    // localdate
    private HashMap<LocalDate, Integer> demeritPoints = new HashMap<>();
    public boolean isSuspended = false;

    public boolean addPerson(String ID, String first, String last, String address, String birthdate) {
        // Condition 1.0: ID checking
        if (!checkID(ID)) {
            return false;
        }

        // Condition 2.0: Address checking
        if (!checkAddress(address)) {
            return false;
        }

        // Condition 3.0: Birthday checking
        if (!checkDate(birthdate)) {
            return false;
        }

        // If all cases are met, add the person
        this.personID = ID;
        this.firstName = first;
        this.lastName = last;
        this.address = address;
        this.birthdate = birthdate;

        // Writes details into a text file called Details.txt
        writeDetails();

        // Returns true if all conditions are met
        return true;
    }

    public boolean updatePersonalDetails(String newID, String newFirst, String newLast, String newAddress,
            String newBirthday) {
        // Condition 2: Changing birthdate (comes first due to its nature)
        // Parse both both new and old birthdays and returns false if either fail
        try {
            parsedBirthday = LocalDate.parse(birthdate, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            newParsedBirthday = LocalDate.parse(newBirthday, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        } catch (DateTimeParseException e) {
            System.out.println("Birthdate is invalid!");
            return false;
        }

        // If the birthdates are different, record the change. If they aren't, proceeds.
        if (parsedBirthday.isBefore(newParsedBirthday)) {
            // Record the change
            this.birthdate = newBirthday;
            writeDetails();

            // Returns now since only birthdate can be changed once
            return true;
        }

        // Condition 1: Changing address
        try {
            // Gets the period between representing age
            int age = Period.between(parsedBirthday, LocalDate.now()).getYears();

            // Checks if person is above 18
            if (age < 18) {
                System.out.println("Cannot change address! (Under 18)");
                return false;
            }

            // Checks if new address is valid and it is, updates it
            if (checkAddress(newAddress)) {
                this.address = newAddress;
            } else {
                System.out.println("Address is not in the right format!");
                return false;
            }
            // Birthdate given is invalid so return false
        } catch (DateTimeParseException e) {
            System.out.println("Birthdate is invalid!");
            return false;
        }

        // Condition 3: Changes ID if the ID meets the previous requirements and the
        // first digit is even
        if (checkID(newID) && Character.getNumericValue(newID.charAt(0)) % 2 != 0) {
            // Successfully updates personID
            this.personID = newID;
        } else {
            System.out.println("ID is either invalid or the first digit is invalid!");
            return false;
        }

        // Simple changes first and last name, no checks needed
        this.firstName = newFirst;
        this.lastName = newLast;

        // Any details changed will be written here
        writeDetails();

        return true;
    }

    public String addDemeritPoints(String offenseDate, int points) {
        // Condition 1: Checks if format is valid
        if (!checkDate(offenseDate)) {
            return "Failure";
        }

        // Condition 2: Checks if points is within range
        if ((points <= 0) && (points >= 7)) {
            return "Failure";
        }

        // Calculate age of person
        int age = Period.between(parsedBirthday, LocalDate.now()).getYears();

        // Add the demerit to the hashmap
        demeritPoints.put(LocalDate.parse(offenseDate, DateTimeFormatter.ofPattern("dd-MM-yyyy")), points);

        // Condition 3: If person is under 21, they are suspended
        if (age < 21) {
            this.isSuspended = countDemerits(6);
        } else {
            this.isSuspended = countDemerits(12);
        }

        return "Success";
    }

    // HELPERS METHODS

    // Address checking function
    public boolean checkAddress(String loc) {
        String[] addressSplit = loc.split("\\|");

        // System.out.print(addressSplit.length);

        // If not length 5, then it is not in the correct format
        if (addressSplit.length != 5) {
            System.out.println("Address does not have 5 segments!");
            return false;
        }

        // Check if Street number is an int
        try {
            Integer.parseInt(addressSplit[0]);
        } catch (NumberFormatException e) {
            System.out.println("Street number is not an int!");
            return false;
        }

        // If the state is not Victoria or the Country is not Australia, fails the
        // check.
        if (!addressSplit[3].equals("Victoria") || !addressSplit[4].equals("Australia")) {
            System.out.println("State is not Victoria or Country is not Australia!");
            return false;
        }

        return true;
    }

    public boolean checkID(String ID) {
        // Checks if Exactly 10 characters long
        if (ID.length() != 10) {
            System.out.println("ID is not 10 characters long!");
            return false;
        }

        // Checking person ID's contents
        int count = 0;
        for (int i = 0; i < ID.length(); ++i) {
            // Check if first two chars are digits
            if (i < 2 && !Character.isDigit(ID.charAt(i))) {
                // If first two are not digits, fails
                System.out.println("First two digits are not ints!");
                return false;
                // NOTE: I asked a tutor about whitespaces and he told me to assume that there
                // will be none
            } else if (!Character.isLetterOrDigit(ID.charAt(i))) {
                // Counts number of special characters (#$@%! etc.)
                count += 1;
            }
        }

        // Checks that there is at least 2 special characters
        if (count < 2) {
            System.out.println("There are less then 2 special characters!");
            return false;
        }

        return true;
    }

    public boolean checkDate(String date) {
        // Parses birthdate and checks if it follows the pattern
        try {
            LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        } catch (DateTimeParseException e) {
            System.out.println("Date is in the incorrect format!");
            return false;
        }

        return true;
    }

    public void writeDetails() {
        // If everything passes, write down these details into a
        try {
            FileWriter addPersonFile = new FileWriter("Details.txt");
            addPersonFile.write("ID: " + personID + "\n");
            addPersonFile.write("First Name: " + firstName + "\n");
            addPersonFile.write("Last Name: " + lastName + "\n");
            addPersonFile.write("Address: " + address + "\n");
            addPersonFile.write("Birthdate: " + birthdate);
            addPersonFile.close();
        } catch (IOException e) {
            System.out.println("File Error.");
        }
    }

    public boolean countDemerits(int limit) {
        // Adds up demerit points
        int counter = 0;
        LocalDate currentDate = LocalDate.now();

        // For every demerit in the hashmap, check the dates
        for (LocalDate date : demeritPoints.keySet()) {
            // Calculates time in months since the demerit was issued
            int timeSince = Period.between(date, currentDate).getMonths();

            // If it is with 24 months, add the demerits to teh counter
            if (timeSince <= 24) {
                counter += demeritPoints.get(date);
            }

            // If the counter surpasses the limit, the driver is suspended
            if (counter > limit) {
                return true;
            }
        }

        // If the counter does not surpass the limit, the driver is not suspended
        return false;
    }
}
