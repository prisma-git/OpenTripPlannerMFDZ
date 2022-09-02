package org.opentripplanner.routing.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.locationtech.jts.geom.Coordinate;
import org.opentripplanner.TestOtpModel;
import org.opentripplanner.model.StopTime;
import org.opentripplanner.routing.algorithm.astar.AStarBuilder;
import org.opentripplanner.routing.api.request.RoutingRequest;
import org.opentripplanner.routing.core.RoutingContext;
import org.opentripplanner.routing.core.TimeRestriction;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.edgetype.ElevatorAlightEdge;
import org.opentripplanner.routing.edgetype.ElevatorBoardEdge;
import org.opentripplanner.routing.edgetype.ElevatorEdge;
import org.opentripplanner.routing.edgetype.ElevatorHopEdge;
import org.opentripplanner.routing.edgetype.FreeEdge;
import org.opentripplanner.routing.edgetype.PathwayEdge;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.edgetype.StreetTransitEntranceLink;
import org.opentripplanner.routing.edgetype.StreetTransitStopLink;
import org.opentripplanner.routing.edgetype.StreetTraversalPermission;
import org.opentripplanner.routing.edgetype.StreetVehicleParkingLink;
import org.opentripplanner.routing.edgetype.StreetVehicleRentalLink;
import org.opentripplanner.routing.edgetype.TemporaryFreeEdge;
import org.opentripplanner.routing.edgetype.VehicleRentalEdge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.location.TemporaryStreetLocation;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.routing.vehicle_parking.VehicleParking;
import org.opentripplanner.routing.vehicle_parking.VehicleParking.VehicleParkingEntranceCreator;
import org.opentripplanner.routing.vehicle_parking.VehicleParkingHelper;
import org.opentripplanner.routing.vehicle_rental.RentalVehicleType;
import org.opentripplanner.routing.vehicle_rental.VehicleRentalPlace;
import org.opentripplanner.routing.vehicle_rental.VehicleRentalStation;
import org.opentripplanner.routing.vertextype.ElevatorOffboardVertex;
import org.opentripplanner.routing.vertextype.ElevatorOnboardVertex;
import org.opentripplanner.routing.vertextype.IntersectionVertex;
import org.opentripplanner.routing.vertextype.StreetVertex;
import org.opentripplanner.routing.vertextype.TemporaryVertex;
import org.opentripplanner.routing.vertextype.TransitEntranceVertex;
import org.opentripplanner.routing.vertextype.TransitStopVertex;
import org.opentripplanner.routing.vertextype.TransitStopVertexBuilder;
import org.opentripplanner.routing.vertextype.VehicleParkingEntranceVertex;
import org.opentripplanner.routing.vertextype.VehicleRentalPlaceVertex;
import org.opentripplanner.transit.model._data.TransitModelForTest;
import org.opentripplanner.transit.model.basic.NonLocalizedString;
import org.opentripplanner.transit.model.basic.TransitMode;
import org.opentripplanner.transit.model.basic.WheelchairAccessibility;
import org.opentripplanner.transit.model.framework.Deduplicator;
import org.opentripplanner.transit.model.framework.FeedScopedId;
import org.opentripplanner.transit.model.network.Route;
import org.opentripplanner.transit.model.network.TripPattern;
import org.opentripplanner.transit.model.organization.Agency;
import org.opentripplanner.transit.model.site.Entrance;
import org.opentripplanner.transit.model.site.PathwayMode;
import org.opentripplanner.transit.model.site.RegularStop;
import org.opentripplanner.transit.service.StopModel;
import org.opentripplanner.transit.service.TransitModel;
import org.opentripplanner.util.geometry.GeometryUtils;

public abstract class GraphRoutingTest {

  public static final String TEST_VEHICLE_RENTAL_NETWORK = "test network";

  public static String graphPathToString(GraphPath graphPath) {
    return graphPath.states
      .stream()
      .flatMap(s ->
        Stream.of(
          s.getBackEdge() != null ? s.getBackEdge().getDefaultName() : null,
          s.getVertex().getDefaultName()
        )
      )
      .filter(Objects::nonNull)
      .collect(Collectors.joining(" - "));
  }

