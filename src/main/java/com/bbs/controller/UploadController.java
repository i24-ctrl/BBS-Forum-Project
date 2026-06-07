package com.bbs.controller;

import com.bbs.common.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@Tag(name = "上传模块", description = "图片上传，返回wangEditor格式")
@RestController
@RequestMapping("/api/v1/upload")
public class UploadController {

    private static final Logger log = LoggerFactory.getLogger(UploadController.class);

    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif"};
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;

    private static final byte[][] MAGIC_NUMBERS = {
            {(byte) 0xFF, (byte) 0xD8},
            {(byte) 0x89, (byte) 0x50},
            {(byte) 0x47, (byte) 0x49},
    };

    @Value("${bbs.upload.path}")
    private String uploadPath;

    @Value("${bbs.upload.access-url}")
    private String accessUrl;

    @Operation(summary = "上传图片", description = "校验登录+文件头魔术数字+扩展名+大小，返回wangEditor格式JSON",
            security = @SecurityRequirement(name = "Bearer"))
    @PostMapping("/image")
    public Map<String, Object> uploadImage(@RequestParam("file") MultipartFile file,
                                           HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId == null) {
            return error(1, "请先登录");
        }

        if (file == null || file.isEmpty()) {
            return error(1, "文件为空");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return error(1, "文件大小不能超过2MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return error(1, "文件名无效");
        }
        String ext = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        boolean allowed = false;
        for (String allowedExt : ALLOWED_EXTENSIONS) {
            if (allowedExt.equals(ext)) { allowed = true; break; }
        }
        if (!allowed) {
            return error(1, "仅支持 jpg/jpeg/png/gif 格式");
        }

        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[2];
            if (is.read(header) < 2 || !matchMagicNumber(header)) {
                return error(1, "文件类型校验失败，请上传真实图片");
            }
        } catch (IOException e) {
            return error(1, "文件读取失败");
        }

        String newFileName = UUID.randomUUID().toString().replace("-", "") + ext;
        String dateDir = java.time.LocalDate.now().toString().replace("-", "/");
        File dir = new File(uploadPath, dateDir);
        if (!dir.exists() && !dir.mkdirs()) {
            return error(1, "服务器文件存储异常");
        }

        File destFile = new File(dir, newFileName);
        try {
            file.transferTo(destFile);
        } catch (IOException e) {
            log.error("文件保存失败", e);
            return error(1, "文件保存失败");
        }

        String baseUrl = accessUrl.endsWith("/") ? accessUrl.substring(0, accessUrl.length() - 1) : accessUrl;
        String fileUrl = baseUrl + "/" + dateDir + "/" + newFileName;

        log.info("图片上传成功 - userId: {}, file: {}", userId, fileUrl);

        Map<String, Object> result = new HashMap<>();
        result.put("errno", 0);
        Map<String, String> dataItem = new HashMap<>();
        dataItem.put("url", fileUrl);
        dataItem.put("alt", originalFilename);
        dataItem.put("href", "");
        result.put("data", new Map[]{dataItem});
        return result;
    }

    private Map<String, Object> error(int errno, String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("errno", errno);
        result.put("message", message);
        return result;
    }

    private boolean matchMagicNumber(byte[] header) {
        for (byte[] magic : MAGIC_NUMBERS) {
            if (header.length >= magic.length) {
                boolean match = true;
                for (int i = 0; i < magic.length; i++) {
                    if (header[i] != magic[i]) { match = false; break; }
                }
                if (match) return true;
            }
        }
        return false;
    }
}