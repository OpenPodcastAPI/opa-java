package org.openpodcastapi.opa.pagination;

import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/// Generic repository for returning paginated results.
/// Works only with entities that implement [Cursorable].
@Repository
public class CursorRepository {
    private final JPAQueryFactory queryFactory;

    /// All-args constructor
    ///
    /// @param queryFactory the JPAQueryFactory to use
    public CursorRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /// Fetches a paginated set of results for an entity type with a cursor
    ///
    /// @param <T>              the [Cursorable] entity type
    /// @param <Q>              the QueryDSL type of the entity
    /// @param qEntity          the entity (as its QueryDSL type)
    /// @param cursor           the cursor used to filter results
    /// @param limit            the number of results to fetch
    /// @param additionalFilter any additional [BooleanExpression] to apply to the query
    /// @param forward          whether to cursor forwards or backwards
    /// @return a paginated response
    public <T extends Cursorable, Q extends EntityPathBase<T>> CursorPage<T> findWithCursor(
            Q qEntity,
            CursorPayload cursor,
            int limit,
            BooleanExpression additionalFilter,
            boolean forward
    ) {
        // Get the `createdAt` timestamp of the entity
        final var createdAtPath = Expressions.dateTimePath(Instant.class, qEntity, "createdAt");
        // Get the `id` of the entity
        final var idPath = Expressions.numberPath(Long.class, qEntity, "id");

        // Create the cursor pagination predicate
        final var predicate = buildKeysetPredicate(createdAtPath, idPath, cursor, forward);

        final List<T> results = queryFactory
                .selectFrom(qEntity)
                .where(additionalFilter, predicate)
                .orderBy(createdAtPath.desc(), idPath.desc())
                .limit(limit)
                .fetch();

        return CursorPage.of(
                results,
                limit,
                e -> new CursorPayload(e.getCreatedAt(), e.getId())
        );
    }

    /// Helper function to fetch pageable results using `id` and `createdAt` fields
    ///
    /// @param createdAt the created at timestamp of the entity
    /// @param id        the database ID of the entity
    /// @param cursor    the cursor payload to use for pagination
    /// @param forward   whether to search forward or backwards (older or newer records)
    private BooleanExpression buildKeysetPredicate(
            DateTimePath<Instant> createdAt,
            NumberPath<Long> id,
            CursorPayload cursor,
            boolean forward
    ) {
        if (cursor == null) return null;

        if (forward) {
            return createdAt.lt(cursor.createdAt())
                    .or(createdAt.eq(cursor.createdAt())
                            .and(id.lt(cursor.id())));
        } else {
            return createdAt.gt(cursor.createdAt())
                    .or(createdAt.eq(cursor.createdAt())
                            .and(id.gt(cursor.id())));
        }
    }
}
