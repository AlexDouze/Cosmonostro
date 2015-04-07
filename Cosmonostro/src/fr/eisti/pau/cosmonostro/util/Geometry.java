package fr.eisti.pau.cosmonostro.util;

import java.util.ArrayList;
import java.util.List;

import uk.co.geolib.geolib.C2DLine;
import uk.co.geolib.geolib.C2DLineBase;
import uk.co.geolib.geolib.C2DLineBaseSet;
import uk.co.geolib.geolib.C2DPoint;
import uk.co.geolib.geolib.C2DVector;
import uk.co.geolib.geopolygons.C2DPolygon;

public class Geometry {
	
	public static C2DPolygon ajustPolygon(C2DPolygon poly, boolean expand, double moduleWidth){
		C2DPoint centroid = poly.GetCentroid();
		C2DLineBaseSet lines = poly.getLines();
		List<C2DLine> newLines = new ArrayList<C2DLine>();

		for (C2DLineBase line : lines) {
			C2DLine current = (C2DLine) line.CreateCopy();
			C2DVector norm = new C2DVector(-current.vector.j, current.vector.i);
			C2DVector vect = new C2DVector(centroid, current.GetPointFrom());

			//inversion de la normale
			if((norm.Dot(vect)>0 && !expand) || (norm.Dot(vect)<0 && expand )){
				norm.Set(-norm.i, -norm.j);
			}

			//normalisation de la normale
			norm.SetLength(moduleWidth);

			current.Move(norm);
			current.GrowFromCentre(2*moduleWidth);
			newLines.add(current);
		}

		//calcul des nouveaux sommet du poly
		ArrayList<C2DPoint> points = new ArrayList<C2DPoint>();
		ArrayList<C2DPoint> intersectPoints = new ArrayList<C2DPoint>();
		newLines.get(newLines.size()-1).Crosses(newLines.get(0), intersectPoints);
		if(intersectPoints.size() ==1){
			points.add(intersectPoints.get(0));
		}else{
			System.out.println("damn2"+intersectPoints.size());
		}

		for (int i = 0; i < newLines.size()-1; i++) {
			intersectPoints = new ArrayList<C2DPoint>();
			newLines.get(i).Crosses(newLines.get(i+1), intersectPoints);
			if(intersectPoints.size() ==1){
				points.add(intersectPoints.get(0));
			}else{
				System.out.println("("+newLines.get(i).GetPointFrom().x+", "+newLines.get(i).GetPointFrom().y+") -> "+"("+newLines.get(i).GetPointTo().x+", "+newLines.get(i).GetPointTo().y+")");
				System.out.println("("+newLines.get(i+1).GetPointFrom().x+", "+newLines.get(i+1).GetPointFrom().y+") -> "+"("+newLines.get(i+1).GetPointTo().x+", "+newLines.get(i+1).GetPointTo().y+")");
				System.out.println("damn"+intersectPoints.size());
			}
		}

		C2DPolygon innerDomain = new C2DPolygon(points, false);
		return innerDomain;
	}

}
