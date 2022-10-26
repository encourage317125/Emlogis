package com.emlogis.common;

import com.emlogis.model.tenant.PasswordPolicies;

import java.util.*;


public class PasswordGenerator {

    private interface CharGenerator {
        char generate();
    }

    public String generatePassword(PasswordPolicies passwordPolicies) {
        int passwordLength = generatePasswordLength(passwordPolicies.getMinPasswordLength(),
                passwordPolicies.getMaxPasswordLength());

        char[] result = new char[passwordLength];
        List<Integer> passwordIndices = new ArrayList<>();
        for (int i = 0; i < passwordLength; i++) {
            passwordIndices.add(i);
        }

        Collection<CharGenerator> requiredGenerators = requiredGenerators(
                passwordPolicies.isRequireAtLeastOneUppercaseChar(),
                passwordPolicies.isRequireAtLeastOneLowercaseChar(),
                passwordPolicies.isRequireAtLeastOneNumberChar(),
                passwordPolicies.isRequireAtLeastOneNonalphaChar());

        for (CharGenerator requiredGenerator : requiredGenerators) {
            char symbol = requiredGenerator.generate();

            if (passwordIndices.isEmpty()) {
                throw new RuntimeException("Password policies are inconsistent");
            }
            int indicesIndex = randomNumberBetween(0, passwordIndices.size() - 1);

            Integer passwordIndex = passwordIndices.get(indicesIndex);
            passwordIndices.remove(indicesIndex);

            result[passwordIndex] = symbol;
        }

        final String wholeCharset = PasswordUtils.UPPERCASE_CHARSET + PasswordUtils.LOWERCASE_CHARSET +
                PasswordUtils.NUMBER_CHARSET + PasswordUtils.NON_ALPHA_CHARSET;
        for (Integer passwordIndex : passwordIndices) {
            result[passwordIndex] = randomChar(wholeCharset);
        }

        return new String(result);
    }

    private Collection<CharGenerator> requiredGenerators(boolean requireAtLeastOneUppercaseChar,
                                                         boolean requireAtLeastOneLowercaseChar,
                                                         boolean requireAtLeastOneNumberChar,
                                                         boolean requireAtLeastOneNonAlphaChar) {
        Collection<CharGenerator> result = new ArrayList<>();

        if (requireAtLeastOneUppercaseChar) {
            CharGenerator generator = new CharGenerator() {
                @Override
                public char generate() {
                    return randomChar(PasswordUtils.UPPERCASE_CHARSET);
                }
            };
            result.add(generator);
        }
        if (requireAtLeastOneLowercaseChar) {
            CharGenerator generator = new CharGenerator() {
                @Override
                public char generate() {
                    return randomChar(PasswordUtils.LOWERCASE_CHARSET);
                }
            };
            result.add(generator);
        }
        if (requireAtLeastOneNumberChar) {
            CharGenerator generator = new CharGenerator() {
                @Override
                public char generate() {
                    return randomChar(PasswordUtils.NUMBER_CHARSET);
                }
            };
            result.add(generator);
        }
        if (requireAtLeastOneNonAlphaChar) {
            CharGenerator generator = new CharGenerator() {
                @Override
                public char generate() {
                    return randomChar(PasswordUtils.NON_ALPHA_CHARSET);
                }
            };
            result.add(generator);
        }

        return result;
    }

    private char randomChar(String charset) {
        int index = randomNumberBetween(0, charset.length() - 1);
        return charset.charAt(index);
    }

    private int generatePasswordLength(int min, int max) {
        int result;
        if (min <= 0 && max > 0) {
            result = randomNumberBetween(Math.min(max, 8), max);
        } else if (min > 0 && max <= 0) {
            result = randomNumberBetween(min, Math.max(min, 12));
        } else if (min <= 0 && max <= 0) {
            result = randomNumberBetween(8, 12);
        } else if (min <= max) {
            result = randomNumberBetween(min, max);
        } else {
            throw new RuntimeException("Password policies are inconsistent");
        }
        return result;
    }

    private int randomNumberBetween(int min, int max) {
        Random random = new Random();
        int delta = random.nextInt(max + 1 - min);
        return min + delta;
    }

}
