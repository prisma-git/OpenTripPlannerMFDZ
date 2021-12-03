package org.opentripplanner.ext.legacygraphqlapi.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.opentripplanner.ext.legacygraphqlapi.LegacyGraphQLRequestContext;
import org.opentripplanner.ext.legacygraphqlapi.generated.LegacyGraphQLDataFetchers;
import org.opentripplanner.ext.legacygraphqlapi.generated.LegacyGraphQLTypes.LegacyGraphQLVertexType;
import org.opentripplanner.model.plan.Place;
import org.opentripplanner.model.plan.StopArrival;
import org.opentripplanner.model.plan.VehicleParkingWithEntrance;
import org.opentripplanner.model.plan.VertexType;
import org.opentripplanner.routing.RoutingService;
import org.opentripplanner.routing.vehicle_parking.VehicleParking;
import org.opentripplanner.routing.vehicle_rental.VehicleRentalPlace;
import org.opentripplanner.routing.vehicle_rental.VehicleRentalStation;
import org.opentripplanner.routing.vehicle_rental.VehicleRentalVehicle;

public class LegacyGraphQLPlaceImpl implements LegacyGraphQLDataFetchers.LegacyGraphQLPlace {

  @Override
  public DataFetcher<String> name() {
    return environment -> getSource(environment).place.getName();
  }

  @Override
  public DataFetcher<String> vertexType() {
    return environment -> {
      var place = getSource(environment).place;
      switch (place.vertexType) {
        case NORMAL:
          return LegacyGraphQLVertexType.Normal.label;
        case TRANSIT:
          return LegacyGraphQLVertexType.Transit.label;
        case VEHICLERENTAL:
          return LegacyGraphQLVertexType.Bikeshare.label;
        case VEHICLEPARKING:
          return LegacyGraphQLVertexType.Bikepark.label;
        default:
          throw new IllegalStateException("Unhandled vertexType: " + place.getVertexType().name());
      }
    };
  }

  @Override
  public DataFetcher<Double> lat() {
    return environment -> getSource(environment).place.getCoordinate().latitude();
  }

  @Override
  public DataFetcher<Double> lon() {
    return environment -> getSource(environment).place.getCoordinate().longitude();
  }

  @Override
  public DataFetcher<Long> arrivalTime() {
    return environment -> getSource(environment).arrival.getTime().getTime();
  }

  @Override
  public DataFetcher<Long> departureTime() {
    return environment -> getSource(environment).departure.getTime().getTime();
  }

  @Override
  public DataFetcher<Object> stop() {
    return environment -> {
      var stop = getSource(environment).place.stop;
      if(stop instanceof Stop) {
        return stop;
      }
      else {
        // TODO: also make it possible to return StopLocations. this however requires some rework
        // as much of the API and internals assume that you're always passing around instances
        // of Stop
        return null;
      }
    };
  }

  @Override
  public DataFetcher<VehicleRentalPlace> bikeRentalStation() {
    return environment -> {
      Place place = getSource(environment).place;

      if (!place.getVertexType().equals(VertexType.VEHICLERENTAL)) { return null; }

      return place.vehicleRentalPlace;
    };
  }

  @Override
  public DataFetcher<VehicleRentalStation> vehicleRentalStation() {
    return environment -> {
      Place place = getSource(environment).place;

      if (!place.vertexType.equals(VertexType.VEHICLERENTAL)
              || !(place.vehicleRentalPlace instanceof VehicleRentalStation)) {
        return null;
      }

      return (VehicleRentalStation) place.vehicleRentalPlace;
    };
  }

  @Override
  public DataFetcher<VehicleRentalVehicle> rentalVehicle() {
    return environment -> {
      Place place = getSource(environment).place;

      if (!place.vertexType.equals(VertexType.VEHICLERENTAL)
              || !(place.vehicleRentalPlace instanceof VehicleRentalVehicle)) {
        return null;
      }

      return (VehicleRentalVehicle) place.vehicleRentalPlace;
    };
  }

  @Override
  public DataFetcher<VehicleParking> bikePark() {
    return this::getVehicleParking;
  }

  @Override
  public DataFetcher<VehicleParking> carPark() {
    return this::getVehicleParking;
  }

  public DataFetcher<VehicleParkingWithEntrance> vehicleParkingWithEntrance() {
    return (environment) -> getSource(environment).place.getVehicleParkingWithEntrance();
  }

  private VehicleParking getVehicleParking(DataFetchingEnvironment environment) {
    var vehicleParkingWithEntrance = getSource(environment).place.getVehicleParkingWithEntrance();
    if (vehicleParkingWithEntrance == null) {
      return null;
    }

    return vehicleParkingWithEntrance.getVehicleParking();
  }

  private RoutingService getRoutingService(DataFetchingEnvironment environment) {
    return environment.<LegacyGraphQLRequestContext>getContext().getRoutingService();
  }

  private StopArrival getSource(DataFetchingEnvironment environment) {
    return environment.getSource();
  }
}
