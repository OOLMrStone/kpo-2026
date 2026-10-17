package studying.vyshkat.model;

public abstract class Transport extends Asset {
    private final int beginnerScore;
    private final boolean operational;

    protected Transport(String name, String number, int beginnerScore, boolean operational) {
        super(name, number);
        if (beginnerScore < 1 || beginnerScore > 10) {
            throw new IllegalArgumentException("Простота должна быть от 1 до 10.");
        }
        this.beginnerScore = beginnerScore;
        this.operational = operational;
    }

    public final int beginnerScore() {
        return beginnerScore;
    }

    /** Состояние до приёмки. Решение о приёмке принимает ServiceCenter. */
    public final boolean operational() {
        return operational;
    }
}
