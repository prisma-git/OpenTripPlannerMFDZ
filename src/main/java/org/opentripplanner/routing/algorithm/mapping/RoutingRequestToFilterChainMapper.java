package org.opentripplanner.routing.algorithm.mapping;

import java.time.Instant;
import java.util.function.Consumer;
import org.opentripplanner.model.plan.Itinerary;
import org.opentripplanner.routing.algorithm.filterchain.GroupBySimilarity;
import org.opentripplanner.routing.algorithm.filterchain.ItineraryListFilterChain;
import org.opentripplanner.routing.algorithm.filterchain.ItineraryListFilterChainBuilder;
import org.opentripplanner.routing.api.request.RoutingRequest;

import java.time.Instant;
import java.util.function.Consumer;
import org.opentripplanner.routing.api.request.StreetMode;

public class RoutingRequestToFilterChainMapper {
  /** Filter itineraries down to this limit, but not below. */
  private static final int KEEP_THREE = 3;

  /** Never return more that this limit of itineraries. */
  private static final int MAX_NUMBER_OF_ITINERARIES = 200;

  public static ItineraryListFilterChain createFilterChain(
      RoutingRequest request,
      Instant filterOnLatestDepartureTime,
      boolean removeWalkAllTheWayResults,
      boolean reverseFilteringDirection,
      Consumer<Itinerary> maxLimitReachedSubscriber
  ) {
    var builder = new ItineraryListFilterChainBuilder(request.arriveBy);
    var p = request.itineraryFilters;

    // Group by similar legs filter
    if (p.groupSimilarityKeepOne >= 0.5) {
      builder.addGroupBySimilarity(
          GroupBySimilarity.createWithOneItineraryPerGroup(p.groupSimilarityKeepOne)
      );
    }

    if (p.groupSimilarityKeepThree >= 0.5) {
      builder.addGroupBySimilarity(
        GroupBySimilarity.createWithMoreThanOneItineraryPerGroup(
            p.groupSimilarityKeepThree,
            KEEP_THREE,
            true,
            p.groupedOtherThanSameLegsMaxCostMultiplier
        )
      );
    }

    if (request.modes.contains(StreetMode.BIKE_TO_PARK)) {
      builder.withMinBikeParkingDistance(p.minBikeParkingDistance);
    }

    if (request.modes.contains(StreetMode.CAR_TO_PARK) && request.modes.directMode == StreetMode.BIKE) {
      builder.withRemoveBikeOnlyParkAndRideItineraries(true);
    }

    var flexWasRequested = request.modes.egressMode == StreetMode.FLEXIBLE ||
            request.modes.directMode == StreetMode.FLEXIBLE;
    builder
        .withMaxNumberOfItineraries(Math.min(request.numItineraries, MAX_NUMBER_OF_ITINERARIES))
        .withTransitGeneralizedCostLimit(p.transitGeneralizedCostLimit)
        .withBikeRentalDistanceRatio(p.bikeRentalDistanceRatio)
        .withParkAndRideDurationRatio(p.parkAndRideDurationRatio)
        .withNonTransitGeneralizedCostLimit(p.nonTransitGeneralizedCostLimit)
        .withRemoveTransitWithHigherCostThanBestOnStreetOnly(true)
        .withLatestDepartureTimeLimit(filterOnLatestDepartureTime)
        .withMaxLimitReachedSubscriber(maxLimitReachedSubscriber)
        .withRemoveWalkAllTheWayResults(removeWalkAllTheWayResults)
        .withReverseFilteringDirection(reverseFilteringDirection)
        .withFlexOnlyToDestination(flexWasRequested && p.flexOnlyToDestination)
        .withDebugEnabled(p.debug);

    return builder.build();
  }
}
