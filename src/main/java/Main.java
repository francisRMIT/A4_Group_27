public class Main {
    public static void main(String[] args) {
        // Just run main to check if the function works for now
        Person p1 = new Person();

        // FUNCTION 1
        // Example of running addPerson for p1
        boolean condition = p1.addPerson("56_ada&fAB", "John", "Doe",
                "32|Highland Street|Melbourne|Victoria|Australia",
                "27-02-2015");
        System.out.println(condition);

        // FUNCTION 2
        // Example of running updatePersonalDetails for p1 (can only run is above is
        // true)
        if (condition) {
            boolean updated = p1.updatePersonalDetails("56_ada&fAB", "John", "Doe",
                    "32|Highland Street|Melbourne|Victoria|Australia", "27-02-2015");
            System.out.println(updated);
        }

        // FUNCTION 3
        // Example of running addDemeritPoints (should)
        String result = p1.addDemeritPoints("27-02-2023", 123216);
        System.out.println(result);

        result = p1.addDemeritPoints("26-02-2001231225", 6);
        System.out.println(result);

        result = p1.addDemeritPoints("25-02-2023", 5);
        System.out.println(result);

        result = p1.addDemeritPoints("24-02-2024", 6);
        System.out.println(result);

        System.out.println(p1.isSuspended);
    }
}