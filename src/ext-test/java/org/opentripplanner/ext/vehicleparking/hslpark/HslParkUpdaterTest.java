package org.opentripplanner.ext.vehicleparking.hslpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Locale;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opentripplanner.model.calendar.openinghours.OpeningHoursCalendarService;
import org.opentripplanner.routing.vehicle_parking.VehicleParkingState;
import org.opentripplanner.transit.model.framework.Deduplicator;

@Disabled
public class HslParkUpdaterTest {

  @Test
  void parseParks() {
    var facilitiesUrl = "file:src/ext-test/resources/vehicleparking/hslpark/facilities.json";
    var utilizationsUrl = "file:src/ext-test/resources/vehicleparking/hslpark/utilizations.json";

    var parameters = new HslParkUpdaterParameters(
      "",
      3000,
      facilitiesUrl,
      "hslpark",
      null,
      30,
      utilizationsUrl
    );
    var openingHoursCalendarService = new OpeningHoursCalendarService(
      new Deduplicator(),
      LocalDate.of(2022, Month.JANUARY, 1),
      LocalDate.of(2023, Month.JANUARY, 1)
    );
    var updater = new HslParkUpdater(
      parameters,
      openingHoursCalendarService,
      ZoneId.of("Europe/Helsinki")
    );

    assertTrue(updater.update());
    var parkingLots = updater.getUpdates();

    assertEquals(4, parkingLots.size());

    var first = parkingLots.get(0);
    assertEquals("Tapiola Park", first.getName().toString());
    assertEquals("hslpark:990", first.getId().toString());
    assertEquals(24.804713028552346, first.getX());
    assertEquals(60.176018858575354, first.getY());
    var entrance = first.getEntrances().get(0);
    assertEquals(24.804713028552346, entrance.getX());
    assertEquals(60.176018858575354, entrance.getY());
    assertTrue(entrance.isCarAccessible());
    assertTrue(entrance.isWalkAccessible());
    assertTrue(first.hasAnyCarPlaces());
    assertFalse(first.hasBicyclePlaces());
    assertFalse(first.hasWheelchairAccessibleCarPlaces());
    assertEquals(1365, first.getCapacity().getCarSpaces());
    assertNull(first.getCapacity().getBicycleSpaces());
    assertNull(first.getCapacity().getWheelchairAccessibleCarSpaces());
    var firstTags = first.getTags();
    assertEquals(7, firstTags.size());
    assertTrue(firstTags.contains("hslpark:SERVICE_COVERED"));
    assertTrue(firstTags.contains("hslpark:AUTHENTICATION_METHOD_HSL_TICKET"));
    assertTrue(firstTags.contains("hslpark:PRICING_METHOD_PAID_10H"));
    assertEquals(VehicleParkingState.OPERATIONAL, first.getState());
    assertTrue(first.hasRealTimeData());
    assertEquals(600, first.getAvailability().getCarSpaces());
    assertNull(first.getAvailability().getBicycleSpaces());
    assertEquals(
      "OHCalendar{" +
      "zoneId: Europe/Helsinki, " +
      "openingHours: [Business days 0:00-23:59:59, " +
      "Saturday 0:00-23:59:59, " +
      "Sunday 0:00-23:59:59]" +
      "}",
      first.getOpeningHours().toString()
    );

    var second = parkingLots.get(1);
    var name = second.getName();
    assertEquals("Kalasatama (Kauppakeskus REDI)", second.getName().toString());
    assertEquals("Kalasatama (Kauppakeskus REDI)", name.toString(new Locale("fi")));
    assertEquals("Fiskhamnen (Köpcenter REDI)", name.toString(new Locale("sv")));
    assertEquals("Kalasatama (Shopping mall REDI)", name.toString(new Locale("en")));
    assertTrue(second.hasAnyCarPlaces());
    assertFalse(second.hasBicyclePlaces());
    assertTrue(second.hasWheelchairAccessibleCarPlaces());
    assertEquals(300, second.getCapacity().getCarSpaces());
    assertEquals(30, second.getCapacity().getWheelchairAccessibleCarSpaces());
    assertNull(second.getCapacity().getBicycleSpaces());
    assertFalse(second.hasRealTimeData());
    assertNull(second.getAvailability());
    assertEquals(
      "OHCalendar{" +
      "zoneId: Europe/Helsinki, " +
      "openingHours: [Business days 6:00-17:30]" +
      "}",
      second.getOpeningHours().toString()
    );

    var third = parkingLots.get(2);
    assertEquals("Alberganpromenadi", third.getName().toString());
    assertFalse(third.hasAnyCarPlaces());
    assertTrue(third.hasBicyclePlaces());
    assertFalse(third.hasWheelchairAccessibleCarPlaces());
    assertNull(third.getCapacity().getCarSpaces());
    assertNull(third.getCapacity().getWheelchairAccessibleCarSpaces());
    assertEquals(80, third.getCapacity().getBicycleSpaces());
    assertTrue(third.hasRealTimeData());
    assertEquals(43, third.getAvailability().getBicycleSpaces());
    assertNull(third.getAvailability().getCarSpaces());

    var fourth = parkingLots.get(3);
    assertEquals(VehicleParkingState.TEMPORARILY_CLOSED, fourth.getState());
    assertEquals(0, fourth.getTags().size());
    assertEquals(
      "OHCalendar{" +
      "zoneId: Europe/Helsinki, " +
      "openingHours: [Saturday 7:00-18:00, " +
      "Business days 7:00-21:00, " +
      "Sunday 12:00-21:00]" +
      "}",
      fourth.getOpeningHours().toString()
    );
  }
}
