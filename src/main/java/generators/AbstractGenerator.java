package generators;

import net.datafaker.Faker;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractGenerator<T> {
    protected int id = 1;
    protected final Faker faker = new Faker();

    public abstract T generate();

    public List<T> generateList(int count) {
        List<T> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(generate());
        }
        return list;
    }
}
