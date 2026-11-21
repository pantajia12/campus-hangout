package com.campushangout.server.storage

import com.campushangout.server.account.exception.InvalidStudentIdImageException
import com.campushangout.server.common.error.StudentIdFileTooLargeException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID

@Component
class LocalFileStorage(
    @Value("\${student-verification.storage-directory:uploads/student-ids}")
    directory: String
) : FileStorage {

    private val root = Paths.get(directory).toAbsolutePath().normalize()

    companion object {
        private const val MAX_FILE_SIZE = 5L * 1024 * 1024

        private val PNG_SIGNATURE = byteArrayOf(
            0x89.toByte(), 0x50, 0x4E, 0x47,
            0x0D, 0x0A, 0x1A, 0x0A
        )
    }

    override fun save(file: MultipartFile): String {

        if (file.size > MAX_FILE_SIZE) {
            throw StudentIdFileTooLargeException()
        }

        if (file.isEmpty) {
            throw InvalidStudentIdImageException()
        }

        val bytes = file.bytes

        val extension = when {
            file.contentType == "image/png" && isPng(bytes) -> "png"
            file.contentType == "image/jpeg" && isJpeg(bytes) -> "jpg"
            else -> throw InvalidStudentIdImageException()
        }

        Files.createDirectories(root)

        val filename = "${UUID.randomUUID()}.$extension"
        val destination = root.resolve(filename)

        Files.write(destination, bytes)

        return filename
    }

    private fun isPng(bytes: ByteArray): Boolean {
        return bytes.size >= PNG_SIGNATURE.size &&
            bytes.take(PNG_SIGNATURE.size) == PNG_SIGNATURE.toList()
    }

    private fun isJpeg(bytes: ByteArray): Boolean {
        return bytes.size >= 4 &&
            bytes[0] == 0xFF.toByte() &&
            bytes[1] == 0xD8.toByte() &&
            bytes[bytes.size - 2] == 0xFF.toByte() &&
            bytes[bytes.size - 1] == 0xD9.toByte()
    }
}
