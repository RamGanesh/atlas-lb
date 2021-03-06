package org.openstack.atlas.api.atom;

import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;

public final class EntryHelper {
    public static final String CREATE_NODE_TITLE = "Node Successfully Created";
    public static final String CREATE_VIP_TITLE = "Virtual Ip Successfully Added";
    public static final String UPDATE_MONITOR_TITLE = "Health Monitor Successfully Updated";
    public static final String UPDATE_PERSISTENCE_TITLE = "Session Persistence Successfully Updated";
    public static final String UPDATE_LOGGING_TITLE = "Connection Logging Successfully Updated";
    public static final String UPDATE_THROTTLE_TITLE = "Connection Throttle Successfully Updated";
    public static final String UPDATE_ACCESS_LIST_TITLE = "Access List Successfully Updated";

    public static String createNodeSummary(Node node) {
        StringBuffer atomSummary = new StringBuffer();
        atomSummary.append("Node successfully created with ");
        atomSummary.append("address: '").append(node.getIpAddress()).append("', ");
        atomSummary.append("port: '").append(node.getPort()).append("', ");
        atomSummary.append("condition: '").append(node.getCondition()).append("', ");
        atomSummary.append("weight: '").append(node.getWeight()).append("'");
        return atomSummary.toString();
    }

    public static String createVirtualIpSummary(VirtualIp virtualIp) {
        StringBuffer atomSummary = new StringBuffer();
        atomSummary.append("Virtual ip successfully added with ");
        atomSummary.append("address: '").append(virtualIp.getIpAddress()).append("', ");
        atomSummary.append("type: '").append(virtualIp.getVipType()).append("'");
        return atomSummary.toString();
    }

    public static String createVirtualIpSummary(VirtualIpv6 virtualIp) {
        String ipv6AsString = null;
        try {
            ipv6AsString = virtualIp.getDerivedIpString();
        } catch (IPStringConversionException e) {
            // Ignore
        }

        StringBuffer atomSummary = new StringBuffer();
        atomSummary.append("Virtual ip successfully added with ");
        if(ipv6AsString != null) atomSummary.append("address: '").append(ipv6AsString).append("', ");
        atomSummary.append("type: '").append(VirtualIpType.PUBLIC.name()).append("'");
        return atomSummary.toString();
    }

    public static String createVirtualIpSummary() {
        return "Virtual ip successfully added";
    }

    public static String createHealthMonitorSummary(LoadBalancer lb) {
        StringBuffer atomSummary = new StringBuffer();
        atomSummary.append("Health monitor successfully updated with ");
        atomSummary.append("type: '").append(lb.getHealthMonitor().getType().name()).append("', ");
        atomSummary.append("delay: '").append(lb.getHealthMonitor().getDelay()).append("', ");
        atomSummary.append("timeout: '").append(lb.getHealthMonitor().getTimeout()).append("', ");
        atomSummary.append("attemptsBeforeDeactivation: '").append(lb.getHealthMonitor().getAttemptsBeforeDeactivation());

        if (lb.getHealthMonitor().getType().equals(HealthMonitorType.CONNECT)) {
            atomSummary.append("'");
        } else {
            atomSummary.append("', ");
            atomSummary.append("path: '").append(lb.getHealthMonitor().getPath()).append("', ");
            atomSummary.append("statusRegex: '").append(lb.getHealthMonitor().getStatusRegex()).append("', ");
            atomSummary.append("bodyRegex: '").append(lb.getHealthMonitor().getBodyRegex()).append("'");
        }

        return atomSummary.toString();
    }

    public static String createConnectionThrottleSummary(LoadBalancer lb) {
        StringBuffer atomSummary = new StringBuffer();
        atomSummary.append("Connection throttle successfully updated with ");
        atomSummary.append("minConnections: '").append(lb.getConnectionLimit().getMinConnections()).append("', ");
        atomSummary.append("maxConnections: '").append(lb.getConnectionLimit().getMaxConnections()).append("', ");
        atomSummary.append("maxConnectionRate: '").append(lb.getConnectionLimit().getMaxConnectionRate()).append("', ");
        atomSummary.append("rateInterval: '").append(lb.getConnectionLimit().getRateInterval()).append("'");
        return atomSummary.toString();
    }

    public static String createAccessListSummary(AccessList accessListItem) {
        StringBuffer atomSummary = new StringBuffer();
        atomSummary.append("Access list successfully updated with the following item: ");
        atomSummary.append("id: '").append(accessListItem.getId()).append("', ");
        atomSummary.append("address: '").append(accessListItem.getIpAddress()).append("', ");
        atomSummary.append("type: '").append(accessListItem.getType()).append("'");
        return atomSummary.toString();
    }
}
