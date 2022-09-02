package org.opentripplanner.graph_builder.module.osm;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.opentripplanner.graph_builder.DataImportIssueStore.noopIssueStore;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentripplanner.openstreetmap.OpenStreetMapProvider;
import org.opentripplanner.routing.algorithm.astar.AStarBuilder;
import org.opentripplanner.routing.api.request.RoutingRequest;
import org.opentripplanner.routing.core.RoutingContext;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.routing.spt.ShortestPathTree;
import org.opentripplanner.transit.model.framework.Deduplicator;
import org.opentripplanner.transit.service.StopModel;
import org.opentripplanner.transit.service.TransitModel;

/**
 * Verify that OSM ways that represent proposed or as yet unbuilt roads are not used for routing.
 * This tests functionality in or around the method isWayRoutable() in the OSM graph builder
 * module.
 *
 * @author abyrd
 */
public class UnroutableTest {

  private Graph graph;

  @BeforeEach
  public void setUp() throws Exception {
    var deduplicator = new Deduplicator();
    var stopModel = new StopModel();
    graph = new Graph(deduplicator);
    TransitModel transitModel = new TransitModel(stopModel, deduplicator);

    URL osmDataUrl = getClass().getResource("bridge_construction.osm.pbf");
    File osmDataFile = new File(URLDecoder.decode(osmDataUrl.getFile(), StandardCharsets.UTF_8));
    OpenStreetMapProvider provider = new OpenStreetMapProvider(osmDataFile, true);
    OpenStreetMapModule osmBuilder = new OpenStreetMapModule(
      List.of(provider),
      Set.of(),
      graph,
      transitModel.getTimeZone(),
      noopIssueStore()
    );
    osmBuilder.setDefaultWayPropertySetSource(new DefaultWayPropertySetSource());
    osmBuilder.buildGraph();
  }

  /**
   * Search for a path across the Willamette river. This OSM data includes a bridge that is not yet
   * built and is therefore tagged highway=construction.
   * TODO also test unbuilt, proposed, raceways etc.
   */
  @Test
  public void testOnBoardRouting() {
    RoutingRequest options = new RoutingRequest();

    Vertex from = graph.getVertex("osm:node:2003617278");
    Vertex to = graph.getVertex("osm:node:40446276");
    options.setMode(TraverseMode.BICYCLE);
    ShortestPathTree spt = AStarBuilder
      .oneToOne()
      .setContext(new RoutingContext(options, graph, from, to))
      .getShortestPathTree();

    GraphPath path = spt.getPath(to);
    // At the time of writing this test, the router simply doesn't find a path at all when highway=construction
    // is filtered out, thus the null check.
    if (path != null) {
      for (Edge edge : path.edges) {
        assertNotEquals(
          "Path should not use the as-yet unbuilt Tilikum Crossing bridge.",
          "Tilikum Crossing",
          edge.getDefaultName()
        );
      }
    }
  }
}
