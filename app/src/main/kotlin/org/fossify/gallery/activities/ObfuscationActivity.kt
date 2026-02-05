package org.fossify.gallery.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.fossify.commons.extensions.*
import org.fossify.commons.helpers.NavigationIcon
import org.fossify.gallery.R
import org.fossify.gallery.databinding.ActivityObfuscationBinding
import org.fossify.gallery.helpers.ObfuscationHelper

class ObfuscationActivity : SimpleActivity() {
    companion object {
        private const val PICK_IMPORT_SOURCE_INTENT = 1
        private const val PICK_EXPORT_FILE_INTENT = 2
    }

    private val binding by viewBinding(ActivityObfuscationBinding::inflate)
    private lateinit var helper: ObfuscationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        helper = ObfuscationHelper(this)

        setupTopAppBar(binding.obfuscationAppbar, NavigationIcon.Arrow)

        updateView()

        binding.obfuscationGenerate.setOnClickListener {
            val map = helper.generateRandomMap()
            helper.saveObfuscationMap(map)
            updateView()
            toast(R.string.mapping_generated_successfully)
        }

        binding.obfuscationImport.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "application/json"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            startActivityForResult(intent, PICK_IMPORT_SOURCE_INTENT)
        }

        binding.obfuscationExport.setOnClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_TITLE, "obfuscation_map.json")
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            startActivityForResult(intent, PICK_EXPORT_FILE_INTENT)
        }
        
        binding.obfuscationDelete.setOnClickListener {
             org.fossify.commons.dialogs.ConfirmationDialog(this, message = getString(R.string.delete_mapping_confirmation), positive = org.fossify.commons.R.string.yes, negative = org.fossify.commons.R.string.no) {
                 helper.saveObfuscationMap(emptyMap())
                 updateView()
             }
        }
    }

    override fun onResume() {
        super.onResume()
        setupTopAppBar(binding.obfuscationAppbar, NavigationIcon.Arrow)
    }

    private fun updateView() {
        val map = helper.getObfuscationMap()
        val json = GsonBuilder().setPrettyPrinting().create().toJson(map)
        binding.obfuscationJson.text = json
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (resultCode != Activity.RESULT_OK || resultData == null || resultData.data == null) return

        if (requestCode == PICK_IMPORT_SOURCE_INTENT) {
            val inputStream = contentResolver.openInputStream(resultData.data!!)
            try {
                val json = inputStream?.bufferedReader().use { it?.readText() } ?: return
                val type = object : TypeToken<Map<String, String>>() {}.type
                val map: Map<String, String>? = Gson().fromJson(json, type)
                if (map != null) {
                    helper.saveObfuscationMap(map)
                    updateView()
                    toast(R.string.mapping_imported_successfully)
                } else {
                    toast(R.string.file_is_malformed_or_corrupted)
                }
            } catch (e: Exception) {
                toast(R.string.file_is_malformed_or_corrupted)
            }
        } else if (requestCode == PICK_EXPORT_FILE_INTENT) {
            val outputStream = contentResolver.openOutputStream(resultData.data!!)
            try {
                val map = helper.getObfuscationMap()
                val json = GsonBuilder().setPrettyPrinting().create().toJson(map)
                outputStream?.bufferedWriter().use { it?.write(json) }
                toast(R.string.mapping_exported_successfully)
            } catch (e: Exception) {
                toast(R.string.error_saving_file)
            }
        }
    }
}
