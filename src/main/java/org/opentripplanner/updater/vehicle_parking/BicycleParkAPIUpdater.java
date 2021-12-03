package org.opentripplanner.updater.vehicle_parking;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Collection;
import org.opentripplanner.routing.vehicle_parking.VehicleParkingSpaces;

public class BicycleParkAPIUpdater extends ParkAPIUpdater {

    public BicycleParkAPIUpdater(String url, String feedId, Collection<String> staticTags) {
        super(url, feedId, staticTags);
    }

    @Override
    protected VehicleParkingSpaces parseCapacity(JsonNode jsonNode) {
        return parseVehicleParkingSpaces(jsonNode, "total", null, null);
    }

    @Override
    protected VehicleParkingSpaces parseAvailability(JsonNode jsonNode) {
        return parseVehicleParkingSpaces(jsonNode, "free", null, null);
    }
}
