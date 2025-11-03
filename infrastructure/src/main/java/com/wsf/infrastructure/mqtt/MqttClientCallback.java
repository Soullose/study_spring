package com.wsf.infrastructure.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttClientCallback implements MqttCallbackExtended {
	private static final Logger logger = LoggerFactory.getLogger(MqttClientCallback.class);
	/**
	 * 系统的mqtt客户端id
	 */
	private String mqttClientId;

	public MqttClientCallback(String mqttClientId) {
		this.mqttClientId = mqttClientId;
	}
	@Override
	public void connectionLost(Throwable cause) {
		logger.debug("断开了MQTT连接 ：{}", cause.getMessage());
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		logger.debug("收到来自 :{}====:{} 的消息：{}", mqttClientId, topic, new String(message.getPayload()));
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		logger.debug("发布消息成功");
	}

	@Override
	public void connectComplete(boolean reconnect, String serverURI) {

	}
}
