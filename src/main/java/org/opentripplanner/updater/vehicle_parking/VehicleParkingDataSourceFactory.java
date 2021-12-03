package org.opentripplanner.updater.vehicle_parking;

public class VehicleParkingDataSourceFactory {

  private VehicleParkingDataSourceFactory() {}

  public static VehicleParkingDataSource create(VehicleParkingUpdaterParameters source) {
    switch (source.getSourceType()) {
      case PARK_API:          return new CarParkAPIUpdater(source.getUrl(), source.getFeedId(), source.getTags());
      case BICYCLE_PARK_API:  return new BicycleParkAPIUpdater(source.getUrl(), source.getFeedId(), source.getTags());
    }
    throw new IllegalArgumentException(
        "Unknown vehicle parking source type: " + source.getSourceType()
    );
  }
}
