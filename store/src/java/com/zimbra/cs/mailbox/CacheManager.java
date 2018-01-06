package com.zimbra.cs.mailbox;

import com.zimbra.common.service.ServiceException;

import java.util.Map;

public interface CacheManager {

    void setMailboxIds(Map<String, Integer> mailboxIds);

    void setCache(LocalCacheManager.MailboxMap cache);

    //LocalCacheManager.MailboxMap getCache();

    Map<String, Integer> getMailboxIds() throws ServiceException;

    Object retrieveFromCache(int mailboxId, boolean trackGC, MailboxManager mailboxManager) throws MailServiceException;

    int getCacheSize();

    Mailbox cacheMailbox(Mailbox mailbox, MailboxManager mailboxManager);

    void cacheAccount(String accountId, int mailboxId);

}
