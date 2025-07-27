package com.salayo.locallifebackend.domain.ai.aptitude.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAptitudeTestHistory is a Querydsl query type for AptitudeTestHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAptitudeTestHistory extends EntityPathBase<AptitudeTestHistory> {

    private static final long serialVersionUID = 999743812L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAptitudeTestHistory aptitudeTestHistory = new QAptitudeTestHistory("aptitudeTestHistory");

    public final com.salayo.locallifebackend.global.entity.QBaseEntity _super = new com.salayo.locallifebackend.global.entity.QBaseEntity(this);

    public final StringPath aiResponse = createString("aiResponse");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.salayo.locallifebackend.domain.member.entity.QMember member;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final StringPath questionText = createString("questionText");

    public final NumberPath<Integer> step = createNumber("step", Integer.class);

    public final StringPath userResponse = createString("userResponse");

    public QAptitudeTestHistory(String variable) {
        this(AptitudeTestHistory.class, forVariable(variable), INITS);
    }

    public QAptitudeTestHistory(Path<? extends AptitudeTestHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAptitudeTestHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAptitudeTestHistory(PathMetadata metadata, PathInits inits) {
        this(AptitudeTestHistory.class, metadata, inits);
    }

    public QAptitudeTestHistory(Class<? extends AptitudeTestHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new com.salayo.locallifebackend.domain.member.entity.QMember(forProperty("member")) : null;
    }

}

