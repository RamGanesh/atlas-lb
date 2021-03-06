package org.openstack.atlas.service.domain.services.impl;


import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.SessionPersistence;
import org.openstack.atlas.service.domain.exceptions.*;
import org.openstack.atlas.service.domain.services.SessionPersistenceService;
import org.openstack.atlas.service.domain.services.helpers.StringHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;

public class SessionPersistenceServiceImpl extends BaseService implements SessionPersistenceService {
    private final Log LOG = LogFactory.getLog(SessionPersistenceServiceImpl.class);

    @Override
    public SessionPersistence get(Integer accountId, Integer lbId) throws EntityNotFoundException, BadRequestException, DeletedStatusException {
        return loadBalancerRepository.getSessionPersistenceByAccountIdLoadBalancerId(accountId, lbId);
    }

    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class, ImmutableEntityException.class, UnprocessableEntityException.class})
    public void update(LoadBalancer queueLb) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException {
        LOG.debug("Entering " + getClass());
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(queueLb.getId(), queueLb.getAccountId());

        if (!(dbLoadBalancer.getProtocol().equals(org.openstack.atlas.service.domain.entities.LoadBalancerProtocol.HTTP)
                || dbLoadBalancer.getProtocol().equals(org.openstack.atlas.service.domain.entities.LoadBalancerProtocol.HTTPS))) {
            throw new UnprocessableEntityException("Unprocessable entity, HTTP_COOKIE Session persistence can only be enabled while load balancer is in HTTP/HTTPS protocol");
        }

        LOG.debug("Updating the lb status to pending_update");
        if(!loadBalancerRepository.testAndSetStatus(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.PENDING_UPDATE, false)) {
            String message = StringHelper.immutableLoadBalancer(dbLoadBalancer);
            LOG.warn(message);
            throw new ImmutableEntityException(message);
        }

        dbLoadBalancer.setSessionPersistence(queueLb.getSessionPersistence());
        loadBalancerRepository.update(dbLoadBalancer);

        LOG.debug("Leaving " + getClass());
    }

    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class, ImmutableEntityException.class, UnprocessableEntityException.class})
    public void delete(LoadBalancer requestLb) throws Exception {
        LOG.debug("Entering " + getClass());
        LoadBalancer dbLb = loadBalancerRepository.getByIdAndAccountId(requestLb.getId(), requestLb.getAccountId());

        if (dbLb.getSessionPersistence().equals(SessionPersistence.NONE)) {
            throw new UnprocessableEntityException("Session persistence is already deleted.");
        }

        if(!loadBalancerRepository.testAndSetStatus(dbLb.getAccountId(), dbLb.getId(), LoadBalancerStatus.PENDING_DELETE, false)) {
            String message = StringHelper.immutableLoadBalancer(dbLb);
            LOG.warn(message);
            throw new ImmutableEntityException(message);
        }
    }
}
