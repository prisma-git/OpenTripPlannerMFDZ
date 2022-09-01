package org.opentripplanner.graph_builder.module;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.opentripplanner.graph_builder.model.GraphBuilderModule;
import org.opentripplanner.model.Timetable;
import org.opentripplanner.transit.service.TransitModel;

/**
 * Adjust all scheduled times to match the transit model timezone.
 */
public class TimeZoneAdjusterModule implements GraphBuilderModule {

  private final TransitModel transitModel;

  @Inject
  public TimeZoneAdjusterModule(TransitModel transitModel) {
    this.transitModel = transitModel;
  }

  @Override
  public void buildGraph() {
    // TODO: We assume that all time zones follow the same DST rules. In reality we need to split up
    //  the services for each DST transition
    final Instant serviceStart = transitModel.getTransitServiceStarts().toInstant();
    var graphOffset = Duration.ofSeconds(
      transitModel.getTimeZone().getRules().getOffset(serviceStart).getTotalSeconds()
    );

    Map<ZoneId, Duration> agencyShift = new HashMap<>();

    transitModel
      .getAllTripPatterns()
      .forEach(pattern -> {
        var timeShift = agencyShift.computeIfAbsent(
          pattern.getRoute().getAgency().getTimezone(),
          zoneId ->
            (graphOffset.minusSeconds(zoneId.getRules().getOffset(serviceStart).getTotalSeconds()))
        );

        if (timeShift.isZero()) {
          return;
        }

        final Timetable scheduledTimetable = pattern.getScheduledTimetable();

        scheduledTimetable.getTripTimes().forEach(tripTimes -> tripTimes.timeShift(timeShift));

        scheduledTimetable
          .getFrequencyEntries()
          .forEach(frequencyEntry -> frequencyEntry.tripTimes.timeShift(timeShift));
      });
  }

  @Override
  public void checkInputs() {}
}
