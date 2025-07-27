package com.salayo.locallifebackend.domain.ai.aptitude.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserAptitude is a Querydsl query type for UserAptitude
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserAptitude extends EntityPathBase<UserAptitude> {

    private static final long serialVersionUID = 1139885161L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserAptitude userAptitude = new QUserAptitude("userAptitude");

    public final com.salayo.locallifebackend.global.entity.QBaseEntity _super = new com.salayo.locallifebackend.global.entity.QBaseEntity(this);

    public final EnumPath<com.salayo.locallifebackend.domain.ai.aptitude.enums.AptitudeType> aptitudeType = createEnum("aptitudeType", com.salayo.locallifebackend.domain.ai.aptitude.enums.AptitudeType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.salayo.locallifebackend.domain.member.entity.QMember member;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final NumberPath<Integer> testCount = createNumber("testCount", Integer.class);

    public final NumberPath<Integer> testStep = createNumber("testStep", Integer.class);

    public QUserAptitude(String variable) {
        this(UserAptitude.class, forVariable(variable), INITS);
    }

    public QUserAptitude(Path<? extends UserAptitude> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserAptitude(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserAptitude(PathMetadata metadata, PathInits inits) {
        this(UserAptitude.class, metadata, inits);
    }

    public QUserAptitude(Class<? extends UserAptitude> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new com.salayo.locallifebackend.domain.member.entity.QMember(forProperty("member")) : null;
    }

}

