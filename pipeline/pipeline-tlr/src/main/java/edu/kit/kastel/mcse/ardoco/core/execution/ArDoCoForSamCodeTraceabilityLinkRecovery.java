/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.execution;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.codetraceability.SamCodeTraceabilityLinkRecovery;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import edu.kit.kastel.mcse.ardoco.core.models.ArCoTLModelProviderAgent;

public class ArDoCoForSamCodeTraceabilityLinkRecovery extends ArDoCoRunner {
    private static final Logger logger = LoggerFactory.getLogger(ArDoCoForSadSamTraceabilityLinkRecovery.class);

    public ArDoCoForSamCodeTraceabilityLinkRecovery(String projectName) {
        super(projectName);
    }

    public void setUp(File inputArchitectureModel, ArchitectureModelType architectureModelType, File inputCode, Map<String, String> additionalConfigs,
            File outputDir) {
        try {
            definePipeline(inputArchitectureModel, architectureModelType, inputCode, additionalConfigs);
        } catch (IOException e) {
            logger.error("Problem in initialising pipeline when loading data (IOException)", e.getCause());
            isSetUp = false;
            return;
        }
        setOutputDirectory(outputDir);
        isSetUp = true;
    }

    private void definePipeline(File inputArchitectureModel, ArchitectureModelType architectureModelType, File inputCode, Map<String, String> additionalConfigs)
            throws IOException {
        ArDoCo arDoCo = this.getArDoCo();
        var dataRepository = arDoCo.getDataRepository();

        ArCoTLModelProviderAgent arCoTLModelProviderAgent = ArCoTLModelProviderAgent.get(inputArchitectureModel, architectureModelType, inputCode,
                additionalConfigs, dataRepository);
        arDoCo.addPipelineStep(arCoTLModelProviderAgent);
        arDoCo.addPipelineStep(SamCodeTraceabilityLinkRecovery.get(additionalConfigs, dataRepository));
    }
}
