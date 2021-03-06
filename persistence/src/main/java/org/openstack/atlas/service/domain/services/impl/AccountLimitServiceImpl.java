package org.openstack.atlas.service.domain.services.impl;

import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.LoadBalancerCountByAccountIdClusterId;
import org.openstack.atlas.service.domain.services.AccountLimitService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountLimitServiceImpl extends BaseService implements AccountLimitService {
    private final Log LOG = LogFactory.getLog(AccountLimitServiceImpl.class);

    @Override
    public AccountLimit getByIdAndAccountId(Integer id, Integer accountId) {
        return accountLimitRepository.getByIdAndAccountId(id, accountId);
    }

    @Override
    public void save(AccountLimit accountLimit) throws BadRequestException {
        try {
            AccountLimit dbLimit = accountLimitRepository.getByAccountIdAndType(accountLimit.getAccountId(), accountLimit.getLimitType());
            if (dbLimit != null) {
                throw new BadRequestException("A limit for the Limit Type " + dbLimit.getLimitType().getName().toString() + " already exists for the account id " + dbLimit.getAccountId());
            }
        } catch(Exception e) {
            //This is good. No record for the account and type already exists.
        }

        accountLimitRepository.save(accountLimit);
    }

    @Override
    public void delete(AccountLimit accountLimit) throws EntityNotFoundException {
        AccountLimit dbLimit = accountLimitRepository.getById(accountLimit.getId());
        if(accountLimit.getAccountId() != dbLimit.getAccountId()) {
            String errMsg = String.format("Cannot access accountLimit {id=%d}", accountLimit.getId());
            throw new EntityNotFoundException(errMsg);
        }
        accountLimitRepository.delete(accountLimit);
    }

    @Override
    public AccountLimit update(AccountLimit accountLimit) throws EntityNotFoundException {
        AccountLimit dbLimit = accountLimitRepository.getById(accountLimit.getId());
        if(accountLimit.getAccountId() != dbLimit.getAccountId()) {
            String errMsg = String.format("Cannot access accountLimit {id=%d}", accountLimit.getId());
            throw new EntityNotFoundException(errMsg);
        }
        dbLimit.setLimit(accountLimit.getLimit());
        return accountLimitRepository.update(dbLimit);
    }

    @Override
    public List<AccountLimit> getCustomAccountLimits(Integer accountId) {
        return accountLimitRepository.getAccountLimits(accountId);
    }

    @Override
    public int getLimit(Integer accountId, AccountLimitType accountLimitType) throws EntityNotFoundException {
        List<AccountLimit> allAccountLimits = getCustomAccountLimits(accountId);

        for (AccountLimit accountLimit : allAccountLimits) {
            if (accountLimit.getLimitType().getName().equals(accountLimitType)) {
                return accountLimit.getLimit();
            }
        }

        LimitType resultLimitType = accountLimitRepository.getLimitType(accountLimitType);
        if (resultLimitType == null) {
            String message = String.format("No limit type found for '%s'", accountLimitType.name());
            LOG.error(message);
            throw new EntityNotFoundException(message);
        }

        return resultLimitType.getDefaultValue();
    }

    @Override
    public List<LimitType> getAllLimitTypes() {
        return accountLimitRepository.getAllLimitTypes();
    }

    @Override
    public Map<String, Integer> getAllLimitsForAccount(Integer accountId) {
        Map<String, Integer> limitsForAccount = new HashMap<String, Integer>();
        List<LimitType> allLimitTypes = getAllLimitTypes();
        List<AccountLimit> customAccountLimits = getCustomAccountLimits(accountId);

        for (LimitType limitType : allLimitTypes) {
            limitsForAccount.put(limitType.getName().name(), limitType.getDefaultValue());
        }

        for (AccountLimit customAccountLimit : customAccountLimits) {
            limitsForAccount.put(customAccountLimit.getLimitType().getName().name(), customAccountLimit.getLimit());
        }

        return limitsForAccount;
    }

    @Override
    public Map<Integer, List<AccountLimit>> getAllAccountLimits() {
        Map<Integer, List<AccountLimit>> customLimitAccounts = new HashMap<Integer, List<AccountLimit>>();
        List<AccountLimit> customAccountLimitAccounts = accountLimitRepository.getAllCustomLimits();
        for (AccountLimit accountLimit : customAccountLimitAccounts) {
            if (customLimitAccounts.containsKey(accountLimit.getAccountId())) {
                customLimitAccounts.get(accountLimit.getAccountId()).add(accountLimit);
            } else {
                List<AccountLimit> newList = new ArrayList<AccountLimit>();
                newList.add(accountLimit);
                customLimitAccounts.put(accountLimit.getAccountId(), newList);
            }
        }
        return customLimitAccounts;
    }

    public Map<Integer, List<AccountLimit>> getAccountLimitsForCluster(Integer clusterId) {
        List<LoadBalancerCountByAccountIdClusterId> accountIds = clusterRepository.getAccountsInCluster(clusterId);
        List<Integer> ids = new ArrayList<Integer>();
        for (LoadBalancerCountByAccountIdClusterId accountId : accountIds) {
            ids.add(accountId.getAccountId());
        }
        Map<Integer, List<AccountLimit>> customLimitAccounts = getAllAccountLimits();
        List<Integer> remList = new ArrayList<Integer>();
        for (Integer key : new ArrayList<Integer>(customLimitAccounts.keySet())) {
            if (!ids.contains(key)) {
                remList.add(key);
            }
        }
        return customLimitAccounts;
    }
}
