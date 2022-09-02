package org.opentripplanner.routing.algorithm.filterchain.deletionflagger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.opentripplanner.model.plan.Itinerary.toStr;
import static org.opentripplanner.model.plan.TestItineraryBuilder.newItinerary;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opentripplanner.model.plan.Itinerary;
import org.opentripplanner.model.plan.PlanTestConstants;
import org.opentripplanner.routing.algorithm.filterchain.api.TransitGeneralizedCostFilterParams;
import org.opentripplanner.routing.api.request.RequestFunctions;

public class TransitGeneralizedCostFilterTest implements PlanTestConstants {

  @Test
  public void filterWithoutWaitCost() {
    // Create a filter with f(x) = 600 + 2x, without any penalty for waiting at the beginning or end.
    // Remove itineraries with a cost equivalent of 10 minutes and twice the min itinerary cost.
    final TransitGeneralizedCostFilter subject = new TransitGeneralizedCostFilter(
      new TransitGeneralizedCostFilterParams(RequestFunctions.createLinearFunction(600, 2.0), 0.0)
    );

    // Walk all the way, not touched by the filter even if cost(7200) is higher than transit limit.
    Itinerary i1 = newItinerary(A, T11_06).walk(60, E).build();

    // Optimal bus ride. Cost: 120 + 3 * 60 = 300  => Limit: 1200
    Itinerary i2 = newItinerary(A).bus(21, T11_06, T11_09, E).build();

    // Within cost limit. Cost: 120 + 18 * 60 = 1200
    Itinerary i3 = newItinerary(A).bus(31, T11_07, T11_25, E).build();

    // Outside cost limit. Cost: 120 + 19 * 60 = 1260
    Itinerary i4 = newItinerary(A).bus(41, T11_08, T11_27, E).build();

    var all = List.of(i1, i2, i3, i4);

    // Expect - i4 to be dropped
    assertEquals(
      toStr(List.of(i1, i2, i3)),
      toStr(DeletionFlaggerTestHelper.process(all, subject))
    );
  }

  @Test
  public void filterWithWaitCost() {
    // Create a filter with f(x) = 0 + 2x and a penalty of 0.5 at the beginning and end.
    // Remove itineraries with a cost equivalent of twice the itinerary cost plus half of the
    // waiting time.
    final TransitGeneralizedCostFilter subject = new TransitGeneralizedCostFilter(
      new TransitGeneralizedCostFilterParams(RequestFunctions.createLinearFunction(0, 2.0), 0.5)
    );

    // Walk all the way, not touched by the filter even if cost(7200) is higher than transit limit.
    Itinerary i1 = newItinerary(A, T11_06).walk(60, E).build();

    // Optimal bus ride. Cost: 120 + 5 * 60 = 420  => Limit: 840 + half of waiting time
    Itinerary i2 = newItinerary(A).bus(21, T11_00, T11_05, E).build();

    // Within cost limit. Cost: 120 + 15 * 60 = 1020, limit 840 + 0.5 * 10 * 60 -> 0k
    Itinerary i3 = newItinerary(A).bus(31, T11_00, T11_15, E).build();

    // Outside cost limit. Cost: 120 + 20 * 60 = 1260, limit 840 + 0.5 * 15 * 60 -> Filtered
    Itinerary i4 = newItinerary(A).bus(41, T11_00, T11_20, E).build();

    var all = List.of(i1, i2, i3, i4);

    // Expect - i4 to be dropped
    assertEquals(
      toStr(List.of(i1, i2, i3)),
      toStr(DeletionFlaggerTestHelper.process(all, subject))
    );
  }
}
