package edu.drexel.se577.grouptwo.viz.filetypes;

import edu.drexel.se577.grouptwo.viz.dataset.Definition;
import edu.drexel.se577.grouptwo.viz.dataset.Sample;
import java.util.List;

public interface FileContents {
    Definition getDefinition();
    List<Sample> getSamples();
}
