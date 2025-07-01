package com.wsf.infrastructure.modbus.server;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class ModbusTcpDecoder extends ByteToMessageDecoder {
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
		// 1. 检查 MBAP 头（Modbus TCP 标识）
		if (in.readableBytes() < 7) { // MBAP 头固定 7 字节
			return;
		}

		in.markReaderIndex(); // 标记当前位置

		// 2. 解析 MBAP 头
		int transactionId = in.readUnsignedShort();
		int protocolId = in.readUnsignedShort();
		int length = in.readUnsignedShort();
		int unitId = in.readUnsignedByte();

		// 3. 检查 PDU（协议数据单元）
		if (in.readableBytes() < length - 2) { // length 包含功能码+数据
			in.resetReaderIndex(); // 数据不足，重置读取位置
			return;
		}

		// 4. 解析功能码和数据
		byte functionCode = in.readByte();
		byte[] data = new byte[length - 3]; // 功能码占 1 字节
		in.readBytes(data);

		// 5. 构建 Modbus 请求对象（需自定义）
		ModbusRequest request = createRequest(functionCode, data);
		out.add(request);
	}

	private ModbusRequest createRequest(byte functionCode, byte[] data) {
		// 根据功能码创建不同请求类型（示例）
		switch (functionCode) {
			case 0x03 : // 读取保持寄存器
				return new ReadHoldingRegistersRequest(data[0] << 8 | data[1], // 起始地址
						data[2] << 8 | data[3] // 寄存器数量
				);
			default :
				throw new UnsupportedOperationException("不支持的功能码: " + functionCode);
		}
	}
}
