package com.salayo.locallifebackend.global.entity;

import com.salayo.locallifebackend.global.enums.DeletedStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
@MappedSuperclass
public class SoftDeletableEntity extends BaseEntity {

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private DeletedStatus deletedStatus = DeletedStatus.DISPLAYED;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	public void softDelete() {
		this.deletedStatus = DeletedStatus.DELETED;
		this.deletedAt = LocalDateTime.now();
	}
}
