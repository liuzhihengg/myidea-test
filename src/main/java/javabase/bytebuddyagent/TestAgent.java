package javabase.bytebuddyagent;

public class TestAgent {
//    static { InAppAgent.install(); }

    public static void main(String[] args) {
        for (int i = 0; i < 1000; i++) {
            test(String.valueOf(i));
            if (Counters.getSize() > 0 && Counters.getSize() % 100 == 0) {
                System.out.println(Counters.getSize());
            }
        }
    }

    public static void test(String str) {

    }
}
