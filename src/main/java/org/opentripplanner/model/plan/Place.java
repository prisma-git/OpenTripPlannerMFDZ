package org.opentripplanner.model.plan;

import org.opentripplanner.model.Stop;
import org.opentripplanner.model.StopLocation;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.opentripplanner.model.FeedScopedId;
import org.opentripplanner.model.StopLocation;
import org.opentripplanner.model.WgsCoordinate;
import org.opentripplanner.model.base.ToStringBuilder;
import org.opentripplanner.routing.api.request.RoutingRequest;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.vehicle_rental.VehicleRentalPlace;
import org.opentripplanner.routing.vehicle_rental.VehicleRentalStation;
import org.opentripplanner.routing.vertextype.TransitStopVertex;
import org.opentripplanner.routing.vertextype.VehicleParkingEntranceVertex;
import org.opentripplanner.routing.vertextype.VehicleRentalStationVertex;

/** 
* A Place is where a journey starts or ends, or a transit stop along the way.
*/
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class Place {

    /** 
     * For transit stops, the name of the stop.  For points of interest, the name of the POI.
     */
    public final String name;

    /**
     * Reference to the stop.
     */
    public StopLocation stop;

    /**
     * The coordinate of the place.
     */
    public final WgsCoordinate coordinate;

    public String orig;

    /**
     * For transit trips, the stop index (numbered from zero from the start of the trip).
     */
    public Integer stopIndex;

    /**
     * For transit trips, the sequence number of the stop. Per GTFS, these numbers are increasing.
     */
    public Integer stopSequence;

    /**
     * Type of vertex. (Normal, Bike sharing station, Bike P+R, Transit stop)
     * Mostly used for better localization of bike sharing and P+R station names
     */
    public VertexType vertexType;

    /**
     * The vehicle parking entrance if the type is {@link VertexType#VEHICLEPARKING}.
     */
    private VehicleParkingWithEntrance vehicleParkingWithEntrance;

    /**
     * In case the vertex is of type vehicle sharing station.
     */
    public VehicleRentalPlace vehicleRentalStation;

    public Place(Double lat, Double lon, String name) {
        this.name = name;
        this.vertexType = VertexType.NORMAL;
        this.coordinate = WgsCoordinate.creatOptionalCoordinate(lat, lon);
    }

    public Place(Stop stop) {
        this.name = stop.getName();
        this.stop = stop;
        this.coordinate = stop.getCoordinate();
        this.vertexType = VertexType.TRANSIT;
    }

    /**
     * Test if the place is likely to be at the same location. First check the coordinates
     * then check the stopId [if it exist].
     */
    public boolean sameLocation(Place other) {
        if(this == other) { return true; }
        if(coordinate != null) {
            return coordinate.sameLocation(other.coordinate);
        }
        return stop != null && stop.equals(other.stop);
    }

    /**
     * Return a short versio to be used in other classes toStringMethods. Should return
     * just the necessary information for a human to identify the place in a given the context.
     */
    public String toStringShort() {
        StringBuilder buf = new StringBuilder(name);
        if(stop != null) {
            buf.append(" (").append(stop.getId()).append(")");
        } else {
            buf.append(" ").append(coordinate.toString());
        }

        return buf.toString();
    }

    @Override
    public String toString() {
        return ToStringBuilder.of(Place.class)
                .addStr("name", name)
                .addObj("stop", stop)
                .addObj("coordinate", coordinate)
                .addStr("orig", orig)
                .addObj("coordinate", coordinate)
                .addEnum("vertexType", vertexType)
                .addObj("vehicleParkingEntrance", vehicleParkingWithEntrance)
                .addObj("stopId", stop != null ? stop.getId() : null)
                .addNum("stopIndex", stopIndex)
                .addNum("stopSequence", stopSequence)
                .addEnum("vertexType", vertexType)
                .addObj("vehicleRentalStation", vehicleRentalStation)
                .toString();
    }

    public static Place normal(Vertex vertex, String name) {
        return defaults(vertex, name)
                .vertexType(VertexType.NORMAL)
                .build();
    }

    public static Place forStop(StopLocation stop, Integer stopIndex, Integer stopSequence) {
        return Place.builder()
                .name(stop.getName())
                .coordinate(stop.getCoordinate())
                .vertexType(VertexType.TRANSIT)
                .stop(stop)
                .stopIndex(stopIndex)
                .stopSequence(stopSequence)
                .build();
    }

    public static Place forFlexStop(
            StopLocation stop,
            Vertex vertex,
            Integer stopIndex,
            Integer stopSequence
    ) {
        // The actual vertex is used because the StopLocation coordinates may not be equal to the vertex's
        // coordinates.
        return defaults(vertex, stop.getName())
                .vertexType(VertexType.TRANSIT)
                .stop(stop)
                .stopIndex(stopIndex)
                .stopSequence(stopSequence)
                .build();
    }

    public static Place forStop(TransitStopVertex vertex, String name) {
        return defaults(vertex, name)
                .vertexType(VertexType.TRANSIT)
                .stop(vertex.getStop())
                .build();
    }

    public static Place forBikeRentalStation(VehicleRentalStationVertex vertex, String name) {
        return defaults(vertex, name)
                .vertexType(VertexType.BIKESHARE)
                .vehicleRentalStation(vertex.getStation())
                .build();
    }

    public static Place forVehicleParkingEntrance(VehicleParkingEntranceVertex vertex, String name, boolean closesSoon, RoutingRequest request) {
        TraverseMode traverseMode = null;
        if (request.streetSubRequestModes.getCar()) {
            traverseMode = TraverseMode.CAR;
        } else if (request.streetSubRequestModes.getBicycle()) {
            traverseMode = TraverseMode.BICYCLE;
        }

        boolean realTime = request.useVehicleParkingAvailabilityInformation
            && vertex.getVehicleParking().hasRealTimeDataForMode(traverseMode, request.wheelchairAccessible);

        return defaults(vertex, name)
                .vertexType(VertexType.VEHICLEPARKING)
                .vehicleParkingWithEntrance(VehicleParkingWithEntrance.builder()
                        .vehicleParking(vertex.getVehicleParking())
                        .entrance(vertex.getParkingEntrance())
                        .closesSoon(closesSoon)
                        .realtime(realTime)
                        .build())
                .build();
    }

    private static Place.PlaceBuilder defaults(Vertex vertex, String name) {
        return Place.builder()
                .name(name)
                .coordinate(WgsCoordinate.creatOptionalCoordinate(vertex.getLat(), vertex.getLon()));
    }
}
