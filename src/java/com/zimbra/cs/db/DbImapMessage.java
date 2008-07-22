/*
 * ***** BEGIN LICENSE BLOCK *****
 * 
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2007 Zimbra, Inc.
 * 
 * The contents of this file are subject to the Yahoo! Public License
 * Version 1.0 ("License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.zimbra.com/license.
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * 
 * ***** END LICENSE BLOCK *****
 */
package com.zimbra.cs.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.common.util.Pair;
import com.zimbra.cs.account.DataSource;
import com.zimbra.cs.datasource.ImapFolder;
import com.zimbra.cs.datasource.ImapMessage;
import com.zimbra.cs.datasource.ImapMessageCollection;
import com.zimbra.cs.db.DbPool.Connection;
import com.zimbra.cs.mailbox.Flag;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;


public class DbImapMessage {

    public static final String TABLE_IMAP_MESSAGE = "imap_message";
    
    /**
     * Stores IMAP message tracker data.
     */
    public static void storeImapMessage(Mailbox mbox, int localFolderId, long remoteUid, int localItemId, int flags)
    throws ServiceException
    {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        ZimbraLog.datasource.debug(
            "Storing IMAP message tracker: mboxId=%d, localFolderId=%d, remoteUid=%d, localItemId=%d flags=%s",
            mbox.getId(), localFolderId, remoteUid, localItemId, Flag.bitmaskToFlags(flags));

        try {
            conn = DbPool.getConnection();
            stmt = conn.prepareStatement(
                "INSERT INTO " + getTableName(mbox) +
                " (mailbox_id, imap_folder_id, uid, item_id, flags) " +
                "VALUES (?, ?, ?, ?, ?)");
            stmt.setInt(1, mbox.getId());
            stmt.setInt(2, localFolderId);
            stmt.setLong(3, remoteUid);
            stmt.setInt(4, localItemId);
            stmt.setInt(5, flags);
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("Unable to store IMAP message data", e);
        } finally {
            DbPool.closeStatement(stmt);
            DbPool.quietClose(conn);
        }
    }

