package com.salayo.locallifebackend.domain.category.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAptitudeCategory is a Querydsl query type for AptitudeCategory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAptitudeCategory extends EntityPathBase<AptitudeCategory> {

    private static final long serialVersionUID = -874131280L;

    public static final QAptitudeCategory aptitudeCategory = new QAptitudeCategory("aptitudeCategory");

    public final StringPath aptitudeCode = createString("aptitudeCode");

    public final StringPath aptitudeName = createString("aptitudeName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QAptitudeCategory(String variable) {
        super(AptitudeCategory.class, forVariable(variable));
    }

    public QAptitudeCategory(Path<? extends AptitudeCategory> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAptitudeCategory(PathMetadata metadata) {
        super(AptitudeCategory.class, metadata);
    }

}

