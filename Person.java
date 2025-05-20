import java.util.HashMap;
import java.util.Date;

public class Person {
    public String personID;
    private String firstName;
    private String lastName;
    private String address;
    private String birthdate;
    private HashMap<Date, Integer> demeritPoints;
    private boolean isSuspended;

    // Temporary class to set details, can change later
    public void setDetails(String ID, String first, String last, String address, String birthdate) {
        this.personID = ID;
        this.firstName = first;
        this.lastName = last;
        this.address = address;
        this.birthdate = birthdate;
    }

    public boolean addPerson() {

        // Condition 1: Exactly 10 characters long
        if (personID.length() != 10) {
            return false;
        }

        // Condition 1: Checking person ID contents
        int count = 0;

        for (int i = 0; i < personID.length(); ++i) {
            // Check if first two chars are digits
            if (i < 2 && !Character.isDigit(personID.charAt(i))) {
                // If first two are not digits, fails
                return false;
            } else if (!Character.isLetterOrDigit(personID.charAt(i))) {
                // Counts number of special characters (#$@%! etc.)
                count += 1;
            }
        }

        // Checks that there is at least 2 special characte
        if (count < 2) {
            return false;
        }

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
