package es.uniovi.avib.morphing.projections.backend.storage.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DownloadFileResponse {
	String name;
	String errorMessage;
	long size;
	
}
