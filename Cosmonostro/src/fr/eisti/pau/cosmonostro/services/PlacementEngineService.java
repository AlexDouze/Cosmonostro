package fr.eisti.pau.cosmonostro.services;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import uk.co.geolib.geolib.C2DLine;
import uk.co.geolib.geolib.C2DLineBase;
import uk.co.geolib.geolib.C2DLineBaseSet;
import uk.co.geolib.geolib.C2DPoint;
import uk.co.geolib.geolib.C2DVector;
import uk.co.geolib.geolib.CGrid;
import uk.co.geolib.geopolygons.C2DHoledPolygon;
import uk.co.geolib.geopolygons.C2DPolygon;
import fr.eisti.pau.cosmonostro.domain.Building;
import fr.eisti.pau.cosmonostro.domain.Module;
import fr.eisti.pau.cosmonostro.domain.Plot;

public class PlacementEngineService {

	public C2DPolygon ajustPolygon(C2DPolygon poly, boolean expand, double moduleWidth){
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

	public List<C2DPolygon> innerDomainFromPlot2(Plot plot, double moduleWidth){
		
		C2DHoledPolygon domain = new C2DHoledPolygon(ajustPolygon(plot, false, moduleWidth));
		
		List<C2DPolygon> innerPolygons = new ArrayList<C2DPolygon>(); 
		for (Building build : plot.getInnerBuildings()) {
			innerPolygons.add(ajustPolygon(build, true, moduleWidth));
		}

//		for (Module mod : plot.getModules()){
//			innerPolygons.add(ajustPolygon(mod, true, moduleWidth));
//		}
		
		innerPolygons.add(ajustPolygon(plot, false, moduleWidth));
		
		return innerPolygons;
	}
	
	public List<C2DPolygon> innerDomainFromPlot(Plot plot, double moduleWidth){
		CGrid grid = new CGrid();
		// TODO: changer la grid en fonction de la taille du module!!
		grid.SetGridSize(0.0001);

		boolean fusion = true;
		boolean fusionParcelle = true;
		int i, j;

		List<C2DPolygon> innerPolygons = new ArrayList<C2DPolygon>(); 
		for (Building build : plot.getInnerBuildings()) {
			innerPolygons.add(ajustPolygon(build, true, moduleWidth));
		}

		for (Module mod : plot.getModules()){
			innerPolygons.add(ajustPolygon(mod, true, moduleWidth));
		}


		while(fusion){
			fusion = false;
			i = 0;
			while(i < innerPolygons.size()){
				j = i +1;
				while (j < innerPolygons.size()){
					ArrayList<C2DHoledPolygon> res = new ArrayList<C2DHoledPolygon>();
					innerPolygons.get(i).GetUnion(innerPolygons.get(j), res, grid);
					if(res.size() == 0){
						j++;
					}else if (res.size() == 1){
						fusion = true;
						innerPolygons.set(i, res.get(0).getRim());
						innerPolygons.remove(j);
					}else{
						j++;
						System.out.println("Error union polygon "+res.size());
					}
				}
				i++;
			}
		}

		List<C2DPolygon> parcelles = new ArrayList<C2DPolygon>();
		List<C2DPolygon> innerPolygons2 = new ArrayList<C2DPolygon>();
		parcelles.add(ajustPolygon(plot, false, moduleWidth));

//		while(fusionParcelle){
//			fusionParcelle = false;
			for(C2DPolygon building : innerPolygons){
				i=0;
				while(i < parcelles.size()){
					ArrayList<C2DHoledPolygon> res = new ArrayList<C2DHoledPolygon>();
					parcelles.get(i).GetNonOverlaps(building, res, grid);
					if(res.size() == 0){
						//parcelles.get(i) dans building ou deux polygonnes cote à cote
						res = new ArrayList<C2DHoledPolygon>();
						parcelles.get(i).GetUnion(building, res, grid);
						if(res.size()==0){
							//deux cote à cote
							innerPolygons2.add(building);
							i++;
						}else{
							//parcelles.get(i) dans building
							parcelles.remove(i);
						}
					}else{
						//building dans parcelles.get(i) 
						//ou intersection entre les deux et on a que la partie qui nous interesse
						parcelles.set(i, res.get(0).getRim());
						for (j = 1; j< res.size();j++){
							parcelles.add(res.get(j).getRim());
						}
						for (j=0; j<res.size(); j++){
							for(int k = 0; k < res.get(j).getHoleCount(); k++){
								innerPolygons2.add(res.get(j).GetHole(k));
							}
								//parcourir tout les hole dans res.get(j)
								//et les mettres dans innerPolyg2
						}
						i++;
//						fusionParcelle = true;
//						ArrayList<C2DHoledPolygon> res2 = new ArrayList<C2DHoledPolygon>();
//						building.GetNonOverlaps(parcelles.get(i), res2, grid);
//						if(res2.size()==0){
//							//building dans parcelles.get(i)
//							innerPolygons2.add(building);
//							i++;
//						}else{
//							//une partie de building est au dessus de parcelles.get(i)
//							parcelles.set(i, res.get(0).getRim());
//							for (j = 1; j< res.size();j++){
//								parcelles.add(res.get(j).getRim());
//							}
//						}
					}
				}
			}
			innerPolygons = new ArrayList<C2DPolygon>(innerPolygons2);
			innerPolygons2 = new ArrayList<C2DPolygon>();
//		}

		innerPolygons.addAll(parcelles);

		return innerPolygons;

		//		List<C2DPolygon> innerDomains = new ArrayList<C2DPolygon>();
		//		C2DPolygon domaineParcelle=ajustPolygon(plot, false, moduleWidth);
		//		C2DPolygon domaineTmp;
		//		for (Building build : plot.getInnerBuildings()) {
		//			innerDomains.add(ajustPolygon(build, true, moduleWidth));
		//		}
		//
		//		for(Module mod : plot.getModules()){
		//			domaineTmp=ajustPolygon(mod, true, moduleWidth);
		//			C2DHoledPolygon holedTmp = new C2DHoledPolygon(domaineTmp);
		//			ArrayList<C2DHoledPolygon> res = new ArrayList<C2DHoledPolygon>();
		//			C2DHoledPolygon holedParcelle = new C2DHoledPolygon(domaineParcelle);
		//			holedTmp.GetNonOverlaps(holedParcelle, res, grid);
		//			if(res.size()>1){
		//				System.out.println("intersection module/parcelle >1");
		//			}else if(res.size() ==1){
		//				res = new ArrayList<C2DHoledPolygon>();
		//				holedParcelle.GetNonOverlaps(holedTmp, res, grid);
		//				domaineParcelle = (C2DPolygon)res.get(0).getRim();
		//			}else{
		//				for(int i =0 ; i < innerDomains.size(); i++){
		//					C2DPolygon poly = innerDomains.get(i);
		//					if(domaineTmp.Crosses(poly)){
		//						C2DHoledPolygon holedPoly = new C2DHoledPolygon(poly);
		//						holedPoly.GetUnion(holedTmp, res, grid);
		//						if(res.size()>1)
		//							System.out.println("union module poly >1");
		//						innerDomains.set(i, res.get(0).getRim());
		//						break;
		//					}
		//				}
		//			}
		//		}
		//
		//		List<C2DPolygon> innerDomains2 = new ArrayList<C2DPolygon>();
		//		for (C2DPolygon poly : innerDomains) {
		//			C2DHoledPolygon holedPoly = new C2DHoledPolygon(poly);
		//			ArrayList<C2DHoledPolygon> res = new ArrayList<C2DHoledPolygon>();
		//			C2DHoledPolygon holedParcelle = new C2DHoledPolygon(domaineParcelle);
		//			
		//			holedPoly.GetNonOverlaps(holedParcelle, res, grid);
		//			
		//			if(res.size() > 1){
		//				System.out.println("Error");
		//			}else if(res.size()==1){
		//				res = new ArrayList<C2DHoledPolygon>();
		//				holedParcelle.GetNonOverlaps(holedPoly, res, grid);
		//				domaineParcelle = (C2DPolygon)res.get(0).getRim();
		//			}else{
		//				innerDomains2.add(poly);
		//			}
		//			
		//			
		//		}
		//		
		//		innerDomains2.add(domaineParcelle);
		//		return innerDomains2;
	}

	public Module randomPoint(Plot plot){
		
		List<C2DPolygon> domain = plot.getDomains();
		
		List<C2DPoint> points = new ArrayList<C2DPoint>();
		List<Double> distances = new ArrayList<Double>();
		List<Boolean> calculDistance = new ArrayList<Boolean>();
		//pour tout les polygones
		for (C2DPolygon poly : domain) {
			ArrayList<C2DPoint> pointsCopy = new ArrayList<C2DPoint>();
			poly.GetPointsCopy(pointsCopy);
			points.add(pointsCopy.get(0));
			calculDistance.add(false);
			for (int i = 1; i < pointsCopy.size(); i++) {
				points.add(pointsCopy.get(i));
				calculDistance.add(true);
			}
			points.add(pointsCopy.get(0));
			calculDistance.add(true);
		}

		int i = 0;

		distances.add(0D);
		double lTotal = 0;
		double lTmp;
		for(i = 1; i < points.size(); i++){
			if(!calculDistance.get(i)){
				lTmp = 0;
			}else{
				lTmp = points.get(i).Distance(points.get(i-1));
			}
			lTotal += lTmp;
			distances.add(lTotal);
		}


		for (i = 0; i < distances.size(); i++) {
			distances.set(i, distances.get(i)/lTotal);
			//System.out.println("("+points.get(i).x+", "+points.get(i).y+") "+calculDistance.get(i)+" "+distances.get(i));
		}

		double rand = Math.random();
		int k=1;
		while(rand > distances.get(k).doubleValue()){
			k++;
		}
		C2DLine line = new C2DLine(points.get(k-1), points.get(k));
		C2DPoint randomPoint = line.GetPointOn((rand-distances.get(k-1))/(distances.get(k)-distances.get(k-1)));

		return new Module(randomPoint, line.vector);
	}
}
