package com.example.LongImage;

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private var mUploadMessage: ValueCallback<Array<Uri>>? = null
    private val FILE_CHOOSER_RESULT_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 动态请求存储权限
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
        }

        val webView = findViewById<WebView>(R.id.webView)
        webView.settings.javaScriptEnabled = true  // 启用 JavaScript
        webView.webViewClient = WebViewClient()  // 用 WebView 打开链接而不是默认浏览器
        // 创建并暴露接口给 JS 使用
        webView.addJavascriptInterface(WebAppInterface(this), "Android")
        webView.webChromeClient = object : WebChromeClient() {
            // 处理文件上传（选择图片）
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                mUploadMessage = filePathCallback
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"  // 只选择图片
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)  // 允许选择多张图片
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                startActivityForResult(
                    Intent.createChooser(intent, "选择图片"),
                    FILE_CHOOSER_RESULT_CODE
                )
                return true
            }
        }

        // 加载网页
        webView.loadUrl("file:///android_asset/index.html")
    }

    inner class WebAppInterface(private val activity: MainActivity) {
        @JavascriptInterface
        fun saveImage(data: String) {
            activity.saveImage(data)
        }
    }

    private fun saveImage(data: String) {
        try {
            var imageBitmap: Bitmap? = null

            var realStr = data
            if (realStr.contains(",")) {
                realStr = realStr.split(",")[1]
            }
            val array = Base64.decode(realStr, Base64.NO_WRAP)
            imageBitmap = BitmapFactory.decodeByteArray(array, 0, array.size)

            if (imageBitmap != null) {
                saveBitmapToGallery(imageBitmap)
            } else {
                // TODO 保存失败
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveBitmapToGallery(bitmap: Bitmap) {
        // 获取当前时间，并格式化为文件名
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val bitName = "$timeStamp.jpg"  // 设置文件名为当前时间，精确到秒

        // 创建文件路径
        val saveFile = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            bitName
        )
        val fos = FileOutputStream(saveFile)

        try {
            // 压缩并保存图片
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)) {
                fos.flush()
            }
            // 扫描文件，使其出现在相册中
            MediaScannerConnection.scanFile(
                this,
                arrayOf(saveFile.absolutePath),
                arrayOf("image/jpeg")
            ) { _, _ -> }
            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show()
        } finally {
            fos.close()
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FILE_CHOOSER_RESULT_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                // 处理多个文件的情况
                val result = data.clipData
                if (result != null) {
                    val uris = Array(result.itemCount) { index -> result.getItemAt(index).uri }
                    mUploadMessage?.onReceiveValue(uris)
                } else {
                    val uri = data.data
                    if (uri != null) {
                        mUploadMessage?.onReceiveValue(arrayOf(uri))  // 返回单个 Uri
                    }
                }
            }
        }
    }
}
