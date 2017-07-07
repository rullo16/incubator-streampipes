package org.streampipes.manager.matching.v2.mapping;

import org.streampipes.manager.matching.v2.PropertyMatch;
import org.streampipes.model.impl.eventproperty.EventProperty;
import org.streampipes.model.impl.eventproperty.EventPropertyList;
import org.streampipes.model.impl.eventproperty.EventPropertyNested;
import org.streampipes.model.impl.eventproperty.EventPropertyPrimitive;

import java.util.ArrayList;
import java.util.List;

public class MappingPropertyCalculator {

	private List<EventProperty> allMatchingProperties;
	
	public MappingPropertyCalculator() {
		this.allMatchingProperties = new ArrayList<>();
	}
	
	public List<EventProperty> matchesProperties(List<EventProperty> offer,
			EventProperty requirement) {
		offer.forEach(of -> matches(of, requirement, true));
		return allMatchingProperties;		
	}
	
	public boolean matches(EventProperty offer, EventProperty requirement, boolean addAsMatching) {
		boolean match = true;
		if (requirement instanceof EventPropertyPrimitive) {
				if (offer instanceof EventPropertyList) match = false;
				else if (offer instanceof EventPropertyPrimitive) {
					if (new PropertyMatch().match(offer, requirement, new ArrayList<>()))
						if (addAsMatching) allMatchingProperties.add(offer);
				} else if (offer instanceof EventPropertyNested) {
					List<EventProperty> nestedProperties = ((EventPropertyNested) offer).getEventProperties();
					if (!matches(nestedProperties, requirement)) match = false;
				}
			} else if (requirement instanceof EventPropertyList)
			{
				if (!(offer instanceof EventPropertyList)) match = false;
				else {
					if (!matchesList((EventPropertyList) offer, (EventPropertyList) requirement)) match = false;
					else if (addAsMatching) allMatchingProperties.add(offer);
				}
				
			} else if (requirement instanceof EventPropertyNested)
			{
				EventPropertyNested rightNested = (EventPropertyNested) requirement;
				for(EventProperty nestedProperty : rightNested.getEventProperties())
				{
					if (!matches(offer, nestedProperty, true)) match = false;
				}
			}
		return match;
	}
	
	public boolean matchesList(EventPropertyList offer, EventPropertyList requirement)
	{
		boolean match = true;
		for(EventProperty p : requirement.getEventProperties())
		{
			if (!matches(offer.getEventProperties(), p)) match = false;
		}
		return match;
	}
	
	public boolean matches(List<EventProperty> offer, EventProperty requirement)
	{
		boolean match = false;
		for(EventProperty of : offer)
		{
			if (matches(of, requirement, false)) match = true;
		}
		return match;
	}

}