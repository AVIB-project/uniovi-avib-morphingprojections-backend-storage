package es.uniovi.avib.morphing.projections.backend.storage.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import es.uniovi.avib.morphing.projections.backend.storage.dto.ProjectionRequestDto;
import es.uniovi.avib.morphing.projections.backend.storage.service.ProjectionService;

@Slf4j
@CrossOrigin(maxAge = 3600)
@RestController
@RequiredArgsConstructor
@RequestMapping("projection")
public class ProjectionController {
	private final ProjectionService projectionService;
			
	@RequestMapping(value = "/primal", method = { RequestMethod.POST }, produces = "application/json")
	public ResponseEntity<List<Object>> findPrimalProjection(@RequestBody ProjectionRequestDto projectionRequestDto) {
		List<Object> projections = new ArrayList<Object>();
		
		try {
			projections = projectionService.findProjection(projectionRequestDto);
			
			log.debug("findPrimalProjection: found {} projections", projections.size());
			
			return ResponseEntity
		            .status(HttpStatus.OK)
		            .body(projections);
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
			
			return ResponseEntity
		            .status(HttpStatus.INTERNAL_SERVER_ERROR)
		            .body(new ArrayList<>());
		}			
	}	
	
	@RequestMapping(value = "/dual", method = { RequestMethod.POST }, produces = "application/json")
	public ResponseEntity<List<Object>> findDualProjection(@RequestBody ProjectionRequestDto projectionRequestDto) {
		List<Object> projections = new ArrayList<Object>();
		
		try {
			projections = projectionService.findProjection(projectionRequestDto);
			
			log.debug("findDualProjection: found {} projections", projections.size());
			
			return ResponseEntity
		            .status(HttpStatus.OK)
		            .body(projections);
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
			
			return ResponseEntity
		            .status(HttpStatus.INTERNAL_SERVER_ERROR)
		            .body(new ArrayList<>());
		}			
	}	
}
