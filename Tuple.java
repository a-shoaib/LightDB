package ed.inf.adbs.lightdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Tuple {
    private List<Integer> fields;

    private DatabaseCatalog catalog = DatabaseCatalog.getInstance();

    public Tuple(String[] data){
        this.fields = new ArrayList<>();
        for (String datum: data) {
            fields.add(Integer.parseInt(datum.trim()));
        }
    }

    /**
     * Constructs a new tuple by combining two tuples.
     *
     * @param left  the left tuple
     * @param right the right tuple
     */
    public Tuple(Tuple left, Tuple right) {
        this.fields = new ArrayList<>(left.fields);
        this.fields.addAll(right.fields);
    }


    // gets a specific entry in the tuple
    public Integer getField(int index) {
        return fields.get(index);
    }



   // changes the value of a specific entry in the tuple, might need in the future
//    public void setField(int index, Object value) {
//        fields.set(index, value);
//    }

    // to get the size of the tuple
    public int size() {
        return fields.size();
    }

    @Override
    // Override the Object class's toString method to provide a string representation of the Tuple object
    public String toString() {
        // Start a stream from the list of fields
        return fields.stream()
                // Convert each object in the stream to its string representation
                .map(Object::toString)
                // To concat the input elements, separated by ", " to match the format of the expected output
                .collect(Collectors.joining(", "));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple tuple = (Tuple) o;
        return fields.equals(tuple.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fields);
    }

    public Integer getFieldByName(String fieldName, Schema schema) {
        // Assume there's a schema attribute or you can pass it to the method if needed
        int index = schema.getColumnIndex(fieldName); // Get the index of the column
        if (!(schema.getColumnNames().contains(fieldName))) {
            throw new IllegalArgumentException("Field name does not exist in tuple: " + fieldName);
        }
        return fields.get(index); // Retrieve the value from the list using the index
    }


}
