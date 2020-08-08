package edu.drexel.se577.grouptwo.viz.storage;

import java.util.List;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.stream.Collectors;
import edu.drexel.se577.grouptwo.viz.dataset.Definition;
import edu.drexel.se577.grouptwo.viz.dataset.Sample;
import edu.drexel.se577.grouptwo.viz.dataset.SampleValidator;
import edu.drexel.se577.grouptwo.viz.dataset.Value;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.types.ObjectId;
import org.bson.BsonInt32;
import org.bson.BsonDouble;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.BsonDocument;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

final class MongoDataset implements Dataset {
    private final Definition definition;
    private final ObjectId id;
    private final MongoCollection<Document> datasets;

    MongoDataset(ObjectId id, Definition definition, MongoCollection<Document> datasets)
    {
        this.id = id;
        this.definition = definition;
        this.datasets = datasets;
    }

    @Override
    public String getId() {
        return id.toHexString();
    }

    @Override
    public String getName() {
        return definition.name;
    }

    @Override
    public Definition getDefinition() {
        return definition;
    }

    @Override
    public List<Sample> getSamples() {
        return StreamSupport.stream(
                datasets.find(Filters.eq("_id",id)).spliterator(), false)
            .map(doc -> {
                @SuppressWarnings("unchecked")
                List<Document> samples = (List<Document>) doc.get(
                        "samples",List.class);
                return Optional.ofNullable(samples)
                    .map(list -> {
                        return list.stream()
                            .map(MongoDataset::toSample)
                            .collect(Collectors.toList());
                    }).orElseGet(Collections::emptyList);
            }).findFirst().orElseGet(Collections::emptyList);
    }

    @Override
    public void addSample(Sample sample) {
        SampleValidator validator = new SampleValidator(definition);
        if (!validator.check(sample)) {
            throw new RuntimeException("Invalid sample");
        }
        datasets.updateOne(
                Filters.eq("_id", id),
                Updates.push("samples",toBson(sample)));
    }

    private static BsonValue toBson(Sample sample) {
        final BsonDocument doc = new BsonDocument();
        sample.getKeys().stream().forEach(key -> {
            sample.get(key).ifPresent(value -> {
                doc.put(key, toBson(value));
            });
        });
        return doc;
    }

    private static BsonValue toBson(Value value) {
        final BsonDocument doc = new BsonDocument();
        value.accept(new ValueEncoder(doc));
        return doc;
    }

    private static class ValueEncoder implements Value.Visitor {
        private final BsonDocument doc;

        ValueEncoder(BsonDocument doc) {
            this.doc = doc;
        }

        @Override
        public void visit(Value.Mapping mapping) {
            // multi-level samples not supported in this version.
        }

        @Override
        public void visit(Value.Int value) {
            doc.put("type", new BsonString(MongoEngine.INTEGER));
            doc.put("value", new BsonInt32(value.value));
        }

        @Override
        public void visit(Value.FloatingPoint value) {
            doc.put("type", new BsonString(MongoEngine.FLOATING_POINT));
            doc.put("value", new BsonDouble(value.value));
        }

        @Override
        public void visit(Value.Enumerated value) {
            doc.put("type", new BsonString(MongoEngine.ENUMERATED));
            doc.put("value", new BsonString(value.value));
        }

        @Override
        public void visit(Value.Arbitrary value) {
            doc.put("type", new BsonString(MongoEngine.ARBITRARY));
            doc.put("value", new BsonString(value.value));
        }
    }

    private static Sample toSample(final Document doc) {
        Sample sample = new Sample();
        doc.keySet().stream().forEach(key -> {
            Document value = doc.get(key, Document.class);
            sample.put(key, toValue(value));
        });
        return sample;
    }

    private static Value toValue(final Document doc) {
        String type = doc.get("type", String.class);
        switch (type) {
        case MongoEngine.INTEGER:
            return new Value.Int(doc.get("value", Integer.class));
        case MongoEngine.FLOATING_POINT:
            return new Value.FloatingPoint(doc.get("value", Double.class));
        case MongoEngine.ENUMERATED:
            return new Value.Enumerated(doc.get("value", String.class));
        case MongoEngine.ARBITRARY:
            return new Value.Arbitrary(doc.get("value", String.class));
        }
        throw new RuntimeException("Unknown Value Type");
    }
}
