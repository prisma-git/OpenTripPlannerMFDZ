package org.opentripplanner.ext.vehicleparking.parkapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.poole.openinghoursparser.OpeningHoursParseException;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opentripplanner.model.calendar.openinghours.OpeningHoursCalendarService;
import org.opentripplanner.routing.core.OsmOpeningHours;
import org.opentripplanner.transit.model.framework.Deduplicator;

public class ParkAPIUpdaterTest {

  @Test
  void parseCars() throws OpeningHoursParseException {
    var url = "file:src/ext-test/resources/vehicleparking/parkapi/parkapi-reutlingen.json";

    var parameters = new ParkAPIUpdaterParameters("", url, "park-api", 30, null, List.of(), null);
    var openingHoursCalendarService = new OpeningHoursCalendarService(
      new Deduplicator(),
      LocalDate.of(2022, Month.JANUARY, 1),
      LocalDate.of(2023, Month.JANUARY, 1)
    );
    var updater = new CarParkAPIUpdater(
      parameters,
      openingHoursCalendarService,
      ZoneId.of("Europe/Berlin")
    );

    assertTrue(updater.update());
    var parkingLots = updater.getUpdates();

    assertEquals(30, parkingLots.size());

    var first = parkingLots.get(0);
    assertEquals("Parkplatz Alenberghalle", first.getName().toString());
    assertEquals(
      OsmOpeningHours.parseFromOsm("Mo-Su 00:00-24:00; PH 00:00-24:00"),
      first.getOpeningHours()
    );

    assertEquals(
      "OHCalendar{zoneId: Europe/Berlin, openingHours: [Mo-Su 0:00-23:59:59]}",
      first.getOpeningHours().toString()
    );
    assertTrue(first.hasAnyCarPlaces());
    assertNull(first.getCapacity());

    var last = parkingLots.get(29);
    assertEquals("Zehntscheuer Kegelgraben", last.getName().toString());
    assertNull(last.getOpeningHours());
    assertTrue(last.hasAnyCarPlaces());
    assertTrue(last.hasWheelchairAccessibleCarPlaces());
    assertEquals(1, last.getCapacity().getWheelchairAccessibleCarSpaces());
  }

  @Test
  void parseLudwigsburg() {
    var url = "file:src/ext-test/resources/vehicleparking/parkapi/ludwigsburg.json";

    var parameters = new ParkAPIUpdaterParameters("", url, "park-api", 30, null, List.of(), null);
    var updater = new CarParkAPIUpdater(parameters);

    assertTrue(updater.update());
    var parkingLots = updater.getUpdates();

    assertEquals(18, parkingLots.size());

    var first = parkingLots.get(0);
    assertEquals("Forum/Blühendes Barock Ost", first.getName().toString());
    assertEquals(
      "Su 00:00-23:59; Mo 00:00-23:59; Tu 00:00-23:59; We 00:00-23:59; Th 00:00-23:59; Fr 00:00-23:59; Sa 00:00-23:59",
      first.getOpeningHours().toString()
    );

    assertTrue(first.hasAnyCarPlaces());
    assertEquals(158, first.getCapacity().getCarSpaces());
  }

  @Test
  void acrossMidnight() {
    var url = "file:src/ext-test/resources/vehicleparking/parkapi/ludwigsburg-across-midnight.json";

    var parameters = new ParkAPIUpdaterParameters("", url, "park-api", 30, null, List.of(), null);
    var updater = new CarParkAPIUpdater(parameters);

    assertTrue(updater.update());
    var parkingLots = updater.getUpdates();

    assertEquals(1, parkingLots.size());

    var first = parkingLots.get(0);
    assertEquals("Lotter", first.getName().toString());
    assertEquals(
      "Su 06:00-02:00, Mo 06:00-02:00, Tu 06:00-02:00, We 06:00-02:00, Th 06:00-02:00, Fr 06:00-02:00, Sa 06:00-02:00",
      first.getOpeningHours().toString()
    );

    assertTrue(first.hasAnyCarPlaces());
    assertEquals(82, first.getCapacity().getCarSpaces());
  }
}
