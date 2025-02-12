package org.opentripplanner.updater.stoptime;

import static java.util.Optional.empty;

import com.google.transit.realtime.GtfsRealtime;
import de.mfdz.Mfdz;
import java.util.Optional;

public record MfdzTripExtension(
  Optional<String> routeUrl,
  Optional<String> agencyId,
  Optional<Integer> routeType,
  Optional<String> routeLongName
) {
  static MfdzTripExtension ofTripDescriptor(GtfsRealtime.TripDescriptor tripDescriptor) {
    if (tripDescriptor.hasExtension(Mfdz.tripDescriptor)) {
      var ext = tripDescriptor.getExtension(Mfdz.tripDescriptor);
      var url = Optional.of(ext.getRouteUrl());
      var agencyId = Optional.of(ext.getAgencyId());
      var routeType = Optional.of(ext.getRouteType());
      var routeName = Optional.of(ext.getRouteLongName());
      return new MfdzTripExtension(url, agencyId, routeType, routeName);
    } else {
      return new MfdzTripExtension(empty(), empty(), empty(), empty());
    }
  }
}
