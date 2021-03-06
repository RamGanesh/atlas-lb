package org.openstack.atlas.service.domain.logs.entities;

public enum NameVal {
    ARCHIVE, COMPUTECYCLES, COMPUTECYCLES_ANALYZER, COMPUTECYCLES_ANALYZER_TOPCC, COMPUTECYCLES_JOIN, LZO_FILECOPY, FILECOPY, FILECOPY_PARENT, FILEASSEMBLE,
    RAWLOGS, RAWLOGS_COMBINE, RAWLOGS_EXTRACT, STATS, WATCHDOG, COMPUTECYCLES_PROFILE_PREPARE, COMPUTECYCLES_NORMALIZE, COMPUTECYCLES_DUMPCREATE,
    ORDER_LOGS, FQDN, FQDN_ALIAS_JOIN, FQDN_ALIAS_DUMPCREATE, URCHIN_CREATE, URCHIN_RUN, EMAIL_TIME_DRIFT, EMAIL_DISK_USAGE, DELETE_8TH_LOGS,
    FILES_SPLIT, URCHIN_RERUN, DELETE_DIRS, MAPREDUCE;
}
