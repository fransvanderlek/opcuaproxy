package org.intelligentindustry.opcua.cloudconnector;

import java.text.MessageFormat;
import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.gson.GsonDataFormat;
import org.apache.camel.component.milo.server.MiloServerComponent;
import org.apache.camel.spi.PropertiesComponent;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;

/**
 * A Camel Java DSL Router
 */
public class CloudConnectorRouteBuilder extends RouteBuilder {
	
	GsonDataFormat gson() {
		return new GsonDataFormat() {
			{
				setUnmarshalType(DataValue.class);

			}
		};

	};

	public void configure() {
		
		PropertiesComponent pc = getContext().getPropertiesComponent();
		pc.addLocation("file:config/opcua-cloud-connect.properties");		
				
		from("file:internal?fileName=nodes.txt").log("${body}").convertBodyTo(String.class).process(new Processor() {

			@Override
			public void process(Exchange exchange) throws Exception {
				String file = exchange.getIn().getBody().toString(); // get file contents
				final String[] nodes = file.split("\\r?\\n");

				getContext().addRoutes(new RouteBuilder() {

					@Override
					public void configure() throws Exception {

						Map<String,Object> properties = this.getContext().getPropertiesComponent().loadPropertiesAsMap();
														
						for (String node : nodes) {
							
							NodeId nodeId = NodeId.parse(node);
							
							String statusTopic = MessageFormat.format(properties.get("broker.status-topic").toString(), nodeId.toParseableString() );
							String commandTopic = MessageFormat.format(properties.get("broker.command-topic").toString(), nodeId.toParseableString() );
							String brokerUrl = properties.get("broker.url").toString();							
							Endpoint mqttNodeStatus = endpoint("paho:"+statusTopic+"?brokerUrl="+brokerUrl+"&retained=true");
							Endpoint mqttNodeCommand = endpoint("paho:"+commandTopic+"?brokerUrl="+brokerUrl+"&retained=true");
							
							String opcuaOriginUrl = properties.get("opcua.origin.url").toString();							
							Endpoint opcuaNodeSubscriber = endpoint("milo-client:"+opcuaOriginUrl+"?node=RAW(" + nodeId.toParseableString() + ")&samplingInterval=1000&allowedSecurityPolicies=None");
							Endpoint opcuaNodeWriter = endpoint("milo-client:"+opcuaOriginUrl+"?node=RAW(" + nodeId.toParseableString() + ")&allowedSecurityPolicies=None");
										
							
							from(opcuaNodeSubscriber)
								.log("${body}")
								.marshal(gson())
								.convertBodyTo(String.class)
							.to(mqttNodeStatus);
							
							from(mqttNodeCommand).
								convertBodyTo(String.class).
								unmarshal(gson()).								
							to( opcuaNodeWriter );									
							
						}
					}
				});
			}

		}).to("seda:done");

	}
}
	
	

/**
 * 
 * 	private Processor addNode(final NodeId node) {
		return new Processor() {

			@Override
			public void process(Exchange exchange) throws Exception {

				NodeDataValue nodeDataValue = new NodeDataValue(node.toParseableString());
				Message m = exchange.getIn();
				DataValue value = m.getBody(DataValue.class);
				nodeDataValue.setDataValue(value);
				m.setBody(nodeDataValue);

			}
		};
	}
 * 
 * 
 * from("file:inbox?fileName=nodes.txt").log("${body}").process(new Processor()
 * {
 * 
 * 
 * @Override public void process(final Exchange exchange) throws Exception {
 * 
 *           final String contents = exchange.getIn().getBody().toString();
 *           final CamelContext context = exchange.getContext();
 * 
 * 
 *           Thread restart = new Thread() {
 * @Override public void run() {
 * 
 * 
 *           System.out.println("starting the restart thread...");
 * 
 *           System.out.println("setting countdown latch for context stop");
 * 
 * 
 * 
 *           try {
 * 
 *           Thread.sleep(1000);
 * 
 * 
 * 
 *           final CountDownLatch contextStopCountDown = new CountDownLatch(1);
 * 
 *           context.addLifecycleStrategy(new LifecycleStrategySupport() {
 *           public void onContextStop(CamelContext context) {
 *           contextStopCountDown.countDown(); } });
 * 
 * 
 *           context.stop(); contextStopCountDown.await();
 * 
 *           System.out.println("latch released, restarting");
 * 
 *           FileWriter myWriter = new FileWriter("internal/nodes.txt", false);
 *           myWriter.write(contents); //myWriter.write("test");
 *           myWriter.close();
 * 
 *           File origin = new File("inbox/nodes.txt");
 * 
 *           if( origin.exists()) { origin.delete(); System.out.println("deleted
 *           origin file"); }
 * 
 *           context.start();
 * 
 *           } catch (Exception e) { e.printStackTrace(); } } };
 *           restart.start();
 * 
 *           } }).log("going to stop...").to("seda:done");
 *           
 *           
 *           
 *       
	public void configurem() {

		this.bindToRegistry("opcua-server", new MiloServerComponent() {
			{
				setBindAddresses("0.0.0.0");
				setEnableAnonymousAuthentication(true);
				setPort(12685);
				setApplicationName("opcua");

			}
		});

		String[] nodes = new String[] { "ns=1;s=temperature", "ns=1;s=Control Relay number 0." };

		NodeId[] nodeIds = new NodeId[] {
				NodeId.parse("ns=1;s=temperature"),
				NodeId.parse("ns=1;s=Control Relay number 0.")
		};

		Endpoint brokerStatus = endpoint(
				"paho:IntelligentIndustryExperience/${header.nodeId}/temperature/status?brokerUrl=tcp://test.mosquitto.org:1883?retained=true");

		for (String node : nodes) {

			from("milo-client:opc.tcp://192.168.1.201:4840?node=RAW(" + node
					+ ")&samplingInterval=1000&allowedSecurityPolicies=None").process(mqttClientPreProcessor())
							.marshal(gson()).convertBodyTo(String.class).to(brokerStatus);
		}

		from(brokerStatus).convertBodyTo(String.class).log("${body}").unmarshal(gson())
				.process(removeNode()).toD("opcua-server:${header.nodeId}");

		EndpointConsumerBuilder c;
	}

	private Processor removeNode() {
		return new Processor() {

			@Override
			public void process(Exchange exchange) throws Exception {

				Message m = exchange.getIn();
				NodeDataValue value = m.getBody(NodeDataValue.class);				
				m.setBody(value.getDataValue());

			}
		};
	}
	

	

	private Processor mqttClientPreProcessor() {
		return new Processor() {

			@Override
			public void process(Exchange exchange) throws Exception {

				String uri = exchange.getFromEndpoint().getEndpointUri();
				String nodeId = uri.split("[()]")[1];
				NodeDataValue nodeDataValue = new NodeDataValue(nodeId);
				Message m = exchange.getIn();
				DataValue value = m.getBody(DataValue.class);
				nodeDataValue.setDataValue(value);
				m.setBody(nodeDataValue);
				m.setHeader("nodeId", nodeId);
			}
		};
	}

}
 */
