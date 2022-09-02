package org.opentripplanner.routing.edgetype;

import lombok.Getter;
import org.locationtech.jts.geom.LineString;
import org.opentripplanner.routing.api.request.RoutingRequest;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateEditor;
import org.opentripplanner.routing.core.TimeRestriction;
import org.opentripplanner.routing.core.TimeRestrictionWithTimeSpan;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.vehicle_parking.VehicleParking;
import org.opentripplanner.routing.vertextype.VehicleParkingEntranceVertex;
import org.opentripplanner.transit.model.basic.I18NString;

/**
 * Parking a vehicle edge.
 */
public class VehicleParkingEdge extends Edge implements TimeRestrictedEdge {

  private static final long serialVersionUID = 1L;

  @Getter
  private final VehicleParking vehicleParking;

  public VehicleParkingEdge(VehicleParkingEntranceVertex vehicleParkingEntranceVertex) {
    this(vehicleParkingEntranceVertex, vehicleParkingEntranceVertex);
  }

  public VehicleParkingEdge(
    VehicleParkingEntranceVertex fromVehicleParkingEntranceVertex,
    VehicleParkingEntranceVertex toVehicleParkingEntranceVertex
  ) {
    super(fromVehicleParkingEntranceVertex, toVehicleParkingEntranceVertex);
    this.vehicleParking = fromVehicleParkingEntranceVertex.getVehicleParking();
  }

  private TimeRestriction getTimeRestriction(int parkingTime) {
    var openingHours = vehicleParking.getOpeningHours();
    if (openingHours == null) {
      return null;
    }

    return TimeRestrictionWithTimeSpan.of(vehicleParking.getOpeningHours(), parkingTime);
  }

  public VehicleParking getVehicleParking() {
    return vehicleParking;
  }

  public boolean equals(Object o) {
    if (o instanceof VehicleParkingEdge) {
      VehicleParkingEdge other = (VehicleParkingEdge) o;
      return other.getFromVertex().equals(fromv) && other.getToVertex().equals(tov);
    }
    return false;
  }

  public String toString() {
    return "VehicleParkingEdge(" + fromv + " -> " + tov + ")";
  }

  @Override
  public State traverse(State s0) {
    RoutingRequest options = s0.getOptions();

    if (!options.parkAndRide) {
      return null;
    }

    if (options.arriveBy) {
      return traverseUnPark(s0);
    } else {
      return traversePark(s0);
    }
  }

  @Override
  public I18NString getName() {
    return getToVertex().getName();
  }

  @Override
  public boolean hasBogusName() {
    return false;
  }

  @Override
  public LineString getGeometry() {
    return null;
  }

  @Override
  public double getDistanceMeters() {
    return 0;
  }

  protected State traverseUnPark(State s0) {
    RoutingRequest options = s0.getOptions();

    if (s0.getNonTransitMode() != TraverseMode.WALK || !s0.isVehicleParked()) {
      return null;
    }

    if (options.streetSubRequestModes.getBicycle()) {
      return traverseUnPark(s0, options.bikeParkCost, options.bikeParkTime, TraverseMode.BICYCLE);
    } else if (options.streetSubRequestModes.getCar()) {
      return traverseUnPark(s0, options.carParkCost, options.carParkTime, TraverseMode.CAR);
    } else {
      return null;
    }
  }

  private State traverseUnPark(State s0, int parkingCost, int parkingTime, TraverseMode mode) {
    RoutingRequest options = s0.getOptions();
    if (
      !vehicleParking.hasSpacesAvailable(
        mode,
        options.wheelchairAccessibility.enabled(),
        options.useVehicleParkingAvailabilityInformation
      )
    ) {
      return null;
    }

    var timeRestriction = getTimeRestriction(parkingTime);
    if (isTraversalBlockedByTimeRestriction(s0, true, timeRestriction)) {
      return null;
    }

    StateEditor s0e = s0.edit(this);

    s0e.incrementWeight(parkingCost);
    s0e.incrementTimeInSeconds(parkingTime);
    s0e.setVehicleParked(false, mode);

    updateEditorWithTimeRestriction(s0, s0e, timeRestriction, getVehicleParking());

    return s0e.makeState();
  }

  private State traversePark(State s0) {
    RoutingRequest options = s0.getOptions();

    if (!options.streetSubRequestModes.getWalk() || s0.isVehicleParked()) {
      return null;
    }

    if (options.streetSubRequestModes.getBicycle()) {
      // Parking a rented bike is not allowed
      if (s0.isRentingVehicle()) {
        return null;
      }

      return traversePark(s0, options.bikeParkCost, options.bikeParkTime);
    } else if (options.streetSubRequestModes.getCar()) {
      return traversePark(s0, options.carParkCost, options.carParkTime);
    } else {
      return null;
    }
  }

  private State traversePark(State s0, int parkingCost, int parkingTime) {
    RoutingRequest options = s0.getOptions();

    if (
      !vehicleParking.hasSpacesAvailable(
        s0.getNonTransitMode(),
        options.wheelchairAccessibility.enabled(),
        options.useVehicleParkingAvailabilityInformation
      )
    ) {
      return null;
    }

    var timeRestriction = getTimeRestriction(parkingTime);
    if (isTraversalBlockedByTimeRestriction(s0, false, timeRestriction)) {
      return null;
    }

    StateEditor s0e = s0.edit(this);

    updateEditorWithTimeRestriction(s0, s0e, timeRestriction, getVehicleParking());

    s0e.incrementWeight(parkingCost);
    s0e.incrementTimeInSeconds(parkingTime);
    s0e.setVehicleParked(true, TraverseMode.WALK);
    return s0e.makeState();
  }
}
