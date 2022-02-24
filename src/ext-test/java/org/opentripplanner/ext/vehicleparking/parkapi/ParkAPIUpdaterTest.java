package org.opentripplanner.ext.vehicleparking.parkapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.poole.openinghoursparser.OpeningHoursParseException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opentripplanner.routing.core.OsmOpeningHours;

public class ParkAPIUpdaterTest {

    @Test
    void parseCars() throws OpeningHoursParseException {
        var url = "file:src/ext-test/resources/vehicleparking/parkapi/parkapi-reutlingen.json";

        var parameters =
                new ParkAPIUpdaterParameters("", url, "park-api", 30, null, List.of(), null);
        var updater = new CarParkAPIUpdater(parameters);

        assertTrue(updater.update());
        var parkingLots = updater.getUpdates();

        assertEquals(30, parkingLots.size());

        var first = parkingLots.get(0);
        assertEquals("Parkplatz Alenberghalle", first.getName().toString());
        assertEquals(OsmOpeningHours.parseFromOsm("Mo-Su 00:00-24:00; PH 00:00-24:00"), first.getOpeningHours());

        assertTrue(first.hasAnyCarPlaces());
        assertNull(first.getCapacity());

        var last = parkingLots.get(29);
        assertEquals("Zehntscheuer Kegelgraben", last.getName().toString());
        assertTrue(last.hasAnyCarPlaces());
        assertTrue(last.hasWheelchairAccessibleCarPlaces());
        assertEquals(1, last.getCapacity().getWheelchairAccessibleCarSpaces());
    }

}
