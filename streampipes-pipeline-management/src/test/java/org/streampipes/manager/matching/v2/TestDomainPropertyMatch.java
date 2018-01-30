package org.streampipes.manager.matching.v2;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import org.streampipes.model.client.matching.MatchingResultMessage;
import org.streampipes.vocabulary.Geo;

public class TestDomainPropertyMatch extends TestCase {

	@Test
	public void testPositiveDomainPropertyMatch() {

		List<URI> offeredDomainProperty = buildDomainProperties(Geo.lat);
		List<URI> requiredDomainProperty = buildDomainProperties(Geo.lat);
		
		List<MatchingResultMessage> resultMessage = new ArrayList<>();
		
		boolean matches = new DomainPropertyMatch().match(offeredDomainProperty, requiredDomainProperty, resultMessage);
		assertTrue(matches);
	}
	
	@Test
	public void testNegativeDomainPropertyMatch() {

		List<URI> offeredDomainProperty = buildDomainProperties(Geo.lat);
		List<URI> requiredDomainProperty = buildDomainProperties(Geo.lng);
		
		List<MatchingResultMessage> resultMessage = new ArrayList<>();
		
		boolean matches = new DomainPropertyMatch().match(offeredDomainProperty, requiredDomainProperty, resultMessage);
		assertFalse(matches);
	}
	
	private List<URI> buildDomainProperties(String name) {
		return Arrays.asList(URI.create(name));
	}
}