package org.opentripplanner.routing.algorithm.raptoradapter.transit.cost;

import org.opentripplanner.transit.model.basic.WheelchairAccessibility;
import org.opentripplanner.transit.model.framework.FeedScopedId;
import org.opentripplanner.transit.raptor.api.transit.RaptorTripSchedule;

public interface DefaultTripSchedule extends RaptorTripSchedule {
  /**
   * This index is used to lookup the transit factor/reluctance to be used with this trip schedule.
   */
  int transitReluctanceFactorIndex();

  /**
   * This is not used by the default calculator, but by the {@link WheelchairCostCalculator} to
   * give non-wheelchair friendly trips a generalized-cost penalty.
   */
  WheelchairAccessibility wheelchairBoarding();
}
