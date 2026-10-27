package com.petsplace;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/** Breeds, categories, products and puppies, read once from static/data/catalog.json. */
@Service
public class CatalogService {

    private final Map<String, Object> data;

    public CatalogService(ObjectMapper mapper) throws IOException {
        try (InputStream in = new ClassPathResource("static/data/catalog.json").getInputStream()) {
            this.data = mapper.readValue(in, new TypeReference<Map<String, Object>>() {});
        }
    }

    public Object breeds() { return data.get("breeds"); }

    public Object categories() { return data.get("categories"); }

    public Object puppies() { return data.get("puppies"); }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> products() {
        return (List<Map<String, Object>>) data.get("products");
    }

    public Map<String, Object> product(String id) {
        for (Map<String, Object> p : products()) {
            if (p.get("id").equals(id)) return p;
        }
        return null;
    }
}
