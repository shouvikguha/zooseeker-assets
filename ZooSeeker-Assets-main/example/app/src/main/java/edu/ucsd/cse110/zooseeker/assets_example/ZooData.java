package edu.ucsd.cse110.zooseeker.assets_example;

import java.io.*;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.GraphWalk;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.json.JSONImporter;

public class ZooData {
    public static class VertexInfo {
        public static enum Kind {
            // The SerializedName annotation tells GSON how to convert
            // from the strings in our JSON to this Enum.
            @SerializedName("gate") GATE,
            @SerializedName("exhibit") EXHIBIT, 
            @SerializedName("intersection") INTERSECTION
        }

        public String id;
        public Kind kind;
        public String name;
        public List<String> tags;
    }

    public static class EdgeInfo {
        public String id;
        public String street;
    }

    public static void storeVertexInfoJSON(List<ZooData.VertexInfo> vInfos, String path) throws IOException {
        Gson gson = new Gson();
        Type type = new TypeToken<List<ZooData.VertexInfo>>(){}.getType();
        FileWriter writer = new FileWriter(path);
        gson.toJson(vInfos, type, writer);
        writer.flush();
        writer.close();
    }

    public static void storeEdgeInfoJSON(List<ZooData.EdgeInfo> eInfos, String path) throws IOException {
        Gson gson = new Gson();
        Type type = new TypeToken<List<ZooData.EdgeInfo>>(){}.getType();
        FileWriter writer = new FileWriter(path);
        gson.toJson(eInfos, type, writer);
        writer.flush();
        writer.close();
    }

    public static ZooData.Graph loadZooGraphJSON(Reader reader) {
        // Create an empty graph to populate.
        ZooData.Graph g = new ZooData.Graph();

        // Create an importer that can be used to populate our empty graph.
        JSONImporter<String, ZooData.Graph.Edge> importer = new JSONImporter<>();

        // We don't need to convert the vertices in the graph, so we return them as is.
        importer.setVertexFactory(v -> v);

        // We need to make sure we set the IDs on our edges from the 'id' attribute.
        // While this is automatic for vertices, it isn't for edges. We keep the
        // definition of this in the IdentifiedWeightedEdge class for convenience.
        importer.addEdgeAttributeConsumer(ZooData.Graph.Edge::attributeConsumer);

        // And now we just import it!
        importer.importGraph(g, reader);

        return g;
    }

    public static Map<String, ZooData.VertexInfo> loadVertexInfoJSON(Reader reader) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<ZooData.VertexInfo>>(){}.getType();
        List<ZooData.VertexInfo> zooData = gson.fromJson(reader, type);

        Map<String, ZooData.VertexInfo> indexedZooData = zooData
            .stream()
            .collect(Collectors.toMap(v -> v.id, datum -> datum));

        return indexedZooData;
    }

    public static Map<String, ZooData.EdgeInfo> loadEdgeInfoJSON(Reader reader) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<ZooData.EdgeInfo>>(){}.getType();
        List<ZooData.EdgeInfo> zooData = gson.fromJson(reader, type);

        Map<String, ZooData.EdgeInfo> indexedZooData = zooData
            .stream()
            .collect(Collectors.toMap(v -> v.id, datum -> datum));

        return indexedZooData;
    }

    public static class Graph extends DefaultUndirectedWeightedGraph<String, ZooData.Graph.Edge> {
        public Graph() {
            // Omit the edge type in construction.
            super(ZooData.Graph.Edge.class);
        }

        public Graph(Class<? extends ZooData.Graph.Edge> edgeClass) {
            // Use a subtype of IWE in construction. Needed to respect Liskov Substitution.
            super(edgeClass);
        }

        public Graph(Supplier<String> vertexSupplier, Supplier<Edge> edgeSupplier) {
            super(vertexSupplier, edgeSupplier);
        }

        public static class Edge extends DefaultWeightedEdge {
            private String id = null;

            @SuppressWarnings("unused")
            public Edge() { /* required by SupplierUtil.createSupplier inside JGraphT */ }

            public Edge(String id) {
                this.id = id;
            }

            public String getId() { return id; }
            public void setId(String id) { this.id = id; }

            @Override
            public String toString() {
                return "(" + getSource() + " :" + id + ": " + getTarget() + ")";
            }

            public static void attributeConsumer(Pair<Edge, String> pair, Attribute attr) {
                Edge edge = pair.getFirst();
                String attrName = pair.getSecond();
                String attrValue = attr.getValue();

                if (attrName.equals("id")) {
                    edge.setId(attrValue);
                }
            }
        }
    }
}