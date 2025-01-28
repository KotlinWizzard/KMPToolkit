package com.kmptoolkit.core.service.file

import okio.FileSystem
import okio.SYSTEM

actual val FileSystem.Companion.SYSTEM: FileSystem
    get() = FileSystem.SYSTEM