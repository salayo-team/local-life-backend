package com.salayo.locallifebackend.domain.program.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QProgramScheduleTime is a Querydsl query type for ProgramScheduleTime
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProgramScheduleTime extends EntityPathBase<ProgramScheduleTime> {

    private static final long serialVersionUID = 2086018424L;

    public static final QProgramScheduleTime programScheduleTime = new QProgramScheduleTime("programScheduleTime");

    public final TimePath<java.time.LocalTime> endTime = createTime("endTime", java.time.LocalTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> scheduleCount = createNumber("scheduleCount", Integer.class);

    public final NumberPath<Integer> scheduleDuration = createNumber("scheduleDuration", Integer.class);

    public final TimePath<java.time.LocalTime> startTime = createTime("startTime", java.time.LocalTime.class);

    public QProgramScheduleTime(String variable) {
        super(ProgramScheduleTime.class, forVariable(variable));
    }

    public QProgramScheduleTime(Path<? extends ProgramScheduleTime> path) {
        super(path.getType(), path.getMetadata());
    }

    public QProgramScheduleTime(PathMetadata metadata) {
        super(ProgramScheduleTime.class, metadata);
    }

}

