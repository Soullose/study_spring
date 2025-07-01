package com.wsf.infrastructure.modbus.client;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class ModbusDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < 8) {
            return; // 至少需要8个字节才能解析基本响应
        }

        in.markReaderIndex();

        // 读取MBAP Header
        int transactionId = in.readUnsignedShort();
        int protocolId = in.readUnsignedShort();
        int length = in.readUnsignedShort();
        int unitId = in.readUnsignedByte();

        // 检查是否有足够的数据
        if (in.readableBytes() < length - 1) {
            in.resetReaderIndex();
            return;
        }

        int functionCode = in.readUnsignedByte();
        boolean isError = (functionCode & 0x80) != 0;

        byte[] data;
        int errorCode = 0;

        if (isError) {
            // 错误响应
            errorCode = in.readUnsignedByte();
            data = new byte[0];
        } else {
            // 正常响应
            int dataLength = length - 2; // 减去功能码字节
            data = new byte[dataLength];
            in.readBytes(data);
        }

        ModbusResponse response = new ModbusResponse(transactionId, unitId,
                functionCode, data, isError, errorCode);
        out.add(response);
    }
}
