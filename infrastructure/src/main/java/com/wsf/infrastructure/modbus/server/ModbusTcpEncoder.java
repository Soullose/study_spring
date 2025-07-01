package com.wsf.infrastructure.modbus.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ModbusTcpEncoder extends MessageToByteEncoder<ModbusResponse> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ModbusResponse msg, ByteBuf out) {
        if (msg instanceof ReadHoldingRegistersResponse) {
            encodeReadHoldingRegistersResponse((ReadHoldingRegistersResponse) msg, out);
        }
        // 其他响应类型处理...
    }

    private void encodeReadHoldingRegistersResponse(ReadHoldingRegistersResponse response, ByteBuf out) {
        // 1. 构建 MBAP 头
        out.writeShort(0x0000); // 事务标识符（示例值）
        out.writeShort(0x0000); // 协议标识符（0 表示 Modbus TCP）
        out.writeShort(6 + response.getData().length); // 长度（头部6字节 + 数据）
        out.writeByte(0x01); // 单元标识符（从机地址）

        // 2. 构建 PDU
        out.writeByte(0x03); // 功能码（03 读保持寄存器）
        out.writeShort(response.getData().length / 2); // 寄存器数量
        out.writeBytes(response.getData()); // 寄存器数据
    }
}
