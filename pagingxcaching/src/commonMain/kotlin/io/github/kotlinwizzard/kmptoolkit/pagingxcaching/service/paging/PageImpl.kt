package io.github.kotlinwizzard.kmptoolkit.pagingxcaching.service.paging


data class PageImpl<T>(
    val content: MutableList<T> = mutableListOf(),
    val pageNumber: Int,
    val pageSize: Int,
    val totalElements: Long,
    val empty: Boolean = content.isEmpty(),
    val first: Boolean = pageNumber <= 0,
    val last: Boolean = pageNumber == maxPages(pageSize, totalElements) - 1,
    val numberOfElements: Int = content.size,
    val totalPages: Int = maxPages(pageSize, totalElements),
) {


    val nextPageNumber: Int?
        get() {
            return if (hasNext()) {
                pageNumber.plus(1)
            } else {
                null
            }
        }
    val offset: Long
        get() {
            return (numberOfElements.times(pageNumber)).toLong()
        }

    val previousPageNumber: Int?
        get() {
            return if (hasPrevious()) {
                pageNumber.minus(1)
            } else {
                null
            }
        }

    fun hasNext(): Boolean {
        return (pageNumber + 1) < totalPages
    }

    fun hasPrevious(): Boolean {
        return (pageNumber - 1) >= 0
    }

    fun <Actual> map(transform: (T) -> Actual): PageImpl<Actual> {
        val content = content.map { transform.invoke(it) }.toMutableList()
        return PageImpl(
            content = content,
            empty = empty,
            first = first,
            last = last,
            pageNumber = pageNumber,
            numberOfElements = numberOfElements,
            totalElements = totalElements,
            totalPages = totalPages,
            pageSize = pageSize
        )
    }

    fun <Actual : Any> mapNotNull(transform: (T) -> Actual?): PageImpl<Actual> {
        val content = content.mapNotNull { transform.invoke(it) }.toMutableList()
        return PageImpl(
            content = content,
            empty = empty,
            first = first,
            last = last,
            pageNumber = pageNumber,
            numberOfElements = numberOfElements,
            totalElements = totalElements,
            totalPages = totalPages,
            pageSize = pageSize
        )
    }

    companion object {
        fun <T> byContent(
            content: MutableList<T>,
            pageNumber: Int, pageSize: Int, totalElements: Long,
        ): PageImpl<T> {
            return PageImpl(
                content = content,
                pageNumber = pageNumber,
                pageSize = pageSize,
                totalElements = totalElements
            )
        }
    }

}

private fun maxPages(pageSize: Int, totalElements: Long): Int {
    return if (totalElements % pageSize == 0L) {
        (totalElements / pageSize).toInt()
    } else {
        (totalElements / pageSize).toInt() + 1
    }
}