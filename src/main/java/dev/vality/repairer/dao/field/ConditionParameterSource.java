package dev.vality.repairer.dao.field;

import lombok.Getter;
import org.jooq.Comparator;
import org.jooq.Field;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class ConditionParameterSource {

    private final List<ConditionField> conditionFields;

    public ConditionParameterSource() {
        this.conditionFields = new ArrayList<>();
    }

    public <T> ConditionParameterSource addValue(Field<T> field, T value, Comparator comparator) {
        if (value != null) {
            ConditionField conditionField = new SimpleConditionField<>(field, value, comparator);
            conditionFields.add(conditionField);
        }
        return this;
    }

    public <T> ConditionParameterSource addInConditionValue(Field<T> field, Collection<T> value) {
        if (value != null) {
            ConditionField conditionField = new CollectionConditionField<>(field, value, Comparator.IN);
            conditionFields.add(conditionField);
        }
        return this;
    }
}
