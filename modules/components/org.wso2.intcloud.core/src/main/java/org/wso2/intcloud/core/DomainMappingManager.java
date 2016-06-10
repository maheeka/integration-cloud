/**
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.intcloud.core;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.intcloud.common.IntCloudException;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Collection;
import java.util.Hashtable;

public class DomainMappingManager {

    private static final Log log = LogFactory.getLog(DomainMappingManager.class);

    // DNS records
    public static final String DNS_A_RECORD = "A";
    public static final String DNS_CNAME_RECORD = "CNAME";

    private static final String JNDI_KEY_NAMING_FACTORY_INITIAL = "java.naming.factory.initial";
    private static final String JNDI_KEY_DNS_TIMEOUT = "com.sun.jndi.dns.timeout.initial";
    private static final String JDNI_KEY_DNS_RETRIES = "com.sun.jndi.dns.timeout.retries";

    /**
     * Check whether there is a CNAME entry from {@code customUrl} to {@code pointedUrl}.
     *
     * @param pointedUrl url that is pointed by the CNAME entry of {@code customUrl}
     * @param customUrl  custom url.
     * @return success whether there is a CNAME entry from {@code customUrl} to {@code pointedUrl}
     * @throws IntCloudException
     */
    public boolean verifyCustomUrlByCname(String pointedUrl, String customUrl) throws IntCloudException {
        Hashtable<String, String> env = new Hashtable<String, String>();
        boolean success;
        // set environment configurations
        env.put(JNDI_KEY_NAMING_FACTORY_INITIAL, "com.sun.jndi.dns.DnsContextFactory");
        env.put(JNDI_KEY_DNS_TIMEOUT, "5000");
        env.put(JDNI_KEY_DNS_RETRIES, "1");
        try {
            Multimap<String, String> resolvedHosts = resolveDNS(customUrl, env);
            Collection<String> resolvedCnames = resolvedHosts.get(DNS_CNAME_RECORD);
            if (!resolvedCnames.isEmpty() && resolvedCnames.contains(pointedUrl)) {
                if (log.isDebugEnabled()) {
                    log.debug(pointedUrl + " can be reached from: " + customUrl + " via CNAME records");
                }
                success = true;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(pointedUrl + " cannot be reached from: " + customUrl + " via CNAME records");
                }
                success = false;
            }
        } catch (IntCloudException e) {
            log.error("Error occurred while resolving dns for: " + customUrl, e);
            throw new IntCloudException("Error occurred while resolving dns for: " + customUrl, e);
        } catch (NamingException e) {
            // we are logging this as warn messages since this is caused, due to an user error. For example if the
            // user entered a rubbish custom url(Or a url which is, CNAME record is not propagated at the
            // time of adding the url), then url validation will fail but it is not an system error
            log.warn(pointedUrl + " cannot be reached from: " + customUrl + " via CNAME records. Provided custom" +
                    " url: " + customUrl + " might not a valid url.", e);
            success = false;
        }
        return success;
    }

    /**
     * Resolve CNAME and A records for the given {@code hostname}.
     *
     * @param domain             hostname to be resolved.
     * @param environmentConfigs environment configuration
     * @return {@link com.google.common.collect.Multimap} of resolved dns entries. This {@link com.google.common.collect.Multimap} will contain the resolved
     * "CNAME" and "A" records from the given {@code hostname}
     * @throws IntCloudException if error occurred while the operation
     */
    public Multimap<String, String> resolveDNS(String domain, Hashtable<String, String> environmentConfigs)
            throws IntCloudException, NamingException {
        // result mutimap of dns records. Contains the cname and records resolved by the given hostname
        // ex:  CNAME   => foo.com,bar.com
        //      A       => 192.1.2.3 , 192.3.4.5
        Multimap<String, String> dnsRecordsResult = ArrayListMultimap.create();
        Attributes dnsRecords;
        boolean isARecordFound = false;
        boolean isCNAMEFound = false;

        try {
            if (log.isDebugEnabled()) {
                log.debug("DNS validation: resolving DNS for " + domain + " " + "(A/CNAME)");
            }
            DirContext context = new InitialDirContext(environmentConfigs);
            String[] dnsRecordsToCheck = new String[] { DNS_A_RECORD, DNS_CNAME_RECORD };
            dnsRecords = context.getAttributes(domain, dnsRecordsToCheck);
        } catch (NamingException e) {
            String msg = "DNS validation: DNS query failed for: " + domain + ". Error occurred while configuring " +
                    "directory context.";
            log.error(msg, e);
            throw new IntCloudException(msg, e);
        }

        try {
            // looking for for A records
            Attribute aRecords = dnsRecords.get(DNS_A_RECORD);
            if (aRecords != null && aRecords.size() > 0) {                      // if an A record exists
                NamingEnumeration aRecordHosts = aRecords.getAll();             // get all resolved A entries
                String aHost;
                while (aRecordHosts.hasMore()) {
                    isARecordFound = true;
                    aHost = (String) aRecordHosts.next();
                    dnsRecordsResult.put(DNS_A_RECORD, aHost);
                    if (log.isDebugEnabled()) {
                        log.debug("DNS validation: A record found: " + aHost);
                    }
                }
            }

            // looking for CNAME records
            Attribute cnameRecords = dnsRecords.get(DNS_CNAME_RECORD);
            if (cnameRecords != null && cnameRecords.size() > 0) {              // if CNAME record exists
                NamingEnumeration cnameRecordHosts = cnameRecords
                        .getAll();     // get all resolved CNAME entries for hostname
                String cnameHost;
                while (cnameRecordHosts.hasMore()) {
                    isCNAMEFound = true;
                    cnameHost = (String) cnameRecordHosts.next();
                    if (cnameHost.endsWith(".")) {
                        // Since DNS records are end with "." we are removing it.
                        // For example real dns entry for www.google.com is www.google.com.
                        cnameHost = cnameHost.substring(0, cnameHost.lastIndexOf('.'));
                    }
                    dnsRecordsResult.put(DNS_CNAME_RECORD, cnameHost);
                    if (log.isDebugEnabled()) {
                        log.debug("DNS validation: recurring on CNAME record towards host " + cnameHost);
                    }
                    dnsRecordsResult.putAll(resolveDNS(cnameHost, environmentConfigs)); // recursively resolve cnameHost
                }
            }

            if (!isARecordFound && !isCNAMEFound && log.isDebugEnabled()) {
                log.debug("DNS validation: No CNAME or A record found for domain: '" + domain);
            }
            return dnsRecordsResult;
        } catch (NamingException ne) {
            String msg = "DNS validation: DNS query failed for: " + domain + ". Provided domain: " + domain +
                    " might be a " +
                    "non existing domain.";
            // we are logging this as warn messages since this is caused, due to an user error. For example if the
            // user entered a rubbish custom url(Or a url which is, CNAME record is not propagated at the
            // time of adding the url), then url validation will fail but it is not an system error
            log.warn(msg, ne);
            throw new NamingException(msg);
        }
    }
}
