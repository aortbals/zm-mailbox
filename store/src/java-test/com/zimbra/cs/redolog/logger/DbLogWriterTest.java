package com.zimbra.cs.redolog.logger;

import static org.junit.Assert.*;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.redolog.RedoLogManager;
import com.zimbra.cs.redolog.op.RedoableOp;
import junit.framework.Assert;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.IOException;


public class DbLogWriterTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private RedoLogManager mockRedoLogManager;
    private DbLogWriter logWriter;

    @BeforeClass
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
    }

    @Before
    public void setUp() throws Exception {
        mockRedoLogManager = EasyMock.createNiceMock(RedoLogManager.class);
        logWriter = new DbLogWriter(mockRedoLogManager);
    }

    @Test
    public void openLogClose() throws Exception {
        logWriter.open();
        Assert.assertTrue("Connection is open successfully", logWriter.isOpen());
        Assert.assertTrue("Table is empty after open the connection first time", logWriter.isEmpty());

        RedoableOp op = EasyMock.createMockBuilder(RedoableOp.class)
                .withConstructor(MailboxOperation.Preview)
                .createMock();

        logWriter.log(op, new ByteArrayInputStream("some bytes".getBytes()), false);
        Assert.assertEquals("file size incorrect.",10, logWriter.getSize());

        logWriter.close();
        Assert.assertTrue("Connection was closed successfully", !logWriter.isOpen());

        logWriter = new DbLogWriter(mockRedoLogManager);
        logWriter.open();
        Assert.assertEquals("file size incorrect.", 10, logWriter.getSize());

        logWriter.log(op, new ByteArrayInputStream("some bytes".getBytes()), false);
        Assert.assertEquals("file size incorrect.",20, logWriter.getSize());
        logWriter.close();
    }

    @Test(expected = Exception.class)
    public void logBeforeOpen() throws Exception {
        logWriter.log(null, null, false);
    }
}