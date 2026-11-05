package mkle.zeroperdite.utils;

import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Paths;

@Service
public class ImageUtils {

    private static final String ICON_BASE_PATH = Paths.get("icons", "%s").toString();
    private static final String ICON_PROGRAMMING_PATH = Paths.get("icons", "programming", "%s").toString();
    private static final String IMAGES_BASE_PATH = Paths.get("images", "%s").toString();
    private static final String RESOURCE_BASE_PATH = StaticResources.PROJECT_DIRECTORY + File.separator + Paths.get(
            "src", "main", "resources", "META-INF", "resources", "images", "%s"
    );

    public String getFileIcon(String filename){
        return switch (FileUtils.getExtension(filename).toLowerCase()){
            //compressed files
            case "zip", "rar", "7zip", "tar", "tgz", "tbx2", "txz", "tar.gz", "tar.bz2", "tar.xz", "tar.lz", "tar.lzma", "tar.zst", "tar.z" -> String.format(ICON_BASE_PATH, "compressed-file-old_icon.png");
            case "pdf" -> String.format(ICON_BASE_PATH, "pdf-file-icon.svg");
            case "txt", "log", "csv" -> String.format(ICON_BASE_PATH, "text-file-icon.svg");
            case "xml", "html" -> String.format(ICON_BASE_PATH, "tagged-file-icon.svg");
            case "exe" -> String.format(ICON_BASE_PATH, "executable-file-icon.svg");
            case "doc", "docx", "odt", "fodt", "pages", "rtf", "gdoc" -> String.format(ICON_BASE_PATH, "writer-file-old_icon.png");
            case "xls", "xlsx", "xlsm", "ods", "fods", "numbers", "gsheet" -> String.format(ICON_BASE_PATH, "calc-file-old_icon.png");
            case "ppt", "pptx", "pptm", "odp", "fodp", "key", "gslides" -> String.format(ICON_BASE_PATH, "impress-file-old_icon.png");
            case "odf" -> String.format(ICON_BASE_PATH, "math-file-old_icon.png");
            case "mdb", "accdb", "odb" -> String.format(ICON_BASE_PATH, "base-file-old_icon.png");
            case "png", "jpeg", "jpg", "svg", "gif", "bpm", "tif", "tiff" -> String.format(ICON_BASE_PATH, "picture-file-icon.svg");
            case "mp4", "m4v", "avi", "mov", "3gp", "3g2", "mpg", "mpeg", "flv", "wmv", "mkv", "webm" -> String.format(ICON_BASE_PATH, "multimedia-file-icon.svg");
            case "mp3", "wav", "aac", "m4a", "ogg", "flac", "wma", "aiff", "aif", "opus", "mid", "midi" -> String.format(ICON_BASE_PATH, "multimedia-file-icon.svg");
            case "puml", "plantuml" -> String.format(ICON_BASE_PATH, "puml-old_icon.png");
            case "iso" -> String.format(ICON_BASE_PATH, "iso-old_icon.png");
            case "apk" -> String.format(ICON_BASE_PATH, "apk-icon.svg");
            case "sql" -> String.format(ICON_BASE_PATH, "sql-icon.svg");

            //PROGRAMMING
            case "python", "py" -> String.format(ICON_PROGRAMMING_PATH, "python-icon.svg");
            case "java" -> String.format(ICON_PROGRAMMING_PATH, "java-icon.svg");
            case "javascript", "js" -> String.format(ICON_PROGRAMMING_PATH, "javascript-icon.svg");
            case "typescript", "ts" -> String.format(ICON_PROGRAMMING_PATH, "typescript-icon.svg");
            case "css" -> String.format(ICON_PROGRAMMING_PATH, "css3-icon.svg");
            case "c" -> String.format(ICON_PROGRAMMING_PATH, "c-icon.svg");
            case "cpp" -> String.format(ICON_PROGRAMMING_PATH, "c-plus-plus-icon.svg");
            case "cs" -> String.format(ICON_PROGRAMMING_PATH, "c-sharp-icon.svg");
            case "php" -> String.format(ICON_PROGRAMMING_PATH, "php-icon.svg");
            case "hs" -> String.format(ICON_PROGRAMMING_PATH, "haskell-icon.svg");
            case "pl" -> String.format(ICON_PROGRAMMING_PATH, "perl-icon.svg");
            case "asm" -> String.format(ICON_PROGRAMMING_PATH, "assembly-icon.svg");
            case "cbl", "cob" -> String.format(ICON_PROGRAMMING_PATH, "cobol-icon.svg");
            case "f90", "for", "f" -> String.format(ICON_PROGRAMMING_PATH, "fortran-icon.svg");
            case "go" -> String.format(ICON_PROGRAMMING_PATH, "golang-icon.svg");
            case "kt", "ktm", "kts" -> String.format(ICON_PROGRAMMING_PATH, "kotlin-icon.svg");
            case "lua" -> String.format(ICON_PROGRAMMING_PATH, "lua-icon.svg");
            case "mat" -> String.format(ICON_PROGRAMMING_PATH, "matlab-icon.svg");
            case "r" -> String.format(ICON_PROGRAMMING_PATH, "r-icon.svg");
            case "rb" -> String.format(ICON_PROGRAMMING_PATH, "ruby-icon.svg");
            case "rs" -> String.format(ICON_PROGRAMMING_PATH, "rust-icon.svg");
            case "sc", "scala" -> String.format(ICON_PROGRAMMING_PATH, "scala-icon.svg");
            case "swift" -> String.format(ICON_PROGRAMMING_PATH, "swift-icon.svg");

            //DEFAULT
            default -> String.format(ICON_BASE_PATH, "generic-file-old_icon.png");
        };
    }

    public String getAssetImage(AssetImage assetImage){
        return switch (assetImage){
            case LOGIN_IMAGE -> String.format(IMAGES_BASE_PATH, "login.png");
            case PREVIEW_NOT_SUPPORTED -> String.format(IMAGES_BASE_PATH, "preview-not-supported.png");
        };
    }

    public String getAssetResource(AssetResource assetImage){
        return switch (assetImage){
            case USER_PHOTO -> String.format(RESOURCE_BASE_PATH, "default-user.png");
        };
    }

    public enum AssetImage {
        LOGIN_IMAGE, PREVIEW_NOT_SUPPORTED
    }

    public enum AssetResource {
        USER_PHOTO
    }

}