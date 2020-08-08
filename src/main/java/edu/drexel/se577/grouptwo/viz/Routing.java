package edu.drexel.se577.grouptwo.viz;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import edu.drexel.se577.grouptwo.viz.dataset.Attribute;
import edu.drexel.se577.grouptwo.viz.dataset.Definition;
import edu.drexel.se577.grouptwo.viz.dataset.Sample;
import edu.drexel.se577.grouptwo.viz.dataset.Value;
import edu.drexel.se577.grouptwo.viz.filetypes.FileContents;
import edu.drexel.se577.grouptwo.viz.filetypes.FileInputHandler;
import edu.drexel.se577.grouptwo.viz.storage.Dataset;
import edu.drexel.se577.grouptwo.viz.visualization.Visualization;
import spark.Route;
import spark.Spark;

public abstract class Routing {
    private static final String INTEGER = "integer";
    private static final String FLOAT = "floating-point";
    private static final String ENUMERATED = "enumerated";
    private static final String ARBITRARY = "arbitrary";

    private static final URI DATASETS_PATH = URI.create("/api/datasets/");
    private static final URI VISUALIZATION_PATH = URI.create("/api/visualizations/");

    private static final String STYLE_SERIES = "series";
    private static final String STYLE_HISTOGRAM = "histogram";
    private static final String STYLE_SCATTERPLOT = "scatterplot";

    private static final Gson gson = new GsonBuilder()
        .registerTypeAdapter(Visualization.class,new VisualizationGsonAdapter())
        .registerTypeAdapter(Definition.class, new DefinitionGsonAdapter())
        .registerTypeAdapter(Attribute.class, new AttributeGsonAdapter())
        .registerTypeAdapter(Sample.class, new SampleGsonAdapter())
        .registerTypeAdapter(Value.class, new ValueGsonAdapter())
        .create();

    abstract Collection<? extends Dataset> listDatasets();

    abstract Optional<? extends Dataset> getDataset(String id);
    abstract Optional<? extends Visualization> getVisualization(String id);
    abstract URI storeVisualization(Visualization def);
    abstract URI storeDataset(Definition def);

    abstract Dataset createDataset(Definition def);

    abstract Optional<? extends FileInputHandler> getFileHandler(String contentType);
    abstract Collection<? extends Visualization> listVisualizations();

    final String allDatasets() {
        Collection<? extends Dataset> datasets = listDatasets();
        Ref[] refs = datasets.stream()
            .map(dataset -> {
                Ref ref = new Ref();
                URI id = URI.create(dataset.getId());
                ref.name = dataset.getName();
                ref.location = DATASETS_PATH.resolve(id);
                return ref;
            }).toArray(Ref[]::new);
        return gson.toJson(refs);
    }

    final String selectDataset(String id) {
        return getDataset(id).map(Routing::serializeDataset).orElse(null);
    };

    final static String serializeDataset(Dataset dataset) {
        DatasetRep rep = new DatasetRep();
        rep.definition = dataset.getDefinition();
        rep.samples = dataset.getSamples();
        return gson.toJson(rep);
    }

    final String appendSample(String id, String body) {
        final Sample sample = gson.fromJson(body, Sample.class);
        return getDataset(id).map(dataset -> {
            dataset.addSample(sample);
            return serializeDataset(dataset);
        }).orElse(null);
    }

    final Visualization selectVisualization(String id) {
        return getVisualization(id).orElseThrow(() -> new RuntimeException("No such Visualization"));
    }

    final String allVisualizations() {
        Ref[] refs = listVisualizations().stream().map(vis -> {
            Ref ref = new Ref();
            URI id = URI.create(vis.getId());
            ref.name = vis.getName();
            ref.location = VISUALIZATION_PATH.resolve(id);
            return ref;
        }).toArray(Ref[]::new);
        return gson.toJson(refs);
    }

    final URI instanciateVisualization(String body) {
        try {
            Visualization viz = gson.fromJson(body, Visualization.class);
            return VISUALIZATION_PATH.resolve(storeVisualization(viz));
        } catch (RuntimeException re) {
            System.err.println(body);
            System.err.println(re.toString());
            Stream.of(re.getStackTrace())
                .map(Object::toString)
                .reduce((a,b) -> a + "\n" + b)
                .ifPresent(System.err::println);
            throw re;
        }
    }

