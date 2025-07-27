package com.salayo.locallifebackend.domain.reservation.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReservation is a Querydsl query type for Reservation
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReservation extends EntityPathBase<Reservation> {

    private static final long serialVersionUID = 860906452L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReservation reservation = new QReservation("reservation");

    public final com.salayo.locallifebackend.global.entity.QBaseEntity _super = new com.salayo.locallifebackend.global.entity.QBaseEntity(this);

    public final DateTimePath<java.time.LocalDateTime> canceledAt = createDateTime("canceledAt", java.time.LocalDateTime.class);

    public final StringPath cancelReason = createString("cancelReason");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final EnumPath<com.salayo.locallifebackend.global.enums.DeletedStatus> deletedStatus = createEnum("deletedStatus", com.salayo.locallifebackend.global.enums.DeletedStatus.class);

    public final DateTimePath<java.time.LocalDateTime> expiredAt = createDateTime("expiredAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.salayo.locallifebackend.domain.member.entity.QMember member;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final com.salayo.locallifebackend.domain.programschedule.entity.QProgramSchedule programSchedule;

    public final DateTimePath<java.time.LocalDateTime> rejectedAt = createDateTime("rejectedAt", java.time.LocalDateTime.class);

    public final StringPath rejectedReason = createString("rejectedReason");

    public final EnumPath<com.salayo.locallifebackend.domain.reservation.enums.ReservationStatus> reservationStatus = createEnum("reservationStatus", com.salayo.locallifebackend.domain.reservation.enums.ReservationStatus.class);

    public QReservation(String variable) {
        this(Reservation.class, forVariable(variable), INITS);
    }

    public QReservation(Path<? extends Reservation> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReservation(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReservation(PathMetadata metadata, PathInits inits) {
        this(Reservation.class, metadata, inits);
    }

    public QReservation(Class<? extends Reservation> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new com.salayo.locallifebackend.domain.member.entity.QMember(forProperty("member")) : null;
        this.programSchedule = inits.isInitialized("programSchedule") ? new com.salayo.locallifebackend.domain.programschedule.entity.QProgramSchedule(forProperty("programSchedule"), inits.get("programSchedule")) : null;
    }

}

