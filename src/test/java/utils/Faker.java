package utils;

import com.github.javafaker.service.FakeValuesService;
import com.github.javafaker.service.RandomService;

import java.util.Locale;

public class Faker {

    private static com.github.javafaker.Faker faker = new com.github.javafaker.Faker();
    private static FakeValuesService fakeValuesService = new FakeValuesService(
            new Locale("en-GB"), new RandomService());

    public static com.github.javafaker.Faker getFaker() {
        return faker;
    }

    public static FakeValuesService getFakeValuesService() {
        return fakeValuesService;
    }
}