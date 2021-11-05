package org.opentripplanner.graph_builder.module.osm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.opentripplanner.graph_builder.DataImportIssueStore;
import org.opentripplanner.graph_builder.module.StreetLinkerModule;
import org.opentripplanner.openstreetmap.BinaryOpenStreetMapProvider;
import org.opentripplanner.routing.edgetype.StreetVehicleParkingLink;
import org.opentripplanner.routing.edgetype.VehicleParkingEdge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.vertextype.VehicleParkingEntranceVertex;

public class TestUnconnectedAreas {

    /**
     * The P+R.osm.gz file contains 2 park and ride, one a single way area and the other a
     * multipolygon with a hole. Both are not linked to any street, apart from three roads that
     * crosses the P+R with w/o common nodes.
     * 
     * This test just make sure we correctly link those P+R with the street network by creating
     * virtual nodes at the place where the street intersects the P+R areas. See ticket #1562.
     */
    @Test
    public void testUnconnectedCarParkAndRide() {
      DataImportIssueStore issueStore = new DataImportIssueStore(true);
      Graph gg = buildOSMGraph("P+R.osm.pbf", issueStore);

        OpenStreetMapModule loader = new OpenStreetMapModule();
        loader.setDefaultWayPropertySetSource(new DefaultWayPropertySetSource());
        File file = new File(getClass().getResource("P+R.osm.pbf").toURI());
        BinaryOpenStreetMapProvider provider = new BinaryOpenStreetMapProvider(file, false);
        loader.setProvider(provider);
        loader.buildGraph(gg, new HashMap<Class<?>, Object>(), issueStore);

      var vehicleParkingVertices = gg.getVerticesOfType(VehicleParkingEntranceVertex.class);
      int nParkAndRide = vehicleParkingVertices.size();
      int nParkAndRideLink = gg.getEdgesOfType(StreetVehicleParkingLink.class).size();
      int nParkAndRideEdge = gg.getEdgesOfType(VehicleParkingEdge.class).size();

      assertEquals(12, nParkAndRide);
      assertEquals(38, nParkAndRideLink);
      assertEquals(42, nParkAndRideEdge);
    }

    @Test
    public void testUnconnectedBikeParkAndRide() {
        DataImportIssueStore issueStore = new DataImportIssueStore(true);
        Graph gg = buildOSMGraph("B+R.osm.pbf", issueStore);

        assertEquals(3, issueStore.getIssues().size());

        var vehicleParkingVertices = gg.getVerticesOfType(VehicleParkingEntranceVertex.class);
        int nParkAndRideEntrances = vehicleParkingVertices.size();
        int nParkAndRideLink = gg.getEdgesOfType(StreetVehicleParkingLink.class).size();
        int nParkAndRideEdge = gg.getEdgesOfType(VehicleParkingEdge.class).size();

        assertEquals(13, nParkAndRideEntrances);
        assertEquals(32, nParkAndRideLink);
        assertEquals(33, nParkAndRideEdge);

    }

    /**
     * This test ensures that if a Park and Ride has a node that is exactly atop a node on a way, the graph
     * builder will not loop forever trying to split it. The hackett-pr.osm.gz file contains a park-and-ride lot in
     * Hackettstown, NJ, which demonstrates this behavior. See discussion in ticket 1605.
     */
    @Test
    public void testCoincidentNodeUnconnectedParkAndRide () throws URISyntaxException {

    	Graph g = new Graph();

        OpenStreetMapModule loader = new OpenStreetMapModule();
        loader.setDefaultWayPropertySetSource(new DefaultWayPropertySetSource());
        File file = new File(getClass().getResource("hackett_pr.osm.pbf").toURI());
        BinaryOpenStreetMapProvider provider = new BinaryOpenStreetMapProvider(file, false);
        loader.setProvider(provider);
        loader.buildGraph(g, new HashMap<Class<?>, Object>());
    	
        Vertex washTwp = null;
        
        int nParkAndRide = 0;
        int nParkAndRideLink = 0;
        for (Vertex v : g.getVertices()) {
        	if (v instanceof ParkAndRideVertex) {
        		nParkAndRide++;
        		washTwp = v;
        	}
        }
        
        for (Edge e : g.getEdges()) {
            if (e instanceof ParkAndRideLinkEdge) {
                nParkAndRideLink++;
            }
        }
        
        assertEquals(1, nParkAndRide);
        // the P+R should get four connections, since the entrance road is duplicated as well, and crosses twice
        // since there are in and out edges, that brings the total to 8 per P+R.
        // Even though the park and rides get merged, duplicate edges remain from when they were separate.
        // FIXME: we shouldn't have duplicate edges.
        assertEquals(16, nParkAndRideLink);
        
        assertNotNull(washTwp);
        
        List<String> connections = new ArrayList<String>();
        
        for (Edge e : washTwp.getOutgoing()) {
        	if (e instanceof ParkAndRideEdge)
        		continue;
        	
        	assertTrue(e instanceof ParkAndRideLinkEdge);
        	
        	connections.add(e.getToVertex().getLabel());
        }
        
        // symmetry
        for (Edge e : washTwp.getIncoming()) {
        	if (e instanceof ParkAndRideEdge)
        		continue;
        	
        	assertTrue(e instanceof ParkAndRideLinkEdge);
        	assertTrue(connections.contains(e.getFromVertex().getLabel()));
        }

      assertTrue(connections.contains("osm:node:3096570222"));
      assertTrue(connections.contains("osm:node:3094264704"));
      assertTrue(connections.contains("osm:node:3094264709"));
      assertTrue(connections.contains("osm:node:3096570227"));
    }
    
