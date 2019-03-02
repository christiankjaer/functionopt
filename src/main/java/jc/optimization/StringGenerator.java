package jc.optimization;

import java.util.Random;

class StringGenerator {
    private static final String CHAR_LIST = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private Random random;

    public StringGenerator() {
        this.random = new Random();
    }

    public String generate(Integer length) {
        StringBuilder randomString = new StringBuilder(length);

        for (int i = 0; i < length; i += 1) {
            randomString.append(CHAR_LIST.charAt(random.nextInt(CHAR_LIST.length())));
        }

        return randomString.toString();
    }
}
