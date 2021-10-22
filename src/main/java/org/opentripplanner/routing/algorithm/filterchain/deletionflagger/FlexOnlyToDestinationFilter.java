package org.opentripplanner.routing.algorithm.filterchain.deletionflagger;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import org.opentripplanner.model.plan.Itinerary;
import org.opentripplanner.routing.algorithm.filterchain.ItineraryListFilter;
import org.opentripplanner.routing.core.TraverseMode;

/**
 * Remove all itineraries where all the final leg is more than x minutes of walking.
 */
public class FlexOnlyToDestinationFilter implements ItineraryListFilter {

    private final long maxWalkDuration = Duration.ofMinutes(2).toSeconds();

    @Override
    public List<Itinerary> filter(List<Itinerary> itineraries) {
        return itineraries
                .stream().filter(it -> {
                    var lastLeg = it.lastLeg();
                    boolean lastLegIsLongWalk = lastLeg.mode == TraverseMode.WALK
                            && lastLeg.getDuration() > maxWalkDuration;

                    var lastLegIsFlex = it.legs.stream()
                            .filter(l -> l.isTransitLeg() || l.flexibleTrip)
                            // get last element of stream
                            .reduce((first, second) -> second)
                            .map(l -> l.flexibleTrip)
                            .orElse(false);

                    return !lastLegIsLongWalk && lastLegIsFlex;
                })
                .collect(Collectors.toList());
    }

}
