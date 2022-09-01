package org.opentripplanner.ext.vectortiles.layers.stops;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.opentripplanner.ext.vectortiles.LayerBuilder;
import org.opentripplanner.ext.vectortiles.PropertyMapper;
import org.opentripplanner.ext.vectortiles.VectorTilesResource;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.transit.model.site.RegularStop;
import org.opentripplanner.transit.service.TransitService;

public class StopsLayerBuilder extends LayerBuilder<RegularStop> {

  static Map<MapperType, Function<TransitService, PropertyMapper<RegularStop>>> mappers = Map.of(
    MapperType.Digitransit,
    DigitransitStopPropertyMapper::create
  );
  private final TransitService transitService;

  public StopsLayerBuilder(
    Graph graph,
    TransitService transitService,
    VectorTilesResource.LayerParameters layerParameters
  ) {
    super(
      layerParameters.name(),
      mappers.get(MapperType.valueOf(layerParameters.mapper())).apply(transitService)
    );
    this.transitService = transitService;
  }

  protected List<Geometry> getGeometries(Envelope query) {
    return transitService
      .findRegularStop(query)
      .stream()
      .map(stop -> {
        Geometry point = stop.getGeometry();

        point.setUserData(stop);

        return point;
      })
      .collect(Collectors.toList());
  }

  enum MapperType {
    Digitransit,
  }
}
