package com.openviewer.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.format.DateUtils
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var recentsList: ListView
    private lateinit var recentsEmpty: TextView
    private var recents: List<RecentFile> = emptyList()
    private var pendingType: FileType? = null

    private val pickFile = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val type = pendingType
        if (uri == null || type == null) return@registerForActivityResult
        try {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (_: SecurityException) { /* provider doesn't support persistable permissions */ }
        val name = displayName(uri)
        if (FileTypes.forFileName(name) != type) {
            Toast.makeText(this, getString(R.string.wrong_type, type.label), Toast.LENGTH_LONG).show()
        }
        startActivity(FileTypes.viewerIntent(this, type, uri, name))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recentsList = findViewById(R.id.recents_list)
        recentsEmpty = findViewById(R.id.recents_empty)
        recentsList.emptyView = recentsEmpty
        recentsList.setOnItemClickListener { _, _, pos, _ -> openRecent(recents[pos]) }
        recentsList.setOnItemLongClickListener { _, _, pos, _ ->
            Recents.remove(this, recents[pos].uri); refreshRecents(); true
        }
        findViewById<View>(R.id.recents_clear).setOnClickListener { Recents.clear(this); refreshRecents() }

        val ext = findViewById<ExpandableListView>(R.id.extensions_list)
        ext.setAdapter(ExtensionsAdapter())
        for (i in FileTypes.categories.indices) ext.expandGroup(i)
        ext.setOnChildClickListener { _, _, g, c, _ ->
            val type = FileTypes.categories[g].types[c]
            pendingType = type
            pickFile.launch(arrayOf("*/*"))
            true
        }
    }

    override fun onResume() {
        super.onResume()
        refreshRecents()
    }

    private fun refreshRecents() {
        recents = Recents.load(this)
        recentsList.adapter = object : ArrayAdapter<RecentFile>(
            this, android.R.layout.simple_list_item_2, android.R.id.text1, recents
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getView(position, convertView, parent)
                val r = recents[position]
                v.findViewById<TextView>(android.R.id.text1).text = r.name
                v.findViewById<TextView>(android.R.id.text2).text =
                    DateUtils.getRelativeTimeSpanString(r.openedAt)
                return v
            }
        }
    }

    private fun openRecent(r: RecentFile) {
        val type = FileTypes.forFileName(r.name)
        if (type == null) {
            Toast.makeText(this, R.string.unsupported, Toast.LENGTH_SHORT).show(); return
        }
        startActivity(FileTypes.viewerIntent(this, type, Uri.parse(r.uri), r.name))
    }

    private fun displayName(uri: Uri): String {
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
            if (it.moveToFirst()) it.getString(0)?.let { n -> return n }
        }
        return uri.lastPathSegment ?: "file"
    }

    private inner class ExtensionsAdapter : BaseExpandableListAdapter() {
        private val cats = FileTypes.categories
        override fun getGroupCount() = cats.size
        override fun getChildrenCount(g: Int) = cats[g].types.size
        override fun getGroup(g: Int) = cats[g]
        override fun getChild(g: Int, c: Int) = cats[g].types[c]
        override fun getGroupId(g: Int) = g.toLong()
        override fun getChildId(g: Int, c: Int) = c.toLong()
        override fun hasStableIds() = true
        override fun isChildSelectable(g: Int, c: Int) = true

        override fun getGroupView(g: Int, expanded: Boolean, convertView: View?, parent: ViewGroup): View {
            val v = convertView ?: layoutInflater.inflate(R.layout.item_category, parent, false)
            (v as TextView).text = cats[g].name
            return v
        }

        override fun getChildView(g: Int, c: Int, last: Boolean, convertView: View?, parent: ViewGroup): View {
            val v = convertView ?: layoutInflater.inflate(R.layout.item_extension, parent, false)
            val t = cats[g].types[c]
            (v as TextView).text = t.label
            return v
        }
    }
}
