package Storage;

import Methods.MethodInitializer;

public class InstanceConstructor {



    private final MethodInitializer mi;
    private final ValueStorage vs;

    public InstanceConstructor(){

        vs = new ValueStorage();
        mi= new MethodInitializer(vs);


    }

    public MethodInitializer getMethods() {
        return mi;
    }
}
