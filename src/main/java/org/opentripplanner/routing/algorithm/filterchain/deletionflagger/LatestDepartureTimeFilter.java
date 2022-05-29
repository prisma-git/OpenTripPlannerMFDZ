package org.opentripplanner.routing.algorithm.filterchain.deletionflagger;

import java.time.Instant;
import java.util.List;
import java.util.function.Predicate;
import org.opentripplanner.model.plan.Itinerary;

public class LatestDepartureTimeFilter implements ItineraryDeletionFlagger {

  public static final String TAG = "latest-departure-time-limit";

  private final Instant limit;

  public LatestDepartureTimeFilter(Instant latestDepartureTime) {
    this.limit = latestDepartureTime;
  }

  @Override
  public String name() {
    return TAG;
  }

  @Override
  public List<Itinerary> getFlaggedItineraries(List<Itinerary> itineraries) {
    var flagged = itineraries.stream().filter(predicate()).toList();
    // if all are flagged, don't do it as you will have no results left
    if (flagged.size() == itineraries.size()) {
      return List.of();
    } else {
      return flagged;
    }
  }

  @Override
  public Predicate<Itinerary> predicate() {
    return it -> it.startTime().toInstant().isAfter(limit);
  }

  @Override
  public boolean skipAlreadyFlaggedItineraries() {
    return false;
  }
}
