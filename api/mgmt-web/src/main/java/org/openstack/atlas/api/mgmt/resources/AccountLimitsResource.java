package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.Limit;
import org.openstack.atlas.service.domain.entities.AccountLimit;
import org.openstack.atlas.api.faults.HttpResponseBuilder;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.repository.ValidatorRepository;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.results.ValidatorResult;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

public class AccountLimitsResource extends ManagementDependencyProvider {

    private AccountLimitResource accountLimitResource;
    private int accountId;
    private int id;


    @Path("{id: [1-9][0-9]*}")
    public AccountLimitResource retrieveLimitResource(@PathParam("id") int id) {
        accountLimitResource.setAccountId(accountId);
        accountLimitResource.setId(id);
        return accountLimitResource;
    }
    
    @GET
    public Response retrieveAllLimitsForAccount(@QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit, @QueryParam("marker") Integer marker) {
        if (!isUserInRole("cp, ops")) {
            return ResponseFactory.accessDenied();
        }
        try {
            Map<Integer, List<AccountLimit>> accountLimits = accountLimitService.getAllAccountLimits();
            return Response.status(200).entity(accountLimits).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }


    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response createAccountLimit(Limit limit){
        if (!isUserInRole("cp,ops")) {
            return ResponseFactory.accessDenied();
        }


        ValidatorResult result = ValidatorRepository.getValidatorFor(Limit.class).validate(limit, HttpRequestType.POST);

        if (!result.passedValidation()) {
            return Response.status(400).entity(HttpResponseBuilder.buildBadRequestResponse("Validation fault", result.getValidationErrorMessages())).build();
        }

        try {
            org.openstack.atlas.service.domain.entities.AccountLimit domainAccountLimit = getDozerMapper().map(limit, org.openstack.atlas.service.domain.entities.AccountLimit.class);
            domainAccountLimit.setAccountId(accountId);

            accountLimitService.save(domainAccountLimit);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }


    public void setAccountLimitResource(AccountLimitResource accountLimitResource) {
        this.accountLimitResource = accountLimitResource;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
