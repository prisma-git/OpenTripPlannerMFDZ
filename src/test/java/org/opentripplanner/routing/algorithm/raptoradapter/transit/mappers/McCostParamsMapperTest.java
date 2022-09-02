package org.opentripplanner.routing.algorithm.raptoradapter.transit.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opentripplanner.transit.model._data.TransitModelForTest.agency;
import static org.opentripplanner.transit.model._data.TransitModelForTest.id;
import static org.opentripplanner.transit.model._data.TransitModelForTest.route;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentripplanner.routing.api.request.RoutingRequest;
import org.opentripplanner.transit.model.framework.FeedScopedId;
import org.opentripplanner.transit.raptor._data.transit.TestRoute;
import org.opentripplanner.transit.raptor._data.transit.TestTransitData;
import org.opentripplanner.transit.raptor._data.transit.TestTripPattern;

class McCostParamsMapperTest {

  static FeedScopedId regularAgency = id("regular-agency");
  static FeedScopedId unpreferredAgency = id("unpreferred-agency");
  static FeedScopedId agencyWithNoRoutes = id("agency-without-routes");

  static FeedScopedId route1 = id("route1");
  static FeedScopedId route2 = id("route2");
  static FeedScopedId route3 = id("route3");
  static Multimap<FeedScopedId, FeedScopedId> routesByAgencies = ArrayListMultimap.create();
  static TestTransitData data;

  static {
    routesByAgencies.putAll(regularAgency, List.of(route1));
    routesByAgencies.putAll(unpreferredAgency, List.of(route2, route3));
    data = new TestTransitData();

    for (var it : routesByAgencies.entries()) {
      data.withRoute(testTripPattern(it.getKey(), it.getValue()));
    }
  }

  @Test
  public void shouldExtractRoutesFromAgencies() {
    var routingRequest = new RoutingRequest();
    routingRequest.setUnpreferredAgencies(List.of(unpreferredAgency));

    BitSet unpreferredPatterns = McCostParamsMapper
      .map(routingRequest, data.getPatterns())
      .unpreferredPatterns();

    for (var pattern : data.getPatterns()) {
      assertEquals(
        pattern.route().getAgency().getId().equals(unpreferredAgency),
        unpreferredPatterns.get(pattern.patternIndex())
      );
    }
  }

  @Test
  public void dealWithEmptyList() {
    var routingRequest = new RoutingRequest();
    routingRequest.setUnpreferredAgencies(List.of(agencyWithNoRoutes));

    assertEquals(
      new BitSet(),
      McCostParamsMapper.map(routingRequest, data.getPatterns()).unpreferredPatterns()
    );
  }

  private static TestRoute testTripPattern(FeedScopedId agencyId, FeedScopedId routeId) {
    return TestRoute.route(
      TestTripPattern
        .pattern(1, 2)
        .withRoute(route(routeId).withAgency(agency(agencyId.getId())).build())
    );
  }
}
