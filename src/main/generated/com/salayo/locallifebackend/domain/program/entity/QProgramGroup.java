package com.salayo.locallifebackend.domain.program.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QProgramGroup is a Querydsl query type for ProgramGroup
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProgramGroup extends EntityPathBase<ProgramGroup> {

    private static final long serialVersionUID = -1086666357L;

    public static final QProgramGroup programGroup = new QProgramGroup("programGroup");

    public final StringPath groupTitle = createString("groupTitle");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QProgramGroup(String variable) {
        super(ProgramGroup.class, forVariable(variable));
    }

    public QProgramGroup(Path<? extends ProgramGroup> path) {
        super(path.getType(), path.getMetadata());
    }

    public QProgramGroup(PathMetadata metadata) {
        super(ProgramGroup.class, metadata);
    }

}

