package com.emlogis.rest.resources;

import com.emlogis.server.services.ESClientService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;

/**
 * REST resource to browse Logs stored in ElasticSearch
 * URL pattern is:
 * host:port/emlogis-application/rest/logs/{tenantId}/{type}/ops/search where
 * - tenantId = customer id OR System for audit records that are not associated to any customer (like login failures)
 * - type = 'audit' for audit logs. (more types can be supported)
 * - POST data specify via a SearchQueryParams object, parameters for selecting returned attributes, paging, orderby, filters, etc 
 * 
 * @author EmLogis
 *
 */
@Path("/logs")
@Stateless
@LocalBean
public class LogResource {
	
	private final Logger logger = LoggerFactory.getLogger(LogResource.class);
	
    @EJB
    private ESClientService esClientService;

    private Client esclient;		// elastic search client

//    @Authenticated
    @Path("/{tenantId}/{type}/ops/search")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public /*SearchResponse*/ String searchObjects( @PathParam("tenantId") String tenantId,  @PathParam("type") String type, SearchQueryParams queryParams) throws IOException {
    				
		String	idx = StringUtils.lowerCase(tenantId);
		String	idxType = type;				// search index: /tenantId/type
		
		// build query
		SearchRequestBuilder rb = esClientService.getESClient().prepareSearch(idx)
        .setTypes(idxType)
        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
//        .setQuery(QueryBuilders.matchQuery("type", abcd))             	
        .setFrom(queryParams.getFrom()).setSize(queryParams.getLimit()).setExplain(true);	

		if( ! StringUtils.isBlank( queryParams.getQuery())){	
	        rb.setPostFilter( FilterBuilders.queryFilter( QueryBuilders.queryString( queryParams.getQuery())));
		}
		if( ! StringUtils.isBlank( queryParams.getSelect())){
			String fields[] = queryParams.getSelect().split(",");
			for( String field : fields){
				if( ! StringUtils.isBlank(field)){  
					rb.addFields(field); 
//					logger.debug("Adding field: " + field + " to search");
				}
			}
		}
		if( ! StringUtils.isBlank( queryParams.getOrderBy())){
			// add sort information
			rb.addSort( queryParams.getOrderBy(), queryParams.getOrderDir());
		}
		// execute it
        SearchResponse response = rb.execute().actionGet();
		logger.debug( "hits: " + response.getHits().getTotalHits());
		
		// for some reason, returning directly the response doesn't work 
		// (exception thrown by container after response is serialized to json, resulting in a 500 error)
		// thus we have to serialize it here and send the response back as a string.
		return ESResponseToJson( response);  	
    }
 
//  @Authenticated
    @Path("/{tenantId}/{type}/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public /*SearchResponse*/ String getObject( @PathParam("tenantId") String tenantId,  @PathParam("type") String type, @PathParam("id") String id) throws IOException {
    	
		String	idx = StringUtils.lowerCase(tenantId);
		String	idxType = type;				// search index: /tenantId/type
		GetResponse response = esClientService.getESClient().prepareGet(idx, idxType, id)
		.execute()
		.actionGet();
	
		// for some reason, returning directly the response doesn't work 
		// (exception thrown by container after response is serialized to json, resulting in a 500 error)
		// thus we have to serialize it here and send the response back as a string.
		return response.getSourceAsString();
    }
    
    protected String ESResponseToJson( Object response) throws IOException{
		String s = null;
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder();
			builder.startObject();
			if( response instanceof SearchResponse){
				((SearchResponse)response).toXContent(builder, ToXContent.EMPTY_PARAMS);
			}
			else if( response instanceof GetResponse){
				((GetResponse)response).toXContent(builder, ToXContent.EMPTY_PARAMS);
			}
			else{
				return null;
			}			
			builder.endObject();
			s = builder.string();					
			logger.debug( "\n\nResult: " + s);
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
		return s;  	
    	
    }
    
}

/**
 * Simple structure to capture query parameters
 *
 */
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
class SearchQueryParams{
	
	@JsonProperty(required = false)
	private String		select = null;		// comma separated list of fields to return (all by default)

	private	String		query;				
	
	@JsonProperty(required = false)
	private String		orderBy = null;

	@JsonProperty(required = false)
	private SortOrder	orderDir = SortOrder.ASC;
	
	@JsonProperty(required = false)
	private int			from  = 0;
	
	@JsonProperty(required = false)
	private int 		limit = 20;
	
	public SearchQueryParams() {
		super();
	}

	public String getSelect() {
		return select;
	}

	public void setSelect(String select) {
		this.select = select;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	public SortOrder getOrderDir() {
		return orderDir;
	}

	public void setOrderDir(SortOrder orderDir) {
		this.orderDir = orderDir;
	}

	public int getFrom() {
		return from;
	}

	public void setFrom(int from) {
		this.from = from;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}	
	
}

