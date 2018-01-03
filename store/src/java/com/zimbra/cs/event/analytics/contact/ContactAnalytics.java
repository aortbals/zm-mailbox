package com.zimbra.cs.event.analytics.contact;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.event.EventStore;

import java.util.List;

public class ContactAnalytics {
    public static enum ContactFrequencyTimeRange {
        LAST_DAY, LAST_WEEK, LAST_MONTH, FOREVER
    }

    public static enum ContactFrequencyGraphTimeRange {
        CURRENT_MONTH, LAST_SIX_MONTHS, CURRENT_YEAR
    }

    public static enum ContactFrequencyEventType {
        SENT, RECEIVED, COMBINED
    }

    public static Long getContactFrequency(String contact, EventStore eventStore) throws ServiceException {
        return eventStore.getContactFrequencyCount(contact, ContactFrequencyEventType.COMBINED, ContactFrequencyTimeRange.FOREVER);
    }

    public static Long getContactFrequency(String contact, EventStore eventStore, ContactFrequencyEventType eventType, ContactFrequencyTimeRange timeRange) throws ServiceException {
        return eventStore.getContactFrequencyCount(contact, eventType, timeRange);
    }

    public static List<ContactFrequencyGraphDataPoint> getContactFrequencyGraph(String contact, ContactFrequencyGraphTimeRange timeRange, EventStore eventStore) throws ServiceException {
        return eventStore.getContactFrequencyGraph(contact, timeRange);
    }
}
