public class Main {
    public static void main(String[] args) {
        // Just run main to check if the function works for now
        Person p1 = new Person();

        // Example of running addPerson for p1
        boolean condition = p1.addPerson("56s_ad&fAB", "John", "Doe", "32|Highland Street|Melbourne|Victoria|Australia", "27-02-2005");
        // System.out.println(condition);

        // Example of running updatePersonalDetails for p1 (can only run is above is true)
        if (condition) {
            boolean updated = p1.updatePersonalDetails("76s_ad&fAB", "John", "Doe","32|Highland Street|Melbourne|Victoria|Australia", "27-02-2005");
        }

        // System.out.println(condition);

        // Example of running addDemeritPoints (should)
        String result = p1.addDemeritPoints("27-02-2023", 6);
        result = p1.addDemeritPoints("26-02-2005", 6);
        result = p1.addDemeritPoints("25-02-2005", 6);
        result = p1.addDemeritPoints("24-02-2005", 6);
        result = p1.addDemeritPoints("23-02-2005", 6);

        // System.out.println(result);
        System.out.println(p1.isSuspended);


        // NOTE: With the current config above, everything should pass. This should be for writing test cases. 
        //       I have no idea how to do the pom.xml stuff
    }
} 