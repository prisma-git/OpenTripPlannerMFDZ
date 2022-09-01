package org.opentripplanner.graph_builder.module.osm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opentripplanner.TestOtpModel;
import org.opentripplanner.graph_builder.module.FakeGraph;
import org.opentripplanner.model.GenericLocation;
import org.opentripplanner.model.plan.Itinerary;
import org.opentripplanner.model.plan.Leg;
import org.opentripplanner.model.plan.Place;
import org.opentripplanner.model.plan.TripPlan;
import org.opentripplanner.routing.algorithm.mapping.GraphPathToItineraryMapper;
import org.opentripplanner.routing.algorithm.mapping.TripPlanMapper;
import org.opentripplanner.routing.api.request.RoutingRequest;
import org.opentripplanner.routing.core.RoutingContext;
import org.opentripplanner.routing.core.TemporaryVerticesContainer;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.impl.GraphPathFinder;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.transit.service.TransitModel;

/**
 * Tests for planning with intermediate places
 * TODO OTP2 - Test is too close to the implementation and will need to be reimplemented.
 */
@Disabled
public class TestIntermediatePlaces {

  /**
   * The spatial deviation that we allow in degrees
   */
  public static final double DELTA = 0.005;

  private static ZoneId timeZone;

  private static GraphPathFinder graphPathFinder;

  private static Graph graph;

  private static GraphPathToItineraryMapper graphPathToItineraryMapper;

  @BeforeAll
  public static void setUp() {
    try {
      TestOtpModel model = FakeGraph.buildGraphNoTransit();
      graph = model.graph();
      TransitModel transitModel = model.transitModel();
      FakeGraph.addPerpendicularRoutes(graph, transitModel);
      FakeGraph.link(graph, transitModel);
      model.index();
      TestIntermediatePlaces.graphPathFinder = new GraphPathFinder(null, Duration.ofSeconds(3));
      timeZone = transitModel.getTimeZone();

      graphPathToItineraryMapper =
        new GraphPathToItineraryMapper(
          timeZone,
          graph.streetNotesService,
          graph.ellipsoidToGeoidDifference
        );
    } catch (Exception e) {
      e.printStackTrace();
      assert false : "Could not add transit data: " + e.toString();
    }
  }

  @Test
  public void testWithoutIntermediatePlaces() {
    GenericLocation fromLocation = new GenericLocation(39.93080, -82.98522);
    GenericLocation toLocation = new GenericLocation(39.96383, -82.96291);
    GenericLocation[] intermediateLocations = {};

    handleRequest(
      fromLocation,
      toLocation,
      intermediateLocations,
      new TraverseModeSet(TraverseMode.WALK),
      false
    );
    handleRequest(
      fromLocation,
      toLocation,
      intermediateLocations,
      new TraverseModeSet(TraverseMode.WALK),
      true
    );
  }

  @Test
  @Disabled
  public void testOneIntermediatePlace() {
    GenericLocation fromLocation = new GenericLocation(39.93080, -82.98522);
    GenericLocation toLocation = new GenericLocation(39.96383, -82.96291);
    GenericLocation[] intermediateLocations = { new GenericLocation(39.92099, -82.95570) };

    handleRequest(
      fromLocation,
      toLocation,
      intermediateLocations,
      new TraverseModeSet(TraverseMode.WALK),
      false
    );
    handleRequest(
      fromLocation,
      toLocation,
      intermediateLocations,
      new TraverseModeSet(TraverseMode.WALK),
      true
    );
  }

  @Test
  @Disabled
  public void testTwoIntermediatePlaces() {
    GenericLocation fromLocation = new GenericLocation(39.93080, -82.98522);
    GenericLocation toLocation = new GenericLocation(39.96383, -82.96291);
    GenericLocation[] intermediateLocations = new GenericLocation[2];
    intermediateLocations[0] = new GenericLocation(39.92099, -82.95570);
    intermediateLocations[1] = new GenericLocation(39.96146, -82.99552);

    handleRequest(
      fromLocation,
      toLocation,
      intermediateLocations,
      new TraverseModeSet(TraverseMode.CAR),
      false
    );
    handleRequest(
      fromLocation,
      toLocation,
      intermediateLocations,
      new TraverseModeSet(TraverseMode.CAR),
      true
    );
  }

  @Test
  public void testTransitWithoutIntermediatePlaces() {
    GenericLocation fromLocation = new GenericLocation(39.9308, -83.0118);
    GenericLocation toLocation = new GenericLocation(39.9998, -83.0198);
    GenericLocation[] intermediateLocations = {};

    handleRequest(
      fromLocation,
      toLocation,
      intermediateLocations,
      new TraverseModeSet(TraverseMode.TRANSIT, TraverseMode.WALK),
      false
    );
    handleRequest(
      fromLocation,
      toLocation,
      intermediateLocations,
      new TraverseModeSet(TraverseMode.TRANSIT, TraverseMode.WALK),
      true
    );
  }

  @Test
  public void testThreeBusStopPlaces() {
    GenericLocation fromLocation = new GenericLocation(39.9058, -83.1341);
    GenericLocation toLocation = new GenericLocation(39.9058, -82.8841);
    GenericLocation[] intermediateLocations = { new GenericLocation(39.9058, -82.9841) };

    handleRequest(
      fromLocation,
      toLocation,
      intermediateLocations,
      new TraverseModeSet(TraverseMode.TRANSIT),
      false
    );
    handleRequest(
      fromLocation,
      toLocation,
      intermediateLocations,
      new TraverseModeSet(TraverseMode.TRANSIT),
      true
    );
  }

