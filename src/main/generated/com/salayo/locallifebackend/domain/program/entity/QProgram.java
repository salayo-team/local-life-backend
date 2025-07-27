package com.salayo.locallifebackend.domain.program.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProgram is a Querydsl query type for Program
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProgram extends EntityPathBase<Program> {

    private static final long serialVersionUID = 220787028L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProgram program = new QProgram("program");

    public final com.salayo.locallifebackend.global.entity.QBaseEntity _super = new com.salayo.locallifebackend.global.entity.QBaseEntity(this);

    public final com.salayo.locallifebackend.domain.category.entity.QAptitudeCategory aptitudeCategory;

    public final StringPath businessName = createString("businessName");

    public final NumberPath<Integer> count = createNumber("count", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath curriculumDescription = createString("curriculumDescription");

    public final EnumPath<com.salayo.locallifebackend.global.enums.DeletedStatus> deletedStatus = createEnum("deletedStatus", com.salayo.locallifebackend.global.enums.DeletedStatus.class);

    public final StringPath description = createString("description");

    public final DatePath<java.time.LocalDate> endDate = createDate("endDate", java.time.LocalDate.class);

    public final NumberPath<java.math.BigDecimal> finalPrice = createNumber("finalPrice", java.math.BigDecimal.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<com.salayo.locallifebackend.domain.program.enums.LocalSpecialized> isLocalSpecialized = createEnum("isLocalSpecialized", com.salayo.locallifebackend.domain.program.enums.LocalSpecialized.class);

    public final StringPath location = createString("location");

    public final NumberPath<Integer> maxCapacity = createNumber("maxCapacity", Integer.class);

    public final com.salayo.locallifebackend.domain.member.entity.QMember member;

    public final NumberPath<Integer> minCapacity = createNumber("minCapacity", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final QProgram originalProgram;

    public final NumberPath<java.math.BigDecimal> percent = createNumber("percent", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> price = createNumber("price", java.math.BigDecimal.class);

    public final ListPath<ProgramDay, QProgramDay> programDays = this.<ProgramDay, QProgramDay>createList("programDays", ProgramDay.class, QProgramDay.class, PathInits.DIRECT2);

    public final QProgramGroup programGroup;

    public final ListPath<com.salayo.locallifebackend.domain.programschedule.entity.ProgramSchedule, com.salayo.locallifebackend.domain.programschedule.entity.QProgramSchedule> programSchedules = this.<com.salayo.locallifebackend.domain.programschedule.entity.ProgramSchedule, com.salayo.locallifebackend.domain.programschedule.entity.QProgramSchedule>createList("programSchedules", com.salayo.locallifebackend.domain.programschedule.entity.ProgramSchedule.class, com.salayo.locallifebackend.domain.programschedule.entity.QProgramSchedule.class, PathInits.DIRECT2);

    public final ListPath<ProgramScheduleTime, QProgramScheduleTime> programScheduleTimes = this.<ProgramScheduleTime, QProgramScheduleTime>createList("programScheduleTimes", ProgramScheduleTime.class, QProgramScheduleTime.class, PathInits.DIRECT2);

    public final EnumPath<com.salayo.locallifebackend.domain.program.enums.ProgramStatus> programStatus = createEnum("programStatus", com.salayo.locallifebackend.domain.program.enums.ProgramStatus.class);

    public final com.salayo.locallifebackend.domain.category.entity.QRegionCategory regionCategory;

    public final DatePath<java.time.LocalDate> startDate = createDate("startDate", java.time.LocalDate.class);

    public final StringPath title = createString("title");

    public QProgram(String variable) {
        this(Program.class, forVariable(variable), INITS);
    }

    public QProgram(Path<? extends Program> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProgram(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProgram(PathMetadata metadata, PathInits inits) {
        this(Program.class, metadata, inits);
    }

    public QProgram(Class<? extends Program> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.aptitudeCategory = inits.isInitialized("aptitudeCategory") ? new com.salayo.locallifebackend.domain.category.entity.QAptitudeCategory(forProperty("aptitudeCategory")) : null;
        this.member = inits.isInitialized("member") ? new com.salayo.locallifebackend.domain.member.entity.QMember(forProperty("member")) : null;
        this.originalProgram = inits.isInitialized("originalProgram") ? new QProgram(forProperty("originalProgram"), inits.get("originalProgram")) : null;
        this.programGroup = inits.isInitialized("programGroup") ? new QProgramGroup(forProperty("programGroup")) : null;
        this.regionCategory = inits.isInitialized("regionCategory") ? new com.salayo.locallifebackend.domain.category.entity.QRegionCategory(forProperty("regionCategory")) : null;
    }

}

