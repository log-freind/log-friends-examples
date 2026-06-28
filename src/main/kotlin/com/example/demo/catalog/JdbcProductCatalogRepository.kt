package com.example.demo.catalog

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class JdbcProductCatalogRepository(
    private val jdbcTemplate: NamedParameterJdbcTemplate
) : ProductCatalogRepository {

    override fun findProducts(
        query: String?,
        category: String?,
        stockStatus: String?,
        minPrice: Int?,
        maxPrice: Int?
    ): List<ProductResponse> {
        val filters = mutableListOf<String>()
        val params = MapSqlParameterSource()

        query?.trim()?.takeIf { it.isNotEmpty() }?.let {
            filters += "(LOWER(name) LIKE LOWER(:query) OR LOWER(brand) LIKE LOWER(:query) OR LOWER(category) LIKE LOWER(:query))"
            params.addValue("query", "%$it%")
        }

        category?.trim()?.takeIf { it.isNotEmpty() }?.let {
            filters += "LOWER(category) = LOWER(:category)"
            params.addValue("category", it)
        }

        stockStatus?.trim()?.takeIf { it.isNotEmpty() }?.let {
            filters += "LOWER(stock_status) = LOWER(:stockStatus)"
            params.addValue("stockStatus", it)
        }

        minPrice?.let {
            filters += "price >= :minPrice"
            params.addValue("minPrice", it)
        }

        maxPrice?.let {
            filters += "price <= :maxPrice"
            params.addValue("maxPrice", it)
        }

        val where = if (filters.isEmpty()) "" else "WHERE ${filters.joinToString(" AND ")}"
        val sql = """
            SELECT product_id, name, category, price, stock_status, stock_quantity, brand, rating, image_url, description
            FROM products
            $where
            ORDER BY sort_order, product_id
        """.trimIndent()

        return jdbcTemplate.query(sql, params) { rs, _ -> rs.toProductResponse() }
    }

    override fun findByProductId(productId: String): ProductResponse? {
        val sql = """
            SELECT product_id, name, category, price, stock_status, stock_quantity, brand, rating, image_url, description
            FROM products
            WHERE product_id = :productId
        """.trimIndent()
        val params = MapSqlParameterSource("productId", productId)

        return jdbcTemplate.query(sql, params) { rs, _ -> rs.toProductResponse() }.firstOrNull()
    }

    private fun java.sql.ResultSet.toProductResponse(): ProductResponse {
        return ProductResponse(
            productId = getString("product_id"),
            name = getString("name"),
            category = getString("category"),
            price = getInt("price"),
            stockStatus = getString("stock_status"),
            stockQuantity = getInt("stock_quantity"),
            brand = getString("brand"),
            rating = getDouble("rating"),
            imageUrl = getString("image_url"),
            description = getString("description")
        )
    }
}
