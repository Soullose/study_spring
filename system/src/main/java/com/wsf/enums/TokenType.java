package com.wsf.enums;

import java.util.Arrays;
import java.util.NoSuchElementException;

public enum TokenType implements EnumEvents<TokenType> {
	BEARER(1, "Bearer");

	public final int code;
	public final String name;

	TokenType(int code, String name) {
		this.code = code;
		this.name = name;
	}

	@Override
	public String Code2String(TokenType t) {
		TokenType type = Arrays.stream(TokenType.values()).filter(tokenType -> tokenType.code == t.code).findFirst()
				.orElseThrow(() -> new NoSuchElementException("没有找到区域类型"));
		return type.name;
	}
}
