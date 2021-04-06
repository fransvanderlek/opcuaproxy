package org.intelligentindustry.opcuaproxy;

import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;

public class NodeDataValue {
	
	private String nodeId;
	private DataValue dataValue;
	
	public NodeDataValue(String nodeId2) {
		this.nodeId = nodeId2;
	}
	
	public String getNodeId() {
		return this.nodeId;
	}

	public DataValue getDataValue() {
		return dataValue;
	}

	public void setDataValue(DataValue dataValue) {
		this.dataValue = dataValue;
	}
	
}