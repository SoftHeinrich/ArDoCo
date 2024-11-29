/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.pipeline;

import edu.kit.kastel.mcse.ardoco.core.configuration.AbstractConfigurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;

/**
 * This class represents an abstract pipeline step and defines the core functionality. Together
 * with {@link Pipeline} and concrete implementations of this class
 * represents a composite pattern.
 */
public abstract class AbstractPipelineStep extends AbstractConfigurable {
    protected final String id;
    protected final DataRepository dataRepository;

    /**
     * Constructor for a pipeline step
     *
     * @param id             the id of the stage
     * @param dataRepository the {@link DataRepository} that should be used
     */
    protected AbstractPipelineStep(String id, DataRepository dataRepository) {
        this.id = id;
        this.dataRepository = dataRepository;
    }

    /**
     * Runs the pipeline step beginning with {@link #before()}, {@link #process()} and finally
     * {@link #after()}
     */
    public void run() {
        this.before();
        this.process();
        this.after();
    }

    /**
     * Processes the pipeline step
     */
    protected abstract void process();

    /**
     * Called before the pipeline step
     */
    protected abstract void before();

    /**
     * Called after the pipeline step
     */
    protected abstract void after();

    /**
     * {@return the {@link DataRepository} that is used for saving and fetching data}
     */
    protected DataRepository getDataRepository() {
        return this.dataRepository;
    }

    /**
     * Returns the id
     *
     * @return the id
     */
    public final String getId() {
        return this.id;
    }
}
