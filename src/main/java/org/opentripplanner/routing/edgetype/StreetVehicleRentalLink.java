package org.opentripplanner.routing.edgetype;

import java.util.Locale;
import java.util.Set;
import org.locationtech.jts.geom.LineString;
import org.opentripplanner.routing.api.request.RoutingRequest;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateEditor;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.vertextype.StreetVertex;
import org.opentripplanner.routing.vertextype.VehicleRentalPlaceVertex;
import org.opentripplanner.transit.model.basic.I18NString;

/**
 * This represents the connection between a street vertex and a bike rental station vertex.
 */
public class StreetVehicleRentalLink extends Edge {

  private final VehicleRentalPlaceVertex vehicleRentalPlaceVertex;

  public StreetVehicleRentalLink(StreetVertex fromv, VehicleRentalPlaceVertex tov) {
    super(fromv, tov);
    vehicleRentalPlaceVertex = tov;
  }

  public StreetVehicleRentalLink(VehicleRentalPlaceVertex fromv, StreetVertex tov) {
    super(fromv, tov);
    vehicleRentalPlaceVertex = fromv;
  }

  public String toString() {
    return "StreetVehicleRentalLink(" + fromv + " -> " + tov + ")";
  }

  public State traverse(State s0) {
    // Disallow traversing two StreetBikeRentalLinks in a row.
    // This prevents the router from using bike rental stations as shortcuts to get around
    // turn restrictions.
    if (s0.getBackEdge() instanceof StreetVehicleRentalLink) {
      return null;
    }

    if (vehicleRentalPlaceVertex.getStation().networkIsNotAllowed(s0.getOptions())) {
      return null;
    }

    if (networkIsNotAllowed(s0.getOptions(), vehicleRentalPlaceVertex.getStation().getNetwork())) {
      return null;
    }

    StateEditor s1 = s0.edit(this);
    //assume bike rental stations are more-or-less on-street
    s1.incrementWeight(1);
    s1.setBackMode(null);
    return s1.makeState();
  }

  private boolean networkIsNotAllowed(RoutingRequest options, String network) {
    if (
      !options.allowedVehicleRentalNetworks.isEmpty() &&
      options.allowedVehicleRentalNetworks.stream().noneMatch(network::equals)
    ) {
      return false;
    }

    if (
      !options.bannedVehicleRentalNetworks.isEmpty() &&
      options.bannedVehicleRentalNetworks.contains(network)
    ) {
      return true;
    }

    return false;
  }

  @Override
  public I18NString getName() {
    return vehicleRentalPlaceVertex.getName();
  }

  public LineString getGeometry() {
    return null;
  }

  public double getDistanceMeters() {
    return 0;
  }
}
