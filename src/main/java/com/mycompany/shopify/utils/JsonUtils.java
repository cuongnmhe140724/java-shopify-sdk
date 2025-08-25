package com.mycompany.shopify.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Utility class for JSON operations using Jackson.
 * Provides methods for serialization, deserialization, and JSON manipulation.
 */
@Component
public class JsonUtils {
    
    private final ObjectMapper objectMapper;
    
    public JsonUtils() {
        this.objectMapper = new ObjectMapper();
        
        // Configure ObjectMapper for better JSON handling
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }
    
    /**
     * Serializes an object to JSON string.
     * @param obj The object to serialize
     * @return JSON string representation
     * @throws JsonProcessingException if serialization fails
     */
    public String toJson(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }
    
    /**
     * Serializes an object to JSON string, returning null on error.
     * @param obj The object to serialize
     * @return JSON string representation or null if serialization fails
     */
    public String toJsonSafe(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
    
    /**
     * Deserializes JSON string to an object of the specified type.
     * @param json The JSON string
     * @param clazz The target class
     * @return The deserialized object
     * @throws IOException if deserialization fails
     */
    public <T> T fromJson(String json, Class<T> clazz) throws IOException {
        return objectMapper.readValue(json, clazz);
    }
    
    /**
     * Deserializes JSON string to an object of the specified type, returning null on error.
     * @param json The JSON string
     * @param clazz The target class
     * @return The deserialized object or null if deserialization fails
     */
    public <T> T fromJsonSafe(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            return null;
        }
    }
    
    /**
     * Deserializes JSON string to an object using TypeReference for generic types.
     * @param json The JSON string
     * @param typeRef The type reference
     * @return The deserialized object
     * @throws IOException if deserialization fails
     */
    public <T> T fromJson(String json, TypeReference<T> typeRef) throws IOException {
        return objectMapper.readValue(json, typeRef);
    }
    
    /**
     * Deserializes JSON string to an object using TypeReference, returning null on error.
     * @param json The JSON string
     * @param typeRef The type reference
     * @return The deserialized object or null if deserialization fails
     */
    public <T> T fromJsonSafe(String json, TypeReference<T> typeRef) {
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (IOException e) {
            return null;
        }
    }
    
    /**
     * Converts an object to a Map representation.
     * @param obj The object to convert
     * @return Map representation of the object
     * @throws JsonProcessingException if conversion fails
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> toMap(Object obj) throws JsonProcessingException {
        return objectMapper.convertValue(obj, Map.class);
    }
    
    /**
     * Converts an object to a Map representation, returning null on error.
     * @param obj The object to convert
     * @return Map representation of the object or null if conversion fails
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> toMapSafe(Object obj) {
        try {
            return objectMapper.convertValue(obj, Map.class);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Converts a Map to an object of the specified type.
     * @param map The map to convert
     * @param clazz The target class
     * @return The converted object
     */
    public <T> T fromMap(Map<String, Object> map, Class<T> clazz) {
        return objectMapper.convertValue(map, clazz);
    }
    
    /**
     * Converts a Map to an object of the specified type, returning null on error.
     * @param map The map to convert
     * @param clazz The target class
     * @return The converted object or null if conversion fails
     */
    public <T> T fromMapSafe(Map<String, Object> map, Class<T> clazz) {
        try {
            return objectMapper.convertValue(map, clazz);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Pretty prints a JSON string with proper indentation.
     * @param json The JSON string to format
     * @return Formatted JSON string
     * @throws IOException if parsing fails
     */
    public String prettyPrint(String json) throws IOException {
        Object obj = objectMapper.readValue(json, Object.class);
        return objectMapper.writeValueAsString(obj);
    }
    
    /**
     * Pretty prints a JSON string, returning the original string on error.
     * @param json The JSON string to format
     * @return Formatted JSON string or original string if formatting fails
     */
    public String prettyPrintSafe(String json) {
        try {
            return prettyPrint(json);
        } catch (IOException e) {
            return json;
        }
    }
    
    /**
     * Checks if a string is valid JSON.
     * @param json The string to validate
     * @return true if valid JSON, false otherwise
     */
    public boolean isValidJson(String json) {
        try {
            objectMapper.readTree(json);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Gets the underlying ObjectMapper instance.
     * @return The ObjectMapper instance
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
