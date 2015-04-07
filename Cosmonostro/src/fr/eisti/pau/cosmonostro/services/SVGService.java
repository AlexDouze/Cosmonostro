package fr.eisti.pau.cosmonostro.services;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.co.geolib.geolib.C2DPoint;
import uk.co.geolib.geolib.C2DRect;
import uk.co.geolib.geopolygons.C2DPolygon;
import fr.eisti.pau.cosmonostro.domain.Building;
import fr.eisti.pau.cosmonostro.domain.Plot;
import fr.eisti.pau.cosmonostro.exception.ConcavePolygonException;

public class SVGService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SVGService.class);


	public List<Plot> getEmptyPlots(Document SVGDoc){

		if(SVGDoc==null){
			LOGGER.error("getPlots: no SVG Document provided");
			throw new IllegalArgumentException();
		}

		List<Plot> plots = new LinkedList<Plot>();

		//recuperation des parcelles
		Element parcellesDOM = SVGDoc.getElementById("PARCELLES");
		NodeList parcelles = parcellesDOM.getElementsByTagName("polyline");
		for (int i = 0; i < parcelles.getLength(); i++) {
			plots.add(getPlotFromNode(parcelles.item(i)));
		}

		return plots;
	}

	public List<Building> getBuildings(Document SVGDoc){

		if(SVGDoc==null){
			LOGGER.error("getBuildings: no SVG Document provided");
			throw new IllegalArgumentException();
		}

		List<Building> buildings = new LinkedList<Building>();

		//recuperation des batis
		Element batiDOM = SVGDoc.getElementById("BATI");
		NodeList batis = batiDOM.getElementsByTagName("polyline");
		for(int i = 0; i < batis.getLength();i++){
			buildings.add(getBuildingFromNode(batis.item(i)));
		}

		return buildings;
	}

	public List<Plot> getPlots(Document SVGDoc) throws ConcavePolygonException{

		List<Plot> plots = new LinkedList<Plot>();

		if(SVGDoc==null){
			LOGGER.error("getMappedBuildings: no SVG Document provided");
			throw new IllegalArgumentException();
		}

		//recuperation des parcelles vides
		Element parcellesDOM = SVGDoc.getElementById("PARCELLES");
		NodeList parcelles = parcellesDOM.getElementsByTagName("polyline");
		for (int i = 0; i < parcelles.getLength(); i++) {
			plots.add(getPlotFromNode(parcelles.item(i)));
		}

		//recuperation des batis
		List<Building> buildings = getBuildings(SVGDoc);

		for (Building building : buildings) {
			for (Plot plot : plots) {
				if(plot.Contains(building)){
					if(building.IsConvex()){
						plot.add(building);
					}else{
						throw new ConcavePolygonException();
//						ArrayList<C2DPolygon> convexBuilding = new ArrayList<C2DPolygon>();
//						building.GetConvexSubAreas(convexBuilding);
//						System.out.println(convexBuilding.size());
//						for (C2DPolygon c2dPolygon : convexBuilding) {
//							System.out.println("saltu");
//							plot.add(new Building(c2dPolygon));
//						}
					}
					
					break;
				}
			}
		}

		return plots;
	}

	private Plot getPlotFromNode(Node node){
		return new Plot(getC2DPolyFromNode(node));
	}

	private Building getBuildingFromNode(Node node){
		return new Building(getC2DPolyFromNode(node));
	}


	private C2DPolygon getC2DRectFromNode(Node node){
		NamedNodeMap attributes = node.getAttributes();

		Double x = Double.parseDouble(attributes.getNamedItem("x").getNodeValue());
		Double y = Double.parseDouble(attributes.getNamedItem("y").getNodeValue());
		Double width = Double.parseDouble(attributes.getNamedItem("width").getNodeValue());
		Double height = Double.parseDouble(attributes.getNamedItem("height").getNodeValue());
		C2DPoint topLeft = new C2DPoint(x, y+height);
		C2DPoint bottomRight = new C2DPoint(x+width, y);
		C2DRect rect = new C2DRect(topLeft, bottomRight);


		return rectToPoly(rect);
	}

	public C2DPolygon rectToPoly(C2DRect rect){
		ArrayList<C2DPoint> points = new ArrayList<C2DPoint>();

		points.add(rect.getTopLeft());
		points.add(rect.GetTopRight());
		points.add(rect.getBottomRight());
		points.add(rect.GetBottomLeft());

		return new C2DPolygon(points, false);


	}

	private C2DPolygon getC2DPolyFromNode(Node node){
		ArrayList<C2DPoint> points = new ArrayList<C2DPoint>();
		NamedNodeMap attributes = node.getAttributes();

		String[] pointsStr = attributes.getNamedItem("points").getNodeValue().split(" ");

		for (String string : pointsStr) {
			if(string != null && string != ""){
				String[] ptStr = string.split(",");
				if(ptStr.length == 2){
					Double x = Double.parseDouble(ptStr[0]);
					Double y = Double.parseDouble(ptStr[1]);
					C2DPoint pt = new C2DPoint(x, y);
					points.add(pt);
				}
			}
		}

		points.remove(points.size()-1);
		C2DPolygon res = new C2DPolygon(points, true);
		return res;
	}
}
