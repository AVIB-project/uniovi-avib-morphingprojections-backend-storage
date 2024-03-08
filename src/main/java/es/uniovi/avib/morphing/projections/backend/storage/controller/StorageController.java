package es.uniovi.avib.morphing.projections.backend.storage.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import es.uniovi.avib.morphing.projections.backend.storage.dto.DownloadFileResponse;
import es.uniovi.avib.morphing.projections.backend.storage.dto.UploadFileResponse;
import es.uniovi.avib.morphing.projections.backend.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin(maxAge = 3600)
@RestController
@RequiredArgsConstructor
@RequestMapping("storage")
public class StorageController {
	private final StorageService resourceService;
	    
    @RequestMapping(method = { RequestMethod.POST }, consumes = { MediaType.MULTIPART_FORM_DATA_VALUE}, produces = "application/json", value = "/organizations/{organizationId}/projects/{projectId}/cases/{caseId}")
    public ResponseEntity<List<UploadFileResponse>> uploadFiles(
    		@PathVariable String organizationId,
    		@PathVariable String projectId,
    		@PathVariable String caseId,
            @RequestPart("file[]") MultipartFile[] files) {
    	List<UploadFileResponse> uploadFilesResponse = new ArrayList<UploadFileResponse>();
    	
    	Arrays.asList(files).forEach(file -> {
            try {
		    	log.debug("uploadFiles: upload file with name: {}", file.getOriginalFilename().toString());
		    	
		    	resourceService.uploadFiles(organizationId, projectId, caseId, file);
		    	
		    	uploadFilesResponse.add(UploadFileResponse.builder()
		    			.name(file.getOriginalFilename())
		    			.size(file.getSize())
		    			.build());
            } catch (Exception e) {
            	uploadFilesResponse.add(UploadFileResponse.builder()
		    			.name(file.getOriginalFilename())
		    			.size(file.getSize())
		    			.errorMessage(e.getMessage())
		    			.build());
            }
        });
    	
    	return new ResponseEntity<List<UploadFileResponse>>(uploadFilesResponse, HttpStatus.OK);
    }
    
    @RequestMapping(method = { RequestMethod.GET }, produces = "application/json", value = "/cases/{caseId}")
    public ResponseEntity<List<DownloadFileResponse>> downloadFiles(
    		@PathVariable String caseId) {
    	
    	List<DownloadFileResponse> downloadFilesResponse = null;
    	try {
    		log.debug("downloadFiles: download files from caseId: {}", caseId);
    		
    		downloadFilesResponse = resourceService.downloadFiles(caseId);
    	} catch (Exception e) {    	
    	}
    	
    	return new ResponseEntity<List<DownloadFileResponse>>(downloadFilesResponse, HttpStatus.OK);
    }
    
    @RequestMapping(method = { RequestMethod.DELETE }, produces = "application/json", value = "/organizations/{organizationId}/projects/{projectId}/cases/{caseId}/file/{file}")
    public ResponseEntity<Boolean> deleteFile(
    		@PathVariable String organizationId,
    		@PathVariable String projectId,
    		@PathVariable String caseId,
    		@PathVariable String file) {
    	
    	Boolean result = true;
    	try {
    		log.debug("downloadFiles: download files from caseId: {}", caseId);
    		
    		result = resourceService.deleteFile(organizationId, projectId, caseId, file);
    	} catch (Exception e) {    	
    	}
    	
    	return new ResponseEntity<Boolean>(result, HttpStatus.OK);
    }
}
