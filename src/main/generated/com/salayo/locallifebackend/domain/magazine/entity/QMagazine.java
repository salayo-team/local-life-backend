package com.salayo.locallifebackend.domain.magazine.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMagazine is a Querydsl query type for Magazine
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMagazine extends EntityPathBase<Magazine> {

    private static final long serialVersionUID = -658581482L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMagazine magazine = new QMagazine("magazine");

    public final com.salayo.locallifebackend.global.entity.QBaseEntity _super = new com.salayo.locallifebackend.global.entity.QBaseEntity(this);

    public final com.salayo.locallifebackend.domain.member.entity.QMember admin;

    public final com.salayo.locallifebackend.domain.category.entity.QAptitudeCategory aptitudeCategory;

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final EnumPath<com.salayo.locallifebackend.global.enums.DeletedStatus> deletedStatus = createEnum("deletedStatus", com.salayo.locallifebackend.global.enums.DeletedStatus.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<com.salayo.locallifebackend.domain.magazine.enums.MagazineStatus> magazineStatus = createEnum("magazineStatus", com.salayo.locallifebackend.domain.magazine.enums.MagazineStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final com.salayo.locallifebackend.domain.category.entity.QRegionCategory regionCategory;

    public final DateTimePath<java.time.LocalDateTime> registeredAt = createDateTime("registeredAt", java.time.LocalDateTime.class);

    public final StringPath thumbnailUrl = createString("thumbnailUrl");

    public final StringPath title = createString("title");

    public final NumberPath<Long> views = createNumber("views", Long.class);

    public QMagazine(String variable) {
        this(Magazine.class, forVariable(variable), INITS);
    }

    public QMagazine(Path<? extends Magazine> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMagazine(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMagazine(PathMetadata metadata, PathInits inits) {
        this(Magazine.class, metadata, inits);
    }

    public QMagazine(Class<? extends Magazine> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.admin = inits.isInitialized("admin") ? new com.salayo.locallifebackend.domain.member.entity.QMember(forProperty("admin")) : null;
        this.aptitudeCategory = inits.isInitialized("aptitudeCategory") ? new com.salayo.locallifebackend.domain.category.entity.QAptitudeCategory(forProperty("aptitudeCategory")) : null;
        this.regionCategory = inits.isInitialized("regionCategory") ? new com.salayo.locallifebackend.domain.category.entity.QRegionCategory(forProperty("regionCategory")) : null;
    }

}

