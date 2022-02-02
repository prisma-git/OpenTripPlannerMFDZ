package org.opentripplanner.routing.algorithm.filterchain.deletionflagger;

import org.opentripplanner.model.plan.Itinerary;

import java.util.List;
import java.util.stream.Collectors;
import org.opentripplanner.routing.algorithm.filterchain.ItineraryListFilter;

import static org.opentripplanner.routing.core.TraverseMode.BICYCLE;

public class ParkAndRideDirectBikeItineraryFilter implements ItineraryListFilter {

  @Override
  public List<Itinerary> filter(List<Itinerary> itineraries) {
    return itineraries.stream()
        .filter(this::filterBikeOnlyParkAndRideItineraries)
        .collect(Collectors.toList());
  }

  private boolean filterBikeOnlyParkAndRideItineraries(Itinerary itinerary) {
    return !itinerary.legs.stream().allMatch(leg -> leg.getMode() == BICYCLE || leg.getWalkingBike());
  }

}
