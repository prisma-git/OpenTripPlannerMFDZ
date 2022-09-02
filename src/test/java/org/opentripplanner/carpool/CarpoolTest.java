package org.opentripplanner.carpool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.opentripplanner.GtfsTest;
import org.opentripplanner.transit.model.basic.TransitMode;
import org.opentripplanner.transit.model.framework.FeedScopedId;

public class CarpoolTest extends GtfsTest {

  @Override
  public String getFeedName() {
    return "mfdz_gtfs.zip";
  }

  @Test
  public void testImport() {
    var routeId = FeedScopedId.parseId("FEED:3");
    var route = transitModel.getTransitModelIndex().getRouteForId(routeId);

    assertNotNull(route);

    assertEquals(TransitMode.CARPOOL, route.getMode());
  }
}
