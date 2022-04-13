/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.agent;

import edu.kit.kastel.mcse.ardoco.core.api.common.AbstractConfigurable;
import edu.kit.kastel.mcse.ardoco.core.api.data.IData;

public abstract class AbstractAgent<D extends IData> extends AbstractConfigurable implements IAgent<D> {

}
