package utils;

import activesupport.faker.FakerUtils;

import java.util.LinkedHashMap;

public class Faker {

    private static FakerUtils fakerUtils = new FakerUtils();

    public static String generateFirstName() {
        return fakerUtils.generateFirstName();
    }

    public static String generateLastName() {
        return fakerUtils.generateLastName();
    }

    public static LinkedHashMap<String, String> generateAddress() {
        return fakerUtils.generateAddress();
    }

    public static String generateCompanyName() {
        return fakerUtils.generateCompanyName();
    }

    public static String generateUniqueId(int sizeMin16) {
        return fakerUtils.generateUniqueId(sizeMin16);
    }

    public static String generateNatureOfBusiness() {
        return fakerUtils.generateNatureOfBusiness();
    }

    public static String letterify(String regex) {
        return fakerUtils.letterify(regex);
    }

    public static String numerify(String regex) {
        return fakerUtils.numerify(regex);
    }

    public static String bothify(String regex) {
        return fakerUtils.bothify(regex);
    }

    public static String getRandomRealUKPostcode() {
        return fakerUtils.getRandomRealUKPostcode();
    }
}