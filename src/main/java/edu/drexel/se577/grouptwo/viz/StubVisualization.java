package edu.drexel.se577.grouptwo.viz;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import edu.drexel.se577.grouptwo.viz.dataset.Attribute;
import edu.drexel.se577.grouptwo.viz.dataset.Definition;
import edu.drexel.se577.grouptwo.viz.dataset.Sample;
import edu.drexel.se577.grouptwo.viz.dataset.Value;
import edu.drexel.se577.grouptwo.viz.storage.Dataset;
import edu.drexel.se577.grouptwo.viz.visualization.Visualization;

class StubVisualization {
    private static class CountableExtraction implements Attribute.Visitor {
        Optional<? extends Attribute.Countable> attribute = Optional.empty();
        @Override
        public void visit(Attribute.FloatingPoint attr) {
        }
        @Override
        public void visit(Attribute.Int attr) {
            attribute = Optional.of(attr);
        }
        @Override
        public void visit(Attribute.Arbitrary attr) {
            attribute = Optional.of(attr);
        }
        @Override
        public void visit(Attribute.Mapping attr) {
        }
        @Override
        public void visit(Attribute.Enumerated attr) {
            attribute = Optional.of(attr);
        }
    }

    static Attribute.Countable asCountable(Attribute attribute) {
        CountableExtraction extract = new CountableExtraction();
        attribute.accept(extract);
        return extract.attribute
            .orElseThrow(() -> new RuntimeException("Invalid Attribute Type"));
    }

    private static class ArithmeticExtraction implements Attribute.Visitor {
        Optional<? extends Attribute.Arithmetic> attribute = Optional.empty();
        @Override
        public void visit(Attribute.FloatingPoint attr) {
            attribute = Optional.of(attr);
        }
        @Override
        public void visit(Attribute.Int attr) {
            attribute = Optional.of(attr);
        }
        @Override
        public void visit(Attribute.Arbitrary attr) {
        }
        @Override
        public void visit(Attribute.Mapping attr) {
        }
        @Override
        public void visit(Attribute.Enumerated attr) {
        }
    }

    static Attribute.Arithmetic asArithmetic(Attribute attribute) {
        ArithmeticExtraction extract = new ArithmeticExtraction();
        attribute.accept(extract);
        return extract.attribute
            .orElseThrow(() -> new RuntimeException("Invalid Attribute Type"));
    }

    static class Series extends Visualization.Series {
        private final String name;

        Series(String name, String datasetId, Attribute.Arithmetic attr) {
            super("",new StubDataset(datasetId), attr);
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public Image render() {
            return null;
        }

        @Override
        public List<Value> data() {
            return Collections.emptyList();
        }
    }

    static class Histogram extends Visualization.Histogram {
        private final String name;

        Histogram(String name, String datasetId, Attribute.Countable attr) {
            super("", new StubDataset(datasetId), attr);
            this.name = name;
        }
        @Override
        public String getName() {
            return name;
        }

        @Override
        public Image render() {
            return null;
        }

        @Override
        public List<DataPoint> data() {
            return Collections.emptyList();
        }
    }

    private static class StubDataset implements Dataset {
        private final String id;
        StubDataset(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getName() {
            return "";
        }

        @Override
        public Definition getDefinition() {
            return null;
        }

        @Override
        public List<Sample> getSamples() {
            return Collections.emptyList();
        }

        @Override
        public void addSample(Sample sample) {
        }
    }

    static class Scatter extends Visualization.Scatter {
        private final String name;

        Scatter(String name, String datasetId,
                Attribute.Arithmetic x,
                Attribute.Arithmetic y)
        {
            super("", new StubDataset(datasetId),x, y);
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Image render() {
            return null;
        }

        @Override
        public List<DataPoint> data() {
            return Collections.emptyList();
        }
    }
}
