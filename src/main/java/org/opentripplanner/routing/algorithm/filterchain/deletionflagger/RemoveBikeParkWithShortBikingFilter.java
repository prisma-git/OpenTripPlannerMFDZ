package org.opentripplanner.routing.algorithm.filterchain.deletionflagger;

import org.opentripplanner.model.plan.Itinerary;
import org.opentripplanner.routing.algorithm.filterchain.ItineraryListFilter;
import org.opentripplanner.routing.core.TraverseMode;

import java.util.List;
import java.util.stream.Collectors;

public class RemoveBikeParkWithShortBikingFilter implements ItineraryListFilter {

  private final double minBikeParkingDistance;

  public RemoveBikeParkWithShortBikingFilter(double minBikeParkingDistance) {
    this.minBikeParkingDistance = minBikeParkingDistance;
  }

  @Override
  public List<Itinerary> filter(List<Itinerary> itineraries) {
    return itineraries.stream()
        .filter(this::filterItinerariesWithShortBikeParkingLeg)
        .collect(Collectors.toList());
  }

  private boolean filterItinerariesWithShortBikeParkingLeg(Itinerary itinerary) {
    double bikeParkingDistance = 0;
    for (var leg : itinerary.legs) {
      if (leg.isTransitLeg()) {
        break;
      }

      if (leg.getMode() == TraverseMode.BICYCLE) {
        bikeParkingDistance += leg.getDistanceMeters();
      }
    }

    return bikeParkingDistance > minBikeParkingDistance;
  }

}
