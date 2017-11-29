package com.zimbra.cs.mailbox;

import org.redisson.api.RLock;


public class DistributedMailboxLock {
    private RLock lock;

    public DistributedMailboxLock(RLock lock) {
        this.lock = lock;
    }

    public void lock() {
        lock(true);
    }

    public void lock(final boolean write) {
        lock.lock();
    }

    public void release() {
        lock.unlock();
    }

    public boolean isWriteLockedByCurrentThread() {
        return lock.isHeldByCurrentThread();
    }

    int getHoldCount() {
        if (lock.isHeldByCurrentThread()) {
            return 1;
        };
        return 0;
    }

    public boolean isUnlocked() {
        return !lock.isHeldByCurrentThread();
    }
}