  protected TestOtpModel modelOf(Builder builder) {
    builder.build();
    Graph graph = builder.graph();
    TransitModel transitModel = builder.transitModel();
    return new TestOtpModel(graph, transitModel).index();
  }

  protected GraphPath routeParkAndRide(
    Graph graph,
    StreetVertex from,
    StreetVertex to,
    TraverseModeSet traverseModeSet
  ) {
    RoutingRequest request = new RoutingRequest(traverseModeSet);
    RoutingContext rctx = new RoutingContext(request, graph, from, to);
    request.parkAndRide = true;

    return AStarBuilder.oneToOne().setContext(rctx).getShortestPathTree().getPath(to);
  }

  public abstract static class Builder {

    private final Graph graph;
    private final TransitModel transitModel;

    protected Builder() {
      var deduplicator = new Deduplicator();
      var stopModel = new StopModel();
      graph = new Graph(deduplicator);
      transitModel = new TransitModel(stopModel, deduplicator);
    }

    public abstract void build();

    public Graph graph() {
      return graph;
    }

    public TransitModel transitModel() {
      return transitModel;
    }

    public <T> T v(String label) {
      return vertex(label);
    }

    public <T> T vertex(String label) {
      return (T) graph.getVertex(label);
    }

    // -- Street network
    public IntersectionVertex intersection(String label, double latitude, double longitude) {
      return new IntersectionVertex(graph, label, longitude, latitude);
    }

    public StreetEdge street(
      StreetVertex from,
      StreetVertex to,
      int length,
      StreetTraversalPermission permissions
    ) {
      return new StreetEdge(
        from,
        to,
        GeometryUtils.makeLineString(from.getLat(), from.getLon(), to.getLat(), to.getLon()),
        String.format("%s%s street", from.getDefaultName(), to.getDefaultName()),
        length,
        permissions,
        false
      );
    }

    public List<StreetEdge> street(
      StreetVertex from,
      StreetVertex to,
      int length,
      StreetTraversalPermission forwardPermissions,
      StreetTraversalPermission reversePermissions
    ) {
      return List.of(
        new StreetEdge(
          from,
          to,
          GeometryUtils.makeLineString(from.getLat(), from.getLon(), to.getLat(), to.getLon()),
          String.format("%s%s street", from.getDefaultName(), to.getDefaultName()),
          length,
          forwardPermissions,
          false
        ),
        new StreetEdge(
          to,
          from,
          GeometryUtils.makeLineString(to.getLat(), to.getLon(), from.getLat(), from.getLon()),
          String.format("%s%s street", from.getDefaultName(), to.getDefaultName()),
          length,
          reversePermissions,
          true
        )
      );
    }

    public List<ElevatorEdge> elevator(StreetTraversalPermission permission, Vertex... vertices) {
      List<ElevatorEdge> edges = new ArrayList<>();
      List<ElevatorOnboardVertex> onboardVertices = new ArrayList<>();

      for (Vertex v : vertices) {
        var level = String.format("L-%s", v.getDefaultName());
        var boardLabel = String.format("%s-onboard", level);
        var alightLabel = String.format("%s-offboard", level);

        var onboard = new ElevatorOnboardVertex(
          graph,
          boardLabel,
          v.getX(),
          v.getY(),
          new NonLocalizedString(boardLabel)
        );
        var offboard = new ElevatorOffboardVertex(
          graph,
          alightLabel,
          v.getX(),
          v.getY(),
          new NonLocalizedString(alightLabel)
        );

        new FreeEdge(v, offboard);
        new FreeEdge(offboard, v);

        edges.add(new ElevatorBoardEdge(offboard, onboard));
        edges.add(new ElevatorAlightEdge(onboard, offboard, new NonLocalizedString(level)));

        onboardVertices.add(onboard);
      }

      for (int i = 1; i < onboardVertices.size(); i++) {
        var from = onboardVertices.get(i - 1);
        var to = onboardVertices.get(i);

        edges.add(new ElevatorHopEdge(from, to, permission, WheelchairAccessibility.POSSIBLE));
        edges.add(new ElevatorHopEdge(to, from, permission, WheelchairAccessibility.POSSIBLE));
      }

      return edges;
    }

