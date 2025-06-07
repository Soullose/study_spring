package com.wsf.domain;

import java.security.SecureRandom;

public class NanoId {

    private static final int POOL_SIZE_MULTIPLIER = 128;
    private static byte[] pool;
    private static int poolOffset;
    private static final SecureRandom random = new SecureRandom();
    private static final String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz-";

    private static void fillPool(int bytes) {
        if (pool == null || pool.length < bytes) {
            pool = new byte[bytes * POOL_SIZE_MULTIPLIER];
            random.nextBytes(pool);
            poolOffset = 0;
        } else if (poolOffset + bytes > pool.length) {
            random.nextBytes(pool);
            poolOffset = 0;
        }
        poolOffset += bytes;
    }

//    public static byte[] random(int bytes) {
//        bytes |= 0; // Convert to integer
//        fillPool(bytes);
//        byte[] result = new byte[bytes];
//        System.arraycopy(pool, poolOffset - bytes, result, 0, bytes);
//        return result;
//    }

//    public static String customRandom(String alphabet, int defaultSize, RandomGenerator getRandom) {
//        int mask = (2 << (31 - Integer.numberOfLeadingZeros((alphabet.length() - 1) | 1))) - 1;
//        int step = (int) Math.ceil((1.6 * mask * defaultSize) / alphabet.length());
//
//        return (size) -> {
//            StringBuilder id = new StringBuilder();
//            while (true) {
//                byte[] bytes = getRandom.generate(step);
//                for (int i = 0; i < step; i++) {
//                    char c = alphabet.charAt(bytes[i] & mask);
//                    id.append(c);
//                    if (id.length() >= size) {
//                        return id.toString();
//                    }
//                }
//            }
//        };
//    }
//
//    public static String customAlphabet(String alphabet, int size) {
//        return customRandom(alphabet, size, NanoId::random).generate(size);
//    }

    public static String nanoid(int size) {
        size |= 0; // Convert to integer
        fillPool(size);
        StringBuilder id = new StringBuilder();
        for (int i = poolOffset - size; i < poolOffset; i++) {
            id.append(alphabet.charAt(pool[i] & 63));
        }
        return id.toString();
    }

    public static void main(String[] args) {
        String id = nanoid(21);
        System.out.println("Generated NanoID: " + id);
    }

    @FunctionalInterface
    public interface RandomGenerator {
        byte[] generate(int size);
    }
}
