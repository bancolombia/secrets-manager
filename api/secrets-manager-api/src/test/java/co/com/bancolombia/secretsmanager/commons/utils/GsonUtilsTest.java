package co.com.bancolombia.secretsmanager.commons.utils;

import com.google.gson.annotations.SerializedName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GsonUtilsTest {

    record Inner(String token) {
    }

    record Nested(Inner service) {
    }

    record Dotted(@SerializedName("service.token") String token) {
    }

    record Underscore(@SerializedName("service_token") String token) {
    }

    record Hyphen(@SerializedName("service-token") String token) {
    }

    record NestedRenamed(@SerializedName("service") Inner inner) {
    }

    record Alternate(@SerializedName(value = "service.token", alternate = {"service_token", "serviceToken"})
                     String token) {
    }

    record NoAnnotations(String token, Inner service) {
    }

    record Mixed(String name, @SerializedName("service.token") String token) {
    }

    static class DottedClass {
        @SerializedName("service.token")
        String token;
    }

    private static final GsonUtils UTILS = GsonUtils.getInstance();

    @Test
    void nestedJsonWithoutAnnotations() {
        var result = UTILS.stringToModel("{\"other\":\"x\",\"service\":{\"token\":\"abc\"}}", Nested.class);
        assertEquals("abc", result.service().token());
    }

    @Test
    void nestedJsonWithRenamedField() {
        var result = UTILS.stringToModel("{\"service\":{\"token\":\"abc\"}}", NestedRenamed.class);
        assertEquals("abc", result.inner().token());
    }

    @Test
    void dottedKey() {
        var result = UTILS.stringToModel("{\"other\":\"x\",\"service.token\":\"abc\"}", Dotted.class);
        assertEquals("abc", result.token());
    }

    @Test
    void underscoreKey() {
        var result = UTILS.stringToModel("{\"other\":\"x\",\"service_token\":\"abc\"}", Underscore.class);
        assertEquals("abc", result.token());
    }

    @Test
    void hyphenKey() {
        var result = UTILS.stringToModel("{\"other\":\"x\",\"service-token\":\"abc\"}", Hyphen.class);
        assertEquals("abc", result.token());
    }

    @Test
    void dottedKeyInClass() {
        var result = UTILS.stringToModel("{\"service.token\":\"abc\"}", DottedClass.class);
        assertEquals("abc", result.token);
    }

    @Test
    void alternateKeys() {
        assertEquals("abc", UTILS.stringToModel("{\"service.token\":\"abc\"}", Alternate.class).token());
        assertEquals("abc", UTILS.stringToModel("{\"service_token\":\"abc\"}", Alternate.class).token());
        assertEquals("abc", UTILS.stringToModel("{\"serviceToken\":\"abc\"}", Alternate.class).token());
    }

    @Test
    void mixedAnnotatedAndPlainFields() {
        var result = UTILS.stringToModel("{\"name\":\"n\",\"service.token\":\"abc\"}", Mixed.class);
        assertEquals("n", result.name());
        assertEquals("abc", result.token());
    }

    @Test
    void withoutAnnotationsFieldNameIsUsed() {
        var result = UTILS.stringToModel("{\"token\":\"abc\",\"service\":{\"token\":\"def\"}}", NoAnnotations.class);
        assertEquals("abc", result.token());
        assertEquals("def", result.service().token());
    }

    @Test
    void withoutAnnotationsDottedKeyIsNotMapped() {
        assertNull(UTILS.stringToModel("{\"service.token\":\"abc\"}", NoAnnotations.class).token());
    }

    @Test
    void serializesUsingSerializedName() {
        assertEquals("{\"service.token\":\"abc\"}", UTILS.modelToString(new Dotted("abc")));
    }
}
