package com.wsf.infrastructure.mqtt;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MqttClientCount  {

	private static final Logger log = LoggerFactory.getLogger(MqttClientCount.class);

	private MqttClient mqttClient;

	private String mqttClientId;
	/**
	 * 客户端
	 */
	public static ConcurrentHashMap<String, MqttClientCount> mqttClients = new ConcurrentHashMap<>();


	public String setMqttClientId(String clientId) {
		return this.mqttClientId = clientId;
	}


	public void setMqttClient(String host, String clientId, String userName, String passWord, MqttCallback mqttCallback)
			throws MqttException {
		MqttConnectOptions mqttConnectOptions = mqttConnectOptions(host, clientId, userName, passWord);
		if (mqttCallback == null) {
			mqttClient.setCallback(new MqttClientCallback(mqttClientId));
		} else {
			mqttClient.setCallback(mqttCallback);
		}
		mqttClient.connect(mqttConnectOptions);
	}

	private MqttConnectOptions mqttConnectOptions(String host, String clientId, String userName, String passWord)
			throws MqttException {
		/// 创建 MQTT 客户端对象
		mqttClient = new MqttClient(host, clientId, new MemoryPersistence());
		MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
		mqttConnectOptions.setUserName(userName);
		mqttConnectOptions.setPassword(passWord.toCharArray());
		mqttConnectOptions.setAutomaticReconnect(true);
		mqttConnectOptions.setCleanSession(false);
		return mqttConnectOptions;
	}

	/**
	 * 关闭MQTT连接
	 */

	public void close() throws MqttException {
		mqttClient.close();
		mqttClient.disconnect();
	}
	/**
	 * 向某个主题发布消息 默认qos：1
	 *
	 * @param topic:发布的主题
	 * @param msg：发布的消息
	 */

	public void pub(String topic, String msg) throws MqttException {
		MqttMessage mqttMessage = new MqttMessage();
		// mqttMessage.setQos(2);
		mqttMessage.setPayload(msg.getBytes(StandardCharsets.UTF_8));
		MqttTopic mqttTopic = mqttClient.getTopic(topic);
		MqttDeliveryToken token = mqttTopic.publish(mqttMessage);
		token.waitForCompletion();
	}

	/**
	 * 向某个主题发布消息
	 *
	 * @param topic:
	 *            发布的主题
	 * @param msg:
	 *            发布的消息
	 * @param qos:
	 *            消息质量 Qos：0、1、2
	 */

	public void pub(String topic, String msg, int qos) throws MqttException {
		MqttMessage mqttMessage = new MqttMessage();
		mqttMessage.setQos(qos);
		mqttMessage.setPayload(msg.getBytes(StandardCharsets.UTF_8));
		MqttTopic mqttTopic = mqttClient.getTopic(topic);
		MqttDeliveryToken token = mqttTopic.publish(mqttMessage);
		token.waitForCompletion();
	}

	/**
	 * 订阅多个主题 ，此方法默认的的Qos等级为：1
	 *
	 * @param topic
	 *            主题
	 */

	public void sub(String[] topic) throws MqttException {
		mqttClient.subscribe(topic);
	}

	/**
	 * 订阅一个个主题 ，此方法默认的的Qos等级为：1
	 *
	 * @param topic
	 *            主题
	 */

	public void sub(String topic) throws MqttException {
		mqttClient.subscribe(topic);
	}

	/**
	 * 订阅某一个主题，可携带Qos
	 *
	 * @param topic
	 *            所要订阅的主题
	 * @param qos
	 *            消息质量：0、1、2
	 */

	public void sub(String topic, int qos) throws MqttException {
		mqttClient.subscribe(topic, qos);
	}
}
