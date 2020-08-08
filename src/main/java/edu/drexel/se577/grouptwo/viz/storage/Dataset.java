package edu.drexel.se577.grouptwo.viz.storage;

import java.util.List;
import edu.drexel.se577.grouptwo.viz.dataset.Definition;
import edu.drexel.se577.grouptwo.viz.dataset.Sample;

public interface Dataset {
    String getId();
    String getName();
    Definition getDefinition();
    List<Sample> getSamples();
    void addSample(Sample sample);
}
