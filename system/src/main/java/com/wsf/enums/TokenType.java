package com.wsf.enums;

import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * Token 类型枚举。
 *
 * @author wsf
 */
public enum TokenType {
	BEARER(1, "Bearer");

	public final int code;
	public final String name;

	TokenType(int code, String name) {
		this.code = code;
		this.name = name;
	}

	/**
	 * 根据 code 获取 TokenType 名称。
	 *
	 * @param code Token 类型编码
	 * @return TokenType 名称
	 */
	public static String codeToString(int code) {
		return Arrays.stream(TokenType.values())
				.filter(tokenType -> tokenType.code == code)
				.findFirst()
				.map(t -> t.name)
				.orElseThrow(() -> new NoSuchElementException("没有找到 Token 类型对应的 code: " + code));
	}
}
