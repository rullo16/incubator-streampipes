package org.streampipes.pe.processors.esper.number;

import org.streampipes.model.impl.graph.SepaInvocation;
import org.streampipes.wrapper.params.binding.EventProcessorBindingParams;

import java.util.List;

public class NumberClassificationParameters extends EventProcessorBindingParams {

	private String propertyName;
	private String outputProperty;
	private List<DataClassification> domainConceptData;

	public NumberClassificationParameters(SepaInvocation graph, String propertyName, String outputProperty,
			List<DataClassification> domainConceptData) {
		super(graph);
		this.propertyName = propertyName;
		this.domainConceptData = domainConceptData;
		this.outputProperty = outputProperty;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public List<DataClassification> getDomainConceptData() {
		return domainConceptData;
	}

	public String getOutputProperty() {
		return outputProperty;
	}
	
	

}
