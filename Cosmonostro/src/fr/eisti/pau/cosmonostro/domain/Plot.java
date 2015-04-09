package fr.eisti.pau.cosmonostro.domain;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import fr.eisti.pau.cosmonostro.util.Geometry;
import uk.co.geolib.geolib.C2DLine;
import uk.co.geolib.geolib.C2DLineBase;
import uk.co.geolib.geolib.C2DLineBaseSet;
import uk.co.geolib.geolib.C2DPoint;
import uk.co.geolib.geolib.C2DVector;
import uk.co.geolib.geolib.CGrid;
import uk.co.geolib.geopolygons.C2DHoledPolygon;
import uk.co.geolib.geopolygons.C2DPolygon;

public class Plot extends C2DPolygon {

	private List<Building> innerBuildings;
	private List<Module> modules;
	private ArrayList<C2DHoledPolygon> domains;
	private double domainMargin;
	private CGrid grid;

	public Plot(C2DPolygon polygon){
		super(polygon);
		grid = new CGrid();
		grid.SetGridSize(0.0001);
		innerBuildings = new LinkedList<Building>();
		modules = new LinkedList<Module>();
		domainMargin = Module.width/2;
		domains = new ArrayList<C2DHoledPolygon>();
		domains.add(new C2DHoledPolygon(Geometry.ajustPolygon(this, false, domainMargin)));
	}

	public List<Building> getInnerBuildings() {
		return innerBuildings;
	}

	public void add(Building building){
		computeNewDomain(Geometry.ajustPolygon(building, true, domainMargin));
		this.innerBuildings.add(building);
	}

	public List<Module> getModules() {
		return modules;
	}

	public void add(Module module){
		computeNewDomain(Geometry.ajustPolygon(module, true, domainMargin));
		this.modules.add(module);
	}

	private void computeNewDomain(C2DPolygon poly){
		C2DHoledPolygon polyHoled = new C2DHoledPolygon(poly);
		int i = 0;
		while( i < domains.size()){
			int j=0;
			C2DHoledPolygon domain = domains.get(i);
			if(domain.Contains(poly)){
				domain.AddHole(poly);
				break;
			}else if(domain.Crosses(poly)){
				ArrayList<C2DHoledPolygon> tmp = new ArrayList<C2DHoledPolygon>();
				domain.GetNonOverlaps(new C2DHoledPolygon(poly), tmp, grid);
				if(!tmp.isEmpty()){
					domains.remove(i);
					for(j = 0 ; j < tmp.size(); j++){
						domains.add(i+j, tmp.get(j));
					}

				}
			}else if(polyHoled.Contains(domain)){
				System.out.println("poly contains domain");
				domains.remove(domain);
			}else if(polyHoled.Crosses(domain.getRim())){
				System.out.println("ok je vois");
			}
			if(j!=0){
				i += j;
			}else{
				i++;
			}
		}
	}

	private List<C2DHoledPolygon> getHoledDomains(){
		return domains;
	}

	public List<C2DPolygon> getDomains(){
		List<C2DPolygon> domainTmp  = new LinkedList<C2DPolygon>();
		List<C2DHoledPolygon> domainList = this.getHoledDomains();
		for (C2DHoledPolygon domains : domainList) {
			domainTmp.add(domains.getRim());
			for(int i = 0 ; i < domains.getHoleCount(); i ++){
				domainTmp.add(domains.GetHole(i));
			}
		}
		return domainTmp;
	}
	
	public boolean isInDomain(C2DPoint point){
		for (C2DHoledPolygon p : this.domains) {
			if(p.Contains(point))
				return true;
		}
		return false;
	}

}
