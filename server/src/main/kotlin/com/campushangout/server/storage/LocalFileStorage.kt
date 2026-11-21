package com.campushangout.server.storage

import com.campushangout.server.account.exception.InvalidStudentIdImageException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.UUID

@Component
class LocalFileStorage(
    @Value("\${student-verification.storage-directory:uploads/student-ids}")
    directory: String
) : FileStorage {

    private val root: Path = Paths.get(directory)
        .toAbsolutePath()
        .normalize()

    override fun save(file: MultipartFile): String {
        val extension = when (file.contentType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            else -> throw InvalidStudentIdImageException()
        }

        val filename = "${UUID.randomUUID()}.$extension"
        val destination = root.resolve(filename).normalize()

        Files.createDirectories(root)

        file.inputStream.use { input ->
            Files.copy(
                input,
                destination,
                StandardCopyOption.REPLACE_EXISTING
            )
        }

        return filename
    }
}
