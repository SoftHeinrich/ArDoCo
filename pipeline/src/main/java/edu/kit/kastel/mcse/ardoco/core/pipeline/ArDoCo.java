/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.pipeline;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.pipeline.Pipeline;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelConnector;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.common.util.FilePrinter;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.InconsistencyChecker;
import edu.kit.kastel.mcse.ardoco.core.model.JavaJsonModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.ModelProvider;
import edu.kit.kastel.mcse.ardoco.core.model.PcmXMLModelConnector;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp.CoreNLPProvider;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtraction;

/**
 * The Pipeline defines the execution of the agents.
 */
public final class ArDoCo extends Pipeline {

    private static final Logger classLogger = LoggerFactory.getLogger(ArDoCo.class);

    public ArDoCo() {
        super("ArDoCo", new DataRepository());
    }

    @Override
    public DataRepository getDataRepository() {
        return super.getDataRepository();
    }

    @Override
    public void run() {
        classLogger.info("Starting ArDoCo");
        super.run();
    }

    /**
     * Run the approach with the given parameters.
     *
     * @param name                   Name of the run
     * @param inputText              File of the input text.
     * @param inputArchitectureModel File of the input model (PCM)
     * @return the {@link ArDoCoResult} that contains the blackboard with all results (of all steps)
     */
    public static ArDoCoResult run(String name, File inputText, File inputArchitectureModel, File additionalConfigs) {
        return runAndSave(name, inputText, inputArchitectureModel, null, additionalConfigs, null);
    }

    /**
     * Run the approach with the given parameters and save the output to the file system.
     *
     * @param name                   Name of the run
     * @param inputText              File of the input text.
     * @param inputArchitectureModel File of the input model (PCM)
     * @param inputCodeModel         File of the input model (Java Code JSON)
     * @param additionalConfigsFile  File with the additional or overwriting config parameters that should be used
     * @param outputDir              File that represents the output directory where the results should be written to
     * @return the {@link ArDoCoResult} that contains the blackboard with all results (of all steps)
     */
    public static ArDoCoResult runAndSave(String name, File inputText, File inputArchitectureModel, File inputCodeModel, File additionalConfigsFile,
            File outputDir) {

        classLogger.info("Loading additional configs ..");
        var additionalConfigs = loadAdditionalConfigs(additionalConfigsFile);

        classLogger.info("Starting {}", name);
        var startTime = System.currentTimeMillis();

        ArDoCo arDoCo;
        try {
            arDoCo = defineArDoCo(inputText, inputArchitectureModel, inputCodeModel, additionalConfigs);
        } catch (IOException e) {
            classLogger.error("Problem in initialising pipeline when loading data (IOException)", e.getCause());
            return null;
        }
        arDoCo.run();

        // save step
        var duration = Duration.ofMillis(System.currentTimeMillis() - startTime);
        ArDoCoResult arDoCoResult = new ArDoCoResult(arDoCo.getDataRepository());
        saveOutput(name, outputDir, arDoCoResult);

        classLogger.info("Finished in {}.{}s.", duration.getSeconds(), duration.toMillisPart());
        return arDoCoResult;
    }

