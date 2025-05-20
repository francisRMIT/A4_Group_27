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
        // Todo: asd

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
