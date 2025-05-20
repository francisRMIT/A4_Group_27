import java.util.HashMap;
import java.util.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.io.FileWriter;
import java.io.IOException;

public class Person {
    public String personID;
    private String firstName;
    private String lastName;
    private String address;
    private String birthdate;
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

        // Checks that there is at least 2 special characte
        if (count < 2) {
            return false;
        }

        // Condition 2.0: Address checking
        String[] addressSplit = address.split("\\|");

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

        // Condition 3.0: Parses birthdate and checks if it follows the pattern
        try {
            LocalDate.parse(birthdate, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        } catch (DateTimeParseException e) {
            return false;
        }

        // If everything passes, write down these details into a
        try {
            FileWriter addPersonFile = new FileWriter("addPerson.txt");
            addPersonFile.write("ID: " + personID + "\n");
            addPersonFile.write("First Name: " + firstName + "\n");
            addPersonFile.write("Last Name: " + lastName + "\n");
            addPersonFile.write("Address: " + address + "\n");
            addPersonFile.write("Birthdate: " + birthdate);
            addPersonFile.close();
        } catch (IOException e) {
            System.out.println("File Error.");
        }

        // Returns true if all conditions are met
        return true;
    }

    public boolean updatePersonalDetails() {
        // Todo:
        return true;
    }

    public boolean addDemeritPoints() {
        // Todo:
        return true;
    }
}
