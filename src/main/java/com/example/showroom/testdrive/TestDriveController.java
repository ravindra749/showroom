package com.example.showroom.testdrive;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test-drives")
public class TestDriveController {

    private final TestDriveService testDriveService;

    public TestDriveController(TestDriveService testDriveService) {
        this.testDriveService = testDriveService;
    }

    @GetMapping
    public List<TestDriveResponse> findAll(
            @RequestParam(required = false) TestDriveStatus status) {
        return testDriveService.findAll(status);
    }

    @PostMapping
    public ResponseEntity<TestDriveResponse> book(@Valid @RequestBody TestDriveRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(testDriveService.book(request));
    }

    @PatchMapping("/{id}/status")
    public TestDriveResponse changeStatus(
            @PathVariable Long id, @Valid @RequestBody TestDriveStatusChange change) {
        return testDriveService.changeStatus(id, change.status());
    }
}
