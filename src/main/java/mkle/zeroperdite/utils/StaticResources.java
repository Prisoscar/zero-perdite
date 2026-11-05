package mkle.zeroperdite.utils;

import com.vaadin.flow.component.notification.Notification;

import java.io.File;

public class StaticResources {

    //UX
    public static final String TIMEZONE_ID = "Europe/Rome";
    public static final String APPLICATION_NAME = "Zero Perdite";

    //PROJECT
    public static final String PROJECT_DIRECTORY = System.getProperty("user.dir");
    public static final String TMP_DIRECTORY = PROJECT_DIRECTORY + File.separator + "tmp";
    public static final String FILESYSTEM_DIRECTORY;
    public static final String DIRECTORY_IMAGE = "icons" + File.separator + "directory-red.svg";

    //WITH DYNAMIC ATTRIBUTES
    public static final String PRIVATE_DOWNLOAD_ENDPOINT = "/io/download/private/file/%s?token=%s";
    public static final String PRIVATE_NOTE_DOWNLOAD_ENDPOINT = "/io/download/private/note/%s?token=%s";
    public static final String PRIVATE_DOWNLOAD_CONVERTED = "/io/download/private/converted/%s/%s?token=%s";
    public static final String DOWNLOAD_ZIP = "/io/download/compressed?token=%s";
    public static final String PRIVATE_STREAM_ENDPOINT = "/io/stream/private/video/%s?token=%s";
    public static final String PRIVATE_FILESYSTEM_DIRECTORY;
    public static final String PRIVATE_NOTES_DIRECTORY;

    //DATABASE
    public static final String DATABASE_NAME = "zero-perdite";
    public static final String DATABASE_FULL_NAME = DATABASE_NAME + ".mv.db";
    public static final String DATABASE_DIRECTORY = PROJECT_DIRECTORY + File.separator + "database";
    public static final String FULL_DATABASE_PATH = DATABASE_DIRECTORY + File.separator + DATABASE_FULL_NAME;
    public static final String DATABASE_TRACE_FULL_NAME = DATABASE_NAME + ".trace.db";
    public static final String FULL_DATABASE_TRACE_PATH = DATABASE_DIRECTORY + File.separator + DATABASE_TRACE_FULL_NAME;


    //STRING BUILDERS
    public static final String ATTRIBUTE_SEPARATOR = " ---> ";
    public static final String DIRECTORY_SEPARATOR = " -> ";

    //Notification
    public static final int VERY_LONG_SHOW = 6000;
    public static final int LONG_SHOW = 3000;
    public static final int MID_SHOW = 1750;
    public static final int SHORT_SHOW = 900;
    public static final Notification.Position DEFAULT_POSITION = Notification.Position.BOTTOM_CENTER;


    static {
        String externalStorageDirectory = "/data/data/com.termux/files/home/storage/external-1";
        String externalStorageDirectoryAlt = "/mnt/external_sd";
        if(new File(externalStorageDirectory).exists()) FILESYSTEM_DIRECTORY = externalStorageDirectory + File.separator + "zero-perdite" + File.separator + "filesystem";
        else if(new File(externalStorageDirectoryAlt).exists()) FILESYSTEM_DIRECTORY = externalStorageDirectoryAlt + File.separator + "zero-perdite" + File.separator + "filesystem";
        else FILESYSTEM_DIRECTORY = PROJECT_DIRECTORY + File.separator + "filesystem";

        PRIVATE_FILESYSTEM_DIRECTORY = FILESYSTEM_DIRECTORY + File.separator + "%s" + File.separator + "filesystem";
        PRIVATE_NOTES_DIRECTORY = FILESYSTEM_DIRECTORY + File.separator + "%s" + File.separator + "notes";
    }

}