    final URI instanciateDefinition(String body) {
        Definition def = gson.fromJson(body, Definition.class);
        return DATASETS_PATH.resolve(storeDataset(def));
    }

    final URI processFile(String contentType, String name, byte[] body) {
        return getFileHandler(contentType).map(handler -> {
            FileContents contents = handler.parseFile(name, body)
                    .orElseThrow(() -> new RuntimeException("Parsing Failed"));
            final Dataset created = createDataset(contents.getDefinition());
            contents.getSamples().stream().forEach(sample -> {
                created.addSample(sample);
            });

            URI id = URI.create(created.getId());
            return DATASETS_PATH.resolve(id);
        }).orElseThrow(() -> new RuntimeException("Bad File Type"));
    }

    static class DatasetRep {
        Definition definition;
        List<Sample> samples; // This is probably serialize only
    }

    private static final class VisualizationGsonAdapter implements JsonDeserializer<Visualization>, JsonSerializer<Visualization> {
        private static class ElementCreator implements Visualization.Visitor {
            Optional<JsonObject> object = Optional.empty();
            private final JsonSerializationContext context;
            ElementCreator(JsonSerializationContext context) {
                this.context = context;
            }

            @Override
            public void visit(Visualization.Histogram hist) {
                JsonObject obj = new JsonObject();
                final JsonArray attributes = new JsonArray();
                final JsonArray data = new JsonArray();
                hist.data().stream()
                    .forEach(point -> {
                        JsonObject pt = new JsonObject();
                        pt.add("bin", context.serialize(point.bin, Value.class));
                        pt.addProperty("count", Long.valueOf(point.count));
                        data.add(pt);
                    });
                obj.add("attributes", attributes);
                obj.add("data", data);
                obj.addProperty("name", hist.getName());
                obj.addProperty("style", STYLE_HISTOGRAM);
                obj.addProperty("dataset", DATASETS_PATH.resolve(
                            URI.create(hist.getId())).toString());
                attributes.add(context.serialize(hist.attribute,Attribute.class));
                object = Optional.of(obj);
            }

            @Override
            public void visit(Visualization.Scatter scatter) {
                JsonObject obj = new JsonObject();
                obj.addProperty("name", scatter.getName());
                obj.addProperty("style", STYLE_SCATTERPLOT);
                obj.addProperty("dataset", DATASETS_PATH.resolve(
                            URI.create(scatter.getId())).toString());
                final JsonArray attributes = new JsonArray();
                final JsonArray data = new JsonArray();
                attributes.add(context.serialize(scatter.xAxis, Attribute.class));
                attributes.add(context.serialize(scatter.yAxis, Attribute.class));
                obj.add("attributes", attributes);
                scatter.data().stream()
                    .forEach(point -> {
                        JsonObject pt = new JsonObject();
                        pt.add("x", context.serialize(point.x, Value.class));
                        pt.add("y", context.serialize(point.y, Value.class));
                        data.add(pt);
                    });
                obj.add("data", data);
                object = Optional.of(obj);
            }

            @Override
            public void visit(Visualization.Series series) {
                JsonObject obj = new JsonObject();
                obj.addProperty("name", series.getName());
                obj.addProperty("style", STYLE_SERIES);
                obj.addProperty("dataset", DATASETS_PATH.resolve(
                            URI.create(series.getId())).toString());
                final JsonArray attributes = new JsonArray();
                final JsonArray data = new JsonArray();
                attributes.add(context.serialize(series.attribute, Attribute.class));
                obj.add("attributes",attributes);
                series.data().stream()
                    .forEach(point -> {
                        data.add(context.serialize(point, Value.class));
                    });
                obj.add("data", data);
                object = Optional.of(obj);
            }
        }
        @Override
        public JsonElement serialize(
                Visualization elem,
                java.lang.reflect.Type typeOfT,
                final JsonSerializationContext context)
        {
            ElementCreator creator = new ElementCreator(context);
            elem.accept(creator);
            return creator.object
                .orElseThrow(() -> new RuntimeException("Unknown Visualization Type"));
        }

