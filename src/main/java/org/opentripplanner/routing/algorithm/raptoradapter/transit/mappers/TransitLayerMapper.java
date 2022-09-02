package org.opentripplanner.routing.algorithm.raptoradapter.transit.mappers;

import static org.opentripplanner.routing.algorithm.raptoradapter.transit.mappers.TransfersMapper.mapTransfers;

import com.google.common.collect.ArrayListMultimap;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentripplanner.model.Timetable;
import org.opentripplanner.routing.algorithm.raptoradapter.transit.Transfer;
import org.opentripplanner.routing.algorithm.raptoradapter.transit.TransitLayer;
import org.opentripplanner.routing.algorithm.raptoradapter.transit.TransitTuningParameters;
import org.opentripplanner.routing.algorithm.raptoradapter.transit.TripPatternForDate;
import org.opentripplanner.routing.algorithm.raptoradapter.transit.constrainedtransfer.ConstrainedTransfersForPatterns;
import org.opentripplanner.routing.algorithm.raptoradapter.transit.constrainedtransfer.TransferIndexGenerator;
import org.opentripplanner.routing.algorithm.raptoradapter.transit.cost.RaptorCostConverter;
import org.opentripplanner.routing.algorithm.raptoradapter.transit.request.RaptorRequestTransferCache;
import org.opentripplanner.transit.model.network.TripPattern;
import org.opentripplanner.transit.model.site.StopTransferPriority;
import org.opentripplanner.transit.model.timetable.TripTimes;
import org.opentripplanner.transit.service.StopModel;
import org.opentripplanner.transit.service.TransitModel;
import org.opentripplanner.util.OTPFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maps the TransitLayer object from the OTP Graph object. The ServiceDay hierarchy is reversed,
 * with service days at the top level, which contains TripPatternForDate objects that contain only
 * TripSchedules running on that particular date. This makes it faster to filter out TripSchedules
 * when doing Range Raptor searches.
 * <p>
 * CONCURRENCY: This mapper run part of the mapping in parallel using parallel streams. This improve
 * startup time on the Norwegian graph by 20 seconds; reducing the this mapper from 36 seconds to 15
 * seconds, and the total startup time from 80 seconds to 60 seconds. (JAN 2020, MacBook Pro, 3.1
 * GHz i7)
 */
public class TransitLayerMapper {

  private static final Logger LOG = LoggerFactory.getLogger(TransitLayerMapper.class);

  private final TransitModel transitModel;

  private TransitLayerMapper(TransitModel transitModel) {
    this.transitModel = transitModel;
  }

  public static TransitLayer map(
    TransitTuningParameters tuningParameters,
    TransitModel transitModel
  ) {
    return new TransitLayerMapper(transitModel).map(tuningParameters);
  }

  // TODO We can save time by either pre-sorting these or use a sorting algorithm that is
  //      optimized for sorting nearly sorted list
  static List<TripTimes> getSortedTripTimes(Timetable timetable) {
    return timetable
      .getTripTimes()
      .stream()
      .sorted(Comparator.comparing(TripTimes::sortIndex))
      .collect(Collectors.toList());
  }

  private TransitLayer map(TransitTuningParameters tuningParameters) {
    HashMap<LocalDate, List<TripPatternForDate>> tripPatternsByStopByDate;
    List<List<Transfer>> transferByStopIndex;
    ConstrainedTransfersForPatterns constrainedTransfers = null;
    StopModel stopModel = transitModel.getStopModel();

    LOG.info("Mapping transitLayer from Graph...");

    Collection<TripPattern> allTripPatterns = transitModel.getAllTripPatterns();

    tripPatternsByStopByDate = mapTripPatterns(allTripPatterns);

    transferByStopIndex = mapTransfers(stopModel, transitModel);

    TransferIndexGenerator transferIndexGenerator = null;
    if (OTPFeature.TransferConstraints.isOn()) {
      transferIndexGenerator =
        new TransferIndexGenerator(transitModel.getTransferService().listAll(), allTripPatterns);
      constrainedTransfers = transferIndexGenerator.generateTransfers();
    }

    var transferCache = new RaptorRequestTransferCache(tuningParameters.transferCacheMaxSize());

    LOG.info("Mapping complete.");

    return new TransitLayer(
      tripPatternsByStopByDate,
      transferByStopIndex,
      transitModel.getTransferService(),
      stopModel,
      transitModel.getTimeZone(),
      transferCache,
      constrainedTransfers,
      transferIndexGenerator,
      createStopTransferCosts(stopModel, tuningParameters)
    );
  }

