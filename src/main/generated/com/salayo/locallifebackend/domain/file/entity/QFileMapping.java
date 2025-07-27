package com.salayo.locallifebackend.domain.file.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFileMapping is a Querydsl query type for FileMapping
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFileMapping extends EntityPathBase<FileMapping> {

    private static final long serialVersionUID = -1295802232L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFileMapping fileMapping = new QFileMapping("fileMapping");

    public final com.salayo.locallifebackend.global.entity.QBaseEntity _super = new com.salayo.locallifebackend.global.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final QFile file;

    public final EnumPath<com.salayo.locallifebackend.domain.file.enums.FileCategory> fileCategory = createEnum("fileCategory", com.salayo.locallifebackend.domain.file.enums.FileCategory.class);

    public final EnumPath<com.salayo.locallifebackend.domain.file.enums.FilePurpose> filePurpose = createEnum("filePurpose", com.salayo.locallifebackend.domain.file.enums.FilePurpose.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final NumberPath<Long> referenceId = createNumber("referenceId", Long.class);

    public QFileMapping(String variable) {
        this(FileMapping.class, forVariable(variable), INITS);
    }

    public QFileMapping(Path<? extends FileMapping> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFileMapping(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFileMapping(PathMetadata metadata, PathInits inits) {
        this(FileMapping.class, metadata, inits);
    }

    public QFileMapping(Class<? extends FileMapping> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.file = inits.isInitialized("file") ? new QFile(forProperty("file")) : null;
    }

}

