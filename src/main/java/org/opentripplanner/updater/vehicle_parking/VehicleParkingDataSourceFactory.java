package org.opentripplanner.updater.vehicle_parking;

import java.time.ZoneId;
import org.opentripplanner.ext.vehicleparking.hslpark.HslParkUpdater;
import org.opentripplanner.ext.vehicleparking.hslpark.HslParkUpdaterParameters;
import org.opentripplanner.ext.vehicleparking.kml.KmlBikeParkDataSource;
import org.opentripplanner.ext.vehicleparking.kml.KmlUpdaterParameters;
import org.opentripplanner.ext.vehicleparking.parkapi.BicycleParkAPIUpdater;
import org.opentripplanner.ext.vehicleparking.parkapi.CarParkAPIUpdater;
import org.opentripplanner.ext.vehicleparking.parkapi.ParkAPIUpdaterParameters;
import org.opentripplanner.model.calendar.openinghours.OpeningHoursCalendarService;
import org.opentripplanner.routing.vehicle_parking.VehicleParking;
import org.opentripplanner.updater.DataSource;

/**
 * Class that can be used to return a custom vehicle parking {@link DataSource}.
 */
public class VehicleParkingDataSourceFactory {

  private VehicleParkingDataSourceFactory() {}

  public static DataSource<VehicleParking> create(
    VehicleParkingUpdaterParameters parameters,
    OpeningHoursCalendarService openingHoursCalendarService,
    ZoneId zoneId
  ) {
    switch (parameters.getSourceType()) {
      case HSL_PARK:
        return new HslParkUpdater(
          (HslParkUpdaterParameters) parameters,
          openingHoursCalendarService,
          zoneId
        );
      case KML:
        return new KmlBikeParkDataSource((KmlUpdaterParameters) parameters);
      case PARK_API:
        return new CarParkAPIUpdater(
          (ParkAPIUpdaterParameters) parameters,
          openingHoursCalendarService,
          zoneId
        );
      case BICYCLE_PARK_API:
        return new BicycleParkAPIUpdater(
          (ParkAPIUpdaterParameters) parameters,
          openingHoursCalendarService,
          zoneId
        );
    }
    throw new IllegalArgumentException(
      "Unknown vehicle parking source type: " + parameters.getSourceType()
    );
  }
}
