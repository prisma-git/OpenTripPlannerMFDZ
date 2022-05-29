package org.opentripplanner.routing.algorithm.mapping;

import java.time.Duration;
import java.time.Instant;
import java.time.Instant;
import java.util.function.Consumer;
import java.util.function.Consumer;
import org.opentripplanner.model.plan.Itinerary;
import org.opentripplanner.model.plan.SortOrder;
import org.opentripplanner.routing.algorithm.filterchain.GroupBySimilarity;
import org.opentripplanner.routing.algorithm.filterchain.ItineraryListFilterChain;
import org.opentripplanner.routing.algorithm.filterchain.ItineraryListFilterChainBuilder;
import org.opentripplanner.routing.algorithm.filterchain.ListSection;
import org.opentripplanner.routing.api.request.ItineraryFilterParameters;
import org.opentripplanner.routing.api.request.RequestModes;
import org.opentripplanner.routing.api.request.StreetMode;

public class RoutingRequestToFilterChainMapper {

  /** Filter itineraries down to this limit, but not below. */
  private static final int KEEP_THREE = 3;

  /** Never return more that this limit of itineraries. */
  private static final int MAX_NUMBER_OF_ITINERARIES = 200;

  public static ItineraryListFilterChain createFilterChain(
    SortOrder sortOrder,
    ItineraryFilterParameters params,
    int maxNumOfItineraries,
    Instant filterOnLatestDepartureTime,
    boolean removeWalkAllTheWayResults,
    boolean maxNumberOfItinerariesCropHead,
    RequestModes modes,
    Consumer<Itinerary> maxLimitReachedSubscriber,
    boolean wheelchairAccessible
  ) {
    var builder = new ItineraryListFilterChainBuilder(sortOrder);

    // Group by similar legs filter
    if (params.groupSimilarityKeepOne >= 0.5) {
      builder.addGroupBySimilarity(
        GroupBySimilarity.createWithOneItineraryPerGroup(params.groupSimilarityKeepOne)
      );
    }

    if (params.groupSimilarityKeepThree >= 0.5) {
      builder.addGroupBySimilarity(
        GroupBySimilarity.createWithMoreThanOneItineraryPerGroup(
          params.groupSimilarityKeepThree,
          KEEP_THREE,
          true,
          params.groupedOtherThanSameLegsMaxCostMultiplier
        )
      );
    }

    if (maxNumberOfItinerariesCropHead) {
      builder.withMaxNumberOfItinerariesCrop(ListSection.HEAD);
    }

    if (modes.contains(StreetMode.BIKE_TO_PARK)) {
      builder.withMinBikeParkingDistance(params.minBikeParkingDistance);
    }

    if (modes.contains(StreetMode.CAR_TO_PARK) && modes.directMode == StreetMode.BIKE) {
      builder.withRemoveBikeOnlyParkAndRideItineraries(true);
    }

    var flexWasRequested =
      modes.egressMode == StreetMode.FLEXIBLE || modes.directMode == StreetMode.FLEXIBLE;
    builder
      .withMaxNumberOfItineraries(Math.min(maxNumOfItineraries, MAX_NUMBER_OF_ITINERARIES))
      .withTransitGeneralizedCostLimit(params.transitGeneralizedCostLimit)
      .withBikeRentalDistanceRatio(params.bikeRentalDistanceRatio)
      .withParkAndRideDurationRatio(params.parkAndRideDurationRatio)
      .withNonTransitGeneralizedCostLimit(params.nonTransitGeneralizedCostLimit)
      .withSameFirstOrLastTripFilter(params.filterItinerariesWithSameFirstOrLastTrip)
      .withAccessibilityScore(params.accessibilityScore && wheelchairAccessible)
      .withRemoveTransitWithHigherCostThanBestOnStreetOnly(true)
      .withLatestDepartureTimeLimit(filterOnLatestDepartureTime)
      .withMaxLimitReachedSubscriber(maxLimitReachedSubscriber)
      .withRemoveWalkAllTheWayResults(removeWalkAllTheWayResults)
      .withFlexOnlyToDestination(flexWasRequested && params.flexOnlyToDestination)
      .withDebugEnabled(params.debug);

    return builder.build();
  }
}
