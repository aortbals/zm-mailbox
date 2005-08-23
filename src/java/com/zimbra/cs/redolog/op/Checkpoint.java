/*
 * ***** BEGIN LICENSE BLOCK *****
 * Version: ZPL 1.1
 * 
 * The contents of this file are subject to the Zimbra Public License
 * Version 1.1 ("License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.zimbra.com/license
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 * 
 * The Original Code is: Zimbra Collaboration Suite.
 * 
 * The Initial Developer of the Original Code is Zimbra, Inc.  Portions
 * created by Zimbra are Copyright (C) 2005 Zimbra, Inc.  All Rights
 * Reserved.
 * 
 * Contributor(s): 
 * 
 * ***** END LICENSE BLOCK *****
 */

/*
 * Created on 2004. 7. 23.
 */
package com.zimbra.cs.redolog.op;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import com.zimbra.cs.redolog.TransactionId;

/**
 * @author jhahm
 *
 * A Checkpoint record is written at the end of a redolog file as it is rolled
 * over.  It lists all uncommitted (i.e. in-progress) operations at the time
 * of log rollover.  Currently checkpoint information is not used.  One
 * potential use is logfile validity check.
 */
public class Checkpoint extends ControlOp {

	LinkedHashSet /*<TxnId>*/ mTxnSet;

	public Checkpoint() {
		mTxnSet = new LinkedHashSet();
	}

	public Checkpoint(LinkedHashSet txns) {
		mTxnSet = txns;
		setTransactionId(new TransactionId());  // don't need a real txnid for checkpoint record
	}

	public int getNumActiveTxns() {
		return mTxnSet.size();
	}

	public Set /*<TxnId>*/ getActiveTxns() {
		return mTxnSet;
	}

	public void log() {
		// this method should not be called
	}

	/* (non-Javadoc)
	 * @see com.zimbra.cs.redolog.op.RedoableOp#getOperationCode()
	 */
	public int getOpCode() {
		return OP_CHECKPOINT;
	}

	protected String getPrintableData() {
		if (mTxnSet.size() > 0) {
			StringBuffer sb = new StringBuffer();
            sb.append(mTxnSet.size()).append(" active txns: ");
			int i = 0;
			for (Iterator it = mTxnSet.iterator(); it.hasNext(); i++) {
				TransactionId txn = (TransactionId) it.next();
				if (i > 0)
					sb.append(", ");
				sb.append(txn.toString());
			}
			return sb.toString();
		} else
			return null;
	}

	protected void serializeData(DataOutput out) throws IOException {
		out.writeInt(mTxnSet.size());
		for (Iterator it = mTxnSet.iterator(); it.hasNext(); ) {
			TransactionId txn = (TransactionId) it.next();
			txn.serialize(out);
		}
	}

	protected void deserializeData(DataInput in) throws IOException {
		int num = in.readInt();
		for (int i = 0; i < num; i++) {
			TransactionId txn = new TransactionId();
			txn.deserialize(in);
			mTxnSet.add(txn);
		}
	}
}
