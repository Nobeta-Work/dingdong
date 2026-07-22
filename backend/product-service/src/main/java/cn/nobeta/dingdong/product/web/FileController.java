package cn.nobeta.dingdong.product.web;

import cn.nobeta.dingdong.common.api.ApiResponse;
import cn.nobeta.dingdong.product.service.GitHubImageBedService;
import cn.nobeta.dingdong.product.service.GitHubImageBedService.UploadResult;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传接口。
 */
@RestController
@RequestMapping("/api")
public class FileController {

    private final GitHubImageBedService imageBedService;

    public FileController(GitHubImageBedService imageBedService) {
        this.imageBedService = imageBedService;
    }

    @PostMapping(value = {"/files/images", "/files"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<FileUploadResponse> upload(@RequestPart("file") MultipartFile file) {
        UploadResult result = imageBedService.upload(file);
        return ApiResponse.success(new FileUploadResponse(result.url(), result.path()));
    }

    public record FileUploadResponse(String url, String path) { }
}
