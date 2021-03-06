package com.markstash.server

object Constants {
    object Jwt {
        const val ISSUER = "jwt.issuer"
        const val AUDIENCE = "jwt.audience"
        const val REALM = "jwt.realm"
        const val SECRET = "jwt.secret"
        const val ALGORITHM = "jwt.algorithm"
    }

    object Storage {
        const val ARCHIVE_DIR = "storage.archive_dir"
    }

    object Binaries {
        const val CHROME_BIN = "binaries.chrome_bin"
    }

    object Settings {
        const val CHROME_USE_DEV_SHM = "settings.chrome_use_dev_shm"
    }
}