  @Test
  public void testTransitOneIntermediatePlace() {
    GenericLocation fromLocation = new GenericLocation(39.9108, -83.0118);
    GenericLocation toLocation = new GenericLocation(39.9698, -83.0198);
    GenericLocation[] intermediateLocations = { new GenericLocation(39.9948, -83.0148) };

    handleRequest(
      fromLocation,
      toLocation,
      intermediateLocations,
      new TraverseModeSet(TraverseMode.TRANSIT, TraverseMode.WALK),
      false
    );
    handleRequest(
      fromLocation,
      toLocation,
      intermediateLocations,
      new TraverseModeSet(TraverseMode.TRANSIT, TraverseMode.WALK),
      true
    );
  }

  @Test
  public void testTransitTwoIntermediatePlaces() {
    GenericLocation fromLocation = new GenericLocation(39.9908, -83.0118);
    GenericLocation toLocation = new GenericLocation(39.9998, -83.0198);
    GenericLocation[] intermediateLocations = new GenericLocation[2];
    intermediateLocations[0] = new GenericLocation(40.0000, -82.900);
    intermediateLocations[1] = new GenericLocation(39.9100, -83.100);

    handleRequest(
      fromLocation,
      toLocation,
      intermediateLocations,
      new TraverseModeSet(TraverseMode.TRANSIT, TraverseMode.WALK),
      false
    );
    handleRequest(
      fromLocation,
      toLocation,
      intermediateLocations,
      new TraverseModeSet(TraverseMode.TRANSIT, TraverseMode.WALK),
      true
    );
  }

  private void handleRequest(
    GenericLocation from,
    GenericLocation to,
    GenericLocation[] via,
    TraverseModeSet modes,
    boolean arriveBy
  ) {
    RoutingRequest request = new RoutingRequest(modes);
    request.setDateTime("2016-04-20", "13:00", timeZone);
    request.setArriveBy(arriveBy);
    request.from = from;
    request.to = to;
    for (GenericLocation intermediateLocation : via) {
      request.addIntermediatePlace(intermediateLocation);
    }
    try (var temporaryVertices = new TemporaryVerticesContainer(graph, request)) {
      var routingContext = new RoutingContext(request, graph, temporaryVertices);
      List<GraphPath> paths = graphPathFinder.graphPathFinderEntryPoint(routingContext);

      assertNotNull(paths);
      assertFalse(paths.isEmpty());
      List<Itinerary> itineraries = graphPathToItineraryMapper.mapItineraries(paths);
      TripPlan plan = TripPlanMapper.mapTripPlan(request, itineraries);
      assertLocationIsVeryCloseToPlace(from, plan.from);
      assertLocationIsVeryCloseToPlace(to, plan.to);
      assertTrue(1 <= plan.itineraries.size());
      for (Itinerary itinerary : plan.itineraries) {
        validateIntermediatePlacesVisited(itinerary, via);
        assertTrue(via.length < itinerary.getLegs().size());
        validateLegsTemporally(request, itinerary);
        validateLegsSpatially(plan, itinerary);
        if (modes.contains(TraverseMode.TRANSIT)) {
          assertTrue(itinerary.getTransitDuration().toSeconds() > 0);
        }
      }
    }
  }

  // Check that every via location is visited in the right order
  private void validateIntermediatePlacesVisited(Itinerary itinerary, GenericLocation[] via) {
    int legIndex = 0;

    for (GenericLocation location : via) {
      Leg leg;
      do {
        assertTrue(
          legIndex < itinerary.getLegs().size(),
          "Intermediate location was not an endpoint of any leg"
        );
        leg = itinerary.getLegs().get(legIndex);
        legIndex++;
      } while (
        Math.abs(leg.getTo().getCoordinate().latitude() - location.lat) > DELTA ||
        Math.abs(leg.getTo().getCoordinate().longitude() - location.lng) > DELTA
      );
    }
  }

  // Check that the end point of a leg is also the start point of the next leg
  private void validateLegsSpatially(TripPlan plan, Itinerary itinerary) {
    Place place = plan.from;
    for (Leg leg : itinerary.getLegs()) {
      assertEquals(place.getCoordinate(), leg.getFrom().getCoordinate());
      place = leg.getTo();
    }
    assertEquals(place.getCoordinate(), plan.to.getCoordinate());
  }

  // Check that the start time and end time of each leg are consistent
  private void validateLegsTemporally(RoutingRequest request, Itinerary itinerary) {
    Instant departTime;
    Instant arriveTime;
    if (request.arriveBy) {
      departTime = itinerary.getLegs().get(0).getStartTime().toInstant();
      arriveTime = request.getDateTime();
    } else {
      departTime = request.getDateTime();
      arriveTime = itinerary.getLegs().get(itinerary.getLegs().size() - 1).getEndTime().toInstant();
    }
    long sumOfDuration = 0;
    for (Leg leg : itinerary.getLegs()) {
      assertFalse(departTime.isAfter(leg.getStartTime().toInstant()));
      assertFalse(leg.getStartTime().isAfter(leg.getEndTime()));

      departTime = leg.getEndTime().toInstant();
      sumOfDuration += leg.getDuration().toSeconds();
    }
    sumOfDuration += itinerary.getWaitingDuration().toSeconds();

    assertFalse(departTime.isAfter(arriveTime));

    // Check the total duration of the legs,
    int accuracy = itinerary.getLegs().size(); // allow 1 second per leg for rounding errors
    assertEquals(sumOfDuration, itinerary.getDuration().toSeconds(), accuracy);
  }

  private void assertLocationIsVeryCloseToPlace(GenericLocation location, Place place) {
    assertEquals(location.lat, place.getCoordinate().latitude(), DELTA);
    assertEquals(location.lng, place.getCoordinate().longitude(), DELTA);
  }
}
