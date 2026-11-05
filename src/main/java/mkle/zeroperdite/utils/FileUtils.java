package mkle.zeroperdite.utils;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
public class FileUtils {

    //logs
    private static final String TXT_FILE_HEADER = "User Id" + StaticResources.ATTRIBUTE_SEPARATOR + "Log Type" +
            StaticResources.ATTRIBUTE_SEPARATOR + "Timestamp" + StaticResources.ATTRIBUTE_SEPARATOR +
            "Log Message";
    private static final String TXT_LOG_ROW = "%s" + StaticResources.ATTRIBUTE_SEPARATOR + "%s" +
            StaticResources.ATTRIBUTE_SEPARATOR + "%s" + StaticResources.ATTRIBUTE_SEPARATOR +
            "%s";

    /**
     * @param log the log to convert in file row
     * @return the txt file standard row containing the provided log
     */
//    public String getTxtLogRow(Log log){
//        return String.format(
//                TXT_LOG_ROW,
//                log.getUserId(),
//                log.getLogType(),
//                log.getUpdatedOn(),
//                log.getLogMessage()
//        );
//    }

    /**
     * @return the txt file standard header row
     */
    public String getTxtLogFileHeader(){
        return TXT_FILE_HEADER;
    }

    public String getUserUuidFromPath(String path){
        if(!path.startsWith(StaticResources.FILESYSTEM_DIRECTORY)) return null;
        path = path.replaceFirst(Pattern.quote(StaticResources.FILESYSTEM_DIRECTORY + File.separator), "");
        return path.split(Pattern.quote(File.separator))[0];
    }

    public LocalDateTime getCustomDateAttribute(File file, String attributeName) throws IOException {
        FileTime fileTime = (FileTime) Files.getAttribute(file.toPath(), attributeName);
        return LocalDateTime.ofInstant(fileTime.toInstant(), ZoneId.of(StaticResources.TIMEZONE_ID));
    }
    
    public void setCustomDateAttribute(File file, LocalDateTime creationTime, String attributeName) throws IOException {
        FileTime fileTime = FileTime.from(creationTime.atZone(ZoneId.of(StaticResources.TIMEZONE_ID)).toInstant());
        Files.setAttribute(file.toPath(), attributeName, fileTime);
    }
    
    public LocalDateTime getLastModified(File file) throws IOException {
        BasicFileAttributes basicAttributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        return LocalDateTime.ofInstant(basicAttributes.lastModifiedTime().toInstant(), ZoneId.of(StaticResources.TIMEZONE_ID));
    }
    
    public boolean isCompressedFile(String filename){
        filename = filename.toLowerCase();
        Set<String> extensions = Set.of(".zip", ".rar", ".7zip", ".tar", ".tgz", ".tbx2", ".txz", ".tar.gz", ".tar.bz2", ".tar.xz", ".tar.lz", ".tar.lzma", ".tar.zst", ".tar.z");
        return extensions.stream().anyMatch(filename::endsWith);
    }

    public static String getExtension(String filename){
        String finalExtension = FilenameUtils.getExtension(filename);
        switch (finalExtension.toLowerCase()) {
            case "gz", "bz2", "xz", "lz", "lzma", "zst", "z" -> finalExtension = FilenameUtils.getExtension(filename.substring(0, filename.length() - (finalExtension.length() + 1))) + "." + finalExtension;
        }
        return finalExtension;
    }

    public long getFileSizeInBytes(String filePath){
        try(Stream<Path> walk = Files.walk(Paths.get(filePath))) {
            return walk.filter(Files::isRegularFile)
                    .mapToLong(p -> {
                        try {
                            return Files.size(p);
                        } catch (IOException e) {
                            return 0L;
                        }
                    })
                    .sum();
        } catch (IOException e) {
            return 0L;
        }
    }
}
