package com.salayo.locallifebackend.domain.paymenthistory.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPaymentHistory is a Querydsl query type for PaymentHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPaymentHistory extends EntityPathBase<PaymentHistory> {

    private static final long serialVersionUID = 1592436394L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPaymentHistory paymentHistory = new QPaymentHistory("paymentHistory");

    public final com.salayo.locallifebackend.global.entity.QBaseEntity _super = new com.salayo.locallifebackend.global.entity.QBaseEntity(this);

    public final DateTimePath<java.time.LocalDateTime> canceledAt = createDateTime("canceledAt", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final EnumPath<com.salayo.locallifebackend.global.enums.DeletedStatus> deletedStatus = createEnum("deletedStatus", com.salayo.locallifebackend.global.enums.DeletedStatus.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath impUid = createString("impUid");

    public final com.salayo.locallifebackend.domain.member.entity.QMember member;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final com.salayo.locallifebackend.domain.payment.entity.QPayment payment;

    public final StringPath paymentCard = createString("paymentCard");

    public final NumberPath<java.math.BigDecimal> paymentCost = createNumber("paymentCost", java.math.BigDecimal.class);

    public final EnumPath<com.salayo.locallifebackend.domain.paymenthistory.enums.PaymentHistoryStatus> paymentHistoryStatus = createEnum("paymentHistoryStatus", com.salayo.locallifebackend.domain.paymenthistory.enums.PaymentHistoryStatus.class);

    public final StringPath pgTid = createString("pgTid");

    public final StringPath refundReason = createString("refundReason");

    public QPaymentHistory(String variable) {
        this(PaymentHistory.class, forVariable(variable), INITS);
    }

    public QPaymentHistory(Path<? extends PaymentHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPaymentHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPaymentHistory(PathMetadata metadata, PathInits inits) {
        this(PaymentHistory.class, metadata, inits);
    }

    public QPaymentHistory(Class<? extends PaymentHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new com.salayo.locallifebackend.domain.member.entity.QMember(forProperty("member")) : null;
        this.payment = inits.isInitialized("payment") ? new com.salayo.locallifebackend.domain.payment.entity.QPayment(forProperty("payment"), inits.get("payment")) : null;
    }

}

