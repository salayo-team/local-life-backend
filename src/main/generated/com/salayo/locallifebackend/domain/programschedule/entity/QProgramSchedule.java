package com.salayo.locallifebackend.domain.programschedule.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProgramSchedule is a Querydsl query type for ProgramSchedule
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProgramSchedule extends EntityPathBase<ProgramSchedule> {

    private static final long serialVersionUID = -1237439852L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProgramSchedule programSchedule = new QProgramSchedule("programSchedule");

    public final com.salayo.locallifebackend.global.entity.QBaseEntity _super = new com.salayo.locallifebackend.global.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final EnumPath<com.salayo.locallifebackend.global.enums.DeletedStatus> deletedStatus = createEnum("deletedStatus", com.salayo.locallifebackend.global.enums.DeletedStatus.class);

    public final TimePath<java.time.LocalTime> endTime = createTime("endTime", java.time.LocalTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final com.salayo.locallifebackend.domain.program.entity.QProgram program;

    public final EnumPath<com.salayo.locallifebackend.domain.programschedule.enums.ProgramScheduleStatus> programScheduleStatus = createEnum("programScheduleStatus", com.salayo.locallifebackend.domain.programschedule.enums.ProgramScheduleStatus.class);

    public final DatePath<java.time.LocalDate> scheduleDate = createDate("scheduleDate", java.time.LocalDate.class);

    public final TimePath<java.time.LocalTime> startTime = createTime("startTime", java.time.LocalTime.class);

    public QProgramSchedule(String variable) {
        this(ProgramSchedule.class, forVariable(variable), INITS);
    }

    public QProgramSchedule(Path<? extends ProgramSchedule> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProgramSchedule(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProgramSchedule(PathMetadata metadata, PathInits inits) {
        this(ProgramSchedule.class, metadata, inits);
    }

    public QProgramSchedule(Class<? extends ProgramSchedule> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.program = inits.isInitialized("program") ? new com.salayo.locallifebackend.domain.program.entity.QProgram(forProperty("program"), inits.get("program")) : null;
    }

}

