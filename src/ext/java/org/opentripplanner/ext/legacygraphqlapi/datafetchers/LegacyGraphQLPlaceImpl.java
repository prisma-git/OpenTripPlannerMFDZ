package org.opentripplanner.ext.legacygraphqlapi.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.opentripplanner.ext.legacygraphqlapi.LegacyGraphQLRequestContext;
import org.opentripplanner.ext.legacygraphqlapi.generated.LegacyGraphQLDataFetchers;
import org.opentripplanner.model.plan.Place;
import org.opentripplanner.model.plan.StopArrival;
import org.opentripplanner.model.plan.VehicleParkingWithEntrance;
import org.opentripplanner.model.plan.VertexType;
import org.opentripplanner.routing.RoutingService;
import org.opentripplanner.routing.vehicle_rental.VehicleRentalPlace;
import org.opentripplanner.routing.vehicle_parking.VehicleParking;

public class LegacyGraphQLPlaceImpl implements LegacyGraphQLDataFetchers.LegacyGraphQLPlace {

  @Override
  public DataFetcher<String> name() {
    return environment -> getSource(environment).place.getName();
  }

  @Override
  public DataFetcher<String> vertexType() {
    return environment -> getSource(environment).place.getVertexType().name();
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
    return environment -> getSource(environment).place.stop;
  }

  @Override
  public DataFetcher<VehicleRentalPlace> bikeRentalStation() {
    return environment -> {
      Place place = getSource(environment).place;

      if (!place.getVertexType().equals(VertexType.BIKESHARE)) { return null; }

      return place.vehicleRentalStation;
    };
  }

  @Override
  public DataFetcher<Object> bikePark() {
    return this::getVehicleParking;
  }

  @Override
  public DataFetcher<Object> carPark() {
    return this::getVehicleParking;
  }

  @Override
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
