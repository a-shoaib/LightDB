package ed.inf.adbs.lightdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Tuple {
    private List<Integer> fields;

    public Tuple(String[] data){
        this.fields = new ArrayList<>();
        for (String datum: data) {
            fields.add(Integer.parseInt(datum.trim()));
        }
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

}
