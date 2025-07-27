package com.salayo.locallifebackend.domain.category.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QRegionCategory is a Querydsl query type for RegionCategory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRegionCategory extends EntityPathBase<RegionCategory> {

    private static final long serialVersionUID = 554376446L;

    public static final QRegionCategory regionCategory = new QRegionCategory("regionCategory");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath regionName = createString("regionName");

    public QRegionCategory(String variable) {
        super(RegionCategory.class, forVariable(variable));
    }

    public QRegionCategory(Path<? extends RegionCategory> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRegionCategory(PathMetadata metadata) {
        super(RegionCategory.class, metadata);
    }

}

