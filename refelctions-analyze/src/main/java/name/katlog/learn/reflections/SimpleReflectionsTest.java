package name.katlog.learn.reflections;

import org.reflections.Reflections;

import java.util.Set;

public class SimpleReflectionsTest {

    class A{}
    class B extends A{}
    class C extends B{}
    class D extends A{}

    public @interface Inter {
    }
    class AI{}
    @Inter
    class BI{}
    class CI extends BI{}

    public static void main(String[] args) {
        Reflections reflections = new Reflections();

        Set<Class<? extends A>> subTypes = reflections.getSubTypesOf(A.class);
        System.out.println("subTypes = " + subTypes);
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Inter.class);
        System.out.println("annotated = " + annotated);
    }
}
