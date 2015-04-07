package fr.eisti.pau.cosmonostro.util;

import uk.co.geolib.geolib.C2DRect;

public class Printer {

	
	public static String print(C2DRect rect){
		return "topLeft("+rect.getTopLeft().x+"/"+rect.getTopLeft().y+")"+
			 " bottomRight("+rect.getBottomRight().x+"/"+rect.getBottomRight().y+")"+
			 " width("+rect.Width()+")"+
			 " height("+rect.Height()+")";
	}
	
}
