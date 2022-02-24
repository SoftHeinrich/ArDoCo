/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.agents;

import org.eclipse.collections.api.list.ImmutableList;
import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.common.Configuration;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionAgent;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.GenericConnectionConfig;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.IRecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.text.IText;
import edu.kit.kastel.mcse.ardoco.core.textextraction.ITextState;

/**
 * This connector finds names of model instance in recommended instances.
 *
 * @author Sophie
 *
 */
@MetaInfServices(ConnectionAgent.class)
public class InstanceConnectionAgent extends ConnectionAgent {

    private double probability;
    private double probabilityWithoutType;

    /**
     * Create the agent.
     */
    public InstanceConnectionAgent() {
        super(GenericConnectionConfig.class);
    }

    private InstanceConnectionAgent(//
            IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState, IConnectionState connectionState,
            GenericConnectionConfig config) {
        super(GenericConnectionConfig.class, text, textState, modelState, recommendationState, connectionState);
        probability = config.instanceConnectionSolverProbability;
        probabilityWithoutType = config.instanceConnectionSolverProbabilityWithoutType;
    }

    @Override
    public ConnectionAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            IConnectionState connectionState, Configuration config) {
        return new InstanceConnectionAgent(text, textState, modelState, recommendationState, connectionState, (GenericConnectionConfig) config);
    }

    /**
     * Executes the connector.
     */
    @Override
    public void exec() {
        findNamesOfModelInstancesInSupposedMappings();
        createLinksForEqualOrSimilarRecommendedInstances();
    }

    /**
     * Searches in the recommended instances of the recommendation state for similar names to extracted instances. If
     * some are found the instance link is added to the connection state.
     */
    private void findNamesOfModelInstancesInSupposedMappings() {
        ImmutableList<IRecommendedInstance> ris = recommendationState.getRecommendedInstances();
        for (IModelInstance i : modelState.getInstances()) {
            ImmutableList<IRecommendedInstance> mostLikelyRi = SimilarityUtils.getMostRecommendedInstancesToInstanceByReferences(i, ris);

            for (var ri : mostLikelyRi) {
                var riProbability = ri.getTypeMappings().isEmpty() ? probabilityWithoutType : probability;
                connectionState.addToLinks(ri, i, riProbability);
            }
        }
    }

    private void createLinksForEqualOrSimilarRecommendedInstances() {
        for (var ri : recommendationState.getRecommendedInstances()) {
            var sameInstances = modelState.getInstances().select(i -> SimilarityUtils.isRecommendedInstanceSimilarToModelInstance(ri, i));
            sameInstances.forEach(i -> connectionState.addToLinks(ri, i, probability));
        }
    }

}
