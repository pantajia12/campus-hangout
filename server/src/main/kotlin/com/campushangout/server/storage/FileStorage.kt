package com.campushangout.server.storage

import org.springframework.web.multipart.MultipartFile

interface FileStorage {
    fun save(file: MultipartFile): String
}
