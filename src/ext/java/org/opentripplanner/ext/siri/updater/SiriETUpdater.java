package org.opentripplanner.ext.siri.updater;

import java.util.List;
import org.apache.commons.lang3.BooleanUtils;
import org.opentripplanner.ext.siri.SiriFuzzyTripMatcher;
import org.opentripplanner.ext.siri.SiriTimetableSnapshotSource;
import org.opentripplanner.transit.service.DefaultTransitService;
import org.opentripplanner.transit.service.TransitModel;
import org.opentripplanner.updater.PollingGraphUpdater;
import org.opentripplanner.updater.WriteToGraphCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.siri.siri20.EstimatedTimetableDeliveryStructure;
import uk.org.siri.siri20.ServiceDelivery;
import uk.org.siri.siri20.Siri;

/**
 * Update OTP stop time tables from some (realtime) source
 * <p>
 * Usage example ('rt' name is an example) in file 'Graph.properties':
 *
 * <pre>
 * rt.type = stop-time-updater
 * rt.frequencySec = 60
 * rt.sourceType = gtfs-http
 * rt.url = http://host.tld/path
 * rt.feedId = TA
 * </pre>
 */
public class SiriETUpdater extends PollingGraphUpdater {

  private static final Logger LOG = LoggerFactory.getLogger(SiriETUpdater.class);
  /**
   * Update streamer
   */
  private final EstimatedTimetableSource updateSource;

  /**
   * Feed id that is used for the trip ids in the TripUpdates
   */
  private final String feedId;
  /**
   * Parent update manager. Is used to execute graph writer runnables.
   */
  protected WriteToGraphCallback saveResultOnGraph;

  /**
   * The place where we'll record the incoming realtime timetables to make them available to the
   * router in a thread safe way.
   */
  private final SiriTimetableSnapshotSource snapshotSource;

  private final SiriFuzzyTripMatcher fuzzyTripMatcher;

  public SiriETUpdater(
    SiriETUpdaterParameters config,
    TransitModel transitModel,
    SiriTimetableSnapshotSource timetableSnapshot
  ) {
    super(config);
    // Create update streamer from preferences
    this.feedId = config.getFeedId();

    this.updateSource = new SiriETHttpTripUpdateSource(config.sourceParameters());

    this.snapshotSource = timetableSnapshot;

    this.blockReadinessUntilInitialized = config.blockReadinessUntilInitialized();
    this.fuzzyTripMatcher = SiriFuzzyTripMatcher.of(new DefaultTransitService(transitModel));

    LOG.info(
      "Creating stop time updater (SIRI ET) running every {} seconds : {}",
      pollingPeriodSeconds(),
      updateSource
    );
  }

  @Override
  public void setGraphUpdaterManager(WriteToGraphCallback saveResultOnGraph) {
    this.saveResultOnGraph = saveResultOnGraph;
  }

  /**
   * Repeatedly makes blocking calls to an UpdateStreamer to retrieve new stop time updates, and
   * applies those updates to the graph.
   */
  @Override
  public void runPolling() {
    boolean moreData = false;
    do {
      Siri updates = updateSource.getUpdates();
      if (updates != null) {
        boolean fullDataset = updateSource.getFullDatasetValueOfLastUpdates();
        ServiceDelivery serviceDelivery = updates.getServiceDelivery();
        // Use isTrue in case isMoreData returns null. Mark this updater as primed after last page of updates.
        // Copy moreData into a final primitive, because the object moreData persists across iterations.
        moreData = BooleanUtils.isTrue(serviceDelivery.isMoreData());
        final boolean markPrimed = !moreData;
        List<EstimatedTimetableDeliveryStructure> etds = serviceDelivery.getEstimatedTimetableDeliveries();
        if (etds != null) {
          saveResultOnGraph.execute((graph, transitModel) -> {
            snapshotSource.applyEstimatedTimetable(
              transitModel,
              fuzzyTripMatcher,
              feedId,
              fullDataset,
              etds
            );
            if (markPrimed) primed = true;
          });
        }
      }
    } while (moreData);
  }

  public String toString() {
    String s = (updateSource == null) ? "NONE" : updateSource.toString();
    return "Polling SIRI ET updater with update source = " + s;
  }
}
