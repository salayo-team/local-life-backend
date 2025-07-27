package com.salayo.locallifebackend.domain.member.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMember is a Querydsl query type for Member
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMember extends EntityPathBase<Member> {

    private static final long serialVersionUID = -804508574L;

    public static final QMember member = new QMember("member1");

    public final com.salayo.locallifebackend.global.entity.QBaseEntity _super = new com.salayo.locallifebackend.global.entity.QBaseEntity(this);

    public final StringPath birth = createString("birth");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath email = createString("email");

    public final EnumPath<com.salayo.locallifebackend.domain.member.enums.Gender> gender = createEnum("gender", com.salayo.locallifebackend.domain.member.enums.Gender.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<com.salayo.locallifebackend.domain.member.enums.MemberRole> memberRole = createEnum("memberRole", com.salayo.locallifebackend.domain.member.enums.MemberRole.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final StringPath nickname = createString("nickname");

    public final StringPath password = createString("password");

    public final StringPath phoneNumber = createString("phoneNumber");

    public QMember(String variable) {
        super(Member.class, forVariable(variable));
    }

    public QMember(Path<? extends Member> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMember(PathMetadata metadata) {
        super(Member.class, metadata);
    }

}

