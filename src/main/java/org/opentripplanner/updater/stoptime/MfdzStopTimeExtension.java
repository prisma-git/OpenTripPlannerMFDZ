package org.opentripplanner.updater.stoptime;

import static java.util.Optional.empty;

import com.google.transit.realtime.GtfsRealtime;
import de.mfdz.Mfdz;
import java.util.Optional;
import org.opentripplanner.model.PickDrop;

public record MfdzStopTimeExtension(Optional<PickDrop> pickup, Optional<PickDrop> dropOff) {
  static MfdzStopTimeExtension ofStopTime(
    GtfsRealtime.TripUpdate.StopTimeUpdate.StopTimeProperties props
  ) {
    if (props.hasExtension(Mfdz.stopTimeProperties)) {
      var ext = props.getExtension(Mfdz.stopTimeProperties);
      var pickup = ext.getPickupType();
      var dropOff = ext.getDropoffType();
      return new MfdzStopTimeExtension(
        Optional.of(toPickDrop(pickup)),
        Optional.of(toPickDrop(dropOff))
      );
    } else {
      return new MfdzStopTimeExtension(empty(), empty());
    }
  }

  private static PickDrop toPickDrop(Mfdz.MfdzStopTimePropertiesExtension.DropOffPickupType gtfs) {
    return switch (gtfs) {
      case REGULAR -> PickDrop.SCHEDULED;
      case NONE -> PickDrop.NONE;
      case PHONE_AGENCY -> PickDrop.CALL_AGENCY;
      case COORDINATE_WITH_DRIVER -> PickDrop.COORDINATE_WITH_DRIVER;
    };
  }
}
