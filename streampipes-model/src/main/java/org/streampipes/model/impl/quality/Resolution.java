package org.streampipes.model.impl.quality;

import javax.persistence.Entity;

import com.clarkparsia.empire.annotation.Namespaces;
import com.clarkparsia.empire.annotation.RdfProperty;
import com.clarkparsia.empire.annotation.RdfsClass;

@Namespaces({"sepa", "http://sepa.event-processing.org/sepa#",
	 "ssn",   "http://purl.oclc.org/NET/ssnx/ssn#"})
@RdfsClass("ssn:Resolution")
@Entity
public class Resolution extends EventPropertyQualityDefinition {

	private static final long serialVersionUID = -8794648771727880619L;
	
	@RdfProperty("sepa:hasQuantityValue")
	float quantityValue;

	public Resolution() {
		super();
	}
	
	public Resolution(float quantityValue) {
		this.quantityValue = quantityValue;
	}
	
	public Resolution(Resolution other) {
		super(other);
		this.quantityValue = other.getQuantityValue();
	}
	
	public float getQuantityValue() {
		return quantityValue;
	}

	public void setQuantityValue(float quantityValue) {
		this.quantityValue = quantityValue;
	}
	
	//@Override
	public int compareTo(EventPropertyQualityDefinition o) {
		Resolution other = (Resolution) o;
		if (other.getQuantityValue() == this.getQuantityValue()) {
			return 0;
			
		} else if ((other).getQuantityValue() > this.getQuantityValue()) {
			return -1;
		} else {
			return 1;
		}
	}
}