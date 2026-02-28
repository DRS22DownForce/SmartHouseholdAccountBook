package com.example.backend.entity.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import jakarta.persistence.PersistenceException;

import java.util.ArrayList;
import java.util.List;

/**
 * List<String> を JSON 文字列へ相互変換するコンバーター。
 *
 * エンティティ側では List として扱い、DB保存時のみ JSON へ変換します。
 */
@Converter
public class StringListJsonConverter implements AttributeConverter<List<String>, String> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        try {
            if (attribute == null) {
                return "[]";
            }
            return OBJECT_MAPPER.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new PersistenceException("改善提案のJSONシリアライズに失敗しました。", e);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.isBlank()) {
                return new ArrayList<>();
            }
            return OBJECT_MAPPER.readValue(dbData, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            throw new PersistenceException("改善提案のJSONデシリアライズに失敗しました。", e);
        }
    }
}
