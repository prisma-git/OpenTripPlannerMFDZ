package org.opentripplanner.routing.vehicle_parking;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.opentripplanner.model.FeedScopedId;
import org.opentripplanner.routing.core.TimeRestriction;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.vehicle_parking.VehicleParkingEntrance.VehicleParkingEntranceBuilder;
import org.opentripplanner.util.I18NString;

/**
 * Vehicle parking locations, which may allow bicycle and/or car parking.
 *
 * All fields are immutable except for the availability, capacity which may be updated by updaters.
 * If any other properties change a new VehicleParking instance should be created.
 */
@Builder(buildMethodName = "buildInternal")
@Getter
@EqualsAndHashCode
public class VehicleParking implements Serializable {

  /**
   * The id of the vehicle parking, prefixed by the source(=feedId) so that it is unique.
   */
  private final FeedScopedId id;

  private final I18NString name;

  /**
   * Note: x = Longitude, y = Latitude
   */
  private final double x, y;

  private final String detailsUrl;

  private final String imageUrl;

  /**
   * Source specific tags of the vehicle parking, which describe the available features. For example
   * park_and_ride, bike_lockers, or static_osm_data.
   */
  private final List<String> tags;

  private final TimeRestriction openingHours;

  private final TimeRestriction feeHours;

  private final I18NString note;

  @Builder.Default
  private final VehicleParkingState state = VehicleParkingState.OPERATIONAL;

  @Getter(AccessLevel.NONE)
  private final boolean bicyclePlaces;

  @Getter(AccessLevel.NONE)
  private final boolean carPlaces;

  @Getter(AccessLevel.NONE)
  private final boolean wheelchairAccessibleCarPlaces;

  private final VehicleParkingSpaces capacity;

  @EqualsAndHashCode.Exclude
  private VehicleParkingSpaces availability;

  @Builder.Default
  private final List<VehicleParkingEntrance> entrances = new ArrayList<>();

  public String toString() {
    return String.format(Locale.US, "VehicleParking(%s at %.6f, %.6f)", name, y, x);
  }

  public boolean hasBicyclePlaces() {
    return bicyclePlaces;
  }

  public boolean hasAnyCarPlaces() {
    return hasCarPlaces() || hasWheelchairAccessibleCarPlaces();
  }

  public boolean hasCarPlaces() {
    return carPlaces;
  }

  public boolean hasWheelchairAccessibleCarPlaces() {
    return wheelchairAccessibleCarPlaces;
  }

  public boolean hasRealTimeData() {
    return availability != null;
  }

  public boolean hasSpacesAvailable(TraverseMode traverseMode, boolean wheelchairAccessible, boolean useAvailability) {
    switch (traverseMode) {
      case BICYCLE:
        if (useAvailability && hasRealTimeDataForMode(TraverseMode.BICYCLE, false)) {
          return availability.getBicycleSpaces() > 0;
        } else {
          return bicyclePlaces;
        }
      case CAR:
        if (wheelchairAccessible) {
          if (useAvailability && hasRealTimeDataForMode(TraverseMode.CAR, true)) {
            return availability.getWheelchairAccessibleCarSpaces() > 0;
          } else {
            return wheelchairAccessibleCarPlaces;
          }
        } else {
          if (useAvailability && hasRealTimeDataForMode(TraverseMode.CAR, false)) {
            return availability.getCarSpaces() > 0;
          } else {
            return carPlaces;
          }
        }
      default:
        return false;
    }
  }

  public boolean hasRealTimeDataForMode(TraverseMode traverseMode, boolean wheelchairAccessibleCarPlaces) {
    if (availability == null) {
      return false;
    }

    switch (traverseMode) {
      case BICYCLE:
        return availability.getBicycleSpaces() != null;
      case CAR:
        var places = wheelchairAccessibleCarPlaces
                ? availability.getWheelchairAccessibleCarSpaces()
                : availability.getCarSpaces();
        return places != null;
      default:
        return false;
    }
  }

  public void updateAvailability(VehicleParkingSpaces vehicleParkingSpaces) {
    this.availability = vehicleParkingSpaces;
  }

  private void addEntrance(VehicleParkingEntranceCreator creator) {
    var entrance = creator.updateValues(VehicleParkingEntrance.builder()
            .vehicleParking(this))
            .build();

    entrances.add(entrance);
  }

  @FunctionalInterface
  public interface VehicleParkingEntranceCreator {
    VehicleParkingEntranceBuilder updateValues(VehicleParkingEntranceBuilder builder);
  }

  /*
   * These methods are overwritten so that the saved list is always an array list for serialization.
   */
  @SuppressWarnings("unused")
  public static class VehicleParkingBuilder {
    private List<String> tags = new ArrayList<>();
    private final List<VehicleParkingEntranceCreator> entranceCreators = new ArrayList<>();

    public VehicleParkingBuilder tags(Collection<String> tags) {
      this.tags = new ArrayList<>(tags);
      return this;
    }

    public VehicleParkingBuilder entrances(Collection<VehicleParkingEntranceCreator> creators) {
        this.entranceCreators.addAll(creators);
        return this;
    }

    public VehicleParkingBuilder entrance(VehicleParkingEntranceCreator creator) {
        this.entranceCreators.add(creator);
        return this;
    }

    public VehicleParking build() {
        VehicleParking vehicleParking = this.buildInternal();
        this.entranceCreators.forEach(vehicleParking::addEntrance);
        return vehicleParking;
    }
  }
}
