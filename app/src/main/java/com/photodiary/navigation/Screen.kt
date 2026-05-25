package com.photodiary.navigation

sealed class Screen(val route: String) {
    object Timeline : Screen("timeline")
    object EntryDetail : Screen("entry_detail/{entryId}") {
        fun createRoute(entryId: Long) = "entry_detail/$entryId"
    }
    object CreateEditEntry : Screen("create_edit_entry?entryId={entryId}&date={date}") {
        fun createRoute(entryId: Long? = null, date: Long? = null): String {
            val params = mutableListOf<String>()
            entryId?.let { params.add("entryId=$it") }
            date?.let { params.add("date=$it") }
            return if (params.isEmpty()) "create_edit_entry"
            else "create_edit_entry?${params.joinToString("&")}"
        }
    }
    object PhotoViewer : Screen("photo_viewer/{entryId}/{photoIndex}") {
        fun createRoute(entryId: Long, photoIndex: Int) = "photo_viewer/$entryId/$photoIndex"
    }
    object Calendar : Screen("calendar")
    object PhotoWall : Screen("photo_wall")
    object TagManagement : Screen("tag_management")
    object TagFilter : Screen("tag_filter?tag={tag}") {
        fun createRoute(tag: String) = "tag_filter?tag=${android.net.Uri.encode(tag)}"
    }
}
