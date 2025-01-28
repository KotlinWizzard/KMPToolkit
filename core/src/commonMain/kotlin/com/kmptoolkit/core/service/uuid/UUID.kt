package com.kmptoolkit.core.service.uuid

import com.benasher44.uuid.uuid4

object UUID {
    fun generate() = uuid4().toString()
}