package com.fazenda50.screen
import android.app.*
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.util.Base64
import java.io.ByteArrayOutputStream

class ScreenService : Service() {
    companion object { var ws: org.java_websocket.client.WebSocketClient? = null }
    var running = false
    var vd: VirtualDisplay? = null
    var reader: ImageReader? = null
    override fun onStartCommand(intent: Intent?, f: Int, s: Int): Int {
        val chan = NotificationChannel("f50", "Fazenda", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(chan)
        val notif = Notification.Builder(this, "f50").setContentTitle("Fazenda transmitindo").setSmallIcon(android.R.drawable.ic_menu_camera).build()
        startForeground(1, notif)
        val rc = intent?.getIntExtra("resultCode", -1)!!
        val data = intent.getParcelableExtra<Intent>("data")
        val mpm = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val mp = mpm.getMediaProjection(rc, data!!)
        val w = resources.displayMetrics.widthPixels
        val h = resources.displayMetrics.heightPixels
        reader = ImageReader.newInstance(w, h, PixelFormat.RGBA_8888, 2)
        vd = mp.createVirtualDisplay("f50", w, h, resources.displayMetrics.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, reader!!.surface, null, null)
        if(!running){ running=true; Thread{ while(running){ try{ val img=reader!!.acquireLatestImage(); if(img!=null){ val buffer=img.planes[0].buffer; val rowStride=img.planes[0].rowStride; val pixelStride=img.planes[0].pixelStride; val rowPadding=rowStride-pixelStride*w; val bitmap=Bitmap.createBitmap(w+rowPadding/pixelStride, h, Bitmap.Config.ARGB_8888); bitmap.copyPixelsFromBuffer(buffer); val cropped=Bitmap.createBitmap(bitmap,0,0,w,h); val baos=ByteArrayOutputStream(); cropped.compress(Bitmap.CompressFormat.JPEG,35,baos); val b64=Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP); ws?.send("{\"type\":\"screen\",\"img\":\"data:image/jpeg;base64,"+b64+"\"}"); img.close() }; Thread.sleep(500) }catch(e:Exception){} } }.start() }
        return START_STICKY
    }
    override fun onBind(i: Intent?): IBinder? = null
}
