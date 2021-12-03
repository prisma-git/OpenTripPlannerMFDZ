package org.opentripplanner.updater.vehicle_parking;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Collection;
import org.opentripplanner.routing.vehicle_parking.VehicleParkingSpaces;

public class CarParkAPIUpdater extends ParkAPIUpdater {

    public CarParkAPIUpdater(String url, String feedId, Collection<String> staticTags) {
        super(url, feedId, staticTags);
    }

    @Override
    protected VehicleParkingSpaces parseCapacity(JsonNode jsonNode) {
        return parseVehicleParkingSpaces(jsonNode, null, "total", "total:disabled");
    }

    @Override
    protected VehicleParkingSpaces parseAvailability(JsonNode jsonNode) {
        return parseVehicleParkingSpaces(jsonNode, null, "free", "free:disabled");
    }
}
