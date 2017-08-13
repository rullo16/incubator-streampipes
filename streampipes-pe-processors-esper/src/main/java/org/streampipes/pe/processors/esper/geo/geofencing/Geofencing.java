package org.streampipes.pe.processors.esper.geo.geofencing;

import static com.espertech.esper.client.soda.Expressions.property;

import com.espertech.esper.client.soda.EPStatementObjectModel;
import com.espertech.esper.client.soda.Expressions;
import com.espertech.esper.client.soda.FromClause;
import com.espertech.esper.client.soda.PatternEveryExpr;
import com.espertech.esper.client.soda.PatternExpr;
import com.espertech.esper.client.soda.PatternStream;
import com.espertech.esper.client.soda.Patterns;
import com.espertech.esper.client.soda.SelectClause;
import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;
import org.streampipes.wrapper.params.binding.InputStreamParams;
import org.streampipes.wrapper.esper.EsperEventEngine;

import java.util.Arrays;
import java.util.List;

public class Geofencing extends EsperEventEngine<GeofencingParameters> {

	@Override
	protected List<String> statements(GeofencingParameters params) {
		EPStatementObjectModel model = new EPStatementObjectModel();
		
		// select * from pattern [every a=GeoEvent -> b=GeoEvent] where !a.isInside() and b.isInside();
		InputStreamParams leftStream = params.getInputStreamParams().get(0);
		
		model.selectClause(makeSelectClause(params));
		
		PatternEveryExpr p1 = Patterns.everyFilter(fixEventName(leftStream.getInName()), "a");
		PatternExpr p2 = Patterns.filter(fixEventName(leftStream.getInName()), "b");
		
		PatternStream patternStream = PatternStream.create(Patterns.followedBy(p1, p2));
		model.setFromClause(FromClause.create(patternStream));
		model.setWhereClause(Expressions.and(
				Expressions.eq(Expressions.staticMethod(
				Geofencing.class.getName(),
				"isInArea", 
				property("a." +params.getLatitudeMapping()), 
				property("a." +params.getLongitudeMapping()), 
				Expressions.constant(params.getGeofencingData().getLatitude()),
				Expressions.constant(params.getGeofencingData().getLongitude()),
				Expressions.constant(params.getGeofencingData().getRadius())), Expressions.constant(!enter(params))),
				Expressions.eq(Expressions.staticMethod(
						Geofencing.class.getName(),
						"isInArea", 
						property("b." +params.getLatitudeMapping()), 
						property("b." +params.getLongitudeMapping()), 
						Expressions.constant(params.getGeofencingData().getLatitude()),
						Expressions.constant(params.getGeofencingData().getLongitude()),
						Expressions.constant(params.getGeofencingData().getRadius())), Expressions.constant(enter(params))),
				Expressions.eq(Expressions.property("a." +params.getPartitionMapping()), Expressions.property("b." +params.getPartitionMapping()))));
		
		return Arrays.asList(model.toEPL());
	}
	
	private SelectClause makeSelectClause(GeofencingParameters params) {
		SelectClause select = SelectClause.create();
		for(String fieldName : params.getInputStreamParams().get(0).getAllProperties()) {
			select.add(Expressions.property("b." +fieldName), fieldName);
		}
		select.add(Expressions.currentTimestamp(), "geofencingTime");
		select.add(Expressions.constant(enter(params)), "insideGeofence");
		return select;
	}
	
	private boolean enter(GeofencingParameters params) {
		return params.getOperation() == GeofencingOperation.ENTER;
	}
	
	public static synchronized boolean isInArea(double latitude, double longitude, double locationLatitude, double locationLongitude, int radius) {
		LatLng point1 = new LatLng(latitude, longitude);
	    LatLng point2 = new LatLng(locationLatitude, locationLongitude);

	    double distance = LatLngTool.distance(point1, point2, LengthUnit.METER);
	    return distance <= radius ? true : false;
	}

}
