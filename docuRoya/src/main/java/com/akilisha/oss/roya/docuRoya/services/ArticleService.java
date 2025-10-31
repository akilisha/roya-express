package com.akilisha.oss.roya.docuRoya.services;

import com.akilisha.oss.roya.plugins.database.Database;
import org.jooq.DSLContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.akilisha.oss.roya.docuRoya.jooq.Tables.ARTICLES;

/**
 * Service for article operations using JOOQ generated classes.
 *
 * This demonstrates the Database plugin's code generation feature.
 */
public class ArticleService {
    private final DSLContext db;

    public ArticleService(Database database) {
        this.db = database.dsl();
    }

    /**
     * List articles with pagination.
     */
    public List<com.akilisha.oss.roya.docuRoya.jooq.tables.pojos.Articles> list(int limit, int offset) {
        return db.selectFrom(ARTICLES)
            .orderBy(ARTICLES.CREATED_AT.desc())
            .limit(limit)
            .offset(offset)
            .fetchInto(com.akilisha.oss.roya.docuRoya.jooq.tables.pojos.Articles.class);
    }

    /**
     * Get article by ID.
     */
    public Optional<com.akilisha.oss.roya.docuRoya.jooq.tables.pojos.Articles> getById(UUID id) {
        return db.selectFrom(ARTICLES)
            .where(ARTICLES.ID.eq(id))
            .fetchOptionalInto(com.akilisha.oss.roya.docuRoya.jooq.tables.pojos.Articles.class);
    }

    /**
     * Create article.
     * @param userId User ID from Auth plugin (String UUID, converted to UUID)
     */
    public com.akilisha.oss.roya.docuRoya.jooq.tables.pojos.Articles create(String userId, String title, String content, List<String> tags) {
        UUID id = UUID.randomUUID();
        UUID userIdUuid = UUID.fromString(userId);
        LocalDateTime now = LocalDateTime.now();

        db.insertInto(ARTICLES)
            .set(ARTICLES.ID, id)
            .set(ARTICLES.USER_ID, userIdUuid)
            .set(ARTICLES.TITLE, title)
            .set(ARTICLES.CONTENT, content)
            .set(ARTICLES.TAGS, tags != null && !tags.isEmpty() ? tags.toArray(new String[0]) : null)
            .set(ARTICLES.CREATED_AT, now)
            .set(ARTICLES.UPDATED_AT, now)
            .execute();

        return getById(id).orElseThrow();
    }

    /**
     * Update article.
     */
    public Optional<com.akilisha.oss.roya.docuRoya.jooq.tables.pojos.Articles> update(UUID id, String title, String content, List<String> tags) {
        int updated = db.update(ARTICLES)
            .set(ARTICLES.TITLE, title)
            .set(ARTICLES.CONTENT, content)
            .set(ARTICLES.TAGS, tags != null && !tags.isEmpty() ? tags.toArray(new String[0]) : null)
            .set(ARTICLES.UPDATED_AT, LocalDateTime.now())
            .where(ARTICLES.ID.eq(id))
            .execute();

        if (updated == 0) {
            return Optional.empty();
        }

        return getById(id);
    }

    /**
     * Delete article.
     */
    public boolean delete(UUID id) {
        int deleted = db.deleteFrom(ARTICLES)
            .where(ARTICLES.ID.eq(id))
            .execute();
        return deleted > 0;
    }

    /**
     * Check if user owns article.
     * @param articleId Article ID (UUID)
     * @param userId User ID from Auth plugin (String UUID, converted to UUID)
     */
    public boolean isOwner(UUID articleId, String userId) {
        UUID userIdUuid = UUID.fromString(userId);
        return db.selectCount()
            .from(ARTICLES)
            .where(ARTICLES.ID.eq(articleId))
            .and(ARTICLES.USER_ID.eq(userIdUuid))
            .fetchOne(0, int.class) > 0;
    }
}

