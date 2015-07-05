package de.fzi.cep.sepa.rest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.fzi.cep.sepa.model.impl.graph.SepaDescription;
import org.apache.http.client.ClientProtocolException;

import de.fzi.cep.sepa.model.impl.graph.SecDescription;
import de.fzi.cep.sepa.rest.api.AbstractRestInterface;
import de.fzi.cep.sepa.rest.api.Action;
import de.fzi.cep.sepa.messages.Notification;
import de.fzi.cep.sepa.messages.NotificationType;
import de.fzi.cep.sepa.storage.util.ClientModelTransformer;
import org.apache.shiro.SecurityUtils;
import org.lightcouch.CouchDbClient;

/**
 * HTTP endpoint for actions
 * supported methods: GET, POST, DELETE
 */

@Path("/actions")
public class ActionImpl extends AbstractRestInterface implements Action {
		
	/**
	 * get all stored actions
	 * @return JSON representation of actions
	 */
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public String getAction() {
		List<SecDescription> secs = requestor.getAllSECs();
		return toJson(ClientModelTransformer.toActionClientModel(secs));
	}

	/**
	 * get all actions from current user.
	 * @return
	 */
	@Path("user")
	@GET
	public String getAllUserActions() {
		CouchDbClient dbClientUser = de.fzi.cep.sepa.storage.util.Utils.getCouchDbUserClient();
		List<SecDescription> secs = new ArrayList<>();
		if (SecurityUtils.getSubject().isAuthenticated()) {
			String username = SecurityUtils.getSubject().getPrincipal().toString();
			JsonArray secIds = dbClientUser.view("users/actions").key(username).query(JsonObject.class).get(0).get("value").getAsJsonArray();
			try {
				for (JsonElement secId : secIds) {
					SecDescription sec = requestor.getSECById(secId.getAsString());
					if (sec != null) secs.add(sec);
				}
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		return toJson(ClientModelTransformer.toActionClientModel(secs));
	}
	

	/**
	 * add a new action or update an existing action
	 * @param uri:  URI endpoint that provides a json-ld description
	 * @return validation message
	 */
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public String postAction(@FormParam("uri") String uri) {
		
		SecDescription sec;
		String jsonldDescription = "";
		
		try {
			jsonldDescription = parseURIContent(uri);
		} catch (ClientProtocolException e) {
			return constructErrorMessage(new Notification(NotificationType.UNKNOWN_ERROR.title(), NotificationType.UNKNOWN_ERROR.description(), e.getMessage()));
		} catch (URISyntaxException e) {
			jsonldDescription = uri;
		} catch (IOException e) {
			return constructErrorMessage(new Notification(NotificationType.UNKNOWN_ERROR.title(), NotificationType.UNKNOWN_ERROR.description(), e.getMessage()));
		}
		
		try {
			sec = parseObjectContent(SecDescription.class, jsonldDescription);
			if (requestor.exists(sec)) 
				requestor.update(sec);
			else 
				requestor.storeSEC(sec);
		} catch (Exception e)
		{
			return constructErrorMessage(new Notification(NotificationType.STORAGE_ERROR.title(), NotificationType.STORAGE_ERROR.description(), e.getMessage()));
		}
		return constructSuccessMessage(NotificationType.STORAGE_SUCCESS.uiNotification());
	}

	/**
	 * delete an existing action
	 * @param actionId: the id of the action that should be deleted
	 * @return validation message
	 */
	
	@Path("{actionId}")
	@DELETE
	@Override
	public String deleteAction(@PathParam("actionId") String actionId) {
		try {
			requestor.deleteSEC(requestor.getSECById(actionId));
			return constructSuccessMessage(NotificationType.STORAGE_SUCCESS.uiNotification());
		} catch (URISyntaxException e) {
			return constructErrorMessage(new Notification(NotificationType.STORAGE_ERROR.title(), NotificationType.STORAGE_ERROR.description(), e.getMessage()));
		}
	}
	
	@Path("{actionId}/jsonld")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getActionAsJsonLd(@PathParam("actionId") String actionId)
	{
		try {
			return toJsonLd(requestor.getSECById(actionId));
		} catch (URISyntaxException e) {
			return constructErrorMessage(new Notification(NotificationType.UNKNOWN_ERROR.title(), NotificationType.UNKNOWN_ERROR.description(), e.getMessage()));
		}
	}
}