    public static void setUid(Mailbox mbox, int itemId, long uid)
        throws ServiceException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ZimbraLog.datasource.debug(
            "Updating IMAP message tracker uid: mboxId=%d, localItemId=%d remoteUid=%x",
            mbox.getId(), itemId, uid);
        try {
            conn = DbPool.getConnection();
            stmt = conn.prepareStatement(
                "UPDATE " + getTableName(mbox) + " SET uid = ?" +
                " WHERE mailbox_id = ? AND item_id = ?");
            stmt.setLong(1, uid);
            stmt.setInt(2, mbox.getId());
            stmt.setInt(3, itemId);
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("Unable to update IMAP message data", e);
        } finally {
            if (stmt != null) DbPool.closeStatement(stmt);
            DbPool.quietClose(conn);
        }
    }

    public static long getMinUid(Mailbox mbox, int folderId) throws ServiceException {
        return getMinMaxUid(mbox, folderId, "min");
    }

    public static long getMaxUid(Mailbox mbox, int folderId) throws ServiceException {
        return getMinMaxUid(mbox, folderId, "max");
    }
    
    private static long getMinMaxUid(Mailbox mbox, int folderId, String minmax)
        throws ServiceException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ZimbraLog.datasource.debug(
            "Getting %s IMAP uid: mboxId=%d, folderId=%d", minmax, mbox.getId(), folderId);
        try {
            conn = DbPool.getConnection();
            stmt = conn.prepareStatement(String.format(
                "SELECT %s FROM %s WHERE mailbox_id = %d AND imap_folder_id = %d AND uid > 0",
                minmax, getTableName(mbox), mbox.getId(), folderId));
            rs = stmt.executeQuery();
            return rs.next() ? rs.getLong(1) : -1;
        } catch (SQLException e) {
            throw ServiceException.FAILURE("Unable to execute query", e);
        } finally {
            DbPool.closeResults(rs);
            DbPool.closeStatement(stmt);
            DbPool.quietClose(conn);
        }
    }

    public static void setFlags(Mailbox mbox, int itemId, int flags)
        throws ServiceException
    {
        Connection conn = null;
        PreparedStatement stmt = null;
        ZimbraLog.datasource.debug(
            "Updating IMAP message tracker flags: mboxId=%d, localItemId=%d flags=%s",
            mbox.getId(), itemId, Flag.bitmaskToFlags(flags));
        try {
            conn = DbPool.getConnection();
            stmt = conn.prepareStatement(
                "UPDATE " + getTableName(mbox) + " SET flags = ?" +
                " WHERE mailbox_id = ? AND item_id = ?");
            stmt.setInt(1, flags);
            stmt.setInt(2, mbox.getId());
            stmt.setInt(3, itemId);
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("Unable to update IMAP message data", e);
        } finally {
            if (stmt != null) DbPool.closeStatement(stmt);
            DbPool.quietClose(conn);
        }
    }

    /**
     * Deletes IMAP message tracker data.
     */
    public static void deleteImapMessage(Mailbox mbox, int localFolderId, int localItemId)
    throws ServiceException
    {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        ZimbraLog.datasource.debug(
            "Deleting IMAP message tracker: mboxId=%d, localFolderId=%d, msgId=%d",
            mbox.getId(), localFolderId, localItemId);

        try {
            conn = DbPool.getConnection();
            stmt = conn.prepareStatement(
                "DELETE FROM " + getTableName(mbox) +
                " WHERE mailbox_id = ? AND imap_folder_id = ? AND item_id = ?");
            stmt.setInt(1, mbox.getId());
            stmt.setInt(2, localFolderId);
            stmt.setInt(3, localItemId);
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("Unable to delete IMAP message data", e);
        } finally {
            DbPool.closeStatement(stmt);
            DbPool.quietClose(conn);
        }
    } 

    /**
     * Deletes IMAP message tracker data.
     */
    public static void deleteImapMessages(Mailbox mbox, int localFolderId)
    throws ServiceException
    {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        ZimbraLog.datasource.debug(
            "Deleting all IMAP message trackers: mboxId=%d, localFolderId=%d", mbox.getId(), localFolderId);

        try {
            conn = DbPool.getConnection();
            stmt = conn.prepareStatement(
                "DELETE FROM " + getTableName(mbox) +
                " WHERE mailbox_id = ? AND imap_folder_id = ?");
            stmt.setInt(1, mbox.getId());
            stmt.setInt(2, localFolderId);
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("Unable to delete IMAP message data", e);
        } finally {
            DbPool.closeStatement(stmt);
            DbPool.quietClose(conn);
        }
    }

    // Returns local message item id for specified folder id and remote uid
    // if a tracker exists, otherwise returns 0.
    public static int getLocalMessageId(Mailbox mbox, int localFolderId, long remoteUid)
        throws ServiceException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        ZimbraLog.datasource.debug(
            "Getting local message id for tracked message: mboxId=%d, localFolderId=%d, remoteUid=%d",
            mbox.getId(), localFolderId, remoteUid);
        try {
            conn = DbPool.getConnection();
            stmt = conn.prepareStatement(
                "SELECT item_id" +
                " FROM " + getTableName(mbox) +
                " WHERE mailbox_id = ? AND imap_folder_id = ? AND uid = ?");
            stmt.setInt(1, mbox.getId());
            stmt.setInt(2, localFolderId);
            stmt.setLong(3, remoteUid);
            rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw ServiceException.FAILURE("Unable to get IMAP message data", e);
        } finally {
            if (rs != null) DbPool.closeResults(rs);
            if (stmt != null) DbPool.closeStatement(stmt);
            DbPool.quietClose(conn);
        }
    }

    public static Pair<ImapMessage, Integer> getImapMessage(Mailbox mbox, int itemId)
        throws ServiceException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DbPool.getConnection();
            stmt = conn.prepareStatement(
                "SELECT imap.uid, imap.flags as tflags, imap.imap_folder_id, mi.unread, mi.flags " +
                "FROM " + getTableName(mbox) + " imap " +
                " LEFT OUTER JOIN " + DbMailItem.getMailItemTableName(mbox) + " mi " +
                " ON imap.mailbox_id = mi.mailbox_id AND imap.item_id = mi.id " +
                "WHERE imap.mailbox_id = ? AND imap.item_id = ?");
            stmt.setInt(1, mbox.getId());
            stmt.setInt(2, itemId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                long uid = rs.getLong("uid");
                int flags = rs.getInt("flags");
                int unread = rs.getInt("unread");
                int tflags = rs.getInt("tflags");
                int folderId = rs.getInt("imap_folder_id");
                flags = unread > 0 ? (flags | Flag.BITMASK_UNREAD) : (flags & ~Flag.BITMASK_UNREAD);
                return new Pair<ImapMessage, Integer>
                    (new ImapMessage(uid, itemId, flags, tflags), folderId);
            }
            return null;
        } catch (SQLException e) {
            throw ServiceException.FAILURE("Unable to get IMAP message data", e);
        } finally {
            DbPool.closeResults(rs);
            DbPool.closeStatement(stmt);
            DbPool.quietClose(conn);
        }
    }
    
    /**
     * Returns a collection of tracked IMAP messages for the given data source.
     */
    public static ImapMessageCollection getImapMessages(Mailbox mbox, DataSource ds, ImapFolder imapFolder)
    throws ServiceException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ImapMessageCollection imapMessages = new ImapMessageCollection();

        try {
            conn = DbPool.getConnection();
            stmt = conn.prepareStatement(
                "SELECT imap.uid, imap.item_id, imap.flags as tflags, mi.unread, mi.flags " +
                "FROM " + getTableName(mbox) + " imap " +
                "  LEFT OUTER JOIN " + DbMailItem.getMailItemTableName(mbox) + " mi " +
                "  ON imap.mailbox_id = mi.mailbox_id AND imap.item_id = mi.id " + 
                "WHERE imap.mailbox_id = ? AND imap.imap_folder_id = ?");

            int i = 1;
            stmt.setInt(i++, mbox.getId());
            stmt.setInt(i++, imapFolder.getItemId());
            rs = stmt.executeQuery();
            while (rs.next()) {
                long uid = rs.getLong("uid");
                int itemId = rs.getInt("item_id");
                int flags = rs.getInt("flags");
                int unread = rs.getInt("unread");
                int tflags = rs.getInt("tflags");
                flags = unread > 0 ? (flags | Flag.BITMASK_UNREAD) : (flags & ~Flag.BITMASK_UNREAD);
                imapMessages.add(new ImapMessage(uid, itemId, flags, tflags));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("Unable to get IMAP message data", e);
        } finally {
            DbPool.closeResults(rs);
            DbPool.closeStatement(stmt);
            DbPool.quietClose(conn);
        }

        ZimbraLog.datasource.debug("Found %d tracked IMAP messages for %s",
            imapMessages.size(), imapFolder.getRemotePath());
        return imapMessages;
    }

    /**
     * Returns a list of the local message ids in the given folder for which
     * there is no corresponding IMAP message tracker. This is the list of
     * new messages which must be appended to the remote mailbox when handling
     * a folder UIDVALIDITY change.
     * @return the new message ids, or an empty <tt>List</tt> if there are none.
     */
    public static List<Integer> getNewLocalMessageIds(Mailbox mbox, int folderId)
    throws ServiceException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Integer> newIds = new ArrayList<Integer>();

        try {
            conn = DbPool.getConnection();
            stmt = conn.prepareStatement(
                "SELECT id FROM " + DbMailItem.getMailItemTableName(mbox) + " mi " +
                "  LEFT OUTER JOIN " + getTableName(mbox) + " imap " +
                "  ON imap.mailbox_id = mi.mailbox_id AND imap.item_id = mi.id " + 
                "WHERE mi.mailbox_id = ? AND mi.folder_id = ? AND imap.item_id IS NULL " +
                "AND mi.type IN (" + MailItem.TYPE_MESSAGE + ", " + MailItem.TYPE_CHAT + ")");

            int i = 1;
            stmt.setInt(i++, mbox.getId());
            stmt.setInt(i++, folderId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                newIds.add(rs.getInt("id"));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("Unable to get new local message ids", e);
        } finally {
            DbPool.closeResults(rs);
            DbPool.closeStatement(stmt);
            DbPool.quietClose(conn);
        }

        return newIds;
    }

    public static String getTableName(int mailboxId, int groupId) {
        return String.format("%s.%s", DbMailbox.getDatabaseName(groupId), TABLE_IMAP_MESSAGE);
    }

    public static String getTableName(Mailbox mbox) {
        return DbMailbox.getDatabaseName(mbox) + "." + TABLE_IMAP_MESSAGE;
    }
}
