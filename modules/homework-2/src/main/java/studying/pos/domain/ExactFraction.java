package studying.pos.domain;

import java.math.BigDecimal;
import java.math.BigInteger;

// Хранит промежуточные суммы без округления, сокращая дробь после каждой операции.
record ExactFraction(BigInteger numerator, BigInteger denominator) {
    ExactFraction {
        if (denominator.signum() <= 0) {
            throw new IllegalArgumentException("Знаменатель должен быть положительным.");
        }
        BigInteger divisor = numerator.gcd(denominator);
        numerator = numerator.divide(divisor);
        denominator = denominator.divide(divisor);
    }

    static ExactFraction ratio(BigDecimal numerator, BigDecimal denominator) {
        BigInteger top = numerator.unscaledValue();
        BigInteger bottom = denominator.unscaledValue();
        int scaleDifference = denominator.scale() - numerator.scale();
        if (scaleDifference >= 0) {
            top = top.multiply(BigInteger.TEN.pow(scaleDifference));
        } else {
            bottom = bottom.multiply(BigInteger.TEN.pow(-scaleDifference));
        }
        return new ExactFraction(top, bottom);
    }

    ExactFraction add(ExactFraction other) {
        return new ExactFraction(numerator.multiply(other.denominator).add(other.numerator.multiply(denominator)),
                denominator.multiply(other.denominator));
    }
}
