package dev.vality.repairer.dao.field;

import lombok.Data;
import org.jooq.Comparator;
import org.jooq.Field;

@Data
public class SimpleConditionField<T> implements ConditionField<T, T> {

    private final Field<T> field;

    private final T value;

    private final Comparator comparator;
}
