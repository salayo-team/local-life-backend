package com.salayo.locallifebackend.domain.localcreator.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QLocalCreator is a Querydsl query type for LocalCreator
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLocalCreator extends EntityPathBase<LocalCreator> {

    private static final long serialVersionUID = 1863971728L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QLocalCreator localCreator = new QLocalCreator("localCreator");

    public final com.salayo.locallifebackend.global.entity.QBaseEntity _super = new com.salayo.locallifebackend.global.entity.QBaseEntity(this);

    public final StringPath businessAddress = createString("businessAddress");

    public final StringPath businessName = createString("businessName");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final EnumPath<com.salayo.locallifebackend.domain.localcreator.enums.CreatorStatus> creatorStatus = createEnum("creatorStatus", com.salayo.locallifebackend.domain.localcreator.enums.CreatorStatus.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.salayo.locallifebackend.domain.member.entity.QMember member;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final StringPath rejectedReason = createString("rejectedReason");

    public QLocalCreator(String variable) {
        this(LocalCreator.class, forVariable(variable), INITS);
    }

    public QLocalCreator(Path<? extends LocalCreator> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QLocalCreator(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QLocalCreator(PathMetadata metadata, PathInits inits) {
        this(LocalCreator.class, metadata, inits);
    }

    public QLocalCreator(Class<? extends LocalCreator> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new com.salayo.locallifebackend.domain.member.entity.QMember(forProperty("member")) : null;
    }

}

