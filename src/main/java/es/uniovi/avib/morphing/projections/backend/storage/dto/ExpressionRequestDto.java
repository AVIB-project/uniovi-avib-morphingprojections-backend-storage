package es.uniovi.avib.morphing.projections.backend.storage.dto;

import lombok.Getter;

@Getter
public class ExpressionRequestDto {
	private String bucketName;
	private String keyObjectName;
	private String annotationId;
}
