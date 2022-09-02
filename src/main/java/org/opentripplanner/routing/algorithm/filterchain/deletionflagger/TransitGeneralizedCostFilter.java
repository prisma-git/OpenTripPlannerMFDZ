package org.opentripplanner.routing.algorithm.filterchain.deletionflagger;

import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.function.DoubleFunction;
import java.util.stream.Collectors;
import org.opentripplanner.model.plan.Itinerary;
import org.opentripplanner.routing.algorithm.filterchain.api.TransitGeneralizedCostFilterParams;

/**
 * This filter remove all transit results which have a generalized-cost higher than the max-limit
 * computed by the {@link #costLimitFunction} plus the wait cost given by
 * {@link TransitGeneralizedCostFilter#getWaitTimeCost}.
 * <p>
 *
 * @see org.opentripplanner.routing.api.request.ItineraryFilterParameters#transitGeneralizedCostLimit
 */
public class TransitGeneralizedCostFilter implements ItineraryDeletionFlagger {

  private final DoubleFunction<Double> costLimitFunction;

  private final double intervalRelaxFactor;

  public TransitGeneralizedCostFilter(TransitGeneralizedCostFilterParams params) {
    this.costLimitFunction = params.costLimitFunction();
    this.intervalRelaxFactor = params.intervalRelaxFactor();
  }

  @Override
  public String name() {
    return "transit-cost-filter";
  }

  @Override
  public List<Itinerary> flagForRemoval(List<Itinerary> itineraries) {
    List<Itinerary> transitItineraries = itineraries
      .stream()
      .filter(Itinerary::hasTransit)
      .sorted(Comparator.comparingDouble(Itinerary::getGeneralizedCost))
      .toList();

    return transitItineraries
      .stream()
      .filter(it ->
        transitItineraries
          .stream()
          .anyMatch(t ->
            it.getGeneralizedCost() >
            (costLimitFunction.apply(t.getGeneralizedCost()) + getWaitTimeCost(t, it))
          )
      )
      .collect(Collectors.toList());
  }

  private double getWaitTimeCost(Itinerary a, Itinerary b) {
    return (
      intervalRelaxFactor *
      Math.max(
        Math.abs(ChronoUnit.SECONDS.between(a.startTime(), b.startTime())),
        Math.abs(ChronoUnit.SECONDS.between(a.endTime(), b.endTime()))
      )
    );
  }
}
