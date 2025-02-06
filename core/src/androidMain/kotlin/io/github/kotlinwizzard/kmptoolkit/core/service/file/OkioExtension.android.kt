package io.github.kotlinwizzard.kmptoolkit.core.service.file

import okio.FileSystem

actual val FileSystem.Companion.SYSTEM: FileSystem
    get() = FileSystem.SYSTEM