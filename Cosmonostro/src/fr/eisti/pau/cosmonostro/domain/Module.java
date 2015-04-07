package fr.eisti.pau.cosmonostro.domain;

import java.util.ArrayList;

import uk.co.geolib.geolib.C2DPoint;
import uk.co.geolib.geolib.C2DVector;
import uk.co.geolib.geopolygons.C2DPolygon;

public class Module extends C2DPolygon {

	public static double width = 20D;
	private C2DVector orientation;
	private C2DPoint centre;
	
	public Module(C2DPoint center, C2DVector orientation){
		super();
		
		
		this.centre = center;
		this.orientation = orientation;
		
		orientation.SetLength(width);
		
		ArrayList<C2DPoint> points = new ArrayList<C2DPoint>();
		
		points.add(new C2DPoint(center.x+width/2, center.y+width/2));
		
		points.add(new C2DPoint(center.x+width/2, center.y-width/2));		

		points.add(new C2DPoint(center.x-width/2, center.y+width/2));
		
		points.add(new C2DPoint(center.x-width/2, center.y-width/2));
		
		this.Create(points, true);
		//attention ne marche que si le module est carre
		this.RotateToRight(orientation.AngleFromNorth(), center);
	}

	public C2DVector getOrientation() {
		return orientation;
	}

	public C2DPoint getCentre() {
		return centre;
	}
}
