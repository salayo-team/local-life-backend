package com.salayo.locallifebackend.domain.program.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QProgramDay is a Querydsl query type for ProgramDay
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProgramDay extends EntityPathBase<ProgramDay> {

    private static final long serialVersionUID = 1871489448L;

    public static final QProgramDay programDay = new QProgramDay("programDay");

    public final EnumPath<com.salayo.locallifebackend.domain.program.enums.DayName> dayName = createEnum("dayName", com.salayo.locallifebackend.domain.program.enums.DayName.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QProgramDay(String variable) {
        super(ProgramDay.class, forVariable(variable));
    }

    public QProgramDay(Path<? extends ProgramDay> path) {
        super(path.getType(), path.getMetadata());
    }

    public QProgramDay(PathMetadata metadata) {
        super(ProgramDay.class, metadata);
    }

}