  /**
   * Map pre-Raptor TripPatterns and Trips to the corresponding Raptor classes.
   * <p>
   * Part of this method runs IN PARALLEL.
   * <p>
   */
  private HashMap<LocalDate, List<TripPatternForDate>> mapTripPatterns(
    Collection<TripPattern> allTripPatterns
  ) {
    TripPatternForDateMapper tripPatternForDateMapper = new TripPatternForDateMapper(
      transitModel.getTransitModelIndex().getServiceCodesRunningForDate()
    );

    Set<LocalDate> allServiceDates = transitModel
      .getTransitModelIndex()
      .getServiceCodesRunningForDate()
      .keySet();

    List<TripPatternForDate> tripPatternForDates = Collections.synchronizedList(new ArrayList<>());

    // THIS CODE RUNS IN PARALLEL
    allServiceDates
      .parallelStream()
      .forEach(serviceDate -> {
        // Create a List to hold the values for this iteration. The results are then added
        // to the common synchronized list at the end.
        List<TripPatternForDate> values = new ArrayList<>();

        // This nested loop could be quite inefficient.
        // Maybe determine in advance which patterns are running on each service and day.
        for (TripPattern oldTripPattern : allTripPatterns) {
          TripPatternForDate tripPatternForDate = tripPatternForDateMapper.map(
            oldTripPattern.getScheduledTimetable(),
            serviceDate
          );
          if (tripPatternForDate != null) {
            values.add(tripPatternForDate);
          }
        }
        if (!values.isEmpty()) {
          tripPatternForDates.addAll(values);
        }
      });
    // END PARALLEL CODE

    return keyByRunningPeriodDates(tripPatternForDates);
  }

  /**
   * Returns a map of TripPatternsForDate objects by their active dates.
   */
  private HashMap<LocalDate, List<TripPatternForDate>> keyByRunningPeriodDates(
    List<TripPatternForDate> tripPatternForDates
  ) {
    // Create multimap by running period dates
    ArrayListMultimap<LocalDate, TripPatternForDate> multiMap = ArrayListMultimap.create();
    for (TripPatternForDate tripPatternForDate : tripPatternForDates) {
      for (LocalDate date : tripPatternForDate.getRunningPeriodDates()) {
        multiMap.put(date, tripPatternForDate);
      }
    }

    // Convert to Map<LocalDate, List<TripPatternForDate>>
    HashMap<LocalDate, List<TripPatternForDate>> result = new HashMap<>();
    for (Map.Entry<LocalDate, Collection<TripPatternForDate>> entry : multiMap.asMap().entrySet()) {
      result.put(entry.getKey(), new ArrayList<>(entry.getValue()));
    }

    return result;
  }

  /**
   * Create static board/alight cost for Raptor to include for each stop.
   */
  static int[] createStopTransferCosts(StopModel stops, TransitTuningParameters tuningParams) {
    if (!tuningParams.enableStopTransferPriority()) {
      return null;
    }
    int[] stopTransferCosts = new int[stops.stopIndexSize()];

    for (int i = 0; i < stops.stopIndexSize(); ++i) {
      StopTransferPriority priority = stops.stopByIndex(i).getPriority();
      int domainCost = tuningParams.stopTransferCost(priority);
      stopTransferCosts[i] = RaptorCostConverter.toRaptorCost(domainCost);
    }
    return stopTransferCosts;
  }
}
