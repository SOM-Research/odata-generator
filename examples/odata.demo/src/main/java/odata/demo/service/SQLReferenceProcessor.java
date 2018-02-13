package odata.demo.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Locale;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.processor.ReferenceProcessor;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.core.uri.parser.Parser;

public class SQLReferenceProcessor implements ReferenceProcessor{

	private OData odata;
	private ServiceMetadata serviceMetadata;
	private Storage storage;

	public void init(OData odata, ServiceMetadata serviceMetadata) {
		this.odata = odata;
		this.serviceMetadata = serviceMetadata;
		try {
			storage = new Storage();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void readReference(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat)
			throws ODataApplicationException, ODataLibraryException {
		
	}

	public void createReference(ODataRequest request, ODataResponse response, UriInfo uriInfo,
			ContentType requestFormat) throws ODataApplicationException, ODataLibraryException {

		
		
	}

	public void updateReference(ODataRequest request, ODataResponse response, UriInfo uriInfo,
			ContentType requestFormat) throws ODataApplicationException, ODataLibraryException {
	
		

	String odataPath  = request.getRawODataPath();

	odataPath = odataPath.substring(0, odataPath.lastIndexOf("/"));
	
	UriInfo uriInfo2 = new Parser(serviceMetadata.getEdm(), odata).parseUri(odataPath,null,null);
	UriResourceNavigation uriResourcenNavigation = (UriResourceNavigation) uriInfo2.getUriResourceParts().get(uriInfo2.getUriResourceParts().size() - 1); 
	EdmNavigationProperty targetEdmNavigationProperty = uriResourcenNavigation.getProperty();
	odataPath = odataPath.substring(0, odataPath.lastIndexOf("/"));
	UriInfo uriInfo3 = new Parser(serviceMetadata.getEdm(), odata).parseUri(odataPath, null, null);
	

	EdmEntityType sourceEdmEntityType = null;
	EdmEntityType targetEdmEntityType = null;
	UriResource sourceResource =  uriInfo3.getUriResourceParts().get(uriInfo3.getUriResourceParts().size() - 1);
	if(sourceResource instanceof UriResourceEntitySet){
		sourceEdmEntityType = ((UriResourceEntitySet)sourceResource).getEntityType();
	}
	if(sourceResource instanceof UriResourceNavigation){
		sourceEdmEntityType = ((UriResourceNavigation)sourceResource).getProperty().getType();
	}
	Entity sourceEntity = null;
	try {
		sourceEntity = storage.readEntityData(uriInfo3, sourceEdmEntityType, serviceMetadata);
	} catch (ExpressionVisitException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}

	InputStream requestInputStream = request.getBody();
	ODataDeserializer deserializer = this.odata.createDeserializer(requestFormat);
	DeserializerResult result = deserializer.entityReferences(requestInputStream);
	URI entityReference = result.getEntityReferences().get(0);
	String odataResourcePath = entityReference.toString().replaceFirst(request.getRawBaseUri(),"");
	UriInfo targetUriInfo = new Parser(serviceMetadata.getEdm(), odata).parseUri(odataResourcePath, entityReference.getQuery(), entityReference.getFragment());
	UriResource targetResource =  targetUriInfo.getUriResourceParts().get(targetUriInfo.getUriResourceParts().size() - 1);
	if(targetResource instanceof UriResourceEntitySet){
		targetEdmEntityType = ((UriResourceEntitySet)targetResource).getEntityType();
	}
	if(targetResource instanceof UriResourceNavigation){
		targetEdmEntityType = ((UriResourceNavigation)targetResource).getProperty().getType();
	}
	if(! targetEdmEntityType.equals(targetEdmNavigationProperty.getType()))
		throw new ODataApplicationException("Invalid resource path.", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
	Entity targetEntity = null;
	try {
		targetEntity = storage.readEntityData(targetUriInfo, targetEdmNavigationProperty.getType(), serviceMetadata);
	} catch (ExpressionVisitException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
	
	
	storage.updateEntityReferenceData(sourceEdmEntityType, sourceEntity, targetEdmNavigationProperty , targetEntity);
	response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
	
	}
	

	public void deleteReference(ODataRequest request, ODataResponse response, UriInfo uriInfo)
			throws ODataApplicationException, ODataLibraryException {
		
	}

}


