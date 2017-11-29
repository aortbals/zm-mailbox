package com.zimbra.cs.mailbox;

import com.zimbra.common.util.ZimbraLog;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public class DistributedMailboxLockManager {
    private Config config;
    private RedissonClient redisson;
    private RReadWriteLock readWriteLock;
    private final static String HOST = "redis";
    private final static String PORT = "6379";

    public DistributedMailboxLockManager(final String id, final Mailbox mbox) {
        try {
            config = new Config();
            config.useSingleServer().setAddress(HOST + ":" + PORT);
            redisson = Redisson.create(config);
            readWriteLock = redisson.getReadWriteLock("mailbox:" + id);
        } catch (Exception e) {
            ZimbraLog.system.fatal("Can't instantiate Redisson server", e);
            System.exit(1);
        }
    }

    public DistributedMailboxLock readLock() {
        return new DistributedMailboxLock(readWriteLock.readLock());
    }

    public DistributedMailboxLock writeLock() {
        return new DistributedMailboxLock(readWriteLock.writeLock());
    }

    public void shutdown() {
        redisson.shutdown();
    }
}
