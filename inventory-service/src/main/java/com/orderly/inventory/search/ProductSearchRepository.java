package com.orderly.inventory.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * Elasticsearch repository for product search.
 * Only enabled when Elasticsearch is configured via ElasticsearchConfig.
 */
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, String> {

    List<ProductDocument> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String name, String description);

    List<ProductDocument> findByCategory(String category);

    List<ProductDocument> findByActiveTrue();

    List<ProductDocument> findByNameContainingIgnoreCaseAndActiveTrue(String name);
}
