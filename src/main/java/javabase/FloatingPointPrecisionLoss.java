package javabase;

public class FloatingPointPrecisionLoss {

    public static void main(String[] args) {
        double sum = 0.0;
        for (int i = 0; i < 10000; i++) {
            sum += 0.1;
        }
        System.out.println("Expected sum: " + (10000 * 0.1));
        System.out.println("Actual sum: " + sum);
    }
}
