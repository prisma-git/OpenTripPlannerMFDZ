package org.opentripplanner.routing.api.request;

import java.util.function.DoubleFunction;
import org.opentripplanner.ext.accessibilityscore.AccessibilityScoreFilter;
import org.opentripplanner.routing.algorithm.filterchain.ItineraryListFilterChainBuilder;
import org.opentripplanner.routing.algorithm.filterchain.api.TransitGeneralizedCostFilterParams;

/**
 * Group by Similarity filter parameters
 */
public class ItineraryFilterParameters {

  /**
   * Switch on to return all itineraries and mark filtered itineraries as deleted.
   */
  public boolean debug;

  /**
   * Keep ONE itinerary for each group with at least this part of the legs in common. Default value
   * is 0.85 (85%), use a value less than 0.50 to turn off.
   *
   * @see ItineraryListFilterChainBuilder#addGroupBySimilarity(org.opentripplanner.routing.algorithm.filterchain.GroupBySimilarity)
   */
  public double groupSimilarityKeepOne;

  /**
   * Keep maximum THREE itineraries for each group with at least this part of the legs in common.
   * Default value is 0.68 (68%), use a value less than 0.50 to turn off.
   */
  public double groupSimilarityKeepThree;

  /**
   * Of the itineraries grouped to maximum of three itineraries, how much worse can the non-grouped
   * legs be compared to the lowest cost. 2.0 means that they can be double the cost, and any
   * itineraries having a higher cost will be filtered. Default value is 2.0, use a value lower than
   * 1.0 to turn off
   */
  public double groupedOtherThanSameLegsMaxCostMultiplier;

  /**
   * A relative maximum limit for the generalized cost for transit itineraries. The limit is a
   * linear function of the generalized-cost of an itinerary.
   * <p>
   * Transit itineraries with a cost higher than the value produced by this function plus wait cost
   * at the beginning or end multiplied by {@link RoutingRequest#waitAtBeginningFactor} for any
   * other itinerary are dropped from the result set. Non-transit itineraries is excluded from the
   * filter.
   * <ul>
   * <li>To set a filter to be 1 hours plus 2 times the lowest cost use:
   * {@code 3600 + 2.0 x}
   * <li>To set an absolute value(3000) use: {@code 3000 + 0x}
   * </ul>
   * The default is {@code 900 + 1.5x} - 15 minutes plus 1.5 times the lowest cost.
   */
  public TransitGeneralizedCostFilterParams transitGeneralizedCostLimit;

  /**
   * This is used to filter out bike rental itineraries that contain mostly walking. The value
   * describes the ratio of the total itinerary that has to consist of bike rental to allow the
   * itinerary.
   * <p>
   * Default value is off (0). If you want a minimum of 30% cycling, use a value of 0.3.
   */
  public double bikeRentalDistanceRatio;

  /**
   * This is used to filter out park and ride itineraries that contain only driving plus a very long
   * walk. The value describes the ratio of the total itinerary duration that has to consist of
   * driving to allow the itinerary.
   * <p>
   * Default value is 0.3 (30%), use a value of 0 to turn off.
   */
  public double parkAndRideDurationRatio;

  public boolean flexOnlyToDestination;

  /**
   * This is a a bit similar to {@link #transitGeneralizedCostLimit}, with a few important
   * differences.
   * <p>
   * This function is used to compute a max-limit for generalized-cost. The limit is applied to
   * itineraries with no transit legs, however ALL itineraries (including those with transit legs)
   * are considered when calculating the minimum cost.
   * <p>
   * The smallest generalized-cost value is used as input to the function. For example if the
   * function is {@code f(x) = 1800 + 2.0 x} and the smallest cost is {@code 5000}, then all
   * non-transit itineraries with a cost larger than {@code 1800 + 2 * 5000 = 11 800} is dropped.
   * <p>
   * The default is {@code 3600 + 2x} - 1 hours plus 2 times the lowest cost.
   */
  public DoubleFunction<Double> nonTransitGeneralizedCostLimit;

  /**
   * This is used to filter out journeys that have either same first or last trip. If two journeys
   * starts or ends with exactly same transit leg (same trip id and same service day), one of them
   * will be filtered out.
   */
  public boolean filterItinerariesWithSameFirstOrLastTrip;

