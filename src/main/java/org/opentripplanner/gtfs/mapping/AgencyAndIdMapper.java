package org.opentripplanner.gtfs.mapping;

import org.opentripplanner.transit.model.framework.FeedScopedId;

/** Responsible for mapping GTFS AgencyAndId into the OTP model. */
public class AgencyAndIdMapper {

  /** Map from GTFS to OTP model, {@code null} safe. */
  public static FeedScopedId mapAgencyAndId(org.onebusaway.gtfs.model.AgencyAndId id) {
    return id == null ? null : new FeedScopedId(id.getAgencyId(), id.getId());
  }
}
