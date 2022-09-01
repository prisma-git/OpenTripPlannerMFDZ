package org.opentripplanner.ext.fares;

import com.fasterxml.jackson.databind.JsonNode;
import org.opentripplanner.ext.fares.impl.DefaultFareServiceFactory;
import org.opentripplanner.ext.fares.impl.HighestFareInFreeTransferWindowFareServiceFactory;
import org.opentripplanner.ext.fares.impl.MultipleFareServiceFactory;
import org.opentripplanner.ext.fares.impl.NoopFareServiceFactory;
import org.opentripplanner.ext.fares.impl.NycFareServiceFactory;
import org.opentripplanner.ext.fares.impl.SFBayFareServiceFactory;
import org.opentripplanner.ext.fares.impl.TimeBasedVehicleRentalFareServiceFactory;
import org.opentripplanner.routing.fares.FareServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FaresConfiguration {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultFareServiceFactory.class);

  /**
   * Build a specific FareServiceFactory given the config node, or fallback to the default if none
   * specified.
   * <p>
   * Accept different formats. Examples:
   *
   * <pre>
   * { fares : "seattle" }
   * --------------------------
   * { fares : {} } // Fallback to default
   * --------------------------
   * { fares : {
   *       type : "foobar",
   *       param1 : 42
   * } }
   * --------------------------
   * { fares : {
   *       combinationStrategy : "additive",
   *       fares : [
   *           "seattle",
   *           { type : "foobar", ... }
   *       ]
   * } }
   * </pre>
   */
  public static FareServiceFactory fromConfig(JsonNode config) {
    String type = null;
    if (config == null) {
      /* Empty block, fallback to default */
      type = null;
    } else if (config.isTextual()) {
      /* Simplest form: { fares : "seattle" } */
      type = config.asText();
    } else if (config.has("combinationStrategy")) {
      /* Composite */
      String combinationStrategy = config.path("combinationStrategy").asText();
      switch (combinationStrategy) {
        case "additive":
          break;
        default:
          throw new IllegalArgumentException(
            "Unknown fare combinationStrategy: " + combinationStrategy
          );
      }
      type = "composite:" + combinationStrategy;
    } else if (config.has("type")) {
      /* Fare with a type: { fares : { type : "foobar", param1 : 42 } } */
      type = config.path("type").asText(null);
    }

    if (type == null) {
      type = "default";
    }

    FareServiceFactory factory = createFactory(type);
    factory.configure(config);
    return factory;
  }

  private static FareServiceFactory createFactory(String type) {
    return switch (type) {
      case "default" -> new DefaultFareServiceFactory();
      case "off" -> new NoopFareServiceFactory();
      case "composite:additive" -> new MultipleFareServiceFactory.AddingMultipleFareServiceFactory();
      case "vehicle-rental-time-based",
        "bike-rental-time-based" -> new TimeBasedVehicleRentalFareServiceFactory(); //TODO: deprecated, remove in next major version
      case "san-francisco" -> new SFBayFareServiceFactory();
      case "new-york" -> new NycFareServiceFactory();
      case "highestFareInFreeTransferWindow" -> new HighestFareInFreeTransferWindowFareServiceFactory();
      default -> throw new IllegalArgumentException(String.format("Unknown fare type: '%s'", type));
    };
  }
}
