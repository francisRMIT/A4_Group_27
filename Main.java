public class Main {
    public static void main(String[] args) {
        // Just run main to check if the function works for now
        Person p1 = new Person();

        // Example of running addPerson for p1
        boolean condition = p1.addPerson("56s_ad&fAB", "John", "Doe",
                "32|Highland Street|Melbourne|Victoria|Australia", "27-02-2005");
        System.out.println(condition);

        // Example of running updatePersonalDetails for p1
        condition = p1.updatePersonalDetails("56s_ad&fAB", "John", "Doe",
                "32|Highland Street|Melbourne|Victoria|Australia", "26-02-2025");
        System.out.println(condition);
    }
} 