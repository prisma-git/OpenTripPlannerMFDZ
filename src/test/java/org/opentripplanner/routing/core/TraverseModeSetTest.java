package org.opentripplanner.routing.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TraverseModeSetTest {

  @Test
  public void testCarMode() {
    TraverseModeSet modeSet = new TraverseModeSet(TraverseMode.CAR);

    assertTrue(modeSet.getCar());
    assertFalse(modeSet.isTransit());
    assertFalse(modeSet.getRail());
    assertFalse(modeSet.getTram());
    assertFalse(modeSet.getSubway());
    assertFalse(modeSet.getFunicular());
    assertFalse(modeSet.getGondola());
    assertFalse(modeSet.getCarpool());
    assertFalse(modeSet.getWalk());
    assertFalse(modeSet.getBicycle());
    assertFalse(modeSet.getTrolleyBus());
  }

  @Test
  public void testWalkMode() {
    TraverseModeSet modeSet = new TraverseModeSet(TraverseMode.WALK);

    assertTrue(modeSet.getWalk());
    assertFalse(modeSet.getCar());
    assertFalse(modeSet.isTransit());
    assertFalse(modeSet.getRail());
    assertFalse(modeSet.getTram());
    assertFalse(modeSet.getSubway());
    assertFalse(modeSet.getFunicular());
    assertFalse(modeSet.getGondola());
    assertFalse(modeSet.getCarpool());
    assertFalse(modeSet.getBicycle());
    assertFalse(modeSet.getTrolleyBus());
  }

  @Test
  public void testBikeMode() {
    TraverseModeSet modeSet = new TraverseModeSet(TraverseMode.BICYCLE);

    assertTrue(modeSet.getBicycle());
    assertFalse(modeSet.getWalk());
    assertFalse(modeSet.getCar());
    assertFalse(modeSet.isTransit());
    assertFalse(modeSet.getRail());
    assertFalse(modeSet.getTram());
    assertFalse(modeSet.getSubway());
    assertFalse(modeSet.getFunicular());
    assertFalse(modeSet.getGondola());
    assertFalse(modeSet.getCarpool());
    assertFalse(modeSet.getWalk());
    assertFalse(modeSet.getTrolleyBus());
  }

  @Test
  public void testTrolleyMode() {
    TraverseModeSet modeSet = new TraverseModeSet(TraverseMode.TROLLEYBUS);

    assertFalse(modeSet.getBicycle());
    assertFalse(modeSet.getWalk());
    assertFalse(modeSet.getCar());
    assertTrue(modeSet.isTransit());
    assertFalse(modeSet.getRail());
    assertFalse(modeSet.getTram());
    assertFalse(modeSet.getSubway());
    assertFalse(modeSet.getFunicular());
    assertFalse(modeSet.getGondola());
    assertFalse(modeSet.getWalk());
    assertTrue(modeSet.getTrolleyBus());
  }

  @Test
  public void testTransitMode() {
    TraverseModeSet modeSet = new TraverseModeSet(TraverseMode.TRANSIT);

    assertFalse(modeSet.getBicycle());
    assertFalse(modeSet.getWalk());
    assertFalse(modeSet.getCar());
    assertTrue(modeSet.isTransit());
    assertTrue(modeSet.getRail());
    assertTrue(modeSet.getTram());
    assertTrue(modeSet.getSubway());
    assertTrue(modeSet.getFunicular());
    assertTrue(modeSet.getGondola());
    assertTrue(modeSet.getTrolleyBus());
  }

  @Test
  public void testCarpool() {
    TraverseModeSet modeSet = new TraverseModeSet(TraverseMode.CARPOOL);

    assertTrue(modeSet.getCarpool());

    assertFalse(modeSet.getBicycle());
    assertFalse(modeSet.getWalk());
    assertFalse(modeSet.getCar());
    assertFalse(modeSet.getFunicular());
    assertFalse(modeSet.getGondola());
  }
}
