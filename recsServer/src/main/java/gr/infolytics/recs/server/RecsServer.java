package gr.infolytics.recs.server;


import gr.infolytics.recs.server.jaxb.Collection;
import gr.infolytics.recs.server.jaxb.ItemType;
import gr.infolytics.recs.server.jaxb.ObjectFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.spy.memcached.MemcachedClient;
import net.spy.memcached.OperationTimeoutException;
import net.spy.memcached.internal.OperationFuture;

import org.apache.log4j.Logger;


import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.amazonaws.services.simpledb.model.DuplicateItemNameException;
import com.amazonaws.services.simpledb.model.InvalidParameterValueException;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.MissingParameterException;
import com.amazonaws.services.simpledb.model.NoSuchDomainException;
import com.amazonaws.services.simpledb.model.NumberDomainAttributesExceededException;
import com.amazonaws.services.simpledb.model.NumberDomainBytesExceededException;
import com.amazonaws.services.simpledb.model.NumberItemAttributesExceededException;
import com.amazonaws.services.simpledb.model.NumberSubmittedAttributesExceededException;
import com.amazonaws.services.simpledb.model.NumberSubmittedItemsExceededException;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.sun.jersey.api.json.JSONWithPadding;

@Path("/items")
public class RecsServer {
	private Logger logger = Logger.getLogger(getClass());
	private static final String SIMPLE_DB_DOMAIN = "SintagesPareasCorrelations";
	private static final String BUCKET_NAME = "sintagespareas";
    private static final String AWS_CREDENTIALS = "AwsCredentials.properties";
    private static final String MEMCACHED_SERVER = "ip-10-122-75-2.ec2.internal";
//    private static final String MEMCACHED_SERVER = "ec2-184-73-93-13.compute-1.amazonaws.com";
    private static final int MEMCACHED_PORT = 11211;
   
