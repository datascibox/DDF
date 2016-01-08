package io.ddf2.datasource.schema;

import javax.annotation.concurrent.NotThreadSafe;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

/**
 * Created by sangdn on 1/4/16.
 * <p>
 * Schema keeps list of IColumn in order.
 */
@NotThreadSafe
public class Schema implements ISchema {


    protected Set<String> colNames;
    protected List<IColumn> columns;

    public Schema() {
        colNames = new HashSet<>();
        columns = new ArrayList<>();

    }

    public Schema(Schema schema) {
        this();
        this.colNames.addAll(schema.colNames);
        columns.addAll(schema.getColumns());
        assert colNames.size() == columns.size();
    }


    @Override
    public int getNumColumn() {
        return columns.size();
    }

    @Override
    public List<IColumn> getColumns() {
        return columns;
    }

    public void append(IColumn column) {
        if (colNames.add(column.getName()))
            columns.add(column);

    }

    public void remove(int index) {
        assert index >= 0 && index < columns.size();
        IColumn column = columns.remove(index);
        colNames.remove(column.getName());
    }

    public void remove(String colName) {
        if (colNames.contains(colName)) {
            columns.removeIf(a -> a.getName() == colName);
            colNames.remove(colName);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(IColumn column : columns){
            try {
                sb.append(column.getName() + ":" + column.getType().getSimpleName() + "|");
            }catch(Exception ex){
            }

        }
        if(sb.length()>0)
            sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }

    public static SchemaBuilder builder(){
        return new SchemaBuilder() {
            @Override
            public Schema newSchema() {
                return new Schema();
            }

            @Override
            protected Class inferType(String typeName) throws SchemaException {
                typeName = typeName.toLowerCase();
                switch (typeName){
                    case "byte":
                    case "smallint":
                    case "int":
                    case "integer":
                        return Integer.class;
                    case "float":
                    case "double":
                    case "number":
                        return Double.class;
                    case "bigdecimal":
                        return BigDecimal.class;
                    case "long":
                    case "bigint":
                        return Long.class;
                    case "string":
                        return String.class;
                    case "bool":
                    case "boolean":
                        return Boolean.class;
                    case "timestamp":
                        return Timestamp.class;
                    case "date":
                    case "datetime":
                        return java.sql.Date.class;
                    default:
                        throw new SchemaException("Couldn't inferType for " + typeName);
                }
            }
        };
    }
    public static abstract class SchemaBuilder<T extends Schema> {
        protected T schema;
        protected abstract T newSchema();

        protected abstract Class inferType(String typeName) throws SchemaException;
        public SchemaBuilder() {
            schema = newSchema();
        }

        /**
         * @param nameAndType A column name with type.
         *                    example. SchemaBuilder.add("username string").add("age int")
         * @return
         */
        protected SchemaBuilder _add(String nameAndType) throws SchemaException {
            String[] nameAndTypes = nameAndType.split(" ");
            if (nameAndTypes != null && nameAndTypes.length == 2) {
                add(nameAndTypes[0], inferType(nameAndTypes[1]));
            } else {
                throw new IllegalArgumentException("Wrong Format Expect: ColumnName Type");
            }
            return this;
        }
        /**
         * @param multiNameAndType a list of multi column name with type, seperate by comma
         *                    example. SchemaBuilder.add("username string, age int, birthdate date")
         * @return
         */
        public SchemaBuilder add(String multiNameAndType) throws SchemaException {
            String[] nameAndTypes = multiNameAndType.split(",");
            for(String nameAndType : nameAndTypes){
                _add(nameAndType);
            }
            return this;
        }

        public SchemaBuilder add(String colName, Class colType) {
            schema.append(new Column(colName, colType));
            return this;
        }
        public T build(){
            return schema;
        }
        /**
         * Fastest way to build
         * @param multiNameAndType a list of multi column name with type, seperate by comma
         *                    example. SchemaBuilder.add("username string, age int, birthdate date")
         * @return Schema
         */
        public T build(String multiNameAndTpe) throws SchemaException {
            this.add(multiNameAndTpe);
            return build();
        }


    }
}