    static ArDoCo defineArDoCo(File inputText, File inputArchitectureModel, File inputCodeModel, Map<String, String> additionalConfigs) throws IOException {
        var arDoCo = new ArDoCo();
        var dataRepository = arDoCo.getDataRepository();

        arDoCo.addPipelineStep(getTextProvider(inputText, additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(getPcmModelProvider(inputArchitectureModel, dataRepository));
        if (inputCodeModel != null) {
            arDoCo.addPipelineStep(getJavaModelProvider(inputCodeModel, dataRepository));
        }
        arDoCo.addPipelineStep(getTextExtraction(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(getRecommendationGenerator(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(getConnectionGenerator(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(getInconsistencyChecker(additionalConfigs, dataRepository));
        return arDoCo;
    }

    private static void saveOutput(String name, File outputDir, ArDoCoResult arDoCoResult) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(outputDir);
        Objects.requireNonNull(arDoCoResult);

        classLogger.info("Starting to write output...");
        FilePrinter.writeTraceabilityLinkRecoveryOutput(getOutputFile(name, outputDir, "traceLinks_"), arDoCoResult);
        FilePrinter.writeInconsistencyOutput(getOutputFile(name, outputDir, "inconsistencyDetection_"), arDoCoResult);
        classLogger.info("Finished to write output.");
    }

    private static File getOutputFile(String name, File outputDir, String prefix) {
        var filename = prefix + name + ".txt";
        var filepath = outputDir.toPath().resolve(filename);
        var file = filepath.toFile();
        return file;
    }

    public static InconsistencyChecker getInconsistencyChecker(Map<String, String> additionalConfigs, DataRepository dataRepository) {
        var inconsistencyChecker = new InconsistencyChecker(dataRepository);
        inconsistencyChecker.applyConfiguration(additionalConfigs);
        return inconsistencyChecker;
    }

    public static ConnectionGenerator getConnectionGenerator(Map<String, String> additionalConfigs, DataRepository dataRepository) {
        var connectionGenerator = new ConnectionGenerator(dataRepository);
        connectionGenerator.applyConfiguration(additionalConfigs);
        return connectionGenerator;
    }

    public static RecommendationGenerator getRecommendationGenerator(Map<String, String> additionalConfigs, DataRepository dataRepository) {
        var recommendationGenerator = new RecommendationGenerator(dataRepository);
        recommendationGenerator.applyConfiguration(additionalConfigs);
        return recommendationGenerator;
    }

    public static TextExtraction getTextExtraction(Map<String, String> additionalConfigs, DataRepository dataRepository) {
        var textExtractor = new TextExtraction(dataRepository);
        textExtractor.applyConfiguration(additionalConfigs);
        return textExtractor;
    }

    public static ModelProvider getJavaModelProvider(File inputCodeModel, DataRepository dataRepository) throws IOException {
        ModelConnector javaModel = new JavaJsonModelConnector(inputCodeModel);
        return new ModelProvider(dataRepository, javaModel);
    }

    public static ModelProvider getPcmModelProvider(File inputArchitectureModel, DataRepository dataRepository) throws IOException {
        ModelConnector pcmModel = new PcmXMLModelConnector(inputArchitectureModel);
        return new ModelProvider(dataRepository, pcmModel);
    }

    public static CoreNLPProvider getTextProvider(File inputText, Map<String, String> additionalConfigs, DataRepository dataRepository)
            throws FileNotFoundException {
        var textProvider = new CoreNLPProvider(dataRepository, new FileInputStream(inputText));
        textProvider.applyConfiguration(additionalConfigs);
        return textProvider;
    }

    public static Map<String, String> loadAdditionalConfigs(File additionalConfigsFile) {
        Map<String, String> additionalConfigs = new HashMap<>();
        if (additionalConfigsFile != null && additionalConfigsFile.exists()) {
            try (var scanner = new Scanner(additionalConfigsFile, StandardCharsets.UTF_8)) {
                while (scanner.hasNextLine()) {
                    var line = scanner.nextLine();
                    if (line == null || line.isBlank()) {
                        continue;
                    }
                    var values = line.split(KEY_VALUE_CONNECTOR, 2);
                    if (values.length != 2) {
                        classLogger.error(
                                "Found config line \"{}\". Layout has to be: 'KEY" + KEY_VALUE_CONNECTOR + "VALUE', e.g., 'SimpleClassName" + CLASS_ATTRIBUTE_CONNECTOR + "AttributeName" + KEY_VALUE_CONNECTOR + "42",
                                line);
                    } else {
                        additionalConfigs.put(values[0], values[1]);
                    }
                }
            } catch (IOException e) {
                classLogger.error(e.getMessage(), e);
            }
        }
        return additionalConfigs;
    }

}
