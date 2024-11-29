/* Licensed under MIT 2024. */
package edu.kit.kastel.mcse.ardoco.core.api.models;

public abstract non-sealed class ArchitectureEntity extends Entity {
    protected ArchitectureEntity(String name) {
        super(name);
    }

    protected ArchitectureEntity(String name, String id) {
        super(name, id);
    }
}
