package org.openstack.atlas.usage.jobs;

import org.openstack.atlas.service.domain.entities.AccountUsage;
import org.openstack.atlas.service.domain.entities.VirtualIpType;
import org.openstack.atlas.service.domain.repository.AccountUsageRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.VirtualIpRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Calendar;
import java.util.List;

public class AccountUsagePoller extends QuartzJobBean {
    private final Log LOG = LogFactory.getLog(AccountUsagePoller.class);
    private LoadBalancerRepository loadBalancerRepository;
    private AccountUsageRepository accountUsageRepository;
    private VirtualIpRepository virtualIpRepository;

    @Required
    public void setLoadBalancerRepository(LoadBalancerRepository loadBalancerRepository) {
        this.loadBalancerRepository = loadBalancerRepository;
    }

    @Required
    public void setAccountUsageRepository(AccountUsageRepository accountUsageRepository) {
        this.accountUsageRepository = accountUsageRepository;
    }

    @Required
    public void setVirtualIpRepository(VirtualIpRepository virtualIpRepository) {
        this.virtualIpRepository = virtualIpRepository;
    }

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        startPoller();
    }

    private void startPoller() {
        Calendar startTime = Calendar.getInstance();
        LOG.info(String.format("Account usage poller job started at %s (Timezone: %s)", startTime.getTime(), startTime.getTimeZone().getDisplayName()));

        List<Integer> accountIds = loadBalancerRepository.getAllAccountIds();

        for (Integer accountId : accountIds) {
            LOG.debug(String.format("Creating account usage entry for account '%d'...", accountId));
            createAccountUsageEntry(accountId);
            LOG.debug(String.format("Account usage entry successfully created for account '%d'.", accountId));
        }

        Calendar endTime = Calendar.getInstance();
        Double elapsedMins = ((endTime.getTimeInMillis() - startTime.getTimeInMillis()) / 1000.0) / 60.0;
        LOG.info(String.format("Account usage poller job completed at '%s' (Total Time: %f mins)", endTime.getTime(), elapsedMins));
    }

    private void createAccountUsageEntry(Integer accountId) {
        AccountUsage usage = new AccountUsage();
        usage.setAccountId(accountId);
        usage.setNumLoadBalancers(loadBalancerRepository.getNumNonDeletedLoadBalancersForAccount(accountId));
        usage.setNumPublicVips(virtualIpRepository.getNumUniqueVipsForAccount(accountId, VirtualIpType.PUBLIC));
        usage.setNumServicenetVips(virtualIpRepository.getNumUniqueVipsForAccount(accountId, VirtualIpType.SERVICENET));
        usage.setStartTime(Calendar.getInstance());
        accountUsageRepository.save(usage);
    }
}
