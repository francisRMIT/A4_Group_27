public class Main {
    public static void main(String[] args) {
        Person p1 = new Person();
        p1.setDetails("56s_1d&fAB", "John", "Doe", "32|Highland Street|Melbourne|Victoria|Australia", "12-31-2025");

        boolean condition = p1.addPerson();
        System.out.print(condition);
    }
}