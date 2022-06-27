package dev.vality.repairer.dao.field;

import lombok.Data;
import org.jooq.Comparator;
import org.jooq.Field;

import java.util.Collection;

@Data
public class CollectionConditionField<T> implements ConditionField<T, Collection<T>> {

    private final Field<T> field;

    private final Collection<T> value;

    private final Comparator comparator;
}
