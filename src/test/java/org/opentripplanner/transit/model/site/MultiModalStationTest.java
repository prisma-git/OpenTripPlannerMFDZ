package org.opentripplanner.transit.model.site;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opentripplanner.transit.model._data.TransitModelForTest;
import org.opentripplanner.transit.model.basic.I18NString;
import org.opentripplanner.transit.model.basic.NonLocalizedString;

class MultiModalStationTest {

  private static final String ID = "1";
  private static final I18NString NAME = new NonLocalizedString("name");

  private static final Station STATION_1 = TransitModelForTest.station("1:1").build();
  private static final Station STATION_2 = TransitModelForTest.station("1:2").build();

  public static final Set<Station> CHILD_STATIONS = Set.of(STATION_1, STATION_2);
  private static final MultiModalStation subject = MultiModalStation
    .of(TransitModelForTest.id(ID))
    .withName(NAME)
    .withChildStations(CHILD_STATIONS)
    .build();

  @Test
  void copy() {
    assertEquals(ID, subject.getId().getId());

    // Make a copy, and set the same name (nothing is changed)
    var copy = subject.copy().withName(NAME).build();

    assertSame(subject, copy);

    // Copy and change name
    copy = subject.copy().withName(new NonLocalizedString("v2")).build();

    // The two objects are not the same instance, but are equal(same id)
    assertNotSame(subject, copy);
    assertEquals(subject, copy);

    assertEquals(ID, copy.getId().getId());
    assertTrue(copy.getChildStations().containsAll(CHILD_STATIONS));
    assertEquals("v2", copy.getName().toString());
  }

  @Test
  void sameAs() {
    assertTrue(subject.sameAs(subject.copy().build()));
    assertFalse(subject.sameAs(subject.copy().withId(TransitModelForTest.id("X")).build()));
    assertFalse(subject.sameAs(subject.copy().withName(new NonLocalizedString("X")).build()));
    assertFalse(subject.sameAs(subject.copy().withChildStations(Set.of(STATION_1)).build()));
  }
}