    // -- Transit network (pathways, linking)
    public Entrance entranceEntity(String id, double latitude, double longitude) {
      return Entrance
        .of(TransitModelForTest.id(id))
        .withName(new NonLocalizedString(id))
        .withCode(id)
        .withCoordinate(latitude, longitude)
        .build();
    }

    public RegularStop stopEntity(String id, double latitude, double longitude) {
      var stop = TransitModelForTest.stop(id).withCoordinate(latitude, longitude).build();
      transitModel.mergeStopModels(StopModel.of().withRegularStop(stop).build());
      return stop;
    }

    public TransitStopVertex stop(String id, double latitude, double longitude) {
      return new TransitStopVertexBuilder()
        .withGraph(graph)
        .withStop(stopEntity(id, latitude, longitude))
        .build();
    }

    public TransitEntranceVertex entrance(String id, double latitude, double longitude) {
      return new TransitEntranceVertex(graph, entranceEntity(id, latitude, longitude));
    }

    public StreetTransitEntranceLink link(StreetVertex from, TransitEntranceVertex to) {
      return new StreetTransitEntranceLink(from, to);
    }

    public StreetTransitEntranceLink link(TransitEntranceVertex from, StreetVertex to) {
      return new StreetTransitEntranceLink(from, to);
    }

    public List<StreetTransitEntranceLink> biLink(StreetVertex from, TransitEntranceVertex to) {
      return List.of(link(from, to), link(to, from));
    }

    public StreetTransitStopLink link(StreetVertex from, TransitStopVertex to) {
      return new StreetTransitStopLink(from, to);
    }

    public StreetTransitStopLink link(TransitStopVertex from, StreetVertex to) {
      return new StreetTransitStopLink(from, to);
    }

    public List<StreetTransitStopLink> biLink(StreetVertex from, TransitStopVertex to) {
      return List.of(link(from, to), link(to, from));
    }

    public PathwayEdge pathway(Vertex from, Vertex to, int time, int length) {
      return new PathwayEdge(
        from,
        to,
        null,
        new NonLocalizedString(
          String.format("%s%s pathway", from.getDefaultName(), to.getDefaultName())
        ),
        time,
        length,
        0,
        0,
        false,
        PathwayMode.WALKWAY
      );
    }

    // -- Street linking
    public TemporaryStreetLocation streetLocation(
      String name,
      double latitude,
      double longitude,
      boolean endVertex
    ) {
      return new TemporaryStreetLocation(
        name,
        new Coordinate(longitude, latitude),
        new NonLocalizedString(name),
        endVertex
      );
    }

    public TemporaryFreeEdge link(TemporaryVertex from, StreetVertex to) {
      return new TemporaryFreeEdge(from, to);
    }

    public TemporaryFreeEdge link(StreetVertex from, TemporaryVertex to) {
      return new TemporaryFreeEdge(from, to);
    }

    public List<TemporaryFreeEdge> biLink(StreetVertex from, TemporaryVertex to) {
      return List.of(link(from, to), link(to, from));
    }

    // -- Vehicle rental
    public VehicleRentalPlace vehicleRentalStationEntity(
      String id,
      double latitude,
      double longitude,
      String network
    ) {
      var vehicleRentalStation = new VehicleRentalStation();
      vehicleRentalStation.id = new FeedScopedId(network, id);
      vehicleRentalStation.name = new NonLocalizedString(id);
      vehicleRentalStation.longitude = longitude;
      vehicleRentalStation.latitude = latitude;
      vehicleRentalStation.vehiclesAvailable = 2;
      vehicleRentalStation.spacesAvailable = 2;
      final RentalVehicleType vehicleType = RentalVehicleType.getDefaultType(network);
      vehicleRentalStation.vehicleTypesAvailable = Map.of(vehicleType, 2);
      vehicleRentalStation.vehicleSpacesAvailable = Map.of(vehicleType, 2);
      vehicleRentalStation.isKeepingVehicleRentalAtDestinationAllowed = false;
      return vehicleRentalStation;
    }

