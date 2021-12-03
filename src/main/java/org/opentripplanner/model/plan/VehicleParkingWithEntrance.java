package org.opentripplanner.model.plan;

import org.opentripplanner.routing.vehicle_parking.VehicleParking;
import org.opentripplanner.routing.vehicle_parking.VehicleParkingEntrance;

public class VehicleParkingWithEntrance {

    private final VehicleParking vehicleParking;

    private final VehicleParkingEntrance entrance;

    private final boolean closesSoon;

    VehicleParkingWithEntrance(
            VehicleParking vehicleParking,
            VehicleParkingEntrance entrance,
            boolean closesSoon
    ) {
        this.vehicleParking = vehicleParking;
        this.entrance = entrance;
        this.closesSoon = closesSoon;
    }

    public VehicleParking getVehicleParking() {
        return this.vehicleParking;
    }

    public VehicleParkingEntrance getEntrance() {
        return this.entrance;
    }

    public static VehicleParkingWithEntranceBuilder builder() {
        return new VehicleParkingWithEntranceBuilder();
    }

    public static class VehicleParkingWithEntranceBuilder {

        private VehicleParking vehicleParking;
        private VehicleParkingEntrance entrance;
        private boolean closesSoon;

        VehicleParkingWithEntranceBuilder() {}

        public VehicleParkingWithEntranceBuilder vehicleParking(
                VehicleParking vehicleParking
        ) {
            this.vehicleParking = vehicleParking;
            return this;
        }

        public VehicleParkingWithEntranceBuilder entrance(
                VehicleParkingEntrance entrance
        ) {
            this.entrance = entrance;
            return this;
        }

        public VehicleParkingWithEntranceBuilder closesSoon(
                boolean closesSoon
        ) {
            this.closesSoon = closesSoon;
            return this;
        }

        public VehicleParkingWithEntrance build() {
            return new VehicleParkingWithEntrance(vehicleParking, entrance, closesSoon);
        }
    }
}
