package edu.drexel.se577.grouptwo.viz.filetypes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.opencsv.CSVReader;

import org.apache.commons.lang3.StringUtils;

import edu.drexel.se577.grouptwo.viz.dataset.Sample;
import edu.drexel.se577.grouptwo.viz.dataset.Value;

/**
 * CSVInputHandler This class is responsible for parsing a bytearray of excel
 * sheet into the required format.
 */

public class CSVInputHandler implements FileInputHandler {

    public static String EXT_CSV = "application/csv";

    public static class ValueInterpreter {
        public static edu.drexel.se577.grouptwo.viz.dataset.Value Interpret(String token){

            Value fp = null;

            // isNumeric fails on 25.5... naughty Oracle has a defect
            String tempStr = token.replace(".", "");
            if (StringUtils.isNumeric(tempStr)) {
                try {
                    // falls though when value has a mantissa.
                    int val = Integer.parseInt(token);
                    fp = new edu.drexel.se577.grouptwo.viz.dataset.Value.Int(val);
                    return fp;
                } catch (NumberFormatException ex) {
                    // Not an integer... thats fine.
                }

                try {
                    // Apprently were dealing with a decimal
                    float val = Float.parseFloat(token);
                    fp = new edu.drexel.se577.grouptwo.viz.dataset.Value.FloatingPoint(val);
                    return fp;
                } catch (NumberFormatException ex) {
                    // Not a float? really?. add as arbitrary
                    fp = new edu.drexel.se577.grouptwo.viz.dataset.Value.Arbitrary(token);
                    return fp;
                }
            } else {
                // Check to see if the value was a comma delimited list./**
                // commas were replaced with pipes above.
                if (token.contains("[") || token.contains("]")) {
                    String enums = token.toString().replace("[", "").replace("]", "");
                    fp = new edu.drexel.se577.grouptwo.viz.dataset.Value.Enumerated(enums);   
                    return fp;                     
                } else {
                    // Alright, all thats left is that the value is an arbitrary string.
                    fp = new edu.drexel.se577.grouptwo.viz.dataset.Value.Arbitrary(token);
                    return fp;
                }
            }       
        }
    }

    @Override
    public Optional<? extends FileContents> parseFile(String name, byte[] inputBuffer) {

        // first non-empty column is the Key. Strings with '#', a.k.a comment.
        // column 2 with no values afterwards :
        // - parse as float (assume all numbers are floats) if 0
        // - assume arbitrary value store as string
        // column 2..n : enumeration

        CSVFileContents contents = new CSVFileContents(name);  
        try {   
            CSVReader csvReader;
            csvReader = new CSVReader(new InputStreamReader(new ByteArrayInputStream(inputBuffer)));
            String[] nextLine;      
            Map<Integer, ColumnDesc> attributeMetrics = new HashMap<>();
            
            while ((nextLine = csvReader.readNext()) != null) {
                int numColumns = nextLine.length;

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < numColumns; i++) {
                    String value = nextLine[i];
                    if (value == null || value.trim().isEmpty() || value.contains("#")) {
                        continue;
                    }

                    if(value.contains("^")){
                        ColumnDesc temp = new ColumnDesc(value.replace("^", ""));
                        attributeMetrics.put(i, temp);
                        continue;
                    }

                    value = value.trim();

                    if (value.contains(",")) {
                        // dealing with a value thats a list.
                        // replace , with | so enum can be split later./**
                        value = value.replace(",", "|");
                    }

                    // add values
                    sb.append(nextLine[i]);

                    // avoid last unnessesary comma
                    if (i != (numColumns - 1)) {
                        sb.append(",");
                    }
                }

                if (sb.length() > 0) {
                    String[] tokens = sb.toString().split(","); // should be no empty tokens

                    if(tokens.length != attributeMetrics.size()){
                        continue;
                    }

                    for (int t = 0; t < tokens.length; t++) {                        
                        ColumnDesc col = attributeMetrics.get(t+1);
                        if(col == null) {
                            // somethings very wrong, throw then return valid object that will fail validation.
                            // Failure will send HTTP Response error letting user know of an issue.
                            // TO DO: Bubble description of dataset errors to be shown to the user.
                            throw new IOException("File parser error.");
                        }
                        String token = tokens[t];
                        Value dSetValue = ValueInterpreter.Interpret(token);
                        col.setValue(dSetValue);
                    }

                    Sample s = new Sample();
                    for (Map.Entry<Integer, ColumnDesc> entry : attributeMetrics.entrySet()) {
                        ColumnDesc desc = entry.getValue();
                        s.put(desc.name, desc.getValue());                  
                    }
                    contents.getSamples().add(s);
                }
            }

            csvReader.close();

            for (Map.Entry<Integer, ColumnDesc> entry : attributeMetrics.entrySet()) {               
                ColumnDesc desc = entry.getValue();
                contents.getDefinition().put(desc.attrib);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }   

        return Optional.of(contents);
    }
}