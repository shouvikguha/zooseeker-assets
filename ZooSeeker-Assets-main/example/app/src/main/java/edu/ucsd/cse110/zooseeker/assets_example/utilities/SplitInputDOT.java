package edu.ucsd.cse110.zooseeker.assets_example.utilities;

import edu.ucsd.cse110.zooseeker.assets_example.App;
import edu.ucsd.cse110.zooseeker.assets_example.ZooData;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.builder.GraphBuilder;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;
import org.jgrapht.nio.dot.DOTImporter;
import org.jgrapht.nio.json.JSONExporter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/* ===================
 * WARNING TO STUDENTS
 * ===================
 *
 * This code isn't meant for you to use. It's for internal use
 * in preparing demo assets. As such, it uses some language
 * features and patterns that aren't in the course curriculum.
 *
 * You might find some of the jGraphT + Stream code instructive.
 */

/**
 * Utility to convert a single DOT with attributes into the 3x JSON format.
 */
public class SplitInputDOT {
    public static void main(String[] args) throws IOException {
        // Load original DOT with all data in attributes.
        var inputG = loadInputDOT("assets/ta_only/input_ms1_graph.dot");

        // Export vertex metadata JSON.
        var vInfos = inputG.vertexSet().stream()
            .map(InputVertex::toVertexInfo)
            .collect(Collectors.toList());
        ZooData.storeVertexInfoJSON(vInfos, "exported_vinfo.json");

        // Export edge metadata JSON.
        var eInfos = inputG.edgeSet().stream()
            .map(InputEdge::toEdgeInfo)
            .collect(Collectors.toList());
        ZooData.storeEdgeInfoJSON(eInfos, "exported_einfo.json");

        var outputG = new DefaultUndirectedWeightedGraph<String, ZooData.Graph.Edge>(ZooData.Graph.Edge.class);
        var builder = new GraphBuilder<>(outputG);

        inputG.vertexSet().stream()
            .map(InputVertex::getId)
            .forEach(builder::addVertex);

        inputG.edgeSet().stream()
            .forEach(e -> {
                var src = inputG.getEdgeSource(e);
                var tgt = inputG.getEdgeTarget(e);

                builder.addEdge(
                    src.getId(),
                    tgt.getId(),
                    new ZooData.Graph.Edge(e.getId()),
                    e.getDistance()
                );
            });

        var exporter = new JSONExporter<String, ZooData.Graph.Edge>();
        exporter.setVertexIdProvider(s -> s);
        exporter.setEdgeIdProvider(ZooData.Graph.Edge::getId);
        exporter.setEdgeAttributeProvider(e -> {
            var attr = DefaultAttribute.createAttribute(outputG.getEdgeWeight(e));
            return Map.of("weight", attr);
        });

        var writer = new FileWriter("exported_zoo_graph.json");

        exporter.exportGraph(builder.build(), writer);
        writer.flush();
        writer.close();
    }

    public static DefaultUndirectedGraph<InputVertex, InputEdge> loadInputDOT(String path) {
        // Create an empty graph to populate.
        var g = new DefaultUndirectedGraph<InputVertex, InputEdge>(InputEdge.class);

        var importer = new InputDOTImporter();
        importer.setVertexFactory(id -> {
            var node = new InputVertex();
            node.setId(id);
            return node;
        });

        DOTExporter<String, DefaultEdge> exporter = new DOTExporter<>(v -> v);

        importer.addVertexAttributeConsumer(InputVertex::attributeConsumer);
        importer.addEdgeAttributeConsumer(InputEdge::attributeConsumer);

        // On Android, you would use context.getAssets().open(path) here like in Lab 5.
        var inputStream = App.class.getClassLoader().getResourceAsStream(path);
        assert inputStream != null;

        var reader = new InputStreamReader(inputStream);
        importer.importGraph(g, reader);

        return g;
    }

    public static class InputDOTImporter extends DOTImporter<InputVertex, InputEdge> {

    }

    public static class InputVertex {
        private String id = null;
        private String label = null;
        private String kind = null;
        private String tagList = null;

        public InputVertex() {
        }

        public static InputVertex equalToWithId(String id) {
            var o = new InputVertex();
            o.setId(id);
            return o;
        }

        public static void attributeConsumer(Pair<InputVertex, String> pair, Attribute attr) {
            InputVertex node = pair.getFirst();
            String attrName = pair.getSecond();
            String attrValue = attr.getValue();

            if (attrName.equals("id")) {
                node.setId(attrValue);
            } else if (attrName.equals("label")) {
                node.setLabel(attrValue);
            } else if (attrName.equals("kind")) {
                node.setKind(attrValue);
            } else if (attrName.equals("tags")) {
                node.setTagList(attrValue);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            InputVertex inputVertex = (InputVertex) o;

            return Objects.equals(id, inputVertex.id);
        }

        @Override
        public int hashCode() {
            return id != null ? id.hashCode() : 0;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getKind() {
            return kind;
        }

        public void setKind(String kind) {
            this.kind = kind;
        }

        public String getTagList() {
            return tagList;
        }

        public void setTagList(String tagList) {
            this.tagList = tagList;
        }

        public ZooData.VertexInfo toVertexInfo() {
            var vInfo = new ZooData.VertexInfo();
            vInfo.id = this.id;
            vInfo.name = this.label;
            vInfo.kind = ZooData.VertexInfo.Kind.valueOf(this.kind.toUpperCase());

            vInfo.tags = Optional.ofNullable(this.tagList)
                .map(tl -> List.of(tl.split(",\\s+")))
                .orElse(Collections.emptyList());
            return vInfo;
        }

        public boolean isValid() {
            return Stream.of(this.id, this.label, this.kind, this.tagList).noneMatch(Objects::isNull);
        }

        @Override
        public String toString() {
            return "InputNode{" +
                "id='" + id + '\'' +
                ", label='" + label + '\'' +
                ", kind='" + kind + '\'' +
                '}';
        }
    }

    public static class InputEdge extends DefaultEdge {
        private static Pattern labelRe = Pattern.compile("(.*)\\s+\\((\\d+)ft\\)");
        private String id;
        private String label;

        public InputEdge() {
        }

        public static void attributeConsumer(Pair<InputEdge, String> pair, Attribute attr) {
            InputEdge edge = pair.getFirst();
            String attrName = pair.getSecond();
            String attrValue = attr.getValue();

            if (attrName.equals("id")) {
                edge.setId(attrValue);
            } else if (attrName.equals("label")) {
                edge.setLabel(attrValue);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            InputEdge inputEdge = (InputEdge) o;

            return Objects.equals(id, inputEdge.id);
        }

        @Override
        public int hashCode() {
            return id != null ? id.hashCode() : 0;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public ZooData.EdgeInfo toEdgeInfo() {
            var eInfo = new ZooData.EdgeInfo();
            var matcher = labelRe.matcher(this.label);
            if (!matcher.matches()) throw new AssertionError();

            eInfo.id = this.id;
            eInfo.street = matcher.group(1);

            return eInfo;
        }

        public int getDistance() {
            var matcher = labelRe.matcher(this.label);
            if (!matcher.matches()) throw new AssertionError();

            return Integer.parseInt(matcher.group(2));
        }

        @Override
        public String toString() {
            return "InputEdge{" +
                "id='" + id + '\'' +
                ", label='" + label + '\'' +
                '}';
        }
    }
}