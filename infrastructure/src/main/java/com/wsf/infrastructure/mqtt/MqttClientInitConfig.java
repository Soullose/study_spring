package com.wsf.infrastructure.mqtt;

import java.util.UUID;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;

@Configuration
public class MqttClientInitConfig {
	private static final Logger logger = LoggerFactory.getLogger(MqttClientInitConfig.class);
	@Bean
	public MessageChannel mqttOutboundChannel() {
		return new DirectChannel();
	}
	// @PostConstruct
	void init() throws MqttException {
//		String host = "tcp://192.168.68.73:1883";
//		String connectId = UUID.randomUUID().toString();
//		MqttClientCount mqttClientService = new MqttClientCount();
//		mqttClientService.setMqttClient(host, connectId, "mqtt_vhost:mqtt", "123", null);
//		mqttClientService.sub("wsf/#");
//		mqttClientService.setMqttClientId(connectId);
	}
}
