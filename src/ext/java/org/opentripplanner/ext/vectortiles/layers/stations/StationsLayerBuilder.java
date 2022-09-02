package org.opentripplanner.ext.vectortiles.layers.stations;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.opentripplanner.ext.vectortiles.LayerBuilder;
import org.opentripplanner.ext.vectortiles.PropertyMapper;
import org.opentripplanner.ext.vectortiles.VectorTilesResource;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.transit.model.site.Station;
import org.opentripplanner.transit.service.TransitService;
import org.opentripplanner.util.geometry.GeometryUtils;

public class StationsLayerBuilder extends LayerBuilder<Station> {

  static Map<MapperType, Function<TransitService, PropertyMapper<Station>>> mappers = Map.of(
    MapperType.Digitransit,
    DigitransitStationPropertyMapper::create
  );
  private final TransitService transitModel;

  public StationsLayerBuilder(
    Graph graph,
    TransitService transitService,
    VectorTilesResource.LayerParameters layerParameters
  ) {
    super(
      layerParameters.name(),
      mappers.get(MapperType.valueOf(layerParameters.mapper())).apply(transitService)
    );
    this.transitModel = transitService;
  }

  protected List<Geometry> getGeometries(Envelope query) {
    return transitModel
      .getStations()
      .stream()
      .map(station -> {
        Coordinate coordinate = station.getCoordinate().asJtsCoordinate();
        Point point = GeometryUtils.getGeometryFactory().createPoint(coordinate);
        point.setUserData(station);
        return point;
      })
      .collect(Collectors.toList());
  }

  enum MapperType {
    Digitransit,
  }
}
