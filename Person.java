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
    private HashMap<Date, Integer> demeritPoints;
    private boolean isSuspended;

    // Class to set details, can change later
    public void setDetails(String ID, String first, String last, String address, String birthdate) {
        this.personID = ID;
        this.firstName = first;
        this.lastName = last;
        this.address = address;
        this.birthdate = birthdate;
    }

    public boolean addPerson() {
        // Condition 1.0: Exactly 10 characters long
        if (personID.length() != 10) {
            return false;
        }

        // Checking person ID's contents
        int count = 0;
        for (int i = 0; i < personID.length(); ++i) {
            // Check if first two chars are digits
            if (i < 2 && !Character.isDigit(personID.charAt(i))) {
                // If first two are not digits, fails
                return false;
                // NOTE: I asked a tutor about whitespaces and he told me to assume that there
                // will be none
            } else if (!Character.isLetterOrDigit(personID.charAt(i))) {
                // Counts number of special characters (#$@%! etc.)
                count += 1;
            }
        }

        // Checks that there is at least 2 special characters
        if (count < 2) {
            return false;
        }

        // Condition 2.0: Address checking
        if (!checkAddress(address)) {
            return false;
        }

        // Condition 3.0: Parses birthdate and checks if it follows the pattern
        try {
            LocalDate.parse(birthdate, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        } catch (DateTimeParseException e) {
            return false;
        }

        // Writes details if true
        writeDetails();

        // Returns true if all conditions are met
        return true;
    }

    // Address checking function
    public boolean checkAddress(String loc) {
        String[] addressSplit = loc.split("\\|");

        // If not length 5, then it is not in the correct format
        if (addressSplit.length != 5) {
            return false;
        }

        // Check if Street number is an int
        try {
            Integer.parseInt(addressSplit[0]);
        } catch (NumberFormatException e) {
            return false;
        }

        // If the state is not Victoria or the Country is not Australia, fails the
        // check.
        if (!addressSplit[3].equals("Victoria") || !addressSplit[4].equals("Australia")) {
            return false;
        }

        return true;
    }

    public boolean updatePersonalDetails(String newID, String newFirst, String newLast, String newAddress, String newBirthday) {
        // Condition 2: Changing birthdate (comes first due to its nature)
        // Parse both both new and old birthdays and returns false of either fail
        try {
            parsedBirthday = LocalDate.parse(birthdate, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            newParsedBirthday = LocalDate.parse(newBirthday, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        } catch (DateTimeParseException e) {
            return false;
        }

        // If the birthdates are different, record the change
        if (parsedBirthday != newParsedBirthday) {
            // Record the change
            this.birthdate = newBirthday;
            writeDetails();

            // Returns now since only birthdate can be changed once
            return true;
        }

        // Condition 1: Changing address
        try {
            // Gets the period between them
            Period period = Period.between(parsedBirthday, LocalDate.now());

            // Checks if person is above 18
            if (period.getYears() < 18) {
                System.out.println("Cannot change address (Under 18)");
                return false;
            }

            // Checks if new address is valid
            if (checkAddress(newAddress)) {
                this.address = newAddress;
            } else {
                return false;
            }

        // Birthdate given is invalid so return false
        } catch (DateTimeParseException e) {
            return false;
        }

        // Any details changed will be written here
        writeDetails();

        return true;
    }

    public boolean addDemeritPoints() {
        // Todo:
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
}
