package ed.inf.adbs.lightdb;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

//import static jdk.jfr.consumer.EventStream.openFile;

public class ScanOperator extends Operator {
    private BufferedReader reader;
    private String filePath;
    private Schema schema;

    public ScanOperator(String filePath, Schema schema){
        this.filePath = filePath;
        this.schema = schema;
        openFile();
    }

    private void openFile(){
        try{
            this.reader = new BufferedReader(new FileReader(filePath));
//            FileReader is wrapped with BufferedReader to improve efficiency
//            by reducing the number of disk reads --> reads larger chunks of data
//            into the buffer at once
        } catch (IOException e){
            e.printStackTrace();    // to point at the line where the exception occured
        }
    }

    @Override
    // reads each row in the database
    public Tuple getNextTuple(){
        try{
            String line = reader.readLine();
            if (line != null){
                return new Tuple(line.split(","));
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override

    public void reset(){
        try{
            reader.close();
            openFile();

        } catch (IOException e){
            e.printStackTrace();
        }

    }

    public void close(){
        try{
            if (reader != null){
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
