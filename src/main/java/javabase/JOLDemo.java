package javabase;

import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.vm.VM;

class Person {
    boolean data;

    {
        data = false;
    }

    static int a = 1;

    static long b = 1;

    public Person() {
    }
}

public class JOLDemo {
    public static void main(String[] args) {
        System.out.println(VM.current().details());

        System.out.println(ClassLayout.parseInstance(new Object()).toPrintable());
        System.out.println(ClassLayout.parseInstance(new String[2]).toPrintable());
        System.out.println(ClassLayout.parseInstance(new Person()).toPrintable());

    }


}
