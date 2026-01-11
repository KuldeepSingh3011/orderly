package com.orderly.inventory.search;

import com.orderly.inventory.entity.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "spring.elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class ProductSearchService {

    private static final Logger log = LoggerFactory.getLogger(ProductSearchService.class);

    private final ProductSearchRepository searchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    @Autowired
    public ProductSearchService(ProductSearchRepository searchRepository,
                                ElasticsearchOperations elasticsearchOperations) {
        this.searchRepository = searchRepository;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    /**
     * Index a product in Elasticsearch
     */
    public void indexProduct(Product product) {
        ProductDocument doc = toDocument(product);
        searchRepository.save(doc);
        log.info("Indexed product: {}", product.getId());
    }

    /**
     * Remove a product from the search index
     */
    public void removeFromIndex(String productId) {
        searchRepository.deleteById(productId);
        log.info("Removed product from index: {}", productId);
    }

    /**
     * Search products by query string (searches name and description)
     */
    public List<ProductDocument> search(String query) {
        Criteria criteria = new Criteria("active").is(true)
                .and(new Criteria("name").contains(query)
                        .or(new Criteria("description").contains(query)));

        CriteriaQuery searchQuery = new CriteriaQuery(criteria);
        SearchHits<ProductDocument> hits = elasticsearchOperations.search(searchQuery, ProductDocument.class);

        return hits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

    /**
     * Search with filters
     */
    public List<ProductDocument> searchWithFilters(String query, String category, 
                                                    Double minPrice, Double maxPrice) {
        Criteria criteria = new Criteria("active").is(true);

        if (query != null && !query.isBlank()) {
            criteria = criteria.and(
                    new Criteria("name").contains(query)
                            .or(new Criteria("description").contains(query))
            );
        }

        if (category != null && !category.isBlank()) {
            criteria = criteria.and(new Criteria("category").is(category));
        }

        if (minPrice != null) {
            criteria = criteria.and(new Criteria("price").greaterThanEqual(minPrice));
        }

        if (maxPrice != null) {
            criteria = criteria.and(new Criteria("price").lessThanEqual(maxPrice));
        }

        CriteriaQuery searchQuery = new CriteriaQuery(criteria);
        SearchHits<ProductDocument> hits = elasticsearchOperations.search(searchQuery, ProductDocument.class);

        return hits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

    /**
     * Get autocomplete suggestions
     */
    public List<String> getSuggestions(String prefix) {
        List<ProductDocument> results = searchRepository
                .findByNameContainingIgnoreCaseAndActiveTrue(prefix);

        return results.stream()
                .map(ProductDocument::getName)
                .distinct()
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * Reindex all products (useful for initial sync)
     */
    public void reindexAll(List<Product> products) {
        List<ProductDocument> documents = products.stream()
                .map(this::toDocument)
                .collect(Collectors.toList());

        searchRepository.saveAll(documents);
        log.info("Reindexed {} products", products.size());
    }

    private ProductDocument toDocument(Product product) {
        ProductDocument doc = new ProductDocument();
        doc.setId(product.getId());
        doc.setName(product.getName());
        doc.setDescription(product.getDescription());
        doc.setCategory(product.getCategory());
        doc.setSku(product.getSku());
        doc.setPrice(product.getPrice());
        doc.setStockQuantity(product.getStockQuantity());
        doc.setImageUrl(product.getImageUrl());
        doc.setActive(product.isActive());
        return doc;
    }
}
