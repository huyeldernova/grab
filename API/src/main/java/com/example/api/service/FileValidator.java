package com.example.api.service;

import com.example.api.exception.AppException;
import com.example.api.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

@Component
public class FileValidator {

    private static final long MAX_SIZE_BYTES = 20 * 1024 * 1024;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif",
            "application/pdf"
    );

         public void validate(MultipartFile file) throws IOException {

            if(file.isEmpty()){
                throw new AppException(ErrorCode.FILE_EMPTY);
            }

             if(file.getSize() > MAX_SIZE_BYTES){
                 throw new AppException(ErrorCode.FILE_TOO_LARGE);
             }

             if(!ALLOWED_CONTENT_TYPES.contains(file.getContentType())){
                 throw new AppException(ErrorCode.INVALID_FILE_TYPE);
             }

             if(!isMagicBytesValid(file)){
                 throw new AppException(ErrorCode.INVALID_FILE_TYPE);
             }
    }

    private boolean isMagicBytesValid(MultipartFile file) throws IOException {
        byte[] bytes = new byte[4];
        file.getInputStream().read(bytes);

        // JPEG: FF D8
        if (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8) return true;
        // PNG: 89 50
        if (bytes[0] == (byte) 0x89 && bytes[1] == (byte) 0x50) return true;
        // PDF: 25 50 (%PDF)
        if (bytes[0] == (byte) 0x25 && bytes[1] == (byte) 0x50) return true;
        // GIF: 47 49 (GI)
        if (bytes[0] == (byte) 0x47 && bytes[1] == (byte) 0x49) return true;
        // WEBP: 52 49 (RI)
        if (bytes[0] == (byte) 0x52 && bytes[1] == (byte) 0x49) return true;

        return false;
    }
}