    /**
     * Test the situation where a road passes over a node of a park and ride but does not have a node there.
     */
     @Test
     public void testRoadPassingOverNode () {
    	 List<String> connections = testGeometricGraphWithClasspathFile("coincident_pr.osm.pbf", 1, 2);
    	 assertTrue(connections.contains("osm:node:-102236"));
     }
     
     /**
      * Test the situation where a park and ride passes over the node of a road but does not have a node there.
      * Additionally, the node of the road is duplicated to test this corner case.
      */
     @Test
     public void testAreaPassingOverNode () {
    	 List<String> connections = testGeometricGraphWithClasspathFile("coincident_pr_reverse.osm.pbf", 1, 2);
    	 assertTrue(connections.contains("osm:node:-102296"));
     }

     /**
     * Test the situation where a road passes over a node of a park and ride but does not have a node there.
      * Additionally, the node of the ring is duplicated to test this corner case.
      */
     @Test
     public void testRoadPassingOverDuplicatedNode () throws URISyntaxException {
    	 List<String> connections = testGeometricGraphWithClasspathFile("coincident_pr_dupl.osm.pbf", 1, 2);
    	 
    	 // depending on what order everything comes out of the spatial index, we will inject one of
    	 // the duplicated nodes into the way. When we get to the other ringsegments, we will just inject
    	 // the node that has already been injected into the way. So either of these cases are valid.
    	 assertTrue(connections.contains("osm:node:-102266") || connections.contains("osm:node:-102267"));
     }

    /**
     * Test the situation where a road passes over an edge of the park and ride. Both ends of the
     * way are connected to the park and ride.
     */
     public List<String> testGeometricGraphWithClasspathFile(String fn, int prCount, int prlCount) throws URISyntaxException {

      private Graph buildOSMGraph(String osmFileName, DataImportIssueStore issueStore) {
        Graph graph = new Graph();

        OpenStreetMapModule loader = new OpenStreetMapModule();
        loader.setDefaultWayPropertySetSource(new DefaultWayPropertySetSource());
        loader.staticParkAndRide = true;
        loader.staticBikeParkAndRide = true;

        var fileUrl = getClass().getResource(osmFileName);
        assertNotNull(fileUrl);
        File file = new File(fileUrl.toURI());
        BinaryOpenStreetMapProvider provider = new BinaryOpenStreetMapProvider(file, false);
        loader.setProvider(provider);
        loader.buildGraph(graph, new HashMap<>(), issueStore);

        StreetLinkerModule streetLinkerModule = new StreetLinkerModule();
        streetLinkerModule.buildGraph(graph, new HashMap<>(), issueStore);

        return graph;
      }

     /**
      * We've written several OSM files that exhibit different situations but should show the same results. Test with this code.
      */
     private List<String> testGeometricGraphWithClasspathFile(String fileName, int prCount, int prlCount) {

       Graph graph = buildOSMGraph(fileName);

       var vehicleParkingVertices = graph.getVerticesOfType(VehicleParkingEntranceVertex.class);
       int nParkAndRide = vehicleParkingVertices.size();

       int nParkAndRideLink = graph.getEdgesOfType(StreetVehicleParkingLink.class).size();

       assertEquals(prCount, nParkAndRide);
       assertEquals(prlCount, nParkAndRideLink);

       var outgoingEdges = vehicleParkingVertices
           .stream()
           .flatMap(v -> v.getOutgoing().stream())
           .filter(e -> !(e instanceof VehicleParkingEdge))
           // make sure it is connected
           .peek(e -> assertTrue(e instanceof StreetVehicleParkingLink))
           .map(StreetVehicleParkingLink.class::cast)
           .collect(Collectors.toCollection(HashSet::new));

       List<String> connections = outgoingEdges
           .stream()
           .map(e -> e.getToVertex().getLabel())
           .collect(Collectors.toList());

       // Test symmetry
       vehicleParkingVertices
           .stream()
           .flatMap(v -> v.getIncoming().stream())
           .filter(e -> !(e instanceof VehicleParkingEdge))
           .peek(e -> assertTrue(e instanceof StreetVehicleParkingLink))
           .map(StreetVehicleParkingLink.class::cast)
           .forEach(e -> assertTrue(connections.contains(e.getFromVertex().getLabel())));

       return connections;
     }
}
