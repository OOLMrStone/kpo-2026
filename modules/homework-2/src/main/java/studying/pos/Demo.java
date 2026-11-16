package studying.pos;

public final class Demo {
    private Demo() { }

    public static String input(String exportPath) {
        return String.join("\n",
                "3", "1", "S1", "Анна", "1", "S2", "Борис",
                "2", "HW", "Домашняя работа", "20", "40",
                "2", "EXAM", "Экзамен", "10", "60", "3",
                "4", "S1", "HW", "15", "4", "S1", "EXAM", "9",
                "4", "S2", "HW", "20", "4", "S2", "EXAM", "6",
                "4", "S1", "EXAM", "12", "4", "S1", "UNKNOWN", "5",
                "4", "S2", "EXAM", "7", "5", "S2", "HW", "6", "S2",
                "4", "S2", "HW", "20", "6", "S1", "8", "9", exportPath, "0", "");
    }
}