    public VehicleRentalPlaceVertex vehicleRentalStation(
      String id,
      double latitude,
      double longitude,
      String network
    ) {
      var vertex = new VehicleRentalPlaceVertex(
        graph,
        vehicleRentalStationEntity(id, latitude, longitude, network)
      );
      new VehicleRentalEdge(vertex, RentalVehicleType.getDefaultType(network).formFactor);
      return vertex;
    }

    public VehicleRentalPlaceVertex vehicleRentalStation(
      String id,
      double latitude,
      double longitude
    ) {
      return vehicleRentalStation(id, latitude, longitude, TEST_VEHICLE_RENTAL_NETWORK);
    }

    public StreetVehicleRentalLink link(StreetVertex from, VehicleRentalPlaceVertex to) {
      return new StreetVehicleRentalLink(from, to);
    }

    public StreetVehicleRentalLink link(VehicleRentalPlaceVertex from, StreetVertex to) {
      return new StreetVehicleRentalLink(from, to);
    }

    public List<StreetVehicleRentalLink> biLink(StreetVertex from, VehicleRentalPlaceVertex to) {
      return List.of(link(from, to), link(to, from));
    }

    public VehicleParking vehicleParking(
      String id,
      double x,
      double y,
      boolean bicyclePlaces,
      boolean carPlaces,
      List<VehicleParkingEntranceCreator> entrances,
      String... tags
    ) {
      return vehicleParking(id, x, y, bicyclePlaces, carPlaces, false, null, entrances, tags);
    }

    public VehicleParking vehicleParking(
      String id,
      double x,
      double y,
      boolean bicyclePlaces,
      boolean carPlaces,
      boolean wheelchairAccessibleCarPlaces,
      TimeRestriction openingHours,
      List<VehicleParkingEntranceCreator> entrances,
      String... tags
    ) {
      var vehicleParking = VehicleParking
        .builder()
        .id(TransitModelForTest.id(id))
        .x(x)
        .y(y)
        .bicyclePlaces(bicyclePlaces)
        .carPlaces(carPlaces)
        .entrances(entrances)
        .openingHours(openingHours)
        .wheelchairAccessibleCarPlaces(wheelchairAccessibleCarPlaces)
        .tags(List.of(tags))
        .build();

      var vertices = VehicleParkingHelper.createVehicleParkingVertices(graph, vehicleParking);
      VehicleParkingHelper.linkVehicleParkingEntrances(vertices);
      vertices.forEach(v -> biLink(v.getParkingEntrance().getVertex(), v));
      return vehicleParking;
    }

    public VehicleParking.VehicleParkingEntranceCreator vehicleParkingEntrance(
      StreetVertex streetVertex,
      String id,
      boolean carAccessible,
      boolean walkAccessible
    ) {
      return builder ->
        builder
          .entranceId(TransitModelForTest.id(id))
          .name(new NonLocalizedString(id))
          .x(streetVertex.getX())
          .y(streetVertex.getY())
          .vertex(streetVertex)
          .carAccessible(carAccessible)
          .walkAccessible(walkAccessible);
    }

    public StreetVehicleParkingLink link(StreetVertex from, VehicleParkingEntranceVertex to) {
      return new StreetVehicleParkingLink(from, to);
    }

    public StreetVehicleParkingLink link(VehicleParkingEntranceVertex from, StreetVertex to) {
      return new StreetVehicleParkingLink(from, to);
    }

    public List<StreetVehicleParkingLink> biLink(
      StreetVertex from,
      VehicleParkingEntranceVertex to
    ) {
      return List.of(link(from, to), link(to, from));
    }

    public Route route(String id, TransitMode mode, Agency agency) {
      return TransitModelForTest.route(id).withAgency(agency).withMode(mode).build();
    }

    // Transit
    public void tripPattern(TripPattern tripPattern) {
      transitModel.addTripPattern(tripPattern.getId(), tripPattern);
    }

    public StopTime st(TransitStopVertex s1) {
      var st = new StopTime();
      st.setStop(s1.getStop());
      return st;
    }
  }
}
