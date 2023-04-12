/* Licensed under MIT 2023. */
package io.github.ardoco.textproviderjson.dto;

import java.util.Objects;

import com.fasterxml.jackson.annotation.*;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.DependencyTag;

public class OutgoingDependencyDTO {
    private DependencyTag dependencyType;
    private long targetWordId;

    @JsonProperty("dependencyType")
    public DependencyTag getDependencyTag() {
        return dependencyType;
    }

    @JsonProperty("dependencyType")
    public void setDependencyTag(DependencyTag value) {
        this.dependencyType = value;
    }

    @JsonProperty("targetWordId")
    public long getTargetWordId() {
        return targetWordId;
    }

    @JsonProperty("targetWordId")
    public void setTargetWordId(long value) {
        this.targetWordId = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || !(o instanceof OutgoingDependencyDTO))
            return false;
        OutgoingDependencyDTO that = (OutgoingDependencyDTO) o;
        return targetWordId == that.targetWordId && Objects.equals(dependencyType, that.dependencyType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dependencyType, targetWordId);
    }
}
