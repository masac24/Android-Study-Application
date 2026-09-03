package com.example.diplomski.controller

import com.example.diplomski.dto.NoteDto
import com.example.diplomski.dto.QuizDto
import com.example.diplomski.entity.Note
import com.example.diplomski.repository.FolderRepository
import com.example.diplomski.repository.NoteRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

@RestController
@RequestMapping("/files")
class FileController(

    @Value("\${app.upload.dir}")
    private val uploadDir: String,
    private val noteRepository: NoteRepository,
    private val folderRepository: FolderRepository

) {

    @PostMapping(
        "/upload",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun uploadFile(
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<Map<String, String>> {

        Files.createDirectories(Paths.get(uploadDir))

        val filename =
            "${System.currentTimeMillis()}_${file.originalFilename}"

        val destination = Paths.get(uploadDir, filename)

        file.transferTo(destination.toFile())

        return ResponseEntity.ok(
            mapOf(
                "path" to filename
            )
        )
    }

    @GetMapping("/download/{filename}")
    fun downloadFile(
        @PathVariable filename: String
    ): ResponseEntity<Resource> {

        val file = File(uploadDir, filename)

        if (!file.exists()) {
            return ResponseEntity.notFound().build()
        }

        val resource = FileSystemResource(file)

        return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"${file.name}\""
            )
            .contentLength(file.length())
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource)
    }


}