        @Override
        public Visualization deserialize(
                JsonElement elem,
                java.lang.reflect.Type typeOfT,
                final JsonDeserializationContext context)
        {
            final JsonObject obj = elem.getAsJsonObject();
            final String name = obj.getAsJsonPrimitive("name").getAsString();
            final String style = obj.getAsJsonPrimitive("style").getAsString();
            final URI datasetURI = DATASETS_PATH.relativize(URI.create(
                        obj.getAsJsonPrimitive("dataset").getAsString()));
            Iterator<JsonElement> attrIterator =
                obj.getAsJsonArray("attributes").iterator();
            Iterable<JsonElement> attrIterable = () -> attrIterator;
            Attribute[] attributes = StreamSupport
                .stream(attrIterable.spliterator(), false)
                .map(e -> context.deserialize(e, Attribute.class))
                .toArray(Attribute[]::new);
            switch (style) {
            case STYLE_SERIES:
                return new StubVisualization.Series(
                        name, datasetURI.toString(),
                        StubVisualization.asArithmetic(attributes[0]));
            case STYLE_HISTOGRAM:
                return new StubVisualization.Histogram(
                        name, datasetURI.toString(),
                        StubVisualization.asCountable(attributes[0]));
            case STYLE_SCATTERPLOT:
                return new StubVisualization.Scatter(
                        name, datasetURI.toString(),
                        StubVisualization.asArithmetic(attributes[0]),
                        StubVisualization.asArithmetic(attributes[1]));
            }
            throw new RuntimeException("Unknown Visualization Type");
        }
    }

    private static final class AttributeGsonAdapter implements JsonSerializer<Attribute>, JsonDeserializer<Attribute> {
        private static final class Visitor implements Attribute.Visitor {
            private final JsonObject obj;

            Visitor(JsonObject obj) {
                this.obj = obj;
            }

            @Override
            public void visit(Attribute.Mapping mapping) {
                throw new JsonIOException("Serializing mapping attributes not currently supported");
            }

            @Override
            public void visit(Attribute.Int attr) {
                JsonObject bounds = new JsonObject();
                obj.addProperty("type", INTEGER);
                bounds.addProperty("max", Integer.valueOf(attr.max));
                bounds.addProperty("min", Integer.valueOf(attr.min));
                obj.add("bounds", bounds);
            }

            @Override
            public void visit(Attribute.FloatingPoint attr) {
                JsonObject bounds = new JsonObject();
                obj.addProperty("type", FLOAT);
                bounds.addProperty("max", Double.valueOf(attr.max));
                bounds.addProperty("min", Double.valueOf(attr.min));
                obj.add("bounds", bounds);
            }

            @Override
            public void visit(Attribute.Enumerated attr) {
                JsonArray choices = new JsonArray();
                obj.addProperty("type", ENUMERATED);
                attr.choices.stream().forEach(choice -> choices.add(choice));
                obj.add("values", choices);
            }

            @Override
            public void visit(Attribute.Arbitrary attr) {
                obj.addProperty("type", ARBITRARY);
            }
        }

        @Override
        public JsonElement serialize(final Attribute attribute, java.lang.reflect.Type typeOfT,
                final JsonSerializationContext context) {
            final JsonObject obj = new JsonObject();
            obj.addProperty("name", attribute.name());
            attribute.accept(new Visitor(obj));
            return obj;
        }

        @Override
        public Attribute deserialize(JsonElement elem, java.lang.reflect.Type typeOfT,
                final JsonDeserializationContext context) {
            final JsonObject obj = elem.getAsJsonObject();
            final String name = obj.getAsJsonPrimitive("name").getAsString();
            final String type = obj.getAsJsonPrimitive("type").getAsString();
            switch (type) {
            case INTEGER:
                return emitInteger(name, obj);
            case FLOAT:
                return emitFloat(name, obj);
            case ENUMERATED:
                return emitEnum(name, obj);
            case ARBITRARY:
                return emitArb(name, obj);
            }

            throw new JsonParseException("Unknown Attribute Type");
        }

        private static Attribute.Int emitInteger(String name, JsonObject obj) {
            JsonObject bounds = obj.getAsJsonObject("bounds");
            int max = bounds.getAsJsonPrimitive("max").getAsInt();
            int min = bounds.getAsJsonPrimitive("min").getAsInt();
            return new Attribute.Int(name, max, min);
        }

