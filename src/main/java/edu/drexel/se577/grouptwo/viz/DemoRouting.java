package edu.drexel.se577.grouptwo.viz;

import com.google.common.io.Resources;
import java.util.Optional;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import edu.drexel.se577.grouptwo.viz.dataset.Sample;
import edu.drexel.se577.grouptwo.viz.storage.Dataset;
import edu.drexel.se577.grouptwo.viz.dataset.Attribute;
import edu.drexel.se577.grouptwo.viz.dataset.Definition;
import edu.drexel.se577.grouptwo.viz.dataset.Value;
import edu.drexel.se577.grouptwo.viz.visualization.Visualization;
import edu.drexel.se577.grouptwo.viz.filetypes.FileContents;
import edu.drexel.se577.grouptwo.viz.filetypes.FileInputHandler;

class DemoRouting extends Routing {
    private static Visualization.Image getDemoImage() {
        return new Visualization.Image() {
            @Override
            public String mimeType() {
                return "image/jpeg";
            }

            @Override
            public byte[] data() {
                try {
                    return Resources.toByteArray(
                            Resources.getResource(
                                DemoRouting.class,
                                "/demo/cat-pic.jpg"));
                } catch (java.io.IOException ex) {
                    return new byte[0];
                }
            }
        };
    }

    private static class StubDataset implements Dataset {
        private final String id;
        StubDataset() {
            this.id = "any-old-id";
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

    private static class Histogram extends Visualization.Histogram {
        Histogram() {
            super("histogram", new StubDataset(),
                    new Attribute.Enumerated("color", "Blue", "Green", "Red"));
        }
        @Override
        public String getName() {
            return "Bear population of certain colors";
        }

        @Override
        public Image render() {
            return getDemoImage();
        }

        @Override
        public List<DataPoint> data() {
            return Stream.of(
                    new DataPoint(new Value.Enumerated("Blue"), 7),
                    new DataPoint(new Value.Enumerated("Green"), 4),
                    new DataPoint(new Value.Enumerated("Red"), 17))
                .collect(Collectors.toList());
        }
    }

    private static class Series extends Visualization.Series {
        Series() {
            super("series", new StubDataset(),
                    new Attribute.FloatingPoint("temperature", 30.0, 10.0));
        }
        @Override
        public String getName() {
            return "Temperature inside a comfortable room";
        }

        @Override
        public Image render() {
            return getDemoImage();
        }

        @Override
        public List<Value> data() {
            return Stream.of(
                    new Value.FloatingPoint(15.0),
                    new Value.FloatingPoint(17.0),
                    new Value.FloatingPoint(11.0))
                .collect(Collectors.toList());
        }
    }

    private static class Scatter extends Visualization.Scatter {
        Scatter() {
            super("scatter", new StubDataset(),
                    new Attribute.Int("commanded-volume", 0, 11),
                    new Attribute.Int("real-volume",0,20));
        }
        @Override
        public String getName() {
            return "Alexa volume after given commands";
        }

        @Override
        public Image render() {
            return getDemoImage();
        }

        @Override
        public List<DataPoint> data() {
            return Stream.of(
                    new DataPoint( new Value.Int(5), new Value.Int(5)),
                    new DataPoint( new Value.Int(10), new Value.Int(10)),
                    new DataPoint( new Value.Int(11), new Value.Int(20)))
                .collect(Collectors.toList());
        }
    }

    @Override
    Collection<? extends Dataset> listDatasets() {
        return Stream.of(new DemoDataset())
            .collect(Collectors.toList());
    }

    @Override
    Optional<? extends Dataset> getDataset(String id) {
        return Optional.of(new DemoDataset());
    }

    @Override
    URI storeDataset(Definition def) {
        return URI.create("any-old-id");
    }

    @Override
    URI storeVisualization(Visualization def) {
        return URI.create("any-old-viz");
    }

    @Override
    Dataset createDataset(Definition def) {
        return new DemoDataset();
    }

    @Override
    Optional<? extends FileInputHandler> getFileHandler(String contentType) {
        
        return Optional.of(new DemoFileInputHandler());
    }

    @Override
    Optional<? extends Visualization> getVisualization(String id) {
        switch (id) {
        case "series":
            return Optional.of(new Series());
        case "histogram":
            return Optional.of(new Histogram());
        case "scatter":
            return Optional.of(new Scatter());
        }
        return Optional.empty();
    }

    @Override
    Collection<? extends Visualization> listVisualizations() {
        return Stream.of(new Histogram(), new Scatter(), new Series()).collect(Collectors.toList());
    };

    private final class DemoFileInputHandler implements FileInputHandler {
        private final Dataset model = new DemoDataset();

        @Override
        public Optional<? extends FileContents> parseFile(String name, byte[] buffer) {
            return Optional.of(new FileContents() {
                @Override
                public Definition getDefinition() {
                    return model.getDefinition();
                }
                @Override
                public List<Sample> getSamples() {
                    return model.getSamples();
                }
            });
        }
    }

    private final class DemoDataset implements Dataset {
        @Override
        public String getId() {
            return "any-old-id";
        }
        @Override
        public String getName() {
            return "Demo Dataset";
        }

        @Override
        public Definition getDefinition() {
            Definition definition = new Definition(getName());
            definition.put(new Attribute.FloatingPoint(
                        "temperature",30.0, -5.0));
            definition.put(new Attribute.Int("capacity",500,10));
            definition.put(new Attribute.Enumerated("color", "Green", "Yellow", "Blue"));
            definition.put(new Attribute.Arbitrary("comment"));
            return definition;
        }

        @Override
        public List<Sample> getSamples() {
            Sample sample = new Sample();
            sample.put("temperature", new Value.FloatingPoint(25.0));
            sample.put("capacity", new Value.Int(100));
            sample.put("color", new Value.Enumerated("Green"));
            sample.put("comment", new Value.Arbitrary("I don't know how this will be used"));
            return Stream.of(sample,sample,sample)
                .collect(Collectors.toList());
        }

        @Override
        public void addSample(Sample sample) {
        } 
    }
}
