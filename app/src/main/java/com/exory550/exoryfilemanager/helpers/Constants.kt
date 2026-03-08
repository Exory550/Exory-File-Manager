package com.exory550.exoryfilemanager.helpers

import android.os.Environment
import java.util.*

object Constants {

    const val APP_NAME = "Exory File Manager"
    const val APP_AUTHOR = "Exory550"
    const val APP_EMAIL = "support@exory550.com"
    const val APP_WEBSITE = "https://exory550.com"
    const val APP_GITHUB = "https://github.com/Exory550/ExoryFileManager"
    const val APP_PLAY_STORE = "https://play.google.com/store/apps/details?id=com.exory550.filemanager"

    const val DATABASE_NAME = "exory_file_manager.db"
    const val DATABASE_VERSION = 1

    const val SHARED_PREFS_NAME = "exory_prefs"
    const val SECURE_PREFS_NAME = "exory_secure_prefs"

    const val NOTIFICATION_CHANNEL_ID = "exory_file_manager_channel"
    const val NOTIFICATION_CHANNEL_NAME = "File Manager Notifications"
    const val NOTIFICATION_ID_PROGRESS = 1001
    const val NOTIFICATION_ID_COMPLETE = 1002
    const val NOTIFICATION_ID_ERROR = 1003
    const val NOTIFICATION_ID_BACKGROUND = 1004

    const val REQUEST_CODE_PERMISSIONS = 1000
    const val REQUEST_CODE_STORAGE_ACCESS = 1001
    const val REQUEST_CODE_MANAGE_STORAGE = 1002
    const val REQUEST_CODE_SYSTEM_SETTINGS = 1003
    const val REQUEST_CODE_FILE_PICKER = 1004
    const val REQUEST_CODE_FOLDER_PICKER = 1005
    const val REQUEST_CODE_CREATE_FILE = 1006
    const val REQUEST_CODE_OPEN_FILE = 1007

    const val BUFFER_SIZE = 8192
    const val BUFFER_SIZE_LARGE = 65536
    const val PROGRESS_UPDATE_INTERVAL = 500L
    const val SEARCH_DEBOUNCE_INTERVAL = 500L
    const val BACKUP_REMINDER_INTERVAL = 7 * 24 * 60 * 60 * 1000L

    const val MAX_SEARCH_RESULTS = 1000
    const val MAX_RECENT_FILES = 50
    const val MAX_FAVORITES = 100
    const val MAX_BOOKMARKS = 50
    const val MAX_THUMBNAIL_CACHE_SIZE = 100 * 1024 * 1024L

    val DEFAULT_STORAGE_PATH: String = Environment.getExternalStorageDirectory().absolutePath

    val DATE_FORMAT = "dd/MM/yyyy HH:mm:ss"
    val DATE_FORMAT_SHORT = "dd/MM/yyyy"
    val DATE_FORMAT_TIME = "HH:mm:ss"
    val DATE_FORMAT_FILE = "yyyyMMdd_HHmmss"

    val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif")
    val VIDEO_EXTENSIONS = setOf("mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "m4v", "3gp", "mpeg", "mpg")
    val AUDIO_EXTENSIONS = setOf("mp3", "wav", "ogg", "m4a", "aac", "flac", "wma", "ape", "ac3", "dts")
    val DOCUMENT_EXTENSIONS = setOf("txt", "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "odt", "ods", "odp", "rtf", "md", "csv", "tsv")
    val ARCHIVE_EXTENSIONS = setOf("zip", "rar", "7z", "tar", "gz", "bz2", "xz", "z", "arj", "cab", "iso")
    val CODE_EXTENSIONS = setOf("java", "kt", "kts", "xml", "json", "yml", "yaml", "properties", "gradle", "c", "cpp", "h", "hpp", "cs", "php", "js", "ts", "py", "rb", "go", "rs", "swift", "html", "css", "scss", "less")
    val APK_EXTENSIONS = setOf("apk", "apks", "xapk")
    val LOG_EXTENSIONS = setOf("log", "logs", "trace", "debug")

    object IntentExtra {
        const val FILE_PATH = "file_path"
        const val FILE_NAME = "file_name"
        const val FILE_SIZE = "file_size"
        const val MIME_TYPE = "mime_type"
        const val FILES_LIST = "files_list"
        const val OPEN_PATH = "open_path"
        const val SELECTION_MODE = "selection_mode"
        const val ALLOW_MULTIPLE = "allow_multiple"
        const val FILE_FILTER = "file_filter"
        const val SORT_MODE = "sort_mode"
        const val VIEW_MODE = "view_mode"
        const val SHOW_HIDDEN = "show_hidden"
    }

    object FileType {
        const val FOLDER = "folder"
        const val FILE = "file"
        const val IMAGE = "image"
        const val VIDEO = "video"
        const val AUDIO = "audio"
        const val DOCUMENT = "document"
        const val ARCHIVE = "archive"
        const val APK = "apk"
        const val CODE = "code"
        const val LOG = "log"
    }

    object Operation {
        const val COPY = 0
        const val MOVE = 1
        const val DELETE = 2
        const val RENAME = 3
        const val EXTRACT = 4
        const val COMPRESS = 5
        const val SHARE = 6
        const val PROPERTIES = 7
    }

    object SortMode {
        const val NAME = 0
        const val SIZE = 1
        const val DATE = 2
        const val TYPE = 3
        const val EXTENSION = 4
    }

    object ViewMode {
        const val LIST = 0
        const val GRID = 1
        const val DETAILED = 2
    }

    object ThemeMode {
        const val SYSTEM = 0
        const val LIGHT = 1
        const val DARK = 2
        const val AMOLED = 3
    }

    object LockMethod {
        const val NONE = "none"
        const val PASSWORD = "password"
        const val PIN = "pin"
        const val PATTERN = "pattern"
        const val BIOMETRIC = "biometric"
    }

    object EncryptionMethod {
        const val AES = "aes"
        const val CHACHA = "chacha"
        const val RSA = "rsa"
    }

    object CloudProvider {
        const val GOOGLE_DRIVE = "google_drive"
        const val DROPBOX = "dropbox"
        const val ONEDRIVE = "onedrive"
        const val BOX = "box"
        const val YANDEX = "yandex"
    }

    object MimeTypes {
        const val ANY = "*/*"
        const val IMAGE = "image/*"
        const val VIDEO = "video/*"
        const val AUDIO = "audio/*"
        const val TEXT = "text/*"
        const val PDF = "application/pdf"
        const val ZIP = "application/zip"
        const val APK = "application/vnd.android.package-archive"
        const val FOLDER = "resource/folder"
    }
}
