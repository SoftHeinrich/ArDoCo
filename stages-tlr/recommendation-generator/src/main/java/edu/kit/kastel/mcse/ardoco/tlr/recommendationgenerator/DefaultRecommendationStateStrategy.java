/* Licensed under MIT 2024. */
package edu.kit.kastel.mcse.ardoco.tlr.recommendationgenerator;

import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendationStateStrategy;
import edu.kit.kastel.mcse.ardoco.core.data.GlobalConfiguration;

public class DefaultRecommendationStateStrategy implements RecommendationStateStrategy {
    private final GlobalConfiguration globalConfiguration;

    public DefaultRecommendationStateStrategy(GlobalConfiguration globalConfiguration) {
        this.globalConfiguration = globalConfiguration;
    }

    @Override
    public boolean areRecommendedInstanceTypesSimilar(String typeA, String typeB) {
        return globalConfiguration.getSimilarityUtils().areWordsSimilar(typeA, typeB);
    }

    @Override
    public boolean areRecommendedInstanceNamesSimilar(String nameA, String nameB) {
        return globalConfiguration.getSimilarityUtils().areWordsSimilar(nameA, nameB);
    }
}