        private static Attribute.FloatingPoint emitFloat(String name, JsonObject obj) {
            JsonObject bounds = obj.getAsJsonObject("bounds");

            double max = bounds.getAsJsonPrimitive("max").getAsDouble();
            double min = bounds.getAsJsonPrimitive("min").getAsDouble();

            return new Attribute.FloatingPoint(name, max, min);
        }

        private static Attribute.Enumerated emitEnum(String name, JsonObject obj) {
            final Set<String> valueSet = new HashSet<>();
            JsonArray values = obj.getAsJsonArray("values");
            values.iterator().forEachRemaining(elem -> {
                valueSet.add(elem.getAsJsonPrimitive().getAsString());
            });
            return new Attribute.Enumerated(name, valueSet);
        }

        private static Attribute.Arbitrary emitArb(String name, JsonObject obj) {
            return new Attribute.Arbitrary(name);
        }
    }

    private static final class DefinitionGsonAdapter
            implements JsonSerializer<Definition>, JsonDeserializer<Definition> {
        @Override
        public JsonElement serialize(final Definition definition, java.lang.reflect.Type typeOfT,
                final JsonSerializationContext context) {
            final JsonObject obj = new JsonObject();
            final JsonArray attributes = new JsonArray();
            obj.addProperty("name", definition.name);
            definition.getKeys().stream().forEach(name -> {
                definition.get(name).ifPresent(attr -> {
                    attributes.add(context.serialize(attr, Attribute.class));
                });
            });
            obj.add("attributes", attributes);
            return obj;
        }

        @Override
        public Definition deserialize(JsonElement json, java.lang.reflect.Type typeOfT,
                JsonDeserializationContext context) {
            if (!json.isJsonObject())
                throw new JsonParseException("Sample not formatted correctly");
            final JsonObject asObject = json.getAsJsonObject();
            final Definition definition = new Definition(asObject.getAsJsonPrimitive("name").getAsString());
            JsonArray attributes = asObject.getAsJsonArray("attributes");
            attributes.iterator().forEachRemaining(elem -> {
                definition.put(context.deserialize(elem, Attribute.class));
            });
            return definition;
        }
    }

    private static final class SampleGsonAdapter implements JsonSerializer<Sample>, JsonDeserializer<Sample> {
        @Override
        public JsonElement serialize(final Sample sample, java.lang.reflect.Type typeOfT,
                final JsonSerializationContext context) {
            final JsonObject obj = new JsonObject();
            sample.getKeys().stream().forEach(name -> {
                sample.get(name).ifPresent(value -> {
                    obj.add(name, context.serialize(value, Value.class));
                });
            });
            return obj;
        }

        @Override
        public Sample deserialize(JsonElement json, java.lang.reflect.Type typeOfT,
                JsonDeserializationContext context) {
            final Sample sample = new Sample();
            if (!json.isJsonObject())
                throw new JsonParseException("Sample not formatted correctly");
            final JsonObject asObject = json.getAsJsonObject();
            asObject.keySet().stream().forEach(key -> {
                sample.put(key, context.deserialize(asObject.get(key), Value.class));
            });
            return sample;
        }
    }

    private static final class ValueGsonAdapter implements JsonDeserializer<Value>, JsonSerializer<Value> {

        static class ValueSerializer implements Value.Visitor {
            Optional<? extends JsonElement> elem = Optional.empty();

            @Override
            public void visit(Value.Int value) {
                JsonObject obj = new JsonObject();
                obj.addProperty("type", INTEGER);
                obj.addProperty("value", Integer.valueOf(value.value));
                elem = Optional.of(obj);
            }

            @Override
            public void visit(Value.FloatingPoint value) {
                JsonObject obj = new JsonObject();
                obj.addProperty("type", FLOAT);
                obj.addProperty("value", Double.valueOf(value.value));
                elem = Optional.of(obj);
            }

            @Override
            public void visit(Value.Enumerated value) {
                JsonObject obj = new JsonObject();
                obj.addProperty("type", ENUMERATED);
                obj.addProperty("value", value.value);
                elem = Optional.of(obj);
            }

            @Override
            public void visit(Value.Arbitrary value) {
                JsonObject obj = new JsonObject();
                obj.addProperty("type", ARBITRARY);
                obj.addProperty("value", value.value);
                elem = Optional.of(obj);
            }

