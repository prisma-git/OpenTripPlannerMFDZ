package org.opentripplanner.updater.vehicle_parking;

import org.opentripplanner.routing.vehicle_parking.VehicleParking;
import org.opentripplanner.updater.DataSource;

public class VehicleParkingDataSourceFactory {

  private VehicleParkingDataSourceFactory() {}

  public static DataSource<VehicleParking> create(VehicleParkingUpdaterParameters source) {
    switch (source.getSourceType()) {
      case PARK_API:          return new CarParkAPIUpdater(source.getUrl(), source.getFeedId(), source.getTags());
      case BICYCLE_PARK_API:  return new BicycleParkAPIUpdater(source.getUrl(), source.getFeedId(), source.getTags());
    }
    throw new IllegalArgumentException(
        "Unknown vehicle parking source type: " + source.getSourceType()
    );
  }
}
