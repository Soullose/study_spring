package com.wsf.infrastructure.mqtt;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.stereotype.Service;

//@Service
public interface MqttClientService {
	String setMqttClientId(String clientId);
	void setMqttClient(String host, String clientId, String userName, String passWord, MqttCallback mqttCallback)
			throws MqttException;
	void close() throws MqttException;
	void pub(String topic, String msg) throws MqttException;
	void pub(String topic, String msg, int qos) throws MqttException;
	public void sub(String[] topic) throws MqttException;
	void sub(String topic) throws MqttException;
	void sub(String topic, int qos) throws MqttException;
}
