package edu.drexel.se577.grouptwo.viz.filetypes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import edu.drexel.se577.grouptwo.viz.dataset.Attribute;
import edu.drexel.se577.grouptwo.viz.dataset.Definition;
import edu.drexel.se577.grouptwo.viz.dataset.Sample;
import edu.drexel.se577.grouptwo.viz.dataset.Value;

/**
 * XLSInputHandler This class is responsible for parsing a bytearray of excel
 * sheet into the required format.
 */

class XLSInputHandler implements FileInputHandler {

    public static String EXT_XLS = "application/xls";
    public static String EXT_XLSX = "application/xlsx";
    
    public static class ValueInterpreter {
        public static edu.drexel.se577.grouptwo.viz.dataset.Value Interpret(Cell cell){

            Value fp = null;

            // ignore any cells of type CellType.ERROR, CellType.BLANK or CellType.FORMULA                    
            if(cell.getCellTypeEnum() == CellType.BOOLEAN) {
                boolean flag = cell.getBooleanCellValue();
                fp = new edu.drexel.se577.grouptwo.viz.dataset.Value.Arbitrary(
                    Boolean.toString(flag));
                return fp;
            } else if(cell.getCellTypeEnum() == CellType.STRING) {
                String token = cell.getStringCellValue();

                // Check to see if the value was a comma delimited list./**
                // commas were replaced with pipes above.
                if (token.contains("[") || token.contains("]")) {  
                    String enums = token.toString().replace("[", "").replace("]", "");
                    fp = new edu.drexel.se577.grouptwo.viz.dataset.Value.Enumerated(
                        enums);
                    return fp;
                } else {
                    // Alright, all thats left is that the value is an arbitrary string.
                    fp = new edu.drexel.se577.grouptwo.viz.dataset.Value.Arbitrary(
                            token);
                    return fp;
                }
            } else if(cell.getCellTypeEnum() == CellType.NUMERIC) {                        
                Double value = cell.getNumericCellValue();

                // Extra work but we want o classify the number as an integer if possible.
                String token = value.toString();

                if(value == value.intValue()){
                    try {
                        // falls though when value has a mantissa.
                        int val = value.intValue();
                        fp = new edu.drexel.se577.grouptwo.viz.dataset.Value.Int(
                                val);
                        return fp;
                    } catch (NumberFormatException ex) {
                        // Not an integer... thats fine.
                    }
                }

                try {
                    // Apprently were dealing with a decimal
                    float val = Float.parseFloat(token);
                    fp = new edu.drexel.se577.grouptwo.viz.dataset.Value.FloatingPoint(
                            val);
                            return fp;
                } catch (NumberFormatException ex) {
                    // Not a float? really?. add as arbitrary
                    fp = new edu.drexel.se577.grouptwo.viz.dataset.Value.Arbitrary(
                            token);
                    return fp;
                }
            }
            return fp;
        }
    }

    Attribute.Int attributeInt;
    Attribute.FloatingPoint attributeFloatingPoint;
    Attribute.Arbitrary attributeArbitary;
    Attribute.Enumerated attributeEnumerated;
    Definition definition;
    String value;

    @Override
    public Optional<? extends FileContents> parseFile(String name, byte[] inputBuffer) {
        XLSFileContents contents = new XLSFileContents(name);
        Map<Integer, ColumnDesc> attributeMetrics = new HashMap<>();

        try {
            ByteArrayInputStream stream = new ByteArrayInputStream(inputBuffer);

            // Use an InputStream, needs more memory
            Workbook wb = WorkbookFactory.create(stream);
            Sheet sheet = wb.getSheetAt(0);

            Iterator<org.apache.poi.ss.usermodel.Row> rowIterator = sheet.iterator();

            List<Value> values = new ArrayList<>();
            int maxInts = 0, minInts = 0;
            float maxFloats = 0,minFloats = 0;
            List<Integer> integers = new ArrayList<>();
            List<Float> floats = new ArrayList<>();
            Set<String> enumerated = new HashSet<String>();
            
            while (rowIterator.hasNext()) { // navigate the excel sheet one row at a time
                org.apache.poi.ss.usermodel.Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                int column = -1;
                while (cellIterator.hasNext()) { // for each row, go through each column
                    column++;
                    Cell cell = cellIterator.next();

                    if(cell.getCellTypeEnum() == CellType.STRING){
                        String value = cell.getStringCellValue();
                        if (value == null || value.isEmpty() || value.contains("#")) {
                            continue;
                        }
                        
                        if(value.contains("^")){
                            ColumnDesc temp = new ColumnDesc(value.replace("^", ""));
                            attributeMetrics.put(column, temp);
                            continue;
                        }
                    }
                    
                    ColumnDesc col = attributeMetrics.get(column);
                    if(col == null) {
                        // somethings very wrong, throw then return valid object that will fail validation.
                        // Failure will send HTTP Response error letting user know of an issue.
                        // TO DO: Bubble description of dataset errors to be shown to the user.
                        throw new IOException("File parser error.");
                    }                    
                    Value dSetValue = ValueInterpreter.Interpret(cell);
                    col.setValue(dSetValue);
                }
                
                if(attributeMetrics.size() > 0){
                    Sample s = new Sample();
                    for (Map.Entry<Integer, ColumnDesc> entry : attributeMetrics.entrySet()) {
                        ColumnDesc desc = entry.getValue();
                        if(desc.getValue() != null){
                            s.put(desc.name, desc.getValue());
                        }                  
                    }

                    if(s.getKeys().size() > 0)
                        contents.getSamples().add(s);
                }
            }            
            stream.close();
            wb.close();
           
            for (Map.Entry<Integer, ColumnDesc> entry : attributeMetrics.entrySet()) {               
                ColumnDesc desc = entry.getValue();
                contents.getDefinition().put(desc.attrib);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (EncryptedDocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return Optional.of(contents);
    }

}
