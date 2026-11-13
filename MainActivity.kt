package com.fazenda50.screen
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class MainActivity : Activity() {
    val SERVER = "wss://fazenda-50.onrender.com/ws"
    val deviceId = "cel-" + (1000..9999).random()
    var mpm: MediaProjectionManager? = null
    lateinit var status: TextView
    var ws: WebSocketClient? = null
    val REQ = 1001
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val lay = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(40,100,40,40) }
        status = TextView(this).apply { text = "ID: $deviceId" }
        val btnAcc = Button(this).apply { text = "1 - Ativar Clique" }
        val btnScreen = Button(this).apply { text = "2 - Ativar Tela" }
        val btnConnect = Button(this).apply { text = "3 - CONECTAR" }
        lay.addView(TextView(this).apply{text="🚜 Fazenda-50 Tela"}); lay.addView(status); lay.addView(btnAcc); lay.addView(btnScreen); lay.addView(btnConnect)
        setContentView(lay)
        mpm = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        btnAcc.setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        btnScreen.setOnClickListener { startActivityForResult(mpm!!.createScreenCaptureIntent(), REQ) }
        btnConnect.setOnClickListener { connectWs() }
    }
    fun connectWs(){
        ws = object : WebSocketClient(URI(SERVER)) {
            override fun onOpen(h: ServerHandshake?) { send("{\"type\":\"register_client\",\"id\":\"$deviceId\"}"); runOnUiThread{status.text="✅ CONECTADO"} }
            override fun onMessage(m: String?) { if(m!!.contains("click")){ val x=Regex("\"x\":([0-9.]+)").find(m)?.groupValues?.get(1)?.toFloat()?:0.5f; val y=Regex("\"y\":([0-9.]+)").find(m)?.groupValues?.get(1)?.toFloat()?:0.5f; ClickService.instance?.click(x,y)} }
            override fun onClose(c:Int,r:String?,b:Boolean){ Thread.sleep(2000); connectWs() }
            override fun onError(e: Exception?) {}
        }; ws!!.connect(); ScreenService.ws = ws
    }
    override fun onActivityResult(req: Int, res: Int, data: Intent?) { if(req==REQ && res==RESULT_OK){ val s=Intent(this, ScreenService::class.java); s.putExtra("resultCode", res); s.putExtra("data", data); startForegroundService(s) } }
}
