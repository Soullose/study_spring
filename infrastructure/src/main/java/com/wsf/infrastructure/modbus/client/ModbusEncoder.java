package com.wsf.infrastructure.modbus.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ModbusEncoder extends MessageToByteEncoder<ModbusRequest> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ModbusRequest msg, ByteBuf out) {
        // MBAP Header (7 bytes)
        out.writeShort(msg.getTransactionId());  // Transaction ID
        out.writeShort(0);                       // Protocol ID (always 0 for Modbus)
        out.writeShort(6);                       // Length (6 bytes following)
        out.writeByte(msg.getUnitId());          // Unit ID

        // PDU (Protocol Data Unit)
        out.writeByte(msg.getFunctionCode());    // Function Code
        out.writeShort(msg.getStartAddress());   // Starting Address
        out.writeShort(msg.getQuantity());       // Quantity of registers
    }
}