            @Override
            public void visit(Value.Mapping mapping) {
                // NOOP for this version
            }
        }

        @Override
        public JsonElement serialize(Value value, java.lang.reflect.Type typeOfT, JsonSerializationContext context) {
            ValueSerializer ser = new ValueSerializer();
            value.accept(ser);
            return ser.elem.orElse(null);
        }

        @Override
        public Value deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) {
            if (!json.isJsonObject())
                throw new JsonParseException("Sample value formatted incorrectly");
            JsonObject asObject = json.getAsJsonObject();
            if (!asObject.has("type"))
                throw new JsonParseException("Missing type attribute");

            String type = asObject.getAsJsonPrimitive("type").getAsString();

            switch (type) {
            case INTEGER:
                return new Value.Int(asObject.getAsJsonPrimitive("value").getAsInt());
            case FLOAT:
                return new Value.FloatingPoint(asObject.getAsJsonPrimitive("value").getAsDouble());
            case ENUMERATED:
                return new Value.Enumerated(asObject.getAsJsonPrimitive("value").getAsString());
            case ARBITRARY:
                return new Value.Arbitrary(asObject.getAsJsonPrimitive("value").getAsString());
            default:
                throw new JsonParseException("Unknown type attribute");
            }
        }
    }

    static class Ref {
        String name;
        URI location;
    }

    private static Route getDefinitions = (request, reply) -> {
        reply.type("application/json");
        return getInstance().allDatasets();
    };

    private static Route postVisualization = (request, reply) -> {
        URI location = getInstance().instanciateVisualization(request.body());
        reply.header("Location", location.toString());
        reply.status(201);
        return "";
    };

    private static Route postDefinition = (request, reply) -> {
        Optional<String> contentType = Optional.ofNullable(request.headers("Content-Type"));
        boolean isJson = contentType.map(content -> content.startsWith("application/json")).orElse(false);
        if (isJson) {
            URI location = getInstance().instanciateDefinition(request.body());
            reply.header("Location", location.toString());
            reply.status(201);
            return "";
        } else if (contentType.isPresent()) {
            String name = Optional.ofNullable(request.queryParams("name"))
                    .orElseThrow(() -> new RuntimeException("Must specify dataset name for file input"));

            URI location = getInstance().processFile(contentType.get(), name, request.bodyAsBytes());
            reply.header("Location", location.toString());
            reply.status(201);
            return "";
        } else {
            reply.status(400);
            return "Unrecognized content type";
        }
    };

    private static Route getDataset = (request, reply) -> {
        String id = request.params(":id");
        reply.type("application/json");
        return getInstance().selectDataset(id);
    };

    private static Route postSample = (request, reply) -> {
        String id = request.params(":id");
        return getInstance().appendSample(id, request.body());
    };

    private static Route getVisualizations = (request, reply) -> {
        return getInstance().allVisualizations();
    };

    private static Route getJsonVisualization = (request, reply) -> {
        String id = request.params(":id");
        Visualization viz = getInstance().selectVisualization(id);
        return gson.toJson(viz, Visualization.class);
    };

    private static Route getOtherVisualization = (request, reply) -> {
        String id = request.params(":id");
        Visualization viz = getInstance().selectVisualization(id);
        Visualization.Image image = viz.render();
        reply.type(image.mimeType());
        return image.data();
    };


    private static Routing instance = null;

    static Routing getInstance() {
        // instance = Optional.ofNullable(instance).orElseGet(DemoRouting::new);
        instance = Optional.ofNullable(instance).orElseGet(RealRouting::new);
        return instance;
    }

    public static void main(String[] args) {
        Spark.staticFileLocation("/public");
        Spark.path("/api", () -> {
            Spark.path("/datasets", () -> {
                Spark.get("", Routing.getDefinitions);
                Spark.post("", Routing.postDefinition);
                Spark.get("/:id", Routing.getDataset);
                Spark.post("/:id", Routing.postSample);
            });
            Spark.path("/visualizations",() -> {
                Spark.get("", Routing.getVisualizations);
                Spark.post("", Routing.postVisualization);
                Spark.get("/:id","application/json",Routing.getJsonVisualization);
                Spark.get("/:id",Routing.getOtherVisualization);
            });
        });
        Spark.init();
    }
}
