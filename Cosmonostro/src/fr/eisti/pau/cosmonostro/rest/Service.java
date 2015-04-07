package fr.eisti.pau.cosmonostro.rest;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import fr.eisti.pau.cosmonostro.domain.Plot;
import fr.eisti.pau.cosmonostro.exception.ConcavePolygonException;
import fr.eisti.pau.cosmonostro.services.SVGService;

@Path("/map")
public class Service {

	private static final Logger LOGGER = LoggerFactory.getLogger(Service.class);
	private final SVGService svgService = new SVGService();

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Plot> getMap() throws ConcavePolygonException{

		List<Plot> res = null;
		try{
			String parser = XMLResourceDescriptor.getXMLParserClassName();
			SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
			String uri = "http://localhost:8080/Cosmonostro/svg/basic.svg";
			Document doc = f.createDocument(uri);
			res = svgService.getPlots(doc);
			return res;

		}catch(IOException e){
			LOGGER.error("No default SVG found");
		}
		return null;
	}
	
	public Document getDoc(String fileName){
		try{
			String parser = XMLResourceDescriptor.getXMLParserClassName();
			SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
			String uri = "http://localhost:8080/Cosmonostro/svg/"+fileName;
			Document doc = f.createDocument(uri);
			return doc;

		}catch(IOException e){
			LOGGER.error("No default SVG found");
		}
		return null;
	}
}