    private Collection getRecsFromSimpleDB(String sourceItemId) {
    	AmazonSimpleDB sdb = null;
    	try {
    		sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
    				RecsServer.class.getResourceAsStream(AWS_CREDENTIALS)));
    	}
    	catch (IOException ex) {
    		String errorMessage = "Cannot connect to Amazon SimpleDB, check credentials";
    		Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
    		throw new WebApplicationException(response);
    	}
    	String selectExpression = "select * from `" + SIMPLE_DB_DOMAIN + "` where Source = '" + sourceItemId + "'";
        SelectRequest selectRequest = new SelectRequest(selectExpression);
        List<AttributeObject> targets = new ArrayList<AttributeObject>();
        for (Item item : sdb.select(selectRequest).getItems()) {
        	double score = 0.0;
        	String targetItemId = null;
        	String shortURL = null;
        	String greekTitle = null;
            for (Attribute attribute : item.getAttributes()) {
            	if (attribute.getName().equals("Target")) {
            		targetItemId = attribute.getValue();
            	}
            	else if (attribute.getName().equals("Score")) {
            		score = Double.parseDouble(attribute.getValue());
            	}
            	else if (attribute.getName().equals("ShortURL")) {
            		shortURL = attribute.getValue();
            	}
            	else if (attribute.getName().equals("GreekTitle")) {
            		greekTitle = attribute.getValue();
            	}
            }
            
            if (targetItemId != null && shortURL != null && greekTitle != null) {
            	ItemType itemType = new ItemType();
            	itemType.setId(targetItemId);
            	itemType.setShortUrl(shortURL);
            	itemType.setGreekTitle(greekTitle);
            	targets.add(new AttributeObject(itemType, score));
            }
            
        }
        Collections.sort(targets);
        
        ObjectFactory objFactory = new ObjectFactory();
    	Collection collection = objFactory.createCollection();
        List<ItemType> targetIds = collection.getItem();
        for (AttributeObject attObject : targets) {
        	targetIds.add(attObject.getItem());
        }
        
        return collection;
    }
    
    private Collection getRecsInternal(String sourceItemId) throws WebApplicationException {
    	if (sourceItemId == null || sourceItemId.isEmpty()) {
    		String errorMessage = "Invalid source itemId entered";
    		Response response = Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).build();
    		throw new WebApplicationException(response);
    	}
    	
    	MemcachedClient client = null;
    	try {
        	client = new MemcachedClient(new InetSocketAddress(MEMCACHED_SERVER, MEMCACHED_PORT));
        	Object cacheCollection = null;
        	try {
        		cacheCollection = client.get(sourceItemId);
        		if (cacheCollection != null) {
                	return (Collection) cacheCollection;
                } 
        	}
        	catch (OperationTimeoutException ex) {
        		logger.error("Timeout accessing memcached ... using simpleDB");
        	}
            
        }
        catch (IOException ex) {
        	logger.error("Cannot insantiate memcached client ... using simpleDB");
        }
    	
        Collection simpleDBCollection = getRecsFromSimpleDB(sourceItemId);
        if (client != null) {
        	try {
        		client.set(sourceItemId, 36000, simpleDBCollection);
        		client.shutdown(10, TimeUnit.SECONDS);
        	}
        	catch (Exception ex) {
        		logger.error("Cannot write to memcached " + MEMCACHED_SERVER + " port " + MEMCACHED_PORT);
        	}
        }
    	
    	
        
        return simpleDBCollection;
       
    }
    
    @GET
	@Path("{sourceItemId}")
    @Produces({"application/x-javascript", MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public JSONWithPadding getItems(@PathParam("sourceItemId") String sourceItemId,
    								@QueryParam("jsoncallback") @DefaultValue("jsoncallback") String jsoncallback) {
    	Collection collection = getRecsInternal(sourceItemId);
    	
    	return new JSONWithPadding(
    	        new GenericEntity<Collection>(collection) {
    	        }, jsoncallback);
    }
    
    
    private void deleteSimpleDBDomain(AmazonSimpleDB sdb, String domainToDelete) throws WebApplicationException {
    	try {
    		for (String domain : sdb.listDomains().getDomainNames()) {
    			if (domain.equals(domainToDelete)) {
    				sdb.deleteDomain(new DeleteDomainRequest(domainToDelete));
    				break;
    			}
    		}
    	}
    	catch (MissingParameterException ex) {
            String errorMessage = "Cannot delete domain in SimpleDB, missing parameter " + ex.getStackTrace();
    		Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
    		throw new WebApplicationException(response);
        }
    	catch (AmazonServiceException ase) {
    		String errorMessage = "Cannot delete domain in SimpleDB, Amazon Service error";
    		Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
    		throw new WebApplicationException(response);
        }
    	catch (AmazonClientException ace) {
            String errorMessage = "Caught an AmazonClientException, which means the client encountered "
                + "a serious internal problem while trying to communicate with SimpleDB, "
                + "such as not being able to access the network.";
    		Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
    		throw new WebApplicationException(response);
        }
    	
    }
    
    private void createSimpleDBDomain(AmazonSimpleDB sdb, String domainToDelete) throws WebApplicationException {
    	try {
    		sdb.createDomain(new CreateDomainRequest(domainToDelete));
    	}
    	catch (AmazonServiceException ase) {
    		String errorMessage = "Cannot create domain in SimpleDB, Amazon Service error";
    		Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
    		throw new WebApplicationException(response);
        }
    	catch (AmazonClientException ace) {
            String errorMessage = "Caught an AmazonClientException, which means the client encountered "
                + "a serious internal problem while trying to communicate with SimpleDB, "
                + "such as not being able to access the network.";
    		Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
    		throw new WebApplicationException(response);
        }
    }
    
    private void deleteFromCache(MemcachedClient memcachedClient, String sourceItemId) {
    	OperationFuture<Boolean> success = memcachedClient.delete(sourceItemId);

    	try {
    	    if (!success.get()) {
    	        logger.error("Delete failed!");
    	    }

    	}
    	catch (Exception e) {
    	    logger.error("Failed to delete " + e);
    	}
    }
    
    /**
     * Gets a file stored in S3 and creates a SimpleDB domain
     * 
     * @param filename
     * @return
     */
    @POST
    @Path("/create")
    @Consumes({"application/x-www-form-urlencoded"})
    public Response createRecs(@FormParam("correlations") String correlationsFilename,
    						   @FormParam("metadata")     String metadataFilename) {
    	
    	// Get metadata
    	HashMap<String, ItemType> metadata = getMetadata(metadataFilename);
    	
    	// Load the correlation data
    	AmazonS3 s3 = null;
    	try {
    		s3 = new AmazonS3Client(new PropertiesCredentials(
    				RecsServer.class.getResourceAsStream(AWS_CREDENTIALS)));
    	}
    	catch (IOException ex) {
    		String errorMessage = "Cannot connect to Amazon S3, check credentials";
    		Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
    		throw new WebApplicationException(response);
    	}
    	
    	S3Object object = null;
    	try {
    		object = s3.getObject(new GetObjectRequest(BUCKET_NAME, correlationsFilename));
    	}
    	catch (AmazonServiceException ase) {
    		String errorMessage = "Cannot get file from Amazon S3, check filename";
    		Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
    		throw new WebApplicationException(response);
        }
    	catch (AmazonClientException ace) {
            String errorMessage = "Caught an AmazonClientException, which means the client encountered "
                + "a serious internal problem while trying to communicate with S3, "
                + "such as not being able to access the network.";
    		Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
    		throw new WebApplicationException(response);
        }
    	
    	// Set-up writing in SimpleDB
    	AmazonSimpleDB sdb = null;
    	try {
    		sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
    				RecsServer.class.getResourceAsStream(AWS_CREDENTIALS)));
    	}
    	catch (IOException ex) {
    		String errorMessage = "Cannot connect to Amazon SimpleDB, check credentials";
    		Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
    		throw new WebApplicationException(response);
    	}
    	
    	// Delete SimpleDB domain
    	deleteSimpleDBDomain(sdb, SIMPLE_DB_DOMAIN);
    	
    	// Create SimpleDB domain
    	createSimpleDBDomain(sdb, SIMPLE_DB_DOMAIN);
        
    	MemcachedClient client = null;
    	try {
        	client = new MemcachedClient(new InetSocketAddress(MEMCACHED_SERVER, MEMCACHED_PORT));
        }
        catch (IOException ex) {
        	logger.error("Cannot insantiate memcached client");
        	String errorMessage = "Cannot insantiate memcached client";
        	Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
    		throw new WebApplicationException(response);
        }
        
    	// Put data into a domain
        List<ReplaceableItem> recsPairs = new ArrayList<ReplaceableItem>();
    	try {
    		BufferedReader reader = new BufferedReader(new InputStreamReader(object.getObjectContent()));
    		int cnt = 0;
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                	break;
                }

                String[] fields = line.split(";");
                String sourceItemId = fields[0];
                
                // Delete from cache
                deleteFromCache(client, sourceItemId);
                
                for (int i = 1; i < fields.length - 1; i = i + 2) {
                	String targetItemId = fields[i];
                	float score = Float.parseFloat(fields[i+1]);
                	String itemName = sourceItemId + "_" + targetItemId;
                	
                	ItemType item = metadata.get(targetItemId);
                	if (item != null) {
                		recsPairs.add(new ReplaceableItem(itemName).withAttributes(
                                new ReplaceableAttribute("Source", sourceItemId, true),
                                new ReplaceableAttribute("Target", targetItemId, true), 
                                new ReplaceableAttribute("Score", Float.toString(score), true),
                                new ReplaceableAttribute("ShortURL", item.getShortUrl(), true),
                                new ReplaceableAttribute("GreekTitle", item.getGreekTitle(), true)));
                	}
                	else {
                		recsPairs.add(new ReplaceableItem(itemName).withAttributes(
                                new ReplaceableAttribute("Source", sourceItemId, true),
                                new ReplaceableAttribute("Target", targetItemId, true), 
                                new ReplaceableAttribute("Score", Float.toString(score), true)));
                	}
                	
                	
                	if (recsPairs.size() == 25) {
                		writeSimpleDB(sdb, recsPairs);
                    	recsPairs = new ArrayList<ReplaceableItem>();
                	}
                	cnt ++;
                }
            }
            if (recsPairs.size() > 0) {
        		writeSimpleDB(sdb, recsPairs);
            }
            
            logger.info("Read " + cnt + " lines");
            
            reader.close();
    	}
    	catch (Exception ex) {
    		String errorMessage = "Error while reading the file with correlations";
    		Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
    		throw new WebApplicationException(response);
    	}
    	
    	
    	return Response.ok().build();
    }
    
    private HashMap<String, ItemType> getMetadata(String metadataFilename) throws WebApplicationException {
    	AmazonS3 s3 = null;
    	try {
    		s3 = new AmazonS3Client(new PropertiesCredentials(
    				RecsServer.class.getResourceAsStream(AWS_CREDENTIALS)));
    	}
    	catch (IOException ex) {
    		String errorMessage = "Cannot connect to Amazon S3, check credentials";
    		Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
    		throw new WebApplicationException(response);
    	}
    	
    	S3Object object = null;
    	try {
    		object = s3.getObject(new GetObjectRequest(BUCKET_NAME, metadataFilename));
    	}
    	catch (AmazonServiceException ase) {
    		String errorMessage = "Cannot get file from Amazon S3, check filename";
    		Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
    		throw new WebApplicationException(response);
        }
    	catch (AmazonClientException ace) {
            String errorMessage = "Caught an AmazonClientException, which means the client encountered "
                + "a serious internal problem while trying to communicate with S3, "
                + "such as not being able to access the network.";
    		Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
    		throw new WebApplicationException(response);
        }
    	HashMap<String, ItemType> hm = new HashMap<String, ItemType>();
    	String line = null;
    	try {
    		BufferedReader reader = new BufferedReader(new InputStreamReader(object.getObjectContent()));
            while (true) {
            	line = reader.readLine();
                if (line == null) {
                	break;
                }

                String[] fields = line.split(";");
                if (fields.length != 3) {
                	continue;
                }
                String itemId = fields[0];
                String shortUrl = fields[1];
                String greekTitle = fields[2];
                
                ItemType item = new ItemType();
                item.setId(itemId);
                item.setShortUrl(shortUrl);
                item.setGreekTitle(greekTitle);
                
                hm.put(itemId, item);
            }
            reader.close();
    	}
    	catch (Exception ex) {
    		String errorMessage = "Error while reading the file with metadata at line \"" + line + "\"";
    		Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
    		throw new WebApplicationException(response);
    	}
    	
    	return hm;
    }
    
    private void writeSimpleDB(AmazonSimpleDB sdb, List<ReplaceableItem> recsPairs) throws WebApplicationException {
    	try {
    		sdb.batchPutAttributes(new BatchPutAttributesRequest(SIMPLE_DB_DOMAIN, recsPairs));
    	}
		catch (DuplicateItemNameException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + SIMPLE_DB_DOMAIN + " because of duplicate item names";
    		Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
    		throw new WebApplicationException(response);
    	}
		catch (InvalidParameterValueException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + SIMPLE_DB_DOMAIN + " because of invalid parameter value";
    		Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
    		throw new WebApplicationException(response);
    	}
		catch (NumberDomainBytesExceededException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + SIMPLE_DB_DOMAIN + " because max number of domain bytes exceeded";
    		Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
    		throw new WebApplicationException(response);
    	}
		catch (NumberSubmittedItemsExceededException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + SIMPLE_DB_DOMAIN + " because max number of submitted items exceeded";
    		Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
    		throw new WebApplicationException(response);
    	}
		catch (NumberSubmittedAttributesExceededException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + SIMPLE_DB_DOMAIN + " because max number of submitted attributes exceeded";
    		Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
    		throw new WebApplicationException(response);
    	}
		catch (NumberDomainAttributesExceededException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + SIMPLE_DB_DOMAIN + " because max number of domain attributes exceeded";
    		Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
    		throw new WebApplicationException(response);
    	}
		catch (NumberItemAttributesExceededException ex) {
    		String errorMessage = "Cannot write to SimpleDB domain " + SIMPLE_DB_DOMAIN + " because max number of item attributes exceeded";
    		Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
    		throw new WebApplicationException(response);
    	}
    	catch (NoSuchDomainException ex) {
    		String errorMessage = "Cannot find SimpleDB domain " + SIMPLE_DB_DOMAIN;
    		Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
    		throw new WebApplicationException(response);
    	}
    	catch (AmazonServiceException ase) {
    		String errorMessage = "Cannot write in SimpleDB, Amazon Service error (" + ase.getErrorType().toString() + ")";
    		Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
    		throw new WebApplicationException(response);
        }
    	catch (AmazonClientException ace) {
            String errorMessage = "Caught an AmazonClientException, which means the client encountered "
                + "a serious internal problem while trying to communicate with SimpleDB, "
                + "such as not being able to access the network.";
    		Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
    		throw new WebApplicationException(response);
        }
    	catch (Exception ex) {
    		String errorMessage = "Cannot write to SimpleDB";
    		Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
    		throw new WebApplicationException(response);
    	}
    }
}
