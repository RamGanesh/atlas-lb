package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.ByIdOrName;

import java.util.HashSet;
import org.openstack.atlas.service.domain.entities.Node;
import java.util.Set;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Blacklist;
import org.openstack.atlas.service.domain.entities.BlacklistItem;
import org.openstack.atlas.api.faults.HttpResponseBuilder;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.repository.ValidatorRepository;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.results.ValidatorResult;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

public class BlackListResource extends ManagementDependencyProvider {
    private LoadBalancerResource loadBalancerResource;
    private HttpHeaders requestHeaders;
    private int id;

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON})
    public Response retrieveBlacklist() {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }

        try {
            List<org.openstack.atlas.service.domain.entities.BlacklistItem> blacklistItems = blacklistRepository.getAllBlacklistItems();
            Blacklist blacklist = new Blacklist();

            for (org.openstack.atlas.service.domain.entities.BlacklistItem blacklistItem : blacklistItems) {
                blacklist.getBlacklistItems().add(dozerMapper.map(blacklistItem, org.openstack.atlas.docs.loadbalancers.api.management.v1.BlacklistItem.class));
            }
            return Response.status(200).entity(blacklist).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addBlacklistItem(Blacklist blackList) {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }

        ValidatorResult result = ValidatorRepository.getValidatorFor(Blacklist.class).validate(blackList, HttpRequestType.POST);

        if (!result.passedValidation()) {
            return Response.status(400).entity(HttpResponseBuilder.buildBadRequestResponse("Validation fault",
                    result.getValidationErrorMessages())).build();
        }

        try {
            List<BlacklistItem> blitems = new ArrayList<BlacklistItem>();
            for (org.openstack.atlas.docs.loadbalancers.api.management.v1.BlacklistItem bli : blackList.getBlacklistItems()) {
                blitems.add(dozerMapper.map(bli, BlacklistItem.class));
            }

            blacklistRepository.saveBlacklist(blitems);


          /*  EsbRequest req = new EsbRequest();
            req.setBlacklistItems(blitems);
            OperationResponse response = getManagementEsbService().callLoadBalancingOperation(Operation.CREATE_BLACKLIST_ITEM, req);

            if (response.isExecutedOkay()) {
                return Response.status(Response.Status.ACCEPTED).build();
            } else {
                return ResponseFactory.getErrorResponse(response);
            } */
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, null, null);
        }
    }

    @DELETE
    @Path("{id: [1-9][0-9]*}")
    public Response deleteBlackListItem(@PathParam("id") int id) {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }
        try {

            org.openstack.atlas.service.domain.entities.BlacklistItem domainBlackListItem = new org.openstack.atlas.service.domain.entities.BlacklistItem();
            domainBlackListItem.setId(id);
            blackListService.deleteBlackList(domainBlackListItem);

          /*  EsbRequest req = new EsbRequest();
            req.setBlacklistItem(domainBlackListItem);

            OperationResponse response = getManagementEsbService().callLoadBalancingOperation(Operation.DELETE_BLACKLIST_ITEM, req);

            if (response.isExecutedOkay()) {
                return Response.status(Response.Status.ACCEPTED).build();
            } else {
                return ResponseFactory.getErrorResponse(response);
            } */
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @GET
    @Path("isBlackListed")
    public Response isBlackListedNode(@QueryParam("ipAddress")String ipAddress) {
        Set<Node> nodes = new HashSet<Node>();
        Node node = new Node();
        ByIdOrName bion = new ByIdOrName();
        node.setIpAddress(ipAddress);
        nodes.add(node);
        try {
            node = blackListService.getBlackListedItemNode(nodes);
        }catch(Exception ex){
            return ResponseFactory.getErrorResponse(ex, null,null);
        }
        if(node == null) {
            bion.setName("false");
        }else{
            bion.setName("true");
        }
        return Response.status(200).entity(bion).build();
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