  /**
   * Whether to compute the sandbox accessibility score currently being tested at IBI.
   * <p>
   * {@link AccessibilityScoreFilter}
   */
  public boolean accessibilityScore;

  /**
   * Minimum biking distance at the beginning of the itinerary.
   */
  public final double minBikeParkingDistance;

  /**
   * Whether to remove timeshifted "duplicate" itineraries from the search results so that you get a
   * greater variety of results rather than the same ones at different times.
   */
  public boolean removeItinerariesWithSameRoutesAndStops;

  private ItineraryFilterParameters() {
    this.debug = false;
    this.groupSimilarityKeepOne = 0.85;
    this.groupSimilarityKeepThree = 0.68;
    this.groupedOtherThanSameLegsMaxCostMultiplier = 2.0;
    this.bikeRentalDistanceRatio = 0.0;
    this.parkAndRideDurationRatio = 0.0;
    this.minBikeParkingDistance = -1;
    this.flexOnlyToDestination = false;
    this.transitGeneralizedCostLimit =
      new TransitGeneralizedCostFilterParams(RequestFunctions.createLinearFunction(900, 1.5), 0.4);
    this.nonTransitGeneralizedCostLimit = RequestFunctions.createLinearFunction(3600, 2);
    this.filterItinerariesWithSameFirstOrLastTrip = false;
    this.accessibilityScore = false;
    this.removeItinerariesWithSameRoutesAndStops = false;
  }

  public ItineraryFilterParameters(ItineraryFilterParameters i) {
    this.debug = i.debug;
    this.groupSimilarityKeepOne = i.groupSimilarityKeepOne;
    this.groupSimilarityKeepThree = i.groupSimilarityKeepThree;
    this.groupedOtherThanSameLegsMaxCostMultiplier = i.groupedOtherThanSameLegsMaxCostMultiplier;
    this.bikeRentalDistanceRatio = i.bikeRentalDistanceRatio;
    this.parkAndRideDurationRatio = i.parkAndRideDurationRatio;
    this.transitGeneralizedCostLimit = i.transitGeneralizedCostLimit;
    this.nonTransitGeneralizedCostLimit = i.nonTransitGeneralizedCostLimit;
    this.filterItinerariesWithSameFirstOrLastTrip = i.filterItinerariesWithSameFirstOrLastTrip;
    this.accessibilityScore = i.accessibilityScore;
    this.minBikeParkingDistance = i.minBikeParkingDistance;
    this.flexOnlyToDestination = i.flexOnlyToDestination;
  }

  public ItineraryFilterParameters(
    boolean debug,
    double groupSimilarityKeepOne,
    double groupSimilarityKeepThree,
    double groupedOtherThanSameLegsMaxCostMultiplier,
    TransitGeneralizedCostFilterParams transitGeneralizedCostLimit,
    DoubleFunction<Double> nonTransitGeneralizedCostLimit,
    double bikeRentalDistanceRatio,
    double parkAndRideDurationRatio,
    boolean filterItinerariesWithSameFirstOrLastTrip,
    boolean accessibilityScore,
    boolean flexOnlyToDestination,
    double minBikeParkingDistance,
    boolean removeItinerariesWithSameRoutesAndStops
  ) {
    this.debug = debug;
    this.groupSimilarityKeepOne = groupSimilarityKeepOne;
    this.groupSimilarityKeepThree = groupSimilarityKeepThree;
    this.groupedOtherThanSameLegsMaxCostMultiplier = groupedOtherThanSameLegsMaxCostMultiplier;
    this.transitGeneralizedCostLimit = transitGeneralizedCostLimit;
    this.nonTransitGeneralizedCostLimit = nonTransitGeneralizedCostLimit;
    this.bikeRentalDistanceRatio = bikeRentalDistanceRatio;
    this.filterItinerariesWithSameFirstOrLastTrip = filterItinerariesWithSameFirstOrLastTrip;
    this.parkAndRideDurationRatio = parkAndRideDurationRatio;
    this.flexOnlyToDestination = flexOnlyToDestination;
    this.minBikeParkingDistance = minBikeParkingDistance;
    this.accessibilityScore = accessibilityScore;
    this.removeItinerariesWithSameRoutesAndStops = removeItinerariesWithSameRoutesAndStops;
  }

  public static ItineraryFilterParameters createDefault() {
    return new ItineraryFilterParameters();
  }
}
