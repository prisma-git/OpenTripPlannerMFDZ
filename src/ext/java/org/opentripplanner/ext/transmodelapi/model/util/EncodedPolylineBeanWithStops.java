package org.opentripplanner.ext.transmodelapi.model.util;

import org.opentripplanner.transit.model.site.StopLocation;
import org.opentripplanner.util.model.EncodedPolyline;

public record EncodedPolylineBeanWithStops(
  StopLocation fromQuay,
  StopLocation toQuay,
  EncodedPolyline pointsOnLink
) {}
