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


import es.uniovi.avib.morphing.projections.backend.storage.domain.Expression;
import es.uniovi.avib.morphing.projections.backend.storage.dto.ExpressionRequestDto;
import es.uniovi.avib.morphing.projections.backend.storage.dto.ProjectionRequestDto;
import es.uniovi.avib.morphing.projections.backend.storage.service.ExpressionService;

@Slf4j
@CrossOrigin(maxAge = 3600)
@RestController
@RequiredArgsConstructor
@RequestMapping("expressions")
public class ExpressionController {
	private final ExpressionService annotationService;
		
	@RequestMapping(value = "/annotations/name", method = { RequestMethod.POST }, produces = "application/json")
	public ResponseEntity<Object> findAllExpressionsByAnnotation(@RequestBody ProjectionRequestDto projectionRequestDto) {
		List<String> expressions = new ArrayList<>();
		
		try {
			expressions = annotationService.findAnnotationsName(projectionRequestDto);
			
			log.debug("findAllExpressionsByAnnotation: found {} expressions", expressions.size());
			
			return ResponseEntity
		            .status(HttpStatus.OK)
		            .body(expressions);
		} catch (Exception e) {
			e.printStackTrace();
			
			return ResponseEntity
		            .status(HttpStatus.INTERNAL_SERVER_ERROR)
		            .body(e.getMessage());
		}			
	}
	
	@RequestMapping(value = "/annotations/value", method = { RequestMethod.POST }, produces = "application/json")
	public ResponseEntity<Object> findAllExpressionsByAnnotation(@RequestBody ExpressionRequestDto expressionRequestDto) {
		List<Expression> expressions = new ArrayList<>();
		
		try {
			expressions = (List<Expression>) annotationService.findAllExpressionsByAnnotation(expressionRequestDto);
			
			log.debug("findAllExpressionsByAnnotation: found {} expressions", expressions.size());
			
			return ResponseEntity
		            .status(HttpStatus.OK)
		            .body(expressions);
		} catch (Exception e) {
			e.printStackTrace();
			
			return ResponseEntity
		            .status(HttpStatus.INTERNAL_SERVER_ERROR)
		            .body(e.getMessage());
		}			
	}		
}
