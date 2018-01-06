package com.zimbra.cs.mailbox;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.db.DbMailbox;
import com.zimbra.cs.db.DbPool;


import java.util.Map;

public class DistributedCacheManager implements CacheManager {

    /**
     * Maps account IDs (<code>String</code>s) to mailbox IDs
     * (<code>Integer</code>s).  <i>Every</i> mailbox in existence on the
     * server appears in this mapping.
     */
    private Map<String, Integer> mailboxIds;
    /**
     * Maps mailbox IDs ({@link Integer}s) to either
     * <ul>
     * <li>a loaded {@link Mailbox}, or
     * <li>a {@link SoftReference} to a loaded {@link Mailbox}, or
     * <li>a {@link MaintenanceContext} for the mailbox.
     * </ul>
     * Mailboxes are faulted into memory as needed, but may drop from memory when the SoftReference expires due to
     * memory pressure combined with a lack of outstanding references to the {@link Mailbox}.  Only one {@link Mailbox}
     * per user is cached, and only that {@link Mailbox} can process user requests.
     */
    private LocalCacheManager.MailboxMap cache;

    @Override
    public void setMailboxIds(Map<String, Integer> mailboxIds) {
        //do nothing
        //In distributed env, we are not going to cache mailboxIds, we are going to retrieve from DB
    }

    @Override
    public void setCache(LocalCacheManager.MailboxMap cache) {

    }

    @Override
    public Map<String, Integer> getMailboxIds() throws ServiceException {
        //In distributed env, we always are going to retrieve from DB
        DbPool.DbConnection conn = null;
        synchronized (this) {
            try {
                return DbMailbox.listMailboxes(conn, null);
            } finally {
                DbPool.quietClose(conn);
            }
        }
    }

    @Override
    public Object retrieveFromCache(int mailboxId, boolean trackGC, MailboxManager mailboxManager) throws MailServiceException {
        return null;
    }

    @Override
    public int getCacheSize() {
        return 0;
    }

    @Override
    public Mailbox cacheMailbox(Mailbox mailbox, MailboxManager mailboxManager) {
        return null;
    }

    @Override
    public void cacheAccount(String accountId, int mailboxId) {
        //do nothing
        //In distributed env, we are not going to cache mailboxIds, we are going to retrieve from DB
    }
}
