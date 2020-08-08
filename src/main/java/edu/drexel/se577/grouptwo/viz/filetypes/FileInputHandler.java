package edu.drexel.se577.grouptwo.viz.filetypes;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import edu.drexel.se577.grouptwo.viz.dataset.Attribute;
import edu.drexel.se577.grouptwo.viz.dataset.Value;

public interface FileInputHandler {
    Optional<? extends FileContents> parseFile(String name, byte[] buffer);

    public static Optional<? extends FileInputHandler> forType(String mimeType) {
        return FileInputMapping.getInstance().get(mimeType);
    }


    public class ColumnDesc {
        public String name;
        private Value value;
        public Attribute attrib;
        ColumnDesc(String _name){
            name = _name;
        }

        public int maxInt;
        public int minInt;
        public double maxFloat;
        public double minFloat;
        public Set<String> enumerated = new HashSet<String>();
        
        public Value getValue(){
            return value;
        }
        
        public void setValue(Value _value){
            value = _value;
            if(_value.getClass() == Value.Int.class){
                minInt = Math.min(((Value.Int)_value).value, minInt);
                maxInt = Math.max(((Value.Int)_value).value, maxInt);
                attrib = new Attribute.Int(name, maxInt, minInt);
            } else if(_value.getClass() == Value.FloatingPoint.class){
                minFloat = Math.min(((Value.FloatingPoint)_value).value, minFloat);
                maxFloat = Math.max(((Value.FloatingPoint)_value).value, maxFloat);
                attrib = new Attribute.FloatingPoint(name, maxFloat, minFloat);
            } else if(_value.getClass() == Value.Enumerated.class){
                String enums = ((Value.Enumerated)_value).value;
                if(!enumerated.contains(enums)){
                    enumerated.add(enums);                        
                }
                // StringBuilder result = new StringBuilder();
                // for(String string : enumerated) {
                //     result.append(string);
                //     result.append(",");
                // }
                // String  result.length() > 0 ? result.substring(0, result.length() - 1): "";

                attrib = new Attribute.Enumerated(name, enumerated);
            } else if(attrib == null && _value.getClass() == Value.Arbitrary.class){
                attrib = new Attribute.Arbitrary(name);
            }
        }
    }    
}
