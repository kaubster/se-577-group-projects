package edu.drexel.se577.grouptwo.viz.filetypes;

import edu.drexel.se577.grouptwo.viz.dataset.Definition;
import edu.drexel.se577.grouptwo.viz.dataset.Sample;

import java.util.ArrayList;
import java.util.List;

public class XLSFileContents implements FileContents {
    Definition _definition;
    List<Sample> _samples = new ArrayList<>();

    public XLSFileContents(String name){        
        _definition = new Definition(name);
    }

    public Definition getDefinition() {
        return _definition;
    }

    public List<Sample> getSamples(){
        return _samples;
    }
}
