package org.opentripplanner.updater.stoptime;

import static java.util.Optional.empty;

import com.google.transit.realtime.GtfsRealtime;
import de.mfdz.Mfdz;
import java.util.Optional;

public record MfdzTripExtension(Optional<String> routeUrl, Optional<String> agencyId) {

  static MfdzTripExtension ofTripDescriptor(GtfsRealtime.TripDescriptor tripDescriptor) {
    if(tripDescriptor.hasExtension(Mfdz.tripDescriptor)){
      var ext = tripDescriptor.getExtension(Mfdz.tripDescriptor);
      var url = Optional.of(ext.getRouteUrl());
      var agencyId = Optional.of(ext.getAgencyId());
      return new MfdzTripExtension(url, agencyId);
    } else {
      return new MfdzTripExtension(empty(), empty());
    }
  }

}
