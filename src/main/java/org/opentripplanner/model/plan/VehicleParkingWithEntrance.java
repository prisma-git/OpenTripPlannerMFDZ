package org.opentripplanner.model.plan;

import org.opentripplanner.routing.vehicle_parking.VehicleParking;
import org.opentripplanner.routing.vehicle_parking.VehicleParkingEntrance;

public class VehicleParkingWithEntrance {

  private final VehicleParking vehicleParking;

  private final VehicleParkingEntrance entrance;

  /**
   * Was realtime data used when parking at this VehicleParking.
   */
  private final boolean realtime;

  private final boolean closesSoon;

  VehicleParkingWithEntrance(
    VehicleParking vehicleParking,
    VehicleParkingEntrance entrance,
    boolean realtime,
    boolean closesSoon
  ) {
    this.vehicleParking = vehicleParking;
    this.entrance = entrance;
    this.realtime = realtime;
    this.closesSoon = closesSoon;
  }

  public static VehicleParkingWithEntranceBuilder builder() {
    return new VehicleParkingWithEntranceBuilder();
  }

  public VehicleParking getVehicleParking() {
    return this.vehicleParking;
  }

  public VehicleParkingEntrance getEntrance() {
    return this.entrance;
  }

  public boolean isRealtime() {
    return realtime;
  }

  public static class VehicleParkingWithEntranceBuilder {

    private VehicleParking vehicleParking;
    private VehicleParkingEntrance entrance;
    private boolean realtime;
    private boolean closesSoon;

    VehicleParkingWithEntranceBuilder() {}

    public VehicleParkingWithEntranceBuilder vehicleParking(VehicleParking vehicleParking) {
      this.vehicleParking = vehicleParking;
      return this;
    }

    public VehicleParkingWithEntranceBuilder entrance(VehicleParkingEntrance entrance) {
      this.entrance = entrance;
      return this;
    }

    public VehicleParkingWithEntranceBuilder realtime(boolean realtime) {
      this.realtime = realtime;
      return this;
    }

    public VehicleParkingWithEntranceBuilder closesSoon(boolean closesSoon) {
      this.closesSoon = closesSoon;
      return this;
    }

    public VehicleParkingWithEntrance build() {
      return new VehicleParkingWithEntrance(vehicleParking, entrance, realtime, closesSoon);
    }
  }
}
