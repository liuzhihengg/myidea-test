public class Main {

    public static void main(String[] args) throws Exception {
            Child child = new Child();
    }
}

class Parent {
    int a = initializeA();  // 成员变量的显式赋值

    {
        System.out.println("Parent instance initializer block");
    }

    public Parent() {
        System.out.println("Parent constructor");
    }

    int initializeA() {
        System.out.println("Initializing a in Parent");
        return 10;
    }
}

class Child extends Parent {

    {
        System.out.println("Child instance initializer block");
    }

    int b = initializeB();  // 成员变量的显式赋值


    public Child() {
        super();  // 显式调用父类构造方法
        System.out.println("Child constructor");
    }

    int initializeB() {
        System.out.println("Initializing b in Child");
        return 20;
    }


}
