public class Main {
    public static void main(String[] args) {
        // Just run main to check if the function works for now
        Person p1 = new Person();
        p1.setDetails("56s_ad&fAB", "John", "Doe", "32|Highland Street|Melbourne|Victoria|Australia", "27-02-2025");

        boolean condition = p1.addPerson();
        System.out.print(condition);
    }
}