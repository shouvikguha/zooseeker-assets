# ZooSeeker-Assets

This repository contains:

 - `assets/`
   - Sample JSON assets for the zoo graph, as well as information (metadata) about nodes and edges in this graph.
 - `example/`
   - Example code showing one possible way to load this data.
     - **This may be rather helpful for your project.**
   - This example is presented as a plain Java gradle project.
     - Despite not being an Android project, you **can** open this project in Android Studio. 
     - We have included a Run Configuration so it should "just work".

**Note:** The terms "node" and "vertex" are interchangeable. JGraphT prefers to use "vertex" in its code, but the standard graph JSON format it loads prefers "node".

## Asset Formats

### `sample_zoo_graph.json`

This JSON file contains a top level object with two arrays named `"nodes"` and `"edges"`. 

The `"nodes"` array contains representations of each node. For now, this consists only of an `"id"`, as everything else is stored in the `sample_node_info.json` file.

For example:

```json
{
  "id": "elephant_odyssey"
}
```
    
The `"edges"` array contains representations of each edge, including an `id`, a `source` and `target`, and a `weight` (a distance in meters).

For example:

```json
{
  "id": "edge-6",
  "source": "gators",
  "target": "lions",
  "weight": 200.0
}
```

**Note**: Weights must be represented as floats for JGraphT to read them properly, at least using the default settings (which we've kept as much as possible here).

### `sample_node_info.json`

This file contains additional information about each node. Firstly, it contains the **unique** `id`, so we can match the info to its node in the graph. It also contains:

  - `"kind"`
    - The type of node. This may be `"gate"`, `"exhibit"`, or `"intersection"`.
  - `"name"`
    - The human readable name of this location.
  - `"tags"`
    - An array of tags which could be used for search functionality.

For example:

```json
{
    "id": "elephant_odyssey",
    "kind": "exhibit",
    "name": "Elephant Odyssey",
    "tags": [
        "elephant",
        "mammal",
        "africa"
    ]
}
```

### `sample_edge_info.json`

This file contains additional information about each edge. Firstly, it containst the **unique** `id`, so we can match the info to its edge in the graph. It also contains:

  - `"street"`
    - The human readable name of the street this edge is a part of.
    - Multiple edges may be a part of the same street.

For example:

```json
{
    "id": "edge-1",
    "street": "Africa Rocks Street"
}
```

## Example Code

The `example` directory contains an example Java project. 

It is **not** an Android project. It does however use the same build system, Gradle, that you have been using for Android apps. Check out the `build.gradle` file to see the dependencies (just JGraphT and GSON) and their versions.

The code inside this directory, with a small exception noted in a comment in the `ZooData.java` file, work on Android. **You are welcome to reuse it in your project, though be warned, you should take the time to understand it as you may have to replace or extend it for MS2.**

The contents of this example are:

  - `App.java`
    - A simple example of loading the assets and then computing a shortest path between two hard-coded locations.
    - This class contains the `main` method and shows how to use all of the below:
  - `IdentifiedWeightedEdge.java`
    - A very, very simple subclass of the `DefaultWeightedEdge` which allows edges in the graph to be given IDs. 
    - Also specifies how to load the `id` attribute from the graph file and assign it onto the edge.
  - `ZooData.java`
    - A utility class (like `Utilities` in previous labs, or `Math`, `Arrays`, `Executors`, etc) which contains a few useful staticmethods and classes:
      - `ZooData.VertexInfo`
        - A simple POJO class matching the contents of the `sample_node_info.json`.
        - This class also shows how you can have GSON load strings into enums.
      - `ZooData.EdgeInfo`
        - Same as the previous, but for edges. 
      - `ZooData.loadVertexInfoJSON`
        - Loader method for `sample_node_info.json`.
      - `ZooData.loadEdgeInfoJSON`
        - Loader method for `sample_edge_info.json`.
      - `ZooData.loadGraphJSON`
        - Loader method for `sample_zoo_graph.json`.

### Running the Code

On Mac or Linux, on the command line run:

```
cd example/
./gradlew run
```

**Note:** If you are on Windows, make sure you are using Git Bash, not `cmd` or PowerShell.

Alternatively, if you have opened the `example` project in Android Studio, make sure the run configuration is set to "run" (with a little Gradle/elephant icon next to it) and then just click the usual green run button.
